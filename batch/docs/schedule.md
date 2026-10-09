# simulation.schedule — Quartz 스케줄 관리와 reconcile

이 모듈의 **모든 주기 실행을 관장하는 패키지**다. `@Scheduled` 는 리포 전체에서 하나도 쓰지 않으며,
스케줄은 100% Quartz 로 관리된다.

핵심 개념은 **reconcile** — 서버가 기동될 때마다 "DB 의 업무 상태"와 "Quartz 메타테이블의 Job 등록 상태"를
비교해 **일치시키는** 방식이다. 스케줄이 코드가 아니라 **데이터에서 파생**되므로, 프로그램·데이터셋이 추가·삭제되어도
서버 재기동만으로 스케줄이 따라온다.

---

## 1. 패키지 구조

```
simulation/schedule/
├── spec/
│   ├── JobSpec.java                        인터페이스 + MisfirePolicy enum
│   └── BuiltinJobs.java                    고정 스케줄 3종 선언 (enum)
├── service/SchedulerManagedService.java    reconcile 오케스트레이터
└── controller/InternalRequestController.java  /internal — API 서버 → 배치 서버 호출

com/hscmt/common/comp/
├── QuartzJobManageComp.java                Quartz 조작 핵심 (JobKey 규약·upsert·변경감지)
└── QuartzReconciler.java                   ApplicationRunner — 기동 시 reconcile 실행

simulation/common/config/quartz/QuartzConfig.java   SchedulerFactoryBean·클러스터 설정
```

---

## 2. Quartz 설정 — `simulation/common/config/quartz/QuartzConfig.java`

```java
props.setProperty("org.quartz.scheduler.instanceName", "SimulationScheduler");
props.setProperty("org.quartz.scheduler.instanceId", "AUTO");
props.setProperty("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
props.setProperty("org.quartz.jobStore.isClustered", "true");            // 클러스터 모드
props.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
props.setProperty("org.quartz.jobStore.misfireThreshold", "30000");
props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
props.setProperty("org.quartz.threadPool.threadCount", "32");
```

| 설정 | 의미 |
|---|---|
| `isClustered = true` | 여러 인스턴스가 같은 DB 를 공유해도 **한 트리거는 한 노드에서만** 실행된다 |
| `instanceId = AUTO` | 노드 ID 자동 생성 — 클러스터 필수 |
| `misfireThreshold = 30000` | 30초 넘게 늦으면 misfire 로 판정 |
| `threadCount = 32` | Quartz 워커 스레드. `batchExecutor`(16/32) 와는 **별개 풀** |

### SpringBeanJobFactory 오버라이드

```java
SpringBeanJobFactory jobFactory = new SpringBeanJobFactory() {
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        Object job = super.createJobInstance(bundle);
        context.getAutowireCapableBeanFactory().autowireBean(job);   // 의존성 주입
        return job;
    }
};
```

Quartz 는 Job 인스턴스를 **자기가 리플렉션으로 생성**하므로 기본 상태로는 스프링 Bean 이 주입되지 않는다.
생성 직후 `autowireBean` 을 호출해 `JobLauncher`·`Job`·서비스가 주입되도록 만든다.
이것이 `TagAutoCollectJobRunner` 같은 클래스가 `@RequiredArgsConstructor` 로 의존성을 받을 수 있는 이유다.

### 기동 제어

```yaml
# resources/application-common.yml
spring.quartz:
  job-store-type: jdbc
  jdbc: { initialize-schema: never }    # doc/ddl/22.quartz테이블초기화.sql 로 수동 생성
  auto-startup: false                   # ← 자동 시작 금지
batch:
  timezone: Asia/Seoul
```

`auto-startup: false` 가 핵심이다. **reconcile 이 끝나기 전에 스케줄러가 돌기 시작하면**
아직 정리되지 않은 낡은 Job 이 실행될 수 있다.

---

## 3. reconcile — 기동 시 스케줄 동기화

### 3.1 진입점 — `common/comp/QuartzReconciler.java`

