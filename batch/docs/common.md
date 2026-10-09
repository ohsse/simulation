# 공통 인프라 — `simulation.common` · `com.hscmt.common`

batch 모듈의 인프라 계층이다. **패키지가 두 곳으로 나뉘어 있고 이름이 비슷해 혼동하기 쉬우므로** 먼저 구분한다.

| 패키지 | 성격 | 예 |
|---|---|---|
| `com.hscmt.simulation.common` | **설정(config) 중심** — DataSource · Tx · MyBatis · Quartz · JWT | `SimulationJpaConfig`, `SimulationTx` |
| `com.hscmt.common` (batch 모듈 내) | **컴포넌트·유틸 중심** — Quartz 조작, 파티션 계산, 웹 필터 | `QuartzJobManageComp`, `PartitionerUtil` |
| `com.hscmt.common` (**common 모듈**) | 3개 모듈이 공유하는 자산 | `AsyncConfig`, `FileUtil`, DTO, enum |

세 번째는 batch 모듈이 아니라 **`common` 서브프로젝트**에 있다. 패키지명이 같아 IDE 에서 섞여 보이므로,
클래스를 찾을 때는 모듈 경로까지 확인해야 한다.

---

## 1. 멀티 데이터소스 구성

이 모듈은 **PostgreSQL(`simulation`) + Tibero(`waternet`)** 두 DB 를 동시에 쓴다.
게다가 `simulation` 쪽은 JPA · QueryDSL · MyBatis · JdbcTemplate **네 가지 접근 방식**을 병행한다.

```
simulation/common/config/
├── jpa/
│   ├── SimulationJpaConfig.java        DataSource · EMF · TxManager  (@Primary)
│   ├── SimulationQueryDslConfig.java   simulationQueryFactory        (@Primary)
│   └── SimulationTx.java               @Transactional 메타 애노테이션
├── jdbc/SimulationJdbcConfig.java      simulationJdbcTemplate
├── mybatis/
│   ├── SimulationMybatisConfig.java    SqlSessionTemplate (ExecutorType.BATCH)
│   └── SimulationMapper.java           @Mapper 마커 애노테이션
└── quartz/QuartzConfig.java            SchedulerFactoryBean
```

### 1.1 `@Primary` 배치가 핵심

```java
// simulation/common/config/jpa/SimulationJpaConfig.java
@Primary
@Bean(name = {"simulationDataSource", "dataSource"})
@QuartzDataSource      // Quartz QRTZ_* 테이블
@BatchDataSource       // Spring Batch BATCH_* 테이블
public DataSource dataSource(@Qualifier("simulationDataSourceProps") DataSourceProps props) {
    return DatabaseUtil.createHikariDataSource(props);
}
```

Bean 이름을 `{"simulationDataSource", "dataSource"}` 두 개로 등록한 것이 요령이다.
`dataSource` 라는 관례적 이름을 함께 노출해 **이름으로 찾는 스프링 내부 코드**와도 맞물린다.

`@QuartzDataSource` + `@BatchDataSource` 를 동시에 붙였으므로
**Quartz 메타 · Spring Batch 메타 · 업무 데이터가 모두 같은 PostgreSQL 인스턴스**에 있다.

| 장점 | 단점 |
|---|---|
| Job 실행 결과와 업무 데이터가 한 트랜잭션 경계에 놓일 수 있다 | 배치 메타 부하가 업무 DB 에 그대로 얹힌다 |
| 운영 DB 가 하나라 백업·모니터링이 단순하다 | 메타테이블 정리(purge)를 별도로 신경 써야 한다 |

또한 `BatchApplication` 이 `DataSourceAutoConfiguration` 을 **제외**하고 있다.
DataSource 를 수동으로 두 개 만드는 이상, 자동 설정이 끼어들면 충돌하기 때문이다.

```java
@SpringBootApplication(scanBasePackages = {"com.hscmt"}, exclude = { DataSourceAutoConfiguration.class })
```

### 1.2 네 가지 접근 방식과 각각의 용처

