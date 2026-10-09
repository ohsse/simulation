package com.hscmt.simulation.layer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hscmt.common.enumeration.ConditionType;
import com.hscmt.common.enumeration.CrsyType;
import com.hscmt.common.enumeration.FeatureType;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.GeometryUtil;
import com.hscmt.common.util.ShpFileUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.layer.dto.LayerListUpsertDto;
import com.hscmt.simulation.layer.dto.LayerStyleInfo;
import com.hscmt.simulation.layer.dto.LayerUpsertRequest;
import com.hscmt.simulation.layer.mapper.LayerMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LayerManageService {
    @Qualifier("simulationSqlSessionTemplate")
    private final SqlSessionTemplate sessionTemplate;
    private final VirtualEnvironmentComponent vComp;
    private final LayerMapper mapper;

    /* 쉐이프파일 to db 이관 */
    public void migrateShpToDb (LayerUpsertRequest request) {
        String layerId = request.layerId();

        CrsyType crsyType = request.crsyType();
        /* 쉐이프 파일 저장된 경로 */
        String shpDirPath = FileUtil.getDirPath(vComp.getLayerBasePath(), layerId);
        /* 쉐이프 파일 경로에 저장된 모든 파일 */
        List<File> shpFiles = FileUtil.getOnlyFilesInDirectory(shpDirPath);
        /* shp 확장자 파일 선택 */
        File shpFile = Objects.requireNonNull(shpFiles).stream()
                .filter(file -> FileUtil.getFileExtension(file.getName()).equals("shp"))
                .findFirst()
                .orElse(null);

        if (shpFile != null) {
            /* shp 파일을 읽어온 geojson 객체 */
            JSONObject geojson = ShpFileUtil.convertShpToGeoJson(shpFile.getAbsolutePath(), crsyType.getEpsgName());
            /* geojson to dto list 변환 */
            List<LayerListUpsertDto> upsertList = convertGeoJsonToUpsertList (geojson, layerId, request.executorId());

            if (upsertList != null && !upsertList.isEmpty()) {
                LayerListUpsertDto dto = upsertList.getFirst();
                ObjectMapper om = new ObjectMapper();
                try {
                    Map<String, Object> propertyMap = om.readValue(dto.getProperty(), new TypeReference<>() {});


                    List<String> propertyKeyList = new ArrayList<>();
                    propertyMap.forEach((k,v)-> propertyKeyList.add(k));

                    mapper.updateLayerLayerProperties(layerId, om.writeValueAsString(propertyKeyList));

                } catch (Exception e) {
                    log.error("property to map error : {}", e.getMessage());
                }
            }

            /* 색깔 가공 */
            setFeatureColors(upsertList, request.styleInfo());

            /* 데이터 DB 저장 */
            save(upsertList);
            /* 레이어 사용가능 여부 수정 */
            mapper.updateLayerUseAble(layerId, request.executorId(), getFeatureType(geojson));
        }
    }

    /* 색상 지정 */
    public void setFeatureColors (List<LayerListUpsertDto> upsertList, List<LayerStyleInfo> styleList) {
        if (upsertList == null || upsertList.isEmpty()) return;
        if (styleList == null || styleList.isEmpty()) return;

        final ObjectMapper om = new ObjectMapper();

        // 1) 스타일 정리: null 제거 + 우선순위 정렬
        List<LayerStyleInfo> ordered = styleList.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt((LayerStyleInfo s) -> parsePriority(s.getPriority())) // priority ASC
                        .thenComparingInt(this::specificityWeight)                           // 정확/좁은폭 우선
                        .thenComparingDouble(this::minValueForSort)                          // min ASC
                )
                .toList();

        // 2) 스타일을 property 별로 그룹핑 (같은 속성끼리만 평가)
        Map<String, List<LayerStyleInfo>> byProperty = new LinkedHashMap<>();
        for (LayerStyleInfo s : ordered) {
            if (s.getProperty() == null || s.getProperty().isBlank()) continue;
            byProperty.computeIfAbsent(s.getProperty(), k -> new ArrayList<>()).add(s);
        }

        // 3) 각 feature에 대해 property 맵을 풀고, property 그룹 순서대로 평가 → 첫 매칭에서 색상 확정
        for (LayerListUpsertDto dto : upsertList) {
            if (dto == null || StringUtils.isBlank(dto.getProperty())) continue;

            Map<String, Object> props;
            try {
                props = om.readValue(dto.getProperty(), new TypeReference<>() {
                });
            } catch (Exception e) {
                log.error("property to map error : {}", e.getMessage());
                continue;
            }

            // 이미 다른 로직에서 색상을 지정할 수도 있으니, 여기서만 덮어쓰고 싶으면 조건 추가 가능
            String finalColor = null;

            // property 그룹 순회
            outer:
            for (Map.Entry<String, List<LayerStyleInfo>> entry : byProperty.entrySet()) {
                String prop = entry.getKey();
                List<LayerStyleInfo> rules = entry.getValue();

                Object val = props.get(prop);
                if (val == null) continue; // null이면 match에서 isNullLike와 함께 판단할 수도 있음

                for (LayerStyleInfo rule : rules) {
                    if (rule.getConditionType() == null) continue;
                    if (rule.getColorStr() == null || rule.getColorStr().isEmpty()) continue;

                    if (matchUnified(val, rule)) {
                        finalColor = rule.getColorStr();
                        break outer; // 첫 매칭으로 종료
                    }
                }
            }

            // 결과 반영
            try {
                dto.setProperty(om.writeValueAsString(props));
                if (finalColor != null) {
                    dto.setColorStr(finalColor);
                }
            } catch (JsonProcessingException e) {
                log.error("property to json error : {}", e.getMessage());
            }
        }
    }

    /* === 매칭 로직 (RANGE 포함 통합) === */
    private boolean matchUnified(Object val, LayerStyleInfo cond) {
        ConditionType type = cond.getConditionType();
        if (type == null) return false;

        // RANGE는 별도 처리
        if (type == ConditionType.RANGE) {
            return matchRange(val, cond);
        }

        // 기존 단일조건 매칭
        return matchSingle(val, cond.getStandValue(), type);
    }

    /* 단일조건 매칭 (기존 로직 기반) */
    private boolean matchSingle(Object val, String standValue, ConditionType type) {
        if (val == null) {
            return isNullLike(standValue);
        }

        Optional<BigDecimal> lhsNum = toNumber(val);
        Optional<BigDecimal> rhsNum = toNumber(standValue);

        String lhsStr = String.valueOf(val);

        return switch (type) {
            case EQ -> {
                if (lhsNum.isPresent() && rhsNum.isPresent()) yield lhsNum.get().compareTo(rhsNum.get()) == 0;
                yield Objects.equals(lhsStr, standValue);
            }
            case NEQ -> {
                if (lhsNum.isPresent() && rhsNum.isPresent()) yield lhsNum.get().compareTo(rhsNum.get()) != 0;
                yield !Objects.equals(lhsStr, standValue);
            }
            case GT -> lhsNum.isPresent() && rhsNum.isPresent() && lhsNum.get().compareTo(rhsNum.get()) > 0;
            case GOE -> lhsNum.isPresent() && rhsNum.isPresent() && lhsNum.get().compareTo(rhsNum.get()) >= 0;
            case LT -> lhsNum.isPresent() && rhsNum.isPresent() && lhsNum.get().compareTo(rhsNum.get()) < 0;
            case LOE -> lhsNum.isPresent() && rhsNum.isPresent() && lhsNum.get().compareTo(rhsNum.get()) <= 0;
            case CONTAINS -> standValue != null && lhsStr != null && lhsStr.contains(standValue);
            case STARTS_WITH -> standValue != null && lhsStr != null && lhsStr.startsWith(standValue);
            case ENDS_WITH -> standValue != null && lhsStr != null && lhsStr.endsWith(standValue);
            case RANGE -> false; // 여기 오면 안 됨(위에서 처리)
        };
    }

    /* RANGE 매칭 */
    private boolean matchRange(Object val, LayerStyleInfo cond) {
        // 숫자 비교만 허용
        Optional<BigDecimal> lhs = toNumber(val);
        if (lhs.isEmpty()) return false;

        BigDecimal v = lhs.get();

        BigDecimal min = cond.getRangeMin() == null ? null :new BigDecimal(cond.getRangeMin());
        BigDecimal max = cond.getRangeMax() == null ? null : new BigDecimal(cond.getRangeMax());

        boolean minInc = cond.getMinInclusive() == null || cond.getMinInclusive();
        boolean maxInc = cond.getMaxInclusive() != null && cond.getMaxInclusive();

        // min/max 둘 다 없는 RANGE는 의미 없음
        if (min == null && max == null) return false;

        boolean geMin = true;
        if (min != null) {
            int cmp = v.compareTo(min);
            geMin = minInc ? (cmp >= 0) : (cmp > 0);
        }

        boolean leMax = true;
        if (max != null) {
            int cmp = v.compareTo(max);
            leMax = maxInc ? (cmp <= 0) : (cmp < 0);
        }

        return geMin && leMax;
    }

    /* === 정렬 유틸 === */

    /**
     * priority 문자열을 안전하게 정수로 파싱 (null/비정상 → 0)
     */
    private int parsePriority(String p) {
        if (p == null || p.isBlank()) return 0;
        try {
            return Integer.parseInt(p.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * specificity(정확/좁은폭 우선) 가중치
     * - EQ 같은 "정확일치"는 최상위 우선 (가중치 0)
     * - RANGE는 폭이 좁을수록 우선이지만, 여기선 우선 정렬 가중치로 RANGE 전체를 단일보다 먼저/나중에 둘지 결정
     *   → 여기선 단일과 RANGE를 같은 선상에 두되, 다음의 min 정렬과 실제 폭 비교로 세밀화
     * - 그 외 단일 비교연산은 기본 (가중치 2)
     */
    private int specificityWeight(LayerStyleInfo s) {
        if (s.getConditionType() == ConditionType.EQ) return 0; // 가장 정확
        if (s.getConditionType() == ConditionType.RANGE) return 1;
        return 2;
    }

    /**
     * 정렬용 min 값.
     * - RANGE: rangeMin 사용 (없으면 -무한대 취급)
     * - 단일: standValue가 숫자면 그 값, 아니면 +무한대로 밀기(문자 비교는 숫자보다 뒤로)
     */
    private double minValueForSort(LayerStyleInfo s) {
        if (s.getConditionType() == ConditionType.RANGE) {
            return s.getRangeMin() == null ? Double.NEGATIVE_INFINITY : Double.parseDouble(s.getRangeMin());
        }
        Optional<BigDecimal> rhs = toNumber(s.getStandValue());
        return rhs.map(BigDecimal::doubleValue).orElse(Double.POSITIVE_INFINITY);
    }

    /* 숫자로 변경 */
    private Optional<BigDecimal> toNumber(Object o) {
        if (o == null) return Optional.empty();
        try {
            if (o instanceof Number n) return Optional.of(new BigDecimal(n.toString()));
            String s = o.toString().trim();
            if (s.isEmpty()) return Optional.empty();
            return Optional.of(new BigDecimal(s));
        } catch (Exception e) {
            log.debug("to number skip (not numeric): {}", o);
            return Optional.empty();
        }
    }

    /* null 확인 */
    private boolean isNullLike(Object v) {
        if (v == null) return true;
        String s = v.toString();
        return s == null || s.trim().isEmpty();
    }

    /* 데이터 저장 */
    public void save (List<LayerListUpsertDto> upsertList) {
        if (upsertList == null || upsertList.isEmpty()) return;

        LayerMapper mapper = sessionTemplate.getMapper(LayerMapper.class);

        long count = 0;

        /* 천건씩 저장 */
        for (LayerListUpsertDto dto : upsertList) {
            mapper.upsertLayerList(dto);
            count++;

            if (count % 1000 == 0) {
                sessionTemplate.flushStatements();
                sessionTemplate.clearCache();
            }
        }

        if (count % 1000 != 0) {
            sessionTemplate.flushStatements();
            sessionTemplate.clearCache();
        }
    }

    /* geojson to upsert list */
    private List<LayerListUpsertDto> convertGeoJsonToUpsertList (JSONObject geojson, String layerId, String executorId) {
        final ObjectMapper mapper = new ObjectMapper();

        List<LayerListUpsertDto> resultList = new ArrayList<>();
        Object jsonArr = geojson.get("features");
        if(Objects.isNull(jsonArr)) return null;

        if (jsonArr instanceof JSONArray features) {
            for (Object elem : features) {
                if (elem instanceof JSONObject feature) {

                    LayerListUpsertDto dto = new LayerListUpsertDto();
                    Long id = (feature.get("id") instanceof Number n) ? n.longValue() : null;
                    dto.setFid(id);
                    dto.setLayerId(layerId);
                    JSONObject geometry = (feature.get("geometry") instanceof JSONObject g) ? g : null;
                    JSONObject properties = (feature.get("properties") instanceof JSONObject p) ? p : null;

                    /* property 바인딩 */
                    if (properties != null) {
                        try {
                            dto.setProperty(mapper.writeValueAsString(properties));
                        } catch (JsonProcessingException e) {
                            log.error("properties to json error : {}", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }

                    /* geometry 및 ftype 바인딩 */
                    if (geometry != null) {
                        dto.setGmtrVal(GeometryUtil.readGeoJsonGeometryToWkt(geometry));
                        String type = geometry.get("type").toString();
                        FeatureType.fromTypeName(type).ifPresent(ftype -> dto.setFtype(ftype.name()));
                    }

                    dto.setLoginId(executorId);

                    resultList.add(dto);
                }
            }
        }

        if (resultList.isEmpty()) return List.of();

        return resultList.stream()
                .filter(Objects::nonNull)
                .filter(dto -> dto.getFid() != null && dto.getLayerId() != null && dto.getFtype() != null)
                .toList();
    }

    private String getFeatureType (JSONObject geojson) {
        Object featureType = geojson.get("featureType");
        if (featureType instanceof String type) {
            FeatureType ftype = FeatureType.fromTypeName(type).orElse(null);
            return ftype == null ? null : ftype.name();
        }
        return null;
    }
}