```java
@Component
public class QuartzReconciler implements ApplicationRunner {
    public void run(ApplicationArguments args) throws Exception {
        scheduler.standby();                        // ① 트리거 발화 정지
        schedulerManagedService.reconcileJobs();    // ② 동기화
        scheduler.start();                          // ③ 발화 시작
    }
}
```

`standby()` → reconcile → `start()` 3단계가 안전 순서다.
`ApplicationRunner` 라 **컨텍스트가 완전히 뜬 뒤** 실행되므로, 리포지토리·서비스가 모두 준비된 상태가 보장된다.

### 3.2 4단계 동기화 — `service/SchedulerManagedService.java`

```java
public void reconcileJobs() {
    reconcileProgramJobs();          // 실시간 프로그램
    reconcileRealtimeDatasetJobs();  // 실시간 데이터셋
    reconcileFixedDatasetJobs();     // 고정 데이터셋 (1회성)
    reconcileStaticJobs();           // BuiltinJobs 고정 스케줄
}
```

### 3.3 diff 알고리즘 — 등록/수정/삭제를 한 번에

앞의 세 단계는 동일한 패턴을 쓴다.

```java
private void reconcileProgramJobs() {
    final String group = JobTargetType.PROGRAM.name();
    List<ProgramDto> targetPrograms = programRepository.findAllRealtimePrograms();   // DB 의 정답
    Set<String> remainingIds = quartzJobManageComp.getJobIdSet(group);               // Quartz 의 현재

    for (ProgramDto p : targetPrograms) {
        upsert(p.getPgmId(), group, p.getStrtExecDttm(), p.getRpttIntvTypeCd(), p.getRpttIntvVal());
        remainingIds.remove(p.getPgmId());       // 처리한 것은 목록에서 제거
    }
    removeJobBySet(remainingIds, group);         // 남은 것 = DB 에 없는 Job → 삭제
}
```

**"DB 목록을 돌며 upsert 하고, 남은 Quartz Job 은 지운다"** — 집합 차집합으로 삭제 대상을 구하는 방식이다.
별도의 삭제 이벤트나 tombstone 테이블 없이도 정합성이 맞춰진다.

### 3.4 변경 감지 — signature

매번 `rescheduleJob` 을 호출하면 불필요한 DB 쓰기가 발생하므로, **주기 정보를 해시로 압축해 비교**한다.

```java
// common/comp/QuartzJobManageComp.java
public String getSignature(CycleCd cycle, Integer interval, LocalDateTime firstStartDttm) {
    String raw = "%s|%d|%s".formatted(cycle.name(), interval,
            firstStartDttm.atZone(ZoneId.of(timezoneId)).toOffsetDateTime());
    return Integer.toHexString(raw.hashCode());
}

public boolean isChanged(String targetId, String group, String signature) {
    if (!isExistJob(getJobKey(targetId, group))) return true;
    String oldSignature = scheduler.getJobDetail(jobkey).getJobDataMap().getString(SIGNATURE_KEY);
    return !Objects.equals(oldSignature, signature);
}
```

signature 는 `JobDataMap["signature"]` 에 함께 저장된다.

```java
public void upsert(String targetId, String group, LocalDateTime firstStartDttm, CycleCd cycle, Integer interval) {
    String signature = quartzJobManageComp.getSignature(cycle, interval, firstStartDttm);
    if (!quartzJobManageComp.isExistJob(quartzJobManageComp.getJobKey(targetId, group))) {
        quartzJobManageComp.registerJob(...);      // 신규
    } else if (quartzJobManageComp.isChanged(targetId, group, signature)) {
        quartzJobManageComp.updateJob(...);        // 변경된 것만
    }                                              // 동일하면 아무것도 안 함
}
```

고정 스케줄(`BuiltinJobs`)은 cron 문자열 자체를 signature 로 쓴다.

### 3.5 키 규약

```java
public final String JOB_ID_PREFIX = "job:";
public final String TRG_KEY_SUFFIX = ":trg";

public JobKey     getJobKey(String targetId, String group)     { return JobKey.jobKey(JOB_ID_PREFIX + targetId, group); }
public TriggerKey getTriggerKey(String targetId, String group)  { return TriggerKey.triggerKey(getJobKey(targetId, group).getName() + TRG_KEY_SUFFIX, group); }
```

