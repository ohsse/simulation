package com.hscmt.simulation.collect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.collect.dto.TagCollectDto;
import com.hscmt.simulation.dataset.domain.WaternetTag;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDto;
import com.hscmt.simulation.dataset.repository.MeasureDatasetRepository;
import com.hscmt.simulation.dataset.repository.WaternetTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectTagService {
    private final MeasureDatasetRepository repository;
    private final WaternetTagRepository waternetTagRepository;
    private final JobLauncher jobLauncher;
    @Qualifier("tagManualCollectJob")
    private final Job job;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public void collectTagData (String dsId) {
        MeasureDatasetDto target = repository.findDatasetDetailInfoByDsId(dsId)
                .stream().anyMatch(t -> t.getDsId().equals(dsId)) ? repository.findDatasetDetailInfoByDsId(dsId).getFirst() : null;

        if (target == null) return;

        List<MeasureDatasetDetailDto> items = target.getDetailItems();
        if (items == null || items.isEmpty()) return;

        Set<String> tagSns = items.stream().map(MeasureDatasetDetailDto::getTagSn).collect(java.util.stream.Collectors.toSet());

        List<WaternetTag> wnetTags = waternetTagRepository.findAllTagsByIds(tagSns);

        List<TagCollectDto> tagCollectDtos = new ArrayList<>();

        LocalDateTime requestStrtDttm;
        LocalDateTime requestEndDttm;

        if (target.getRltmYn() == YesOrNo.Y) {
            requestEndDttm = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            requestStrtDttm = requestEndDttm.minus(target.getInqyTerm(), target.getTermTypeCd().getUnit());
        } else {
            requestStrtDttm = target.getStrtDttm();
            requestEndDttm = target.getEndDttm();
        }

        for (WaternetTag wnetTag : wnetTags) {
            TagCollectDto dto = new TagCollectDto();
            dto.setStartLogTime(formatter.format(requestStrtDttm));
            dto.setEndLogTime(formatter.format(requestEndDttm));
            dto.setTagsn(wnetTag.getTagSn());
            dto.setTagSeCd(wnetTag.getTagSeCd());
            tagCollectDtos.add(dto);
        }

        Path tempFilePath;
        try {
            tempFilePath = Files.createTempFile(UuidCreator.getTimeOrderedEpoch() + "_" + "tags",".txt");
            ObjectMapper om = new ObjectMapper();
            try (BufferedWriter writer = Files.newBufferedWriter(tempFilePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (TagCollectDto dto : tagCollectDtos) {
                    writer.write(om.writeValueAsString(dto));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            log.error("create temp file error : {}", e.getMessage());
            throw new RuntimeException(e);
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("tempFilePath", tempFilePath.toAbsolutePath().toString())
                .addString("jobExecutor", target.getMdfId())
                .addJobParameter("fireTime", LocalDateTime.now(), LocalDateTime.class)
                .toJobParameters();

        try {
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            log.error("tag manual collect Job start error : ", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