| 방식 | Bean | 이 모듈에서의 용처 |
|---|---|---|
| **JPA** | `simulationEntityManagerFactory` | 엔티티 CRUD, `deleteAllByIdInBatch` 벌크 삭제 |
| **QueryDSL** | `simulationQueryFactory` | 동적 조건 조회, 계측값 동적 피벗 |
| **MyBatis** | `simulationSqlSessionTemplate` | **DDL 실행**(파티션), upsert, 대량 UPDATE |
| **JdbcTemplate** | `simulationJdbcTemplate` | `pg_inherits` 카탈로그 조회 |

**왜 넷이나 필요한가** — 각각이 아니면 곤란한 지점이 있다.

- **MyBatis** — `create table ${name} partition of ...` 같은 **DDL 은 JPA/QueryDSL 로 표현할 수 없다**. `${}` 치환으로 테이블명을 조립해야 하는데 이건 MyBatis 의 영역이다([partition.md](partition.md))
- **JdbcTemplate** — `pg_inherits` 조인 같은 **DB 카탈로그 조회**는 엔티티가 없다. RowMapper 한 줄이면 끝나는 일에 MyBatis XML 을 만들 이유가 없다
- **QueryDSL** — 태그 개수만큼 `CASE WHEN` 컬럼을 동적으로 만드는 피벗은 JPQL 문자열로는 감당이 안 된다([dataset.md](dataset.md))
- **JPA** — 프로그램 실행이력처럼 **상태 전이가 있는 엔티티**는 도메인 메서드(`success()`·`fail()`·`stop()`)로 다루는 편이 안전하다

### 1.3 `SqlSessionTemplate` 의 `ExecutorType.BATCH`

```java
// simulation/common/config/mybatis/SimulationMybatisConfig.java
@Bean(name = "simulationSqlSessionTemplate")
public SqlSessionTemplate sqlSessionTemplate(@Qualifier("simulationSqlSessionFactory") SqlSessionFactory factory) {
    return new SqlSessionTemplate(factory, ExecutorType.BATCH);
}
```

이 한 줄이 batch 모듈 전체의 쓰기 성능을 좌우한다.

| ExecutorType | 동작 |
|---|---|
| `SIMPLE` (기본) | statement 마다 즉시 실행 |
| `REUSE` | PreparedStatement 재사용 |
| **`BATCH`** | statement 를 **쌓아두었다가 flush 시점에 JDBC 배치로 일괄 전송** |

덕분에 `TagCollectItemWriter` 가 `chunk.forEach(... upsert ...)` 로 1건씩 호출해도
**JDBC 레벨에서는 1000건이 한 번에** 나간다([collect.md](collect.md)).

**주의점 두 가지**

1. `BATCH` 모드에서는 insert/update 의 **영향 행 수가 즉시 반환되지 않는다**. 반환값에 의존하는 로직은 쓸 수 없다
2. flush 되지 않은 statement 가 계속 쌓이므로, 트랜잭션이 긴 대량 처리에서는 **명시적 `flushStatements()` + `clearCache()`** 가 필요하다 — `LayerManageService.save` 가 그 사례다([layer.md](layer.md))

Step 의 트랜잭션 매니저는 `JpaTransactionManager` 인데 MyBatis BATCH executor 가 그 위에 얹히는 구조다.
같은 `DataSource` 를 공유하므로 커넥션은 하나로 묶이지만, **두 프레임워크의 flush 시점이 다르다**는 점은 유의해야 한다.

### 1.4 매퍼 스캔을 애노테이션으로 가르기

```java
@MapperScan(basePackages = {"com.hscmt.simulation"},
            sqlSessionTemplateRef = "simulationSqlSessionTemplate",
            annotationClass = SimulationMapper.class)     // ← 이 애노테이션이 붙은 것만
```

```java
@Mapper
public @interface SimulationMapper { }     // @Mapper 를 메타 애노테이션으로 포함
```

패키지 경로가 아니라 **애노테이션으로 대상을 한정**한다. 나중에 waternet 쪽 MyBatis 매퍼가 생겨도
`@WaternetMapper` 를 만들어 스캔 범위를 깔끔히 나눌 수 있다.

