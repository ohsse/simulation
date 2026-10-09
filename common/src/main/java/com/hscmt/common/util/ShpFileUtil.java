package com.hscmt.common.util;

import com.hscmt.common.enumeration.CrsyType;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.w3.xlink.Simple;

import java.io.File;

@Slf4j
public class ShpFileUtil {

    public static final CrsyType DEFAULT_COORDINATE_SYSTEM = CrsyType.EPSG5186;

    /* shp 파일 읽어서 geojson으로 변경 (source/target 같으면 변환 스킵) */
    public static JSONObject convertShpToGeoJson(String shpFilePath, String sourceEpsg, String targetEpsg) {
        File shpFile = new File(shpFilePath);
        JSONObject geojson = new JSONObject();
        geojson.put("type", "FeatureCollection");
        JSONArray features = new JSONArray();

        FileDataStore fileDataStore = null;
        try {
            fileDataStore = FileDataStoreFinder.getDataStore(shpFile);
            if (fileDataStore instanceof ShapefileDataStore sds) {
                // 보편적으로 국내 SHP는 MS949
                sds.setCharset(java.nio.charset.Charset.forName("MS949"));
            }
            SimpleFeatureSource featureSource = fileDataStore.getFeatureSource();
            SimpleFeatureType featureType = featureSource.getSchema();
            geojson.put("featureType", featureType.getGeometryDescriptor().getType().getBinding().getSimpleName());
            SimpleFeatureCollection featureCollection = featureSource.getFeatures();

            // 1) 원본 CRS (.prj) 읽기
            CoordinateReferenceSystem srcCrs = featureSource.getSchema().getCoordinateReferenceSystem();

            // 2) 사용자가 source EPSG를 넘겼다면 우선 적용 (축순서 EAST_NORTH 고정)
            if (sourceEpsg != null && !sourceEpsg.isBlank()) {
                srcCrs = CRS.decode(normalizeEpsg(sourceEpsg), true);
            }

            // 3) 타깃 CRS 결정 + 변환기 준비 (동일하면 스킵)
            MathTransform tx = null;
            if (targetEpsg != null && !targetEpsg.isBlank()) {
                // 빠른 문자열 비교로 스킵(예: "EPSG:5186" vs "epsg:5186")
                if (srcCrs != null && sourceEpsg != null
                        && normalizeEpsg(sourceEpsg).equalsIgnoreCase(normalizeEpsg(targetEpsg))) {
                    tx = null; // 동일 → 스킵
                } else {
                    CoordinateReferenceSystem targetCrs = CRS.decode(normalizeEpsg(targetEpsg), true);
                    if (srcCrs != null) {
                        // 메타데이터 무시 동등성 → 동일 좌표계면 변환 스킵
                        if (CRS.equalsIgnoreMetadata(srcCrs, targetCrs)) {
                            tx = null;
                        } else {
                            tx = CRS.findMathTransform(srcCrs, targetCrs, true);
                        }
                    } else {
                        log.error("source epsg is null (no .prj and no source override)");
                    }
                }
            }

            convertFeaturesToJson(featureCollection, features, tx, targetEpsg);
            geojson.put("features", features);
            return geojson;

        } catch (Exception e) {
            log.error("shp file convert to geojson error : {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try { if (fileDataStore != null) fileDataStore.dispose(); } catch (Exception ignore) {}
        }
    }

    /** source만 받고 target은 기본좌표계로 */
    public static JSONObject convertShpToGeoJson(String shpFilePath, String sourceEpsg ) {
        return convertShpToGeoJson(shpFilePath, sourceEpsg, DEFAULT_COORDINATE_SYSTEM.getEpsgName());
    }

    @SuppressWarnings("unchecked")
    private static void convertFeaturesToJson(SimpleFeatureCollection featureCollection,
                                              JSONArray features,
                                              MathTransform tx,
                                              String targetEpsg) {

        try (FeatureIterator<SimpleFeature> featureIter = featureCollection.features()) {
            long index = 0;
            final int targetSrid = extractEpsgNumber(targetEpsg);

            while (featureIter.hasNext()) {
                SimpleFeature feature = featureIter.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                if (geometry == null || geometry.isEmpty()) continue;

                // (동일 CRS면 tx == null → 변환 스킵)
                if (tx != null && !tx.isIdentity()) {
                    try {
                        geometry = JTS.transform(geometry, tx);
                    } catch (Exception e) {
                        log.error("convert coordinate system error : {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                }

                // 유효성 보정 및 멀티화
                geometry = GeometryFixer.fix(geometry);
                geometry = promoteToMultiIfNeeded(geometry);

                // 정밀도 감소(전송량/클라 성능 개선)
                geometry = GeometryPrecisionReducer.reduce(geometry, new PrecisionModel(1e6));

                // 타깃 SRID 기록
                if (targetSrid > 0) {
                    geometry.setSRID(targetSrid);
                }

                JSONObject featureJson = new JSONObject();
                featureJson.put("type", "Feature");
                featureJson.put("id", index);

                JSONObject geometryJson = new JSONObject();
                geometryJson.put("type", geometry.getGeometryType());
                JSONArray coordinates = convertCoordinates(geometry);
                geometryJson.put("coordinates", coordinates);
                featureJson.put("geometry", geometryJson);

                JSONObject properties = new JSONObject();
                for (AttributeDescriptor descriptor : feature.getFeatureType().getAttributeDescriptors()) {
                    String attrName = descriptor.getLocalName();
                    Object value = feature.getAttribute(attrName);
                    if (value != null && !(value instanceof Geometry)) {
                        properties.put(attrName, value.toString());
                    }
                }
                featureJson.put("properties", properties);

                features.add(featureJson);
                index++;
            }
        } catch (Exception e) {
            log.error("shp file convert feature to geojson error : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /** 단일/복수 타입을 GeoJSON 멀티 타입으로 승격 */
    private static Geometry promoteToMultiIfNeeded(Geometry g) {
        GeometryFactory gf = g.getFactory();
        return switch (g) {
            case Polygon polygon -> gf.createMultiPolygon(new Polygon[]{polygon});
            case LineString lineString -> gf.createMultiLineString(new LineString[]{lineString});
            case Point point -> gf.createMultiPoint(new Point[]{point});
            default -> g;
        };
    }

    /* shp좌표 -> geojson */
    @SuppressWarnings("unchecked")
    private static JSONArray convertCoordinates(Geometry geometry) {
        String type = geometry.getGeometryType();
        switch (type) {
            case "Point":
                return toCoordinate(geometry.getCoordinate());
            case "MultiPoint":
            case "LineString":
                return toCoordinates(geometry.getCoordinates());
            case "MultiLineString":
                return toMultiLineStringCoordinates(geometry);
            case "Polygon":
                return toPolygonCoordinates(geometry);
            case "MultiPolygon":
                return toMultiPolygonCoordinates(geometry);
            default:
                throw new UnsupportedOperationException("Unsupported geometry type: " + type);
        }
    }

    // [x, y]
    @SuppressWarnings("unchecked")
    private static JSONArray toCoordinate(Coordinate coord) {
        JSONArray point = new JSONArray();
        point.add(coord.x);
        point.add(coord.y);
        return point;
    }

    // [[x1, y1], [x2, y2], ...]
    @SuppressWarnings("unchecked")
    private static JSONArray toCoordinates(Coordinate[] coords) {
        JSONArray array = new JSONArray();
        for (Coordinate coord : coords) {
            JSONArray point = new JSONArray();
            point.add(coord.x);
            point.add(coord.y);
            array.add(point);
        }
        return array;
    }

    // [[[x1, y1], ...], [[x2, y2], ...]]
    @SuppressWarnings("unchecked")
    private static JSONArray toMultiLineStringCoordinates(Geometry geometry) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry line = geometry.getGeometryN(i);
            array.add(toCoordinates(line.getCoordinates()));
        }
        return array;
    }

    // [[[x1, y1], ...], [[inner1], [inner2], ...]]
    @SuppressWarnings("unchecked")
    private static JSONArray toPolygonCoordinates(Geometry geometry) {
        JSONArray polygon = new JSONArray();
        org.locationtech.jts.geom.Polygon poly = (org.locationtech.jts.geom.Polygon) geometry;
        polygon.add(toCoordinates(poly.getExteriorRing().getCoordinates()));
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            polygon.add(toCoordinates(poly.getInteriorRingN(i).getCoordinates()));
        }
        return polygon;
    }

    // [[[[x1, y1], ...]], [[[x2, y2], ...]], ...]
    @SuppressWarnings("unchecked")
    private static JSONArray toMultiPolygonCoordinates(Geometry geometry) {
        JSONArray multiPolygon = new JSONArray();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            org.locationtech.jts.geom.Polygon poly = (org.locationtech.jts.geom.Polygon) geometry.getGeometryN(i);
            multiPolygon.add(toPolygonCoordinates(poly));
        }
        return multiPolygon;
    }

    /* --------- helpers --------- */

    /** "4326", "EPSG:4326", "epsg4326" 등 어떤 입력이 와도 "EPSG:4326" 으로 정규화 */
    private static String normalizeEpsg(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D+", "");
        return digits.isEmpty() ? s : "EPSG:" + digits;
    }

    /** 문자열에서 숫자만 추출해 SRID 정수로 반환(없으면 -1) */
    private static int extractEpsgNumber(String s) {
        if (s == null || s.isBlank()) return -1;
        String digits = s.replaceAll("\\D+", "");
        if (digits.isEmpty()) return -1;
        try { return Integer.parseInt(digits); } catch (NumberFormatException e) { return -1; }
    }
}