`pgmId = ABC` · group = `PROGRAM` → JobKey `PROGRAM.job:ABC`, TriggerKey `PROGRAM.job:ABC:trg`.
역변환(`getJobIdSet`)은 prefix 를 잘라 targetId 집합을 만든다 — diff 알고리즘의 전제다.

---

## 4. 그룹별 등록 전략

`JobTargetType` 4종에 따라 Job 클래스와 트리거 방식이 갈린다.

| 그룹 | Job 클래스 | 트리거 | 특징 |
|---|---|---|---|
| `PROGRAM` | `RealtimeProgramExecuteJob` | `CalendarIntervalSchedule` | `strtExecDttm` 부터 주기 반복 |
| `DATASET` | `MeasureDatasetCreatorJob` | `CalendarIntervalSchedule` | interval 고정 1 |
| `TEMP` | `MeasureDatasetCreatorJob` | `SimpleSchedule().withRepeatCount(0)` + `startNow()` | **1회성**, `storeDurably(false)` |
| `STATIC` | `BuiltinJobs.jobClass` | `CronScheduleBuilder` | cron 기반 시스템 배치 |

### CalendarIntervalSchedule 을 쓰는 이유

```java
CalendarIntervalScheduleBuilder cib = CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
        .inTimeZone(TimeZone.getTimeZone(timezoneId))
        .withMisfireHandlingInstructionFireAndProceed();

ScheduleBuilder<?> schedule = switch (cycle) {
    case MIN  -> cib.withIntervalInMinutes(triggerInterval);
    case HOUR -> cib.withIntervalInHours(triggerInterval);
    case DAY  -> cib.withIntervalInDays(triggerInterval);
    case MON  -> cib.withIntervalInMonths(triggerInterval);
    case YEAR -> cib.withIntervalInYears(triggerInterval);
};
```

`SimpleTrigger` 는 밀리초 단위 고정 간격이라 **"매월"·"매년" 을 표현할 수 없고 DST 를 반영하지 못한다**.
`CalendarIntervalTrigger` 는 캘린더 기준으로 계산하므로 월/년 주기와 타임존을 정확히 다룬다.
타임존은 `batch.timezone` (`Asia/Seoul`) 프로퍼티에서 온다.

---

## 5. `BuiltinJobs` — 코드로 선언하는 고정 스케줄

`spec/BuiltinJobs.java`

| enum | key | cron | 의미 | Misfire | Job 클래스 | jobData |
|---|---|---|---|---|---|---|
| `AUTO_TAG_COLLECT` | `autoTagCollect` | `50 * * * * ?` | 매분 50초 | `DO_NOTHING` | `TagAutoCollectJobRunner` | `timeGapMinutes=3`, `jobExecutor=AUTO_TAG_COLLECT` |
| `MONTHLY_PARTITION` | `monthlyPartition` | `30 50 23 L * ?` | 월말 23:50:30 | `FIRE_AND_PROCEED` | `PartitionTableCheckRunner` | `jobExecutor=PARTITION_TABS_CHK` |
| `TAG_INFO_CLEANUP` | `tagInfoCleanUp` | `40 5 0/2 * * ?` | 2시간마다 | `DO_NOTHING` | `TagInfoCleanUpRunner` | `jobExecutor=TAG_INFO_CLEANUP` |

세 개 모두 `JobTargetType.STATIC` 그룹이다.

### Misfire 정책 선택의 논리

서버 다운·과부하로 트리거를 놓쳤을 때의 행동을 정한다.

| 정책 | 동작 | 적용 Job과 이유 |
|---|---|---|
| `DO_NOTHING` | 놓친 실행을 버리고 **다음 정상 시각**을 기다림 | `AUTO_TAG_COLLECT` — 밀린 1분 수집을 몰아서 돌 이유가 없다. 매분 새 데이터가 온다<br/>`TAG_INFO_CLEANUP` — 2시간 뒤 어차피 최신 상태로 맞춰진다 |
| `FIRE_AND_PROCEED` | **즉시 한 번 실행**한 뒤 정상 스케줄 복귀 | `MONTHLY_PARTITION` — 월말 실행을 놓치면 **다음 달 파티션이 없어 insert 가 실패**한다. 반드시 한 번은 돌아야 한다 |
| `IGNORE_MISFIRES` | 놓친 실행을 **전부** 몰아서 실행 | 현재 사용처 없음 |