현재 `@SimulationMapper` 를 쓰는 매퍼는 4개다 — `CollectMapper` · `PartitionMapper` · `WaternetTagMapper` · `LayerMapper`.

### 1.5 트랜잭션 메타 애노테이션

```java
// simulation/common/config/jpa/SimulationTx.java
@Transactional(value = "simulationTransactionManager")
public @interface SimulationTx {
    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean readOnly() default false;
    @AliasFor(annotation = Transactional.class, attribute = "propagation")
    Propagation propagation() default Propagation.REQUIRED;
}
```

TxManager 가 둘이므로 `@Transactional` 을 그냥 쓰면 어느 쪽인지 매번 명시해야 한다.
`@AliasFor` 로 자주 쓰는 두 속성만 뚫어두어 **의도는 유지하면서 이름은 짧게** 만들었다.

```java
@SimulationTx(readOnly = true)                              // 클래스 기본
public class ProgramExecuteService { ... }

@SimulationTx(propagation = Propagation.REQUIRES_NEW)       // 메서드에서 덮어쓰기
public ProgramExecHist registerProgramExecHist(...) { ... }
```

`waternet` 쪽에도 대칭으로 `@WaternetTx` 가 있다([waternet.md](waternet.md)).

---

## 2. 배치 공통 유틸 — `com/hscmt/common/util/`

### 2.1 `PartitionerUtil` — Spring Batch 파티션 분할

Partitioner 3종이 공유한다. 상세는 [README.md](README.md) §4.2.

| 메서드 | 역할 |
|---|---|
| `getActualGridSize(gridSize, targetSize)` | `min(gridSize, targetSize)` — 대상보다 파티션이 많아지지 않게 |
| `getPartitionSize(actualGridSize, targetSize)` | `ceil(targetSize / actualGridSize)` |
| `splitPartition(map, gridSize, partitionSize, targetList)` | `partition0..N` 키로 `targetList` 슬라이스를 `ExecutionContext` 에 저장 |

> `targetSize == 0` 이면 `actualGridSize = 0` → `ceil(0/0)` = `NaN` → `(int) 0`. 루프가 돌지 않아 파티션 0개가 되고 Step 이 실패할 수 있다.

### 2.2 `PartitionCheckUtil` — DB 파티션 명명·계산

`PartitionerUtil` 과 이름이 비슷하지만 **완전히 다른 일**을 한다. 이쪽은 PostgreSQL 물리 파티션 계산이다.

| 메서드 | 역할 |
|---|---|
| `isNeedCreateRangePartition(rule, lastPartitionName)` | 마지막 파티션 기준으로 새 파티션이 필요한지 판정 |
| `getNextPartitionName(rule, lastPartitionName)` | 다음 파티션명 계산. 불필요하면 `null` |
| `getAllNeededPartitionNames(rule, lastPartitionName)` | `null` 이 나올 때까지 while 루프 — **밀린 달을 한 번에 채운다** |
| `getPartitionRangeDto(rule, partitionName)` | 파티션명에서 from/to 역산 |
| `getRangePartitionInfoMap(rule, partitionTableName)` | MyBatis DDL 파라미터 Map 조립 |
| `getDetachPartitionName(rule)` | 보관기간 초과 파티션명 계산 |

**생성 판정 기준이 "내일"** 인 것이 설계 포인트다.

```java
LocalDate compareDate = LocalDate.now().plusDays(1);
return !compareDate.isBefore(nextPartitionDate);
```

월말 23:50 에 도는 스케줄과 짝을 이뤄, 자정 직후 다음 달 파티션이 없어 insert 가 실패하는 사태를 막는다([partition.md](partition.md)).

`RangeFieldType` 에 따라 DDL 값 타입을 갈라주는 것도 이 유틸의 몫이다.

```java
if (RangeFieldType == RangeFieldType.DATE)           rangePartitionInfo.put("fromDate", rangeDto.getFromDate());
else if (RangeFieldType == RangeFieldType.TIMESTAMP) rangePartitionInfo.put("fromDate", rangeDto.getFromDate().atStartOfDay());
```

---

## 3. Quartz 조작 — `com/hscmt/common/comp/`

