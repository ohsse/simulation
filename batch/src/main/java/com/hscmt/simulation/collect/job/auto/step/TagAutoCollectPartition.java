package com.hscmt.simulation.collect.job.auto.step;

import com.hscmt.common.util.PartitionerUtil;
import com.hscmt.simulation.collect.dto.TagCollectDto;
import com.hscmt.simulation.collect.repository.TagCollectRepository;
import com.hscmt.simulation.dataset.dto.WaternetTagDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@StepScope
@Slf4j
public class TagAutoCollectPartition implements Partitioner {

    @Value("#{jobParameters['targetDateTime']}")
    private LocalDateTime targetDateTime;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final TagCollectRepository collectRepository;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        String targetLogTime = formatter.format(targetDateTime);
        /* 파티션 정보 담을 객체 */
        Map<String, ExecutionContext> partitions = new HashMap<>();
        /* 대상태그목록 조회 */
        List<WaternetTagDto> tags = collectRepository.findAllCollectTag(null);
        log.info("수집대상 태그 갯수 : {}", tags.size());

        List<TagCollectDto> targets = tags
                .stream()
                .map(waternetTagDto -> {
                    TagCollectDto dto = new TagCollectDto();
                    dto.setTagsn(waternetTagDto.getTagSn());
                    dto.setTagSeCd(waternetTagDto.getTagSeCd());
                    dto.setTargetLogTime(targetLogTime);
                    return dto;
                })
                .toList();
        
        /* 파티션크기 설정 */
        int actualGridSize = PartitionerUtil.getActualGridSize(gridSize, targets.size());
        int partitionSize = PartitionerUtil.getPartitionSize(actualGridSize, targets.size());

        /* 파티션할당 */
        PartitionerUtil.splitPartition(partitions, actualGridSize, partitionSize, targets);

        return partitions;
    }
}
