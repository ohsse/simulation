# simulation — 온라인 시뮬레이션 플랫폼 백엔드

사용자가 **Python 분석 프로그램을 웹에서 등록·실행·시각화**하는 플랫폼의 백엔드다.
계측 태그 데이터로 데이터셋을 만들고, 프로그램마다 Python 가상환경과 패키지를 관리한다. 실행 결과는 차트, 대시보드, GIS 레이어로 보여준다.

## 모듈 구성

| 모듈 | 역할 |
|---|---|
| `common` | 공통 도메인·DTO, JWT, 유틸(CSV/XLSX, 좌표 변환, INP 파일 조립), QueryDSL Q클래스 |
| `api` | REST API 서버 (Swagger 기준 15개 그룹, 컨트롤러 16개) |
| `batch` | **Quartz(스케줄) + Spring Batch(대량 처리)** 하이브리드 배치 서버. 태그 수집, 파티션 테이블 관리, 이력 정리 |

### api 기능

| 그룹 | 내용 |
|---|---|
| 사용자 | JWT 액세스·리프레시 토큰 인증, AOP로 전역 토큰 검사 |
| 파이썬 패키지 / 가상환경 | 프로그램별 Python 가상환경 생성, 패키지 설치·조회 (`zt-exec`로 프로세스 실행) |
| 데이터셋 | 계측·관망·사용자 정의 데이터셋, 파일(XLSX/CSV) 생성과 시각화 |
| 프로그램 | Python 프로그램 등록·실행, 실행 이력, 결과 시각화 |
| 레이어 | SHP(쉐이프파일) 업로드와 GeoJSON 변환(GeoTools, JTS, Proj4j) |
| 대시보드 / 그룹 | 대시보드 구성, 리소스 그룹 관리 |
| 외부 연계 | 외부 DB(Tibero) 계측 데이터 조회(조회 전용 2차 DataSource) |

## 기술 스택

- Java 21, Spring Boot 3.4, Spring Data JPA + QueryDSL, MyBatis
- Spring Batch + Quartz, Caffeine Cache
- PostgreSQL(주 DB, 파티션 테이블) + Tibero(외부 조회) **멀티 DataSource**
- GeoTools / JTS / Proj4j (GIS), Apache POI / OpenCSV
- springdoc-openapi (Swagger UI), p6spy

## 설계 포인트

- **스케줄과 실행의 분리**: Quartz는 "언제 돌릴지"만 맡고, 실제 처리는 `JobLauncher`로 Spring Batch에 넘긴다. Master/Slave 파티션 Step으로 병렬 처리한다. → [batch/docs/README.md](batch/docs/README.md)
- **멀티 DataSource와 트랜잭션 분리**: 주 DB와 외부 조회 DB가 각자 EntityManager와 TransactionManager를 가진다. → [batch/docs/common.md](batch/docs/common.md)
- **파티션 테이블 생명주기 자동화**: 계측 데이터 테이블을 월 단위 파티션으로 미리 만들고 정리한다. → [batch/docs/partition.md](batch/docs/partition.md)

## 실행 방법

```bash
# 1) 환경변수 준비 (.env.example 참고)
export SPRING_DATASOURCE_SIMULATION_USERNAME=postgres
export SPRING_DATASOURCE_SIMULATION_PASSWORD=postgres
export JWT_SECRET=<32바이트 이상 임의 문자열>

# 2) DB 스키마: doc/ddl/*.sql 을 번호 순서대로 실행

# 3) 빌드·실행
./gradlew :api:bootRun     # API 서버 (기본 8080)
./gradlew :batch:bootRun   # 배치 서버 (기본 8081)
```

- 프로파일별 설정은 `*/src/main/resources-env/{local,dev,prod}/`에 있습니다. 접속정보는 모두 환경변수로 주입합니다.
- **Tibero JDBC 드라이버는 상용 라이선스라서 저장소에 넣지 않았습니다.** 외부 연계 기능이 필요하면 `libs/tibero6-jdbc.jar`를 직접 넣으세요. 넣으면 빌드에 자동으로 포함됩니다.

## 공개 버전에서 바뀐 점

원본에서 다음을 제거하거나 치환했습니다.
- DB 접속정보와 서버 주소 → 환경변수 자리표시자
- 하드코딩된 JWT 서명 키 → 환경변수 `JWT_SECRET`
- 상용 드라이버 jar, 빌드 산출물, 로그