| 클래스 | 역할 |
|---|---|
| `QuartzJobManageComp` | JobKey/TriggerKey 규약, `registerJob`·`updateJob`·`upsertJob(JobSpec)`·`deleteById`, signature 변경 감지 |
| `QuartzReconciler` | `ApplicationRunner` — `standby()` → reconcile → `start()` |

상세는 [schedule.md](schedule.md). 여기서는 에러 코드만 짚는다.

```java
// com/hscmt/common/QuartzJobErrorCode.java
public enum QuartzJobErrorCode implements ErrorCode {
    DELETE_JOB_ERROR, CHECK_JOB_ERROR, CHECK_TRIGGER_ERROR,
    REGISTER_JOB_ERROR, UPDATE_JOB_ERROR, GROUPING_JOB_ERROR;
}
```

`SchedulerException` 은 체크 예외라 호출부마다 try-catch 를 강요한다.
`QuartzJobManageComp` 는 이를 전부 잡아 `RestApiException` 으로 바꿔 던지므로 **상위 계층이 Quartz API 에 오염되지 않는다.**

---

## 4. Auditing — 배치에서의 "누가"

```java
// com/hscmt/common/comp/AuditingComp.java
@Component
public class AuditingComp implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        String fromRequest = currentUserFromRequest();      // 웹 요청이면 JWT subject
        if (fromRequest != null) return Optional.of(fromRequest);
        return Optional.of("BATCH_SYSTEM");                 // 배치/Quartz/비동기
    }
}
```

`RequestContextHolder` 에 바인딩된 요청이 없으면 `IllegalStateException` 이 나는데, 이를 잡아 `null` 로 처리한다.

```java
} catch (IllegalStateException ignore) {
    return null;   // No thread-bound request (배치/Quartz 등) → 조용히 무시
}
```

배치 스레드에는 요청 컨텍스트가 없으므로 이 경로가 정상이다. 결과적으로 **`rgst_id` 만 봐도 사람이 한 일인지 배치가 한 일인지 구분**된다.

> 배치 Job 들은 별도로 `jobExecutor` JobParameter(`AUTO_TAG_COLLECT` 등)를 전달해 `MsrmUpsertDto` 의 등록자로 쓴다. `AuditorAware` 는 JPA 엔티티용, `jobExecutor` 는 MyBatis DTO 용으로 **경로가 나뉘어 있다.**

---

## 5. 웹 계층 — 배치 서버가 API 도 제공하는 이유

배치 서버(8081)는 API 서버(8080)로부터 명령을 받아야 하므로 `spring-boot-starter-web` 을 포함한다.

### 5.1 JWT 검사 AOP

```java
// simulation/common/aop/CheckJwtToken.java
@Around("execution(* com.hscmt.simulation..controller..*Controller.*(..)) "
      + "&& !@annotation(com.hscmt.simulation.common.annotation.UncheckedJwtToken) "
      + "&& !@within(com.hscmt.simulation.common.annotation.UncheckedJwtToken)")
```

컨트롤러 전역에 JWT 검사를 걸되, `@UncheckedJwtToken` 이 붙은 **메서드(`@annotation`) 또는 클래스(`@within`)** 는 제외한다.
`InternalRequestController` 가 클래스 레벨로 이 애노테이션을 달고 있어 `/internal` 전체가 검사를 우회한다([schedule.md](schedule.md)).

토큰 상태를 3분기로 처리한다.

```java
return switch (tokenState) {
    case VALID   -> { Object result = joinPoint.proceed(); ... }
    case INVALID -> throw new RestApiException(JwtTokenErrorCode.INVALID_TOKEN);
    case EXPIRED -> throw new RestApiException(JwtTokenErrorCode.EXPIRED_TOKEN);
};
```

`EXPIRED` 를 `INVALID` 와 구분하는 것은 프론트가 **재발급을 시도할지 로그아웃시킬지** 판단해야 하기 때문이다.