정책은 `JobSpec.MisfirePolicy` enum 으로 선언하고 `QuartzJobManageComp.upsertJob` 이 스케줄 빌더에 반영한다.

```java
switch (spec.getMisfire()) {
    case DO_NOTHING      -> scheduleBuilder.withMisfireHandlingInstructionDoNothing();
    case FIRE_AND_PROCEED -> scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
    case IGNORE_MISFIRES  -> scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
}
```

### 고정 스케줄 추가 방법

`BuiltinJobs` 에 enum 상수 하나를 추가하면 끝이다. `reconcileStaticJobs()` 가 `values()` 를 순회하므로
등록 코드를 따로 쓸 필요가 없고, 반대로 상수를 지우면 **다음 기동 시 Quartz 에서도 자동 삭제**된다.

```java
Set<String> jobKeys = quartzJobManageComp.getJobIdSet(group);
for (BuiltinJobs spec : BuiltinJobs.values()) {
    quartzJobManageComp.upsertJob(spec);
    jobKeys.remove(spec.getKey());
}
removeJobBySet(jobKeys, group);      // enum 에서 사라진 Job 정리
```

---

## 6. `/internal` — API 서버와의 연계

`controller/InternalRequestController.java` 는 API 서버(8080)가 배치 서버(8081)를 호출하는 창구다.
`@UncheckedJwtToken` 이 붙어 JWT 검사 AOP 를 우회한다([common.md](common.md)).

| Method | Path | 동작 |
|---|---|---|
| `POST` | `/internal/job` | 스케줄 등록/수정 (`ScheduleJobRequest`) |
| `DELETE` | `/internal/job/{group}/{targetId}` | 스케줄 삭제 |
| `POST` | `/internal/collect/{dsId}` | 데이터셋 태그 재수집 → `tagManualCollectJob` |
| `POST` | `/internal/dataset/{dsId}` | 데이터셋 파일 생성 |
| `POST` | `/internal/program-run/{pgmId}` | 프로그램 최초 실행 |
| `POST` | `/internal/layer-manage` | SHP → DB 이관 |

사용자가 API 서버에서 프로그램을 등록하면 곧바로 `/internal/job` 이 호출되어 **재기동 없이도 스케줄이 반영**된다.
reconcile 은 그 경로가 실패했거나 서버가 재기동됐을 때를 위한 **보정 장치**다.

> `deleteJob` 은 `scheduleService.delete(group.name(), targetId)` 로 호출하는데 실제 시그니처는 `delete(String targetId, String group)` 이다. 인자 순서가 뒤바뀌어 JobKey 가 `{targetId} 그룹의 job:{group}` 으로 조립된다.

---

## 7. 종료 — `ShutdownOrderGuard`

`SmartLifecycle` 의 `getPhase()` 를 `Integer.MAX_VALUE` 로 두어 **가장 마지막에 stop** 되게 한다.

```java
scheduler.shutdown(true);   // ① 실행 중 Job 완료 대기
// finally
tpe.shutdown();             // ② batchExecutor graceful shutdown
```

순서가 뒤바뀌면 실행 중인 파티션 Slave Step 이 스레드풀 부재로 실패한다.
`SchedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true)` 와 함께 작동한다.

---

## 8. 관련 문서

- [README.md](README.md) — Quartz → Spring Batch 브릿지 구조 총론
- [collect.md](collect.md) · [partition.md](partition.md) · [tag.md](tag.md) — `BuiltinJobs` 가 실행하는 세 Job
- [program.md](program.md) · [dataset.md](dataset.md) — `PROGRAM` / `DATASET` / `TEMP` 그룹 Job
- [common.md](common.md) — `QuartzJobManageComp` · JWT AOP · Auditing
- `doc/ddl/22.quartz테이블초기화.sql` — `QRTZ_*` 스키마
