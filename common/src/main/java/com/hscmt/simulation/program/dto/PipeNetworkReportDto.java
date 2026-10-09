package com.hscmt.simulation.program.dto;

import com.hscmt.common.util.RptFileReader;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.hpsf.Section;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@Schema(description = "관망해석 보고서 결과")
public class PipeNetworkReportDto {
    @Schema(description = "링크 결과 목록")
    private List<LinkResultDto> linkResults;
    @Schema(description = "노드 결과 목록")
    private List<NodeResultDto> nodeResults;
    @Schema(description = "보고서 시간 목록")
    private List<String> reportTimes;
    @Schema(description = "노드 범위 설정 목록")
    private List<SectionRange> nodeRngList;
    @Schema(description = "링크 범위 설정 목록")
    private List<SectionRange> linkRngList;

    @SuppressWarnings({"unchecked"})
    public PipeNetworkReportDto(List<SectionRange> sectionRangeList, File reportFile) throws IOException {
        Map<String, Object> reportResult = RptFileReader.read(reportFile.getAbsolutePath());

        List<Map<String, Object>> linkResultList = (List<Map<String, Object>>) reportResult.get("linkResults");
        List<SectionRange> linkRngList = sectionRangeList.stream().filter(x -> x.getSectionType() == "LINK").toList();
        addResultColor(linkResultList, linkRngList);
        List<Map<String, Object>> nodeResultList = (List<Map<String, Object>>) reportResult.get("nodeResults");
        List<SectionRange> nodeRngList = sectionRangeList.stream().filter(x -> x.getSectionType() == "NODE").toList();
        addResultColor(nodeResultList, nodeRngList);

        this.reportTimes = (List<String>) reportResult.get("reportTimes");
        this.linkResults = linkResultList.stream().map(LinkResultDto::new).toList();
        this.nodeResults = nodeResultList.stream().map(NodeResultDto::new).toList();
        this.linkRngList = linkRngList;
        this.nodeRngList = nodeRngList;
    }

    /* 보고서 결과 색상 칠하기 */
    private void addResultColor (List<Map<String, Object>> resultList, List<SectionRange> sectionRangeList) {
        for (SectionRange range : sectionRangeList) {
            String key = range.getAttributeId();

            double rngOdn1 = range.getRngOdn1();
            double rngOdn2 = range.getRngOdn2();
            double rngOdn3 = range.getRngOdn3();
            double rngOdn4 = range.getRngOdn4();

            String colorOdn1 = range.getColorOdn1();
            String colorOdn2 = range.getColorOdn2();
            String colorOdn3 = range.getColorOdn3();
            String colorOdn4 = range.getColorOdn4();

            for (Map<String, Object> target : resultList) {
                if (!Objects.isNull(target.get(key))) {
                    double value = Double.parseDouble(String.valueOf(target.get(key)));

                    String resultColor = "";
                    String colorKey = key + "Color";

                    if (value <= rngOdn1) {
                        resultColor = colorOdn1;
                    } else if (value > rngOdn1 && value <= rngOdn2) {
                        resultColor = colorOdn2;
                    } else if (value > rngOdn2 && value <= rngOdn3) {
                        resultColor = colorOdn3;
                    } else if (value > rngOdn3 && value <= rngOdn4) {
                        resultColor = colorOdn4;
                    } else if (value > rngOdn4){
                        resultColor = "#A862F3";
                    }
                    target.put(colorKey, resultColor);
                }
            }
        }
    }
}
