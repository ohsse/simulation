package com.hscmt.simulation.common.comp;

import com.hscmt.common.enumeration.JobTargetType;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.job.ScheduleJobRequest;
import com.hscmt.simulation.layer.dto.LayerUpsertRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchClientComp {
    @Qualifier("batchWebClient")
    private final WebClient webClient;

    /* job 등록 및 수정 */
    public void upsertJob(ScheduleJobRequest request) {
        webClient.post()
                .uri("/job")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ResponseObject.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(t -> t instanceof IOException || t instanceof TimeoutException))
                .doOnError(e -> log.error("sendJob error", e))
                .subscribe();
    }

    /* job 삭제 */
    public void deleteJob (JobTargetType group, String targetId) {
        webClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/job/{group}/{targetId}").build(group, targetId))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }

    /* 태그데이터 수집 */
    public void collectTags (String dsId) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/collect/{dsId}").build(dsId))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }

    /* 계측데이터셋 파일 생성 */
    public void createDatasetFile(String dsId) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/dataset/{dsId}").build(dsId))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();

    }

    /* 시각화 파일을 위한 프로그램 최초 실행 */
    public void firstRunProgram (String pgmId) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/program-run/{pgmId}").build(pgmId))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
    
    /* 레이어 파일 내용 저장 */
    public void saveLayerFileToDb (LayerUpsertRequest request) {
        webClient.post()
                .uri("/layer-manage")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