> AOP 가 컨트롤러 반환값을 `ResponseEntity<ResponseObject<T>>` 로 캐스팅하므로 **모든 컨트롤러가 이 시그니처를 지켜야 한다.** 어기면 `IllegalStateException` 이 난다. Filter 나 Interceptor 로 구현했다면 이 제약이 없었을 자리다.

### 5.2 CORS 필터

`com/hscmt/common/web/CorsFilter.java` — `OncePerRequestFilter`. `X-Internal-Request` 헤더와 Swagger URI 를 우회 대상으로 둔다.

> `request.getHeader("X-Internal-Request") == "true"` 는 문자열 **참조 비교**라 항상 false 다. `"true".equals(...)` 여야 한다.

### 5.3 Swagger

`com/hscmt/common/swagger/SwaggerConfig.java` — "시뮬레이션 Scheduling API" 정의, Authorization 헤더 apiKey 스킴.

```yaml
springdoc:
  api-docs.path: /simulation-batch-docs
  swagger-ui.path: /simulation-batch-ui.html
```

API 서버와 경로를 겹치지 않게 `simulation-batch-` prefix 를 붙였다.

---

## 6. common 모듈(서브프로젝트)에서 오는 것

batch 모듈이 의존하는 `implementation project(':common')` 쪽 자산 중 배치에 직접 관계된 것들이다.

| 클래스 | 역할 |
|---|---|
| `com.hscmt.common.config.AsyncConfig` | `batchExecutor`(16/32/1000) · `asyncExecutor`(8/24/200) 스레드풀 |
| `com.hscmt.common.util.FileUtil` | 경로 조립, `retryDelete`, 디렉토리 크기 계산 |
| `com.hscmt.common.util.ProcessUtil` | 외부 프로세스 실행·종료 |
| `com.hscmt.common.util.ShpFileUtil` | SHP → GeoJSON 변환 |
| `com.hscmt.common.event.AbstractDomainEventPublisher` | 도메인 이벤트 발행 기반 클래스 |
| `com.hscmt.common.enumeration.*` | `JobTargetType` · `CycleCd` · `YesOrNo` · `ExecStat` 등 |
| `com.hscmt.simulation.collect.dto.MsrmUpsertDto` | 수집 결과 DTO (common 모듈에 위치) |

`batchExecutor` 는 **파티션 Slave Step 과 `@Async` 가 공유**한다는 점을 기억해 둘 만하다([program.md](program.md)).

---

## 7. 애플리케이션 생명주기

### 기동

```
BatchApplication
  └ DataSource 2종 수동 생성 (자동설정 제외)
  └ EntityManagerFactory 2종, TxManager 2종
  └ SchedulerFactoryBean (auto-startup: false)
  └ ApplicationRunner: QuartzReconciler
       standby() → reconcileJobs() → start()
```

### 종료 — `com/hscmt/ShutdownOrderGuard.java`

```java
@Override public int getPhase() { return Integer.MAX_VALUE; }   // 가장 마지막에 stop

@Override public void stop() {
    try {
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdown(true);   // ① Job 완료 대기
    } catch (SchedulerException e) {
    } finally {
        if (batchExecutor instanceof ThreadPoolTaskExecutor tpe) tpe.shutdown();      // ② 풀 정리
        else if (batchExecutor instanceof ConcurrentTaskExecutor cte) { ... }
    }
    running = false;
}
```

`SmartLifecycle` 의 phase 가 클수록 늦게 stop 된다. `Integer.MAX_VALUE` 는 **"모든 것보다 나중에"** 라는 선언이다.
`instanceof` 로 실행기 타입을 분기하는 것은 `SimpleAsyncTaskExecutor` 처럼 종료 개념이 없는 구현체로 교체될 가능성에 대한 방어다.

---

## 8. 관련 문서

- [README.md](README.md) — 모듈 개요 · 파티셔닝 · chunk 총론
- [schedule.md](schedule.md) — `QuartzJobManageComp` · `QuartzReconciler` 상세
- [partition.md](partition.md) — `PartitionCheckUtil` 사용처
- [waternet.md](waternet.md) — 두 번째 DataSource 구성
- [collect.md](collect.md) · [layer.md](layer.md) — MyBatis `ExecutorType.BATCH` 활용 사례
