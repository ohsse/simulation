package com.hscmt.common.util;

import com.hscmt.common.enumeration.CrsyType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.proj4j.*;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GeometryUtil {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateTransformFactory TRANSFORM_FACTORY = new CoordinateTransformFactory();

    private static final Map<String, Map<String, CoordinateTransform>> TRANSFORM_CACHE = new ConcurrentHashMap<>();


    /**
     * 좌표계 변환
     * @param geometry 원본 좌표정보
     * @param sourceCRS 원본 CRS 정보
     * @param targetCRS 목표 CRS 정보
     * @return 변환 좌표
     */
    public static Geometry transformGeometry (Geometry geometry, CrsyType sourceCRS, CrsyType targetCRS) {
        CoordinateTransform transform = getTransform(sourceCRS.getEpsgName(), targetCRS.getEpsgName());
        return transform(geometry, transform);
    }

    /**
     * 좌표변환기 가져오기
     * @param sourceCRS 원본 CRS
     * @param targetCRS 목표 CRS
     * @return 좌표변환기
     */
    protected static CoordinateTransform getTransform (String sourceCRS, String targetCRS) {
        Map<String, CoordinateTransform> targetTransforms = TRANSFORM_CACHE
                .computeIfAbsent(sourceCRS, k -> new ConcurrentHashMap<>());
        return targetTransforms.computeIfAbsent(targetCRS, k -> {
            CoordinateReferenceSystem srsCrs = CRS_FACTORY.createFromName(sourceCRS);
            CoordinateReferenceSystem targetCrs = CRS_FACTORY.createFromName(targetCRS);
            return TRANSFORM_FACTORY.createTransform(srsCrs, targetCrs);
        });
    }

    protected static Geometry transform (Geometry geometry, CoordinateTransform transform) {
        /* point 좌표변환 */
        if (geometry instanceof Point) {
            return transform((Point) geometry, transform);
        }
        /* linestring 좌표변환 */
        if (geometry instanceof LineString) {
            return transform((LineString) geometry, transform);
        }
        /* polygon 좌표변환 */
        if (geometry instanceof Polygon) {
            return transform((Polygon) geometry, transform);
        }
        /* multipoint 좌표변환 */
        if (geometry instanceof MultiPoint) {
            return transform((MultiPoint) geometry, transform);
        }
        /* multilinestring 좌표변환 */
        if (geometry instanceof MultiLineString) {
            return transform((MultiLineString) geometry, transform);
        }
        /* multipolygon 좌표변환 */
        if (geometry instanceof MultiPolygon) {
            return transform((MultiPolygon) geometry, transform);
        }
        /* collection 좌표변환 */
        if (geometry instanceof GeometryCollection) {
            return transform((GeometryCollection) geometry, transform);
        }
        throw new RuntimeException("Unsupported geometry type : " + geometry.getGeometryType() );
    }

    /**
     * point 좌표 변환
     * @param point point 객체
     * @param transform 좌표변환기
     * @return 변경 point
     */
    protected static Geometry transform (Point point, CoordinateTransform transform) {
        return GEOMETRY_FACTORY.createPoint(transformCoordinate(point.getCoordinate(), transform));
    }

    /**
     * linestring 좌표 변환
     * @param lineString  linestring 객체
     * @param transform 좌표변환기
     * @return 변경 linestring
     */
    protected static Geometry transform (LineString lineString, CoordinateTransform transform) {
        return GEOMETRY_FACTORY.createLineString(transformCoordinates(lineString.getCoordinates(), transform));
    }

    /**
     * polygon 좌표 변환
     * @param polygon 원본 polygon
     * @param transform 좌표변환기
     * @return 변경 polygon
     */
    protected static Geometry transform (Polygon polygon, CoordinateTransform transform) {
        /* 외곽 경계 생성 */
        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(transformCoordinates(polygon.getExteriorRing().getCoordinates(), transform));
        /* 내부 경계 생성 */
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int i = 0 ; i < holes.length ; i ++) {
            holes[i] = GEOMETRY_FACTORY.createLinearRing(transformCoordinates(polygon.getInteriorRingN(i).getCoordinates(), transform));
        }
        return GEOMETRY_FACTORY.createPolygon(shell, holes);
    }

    /**
     * multipoint 좌표 변환
     * @param multiPoint multipoint
     * @param transform 좌표변환기
     * @return 변환 multipoint
     */
    protected static Geometry transform (MultiPoint multiPoint, CoordinateTransform transform) {
        Point[] points = new Point[multiPoint.getNumGeometries()];
        for (int i = 0 ; i < points.length ; i ++) {
            points[i] = (Point) transform( multiPoint.getGeometryN(i), transform);
        }
        return GEOMETRY_FACTORY.createMultiPoint(points);
    }

    /**
     * multilinestring 좌표 변환
     * @param multiLineString 원본 multilinestring
     * @param transform 좌표변환기
     * @return 변환 multilinestring
     */
    protected static Geometry transform (MultiLineString multiLineString, CoordinateTransform transform) {
        LineString[] lineStrings = new LineString[multiLineString.getNumGeometries()];
        for (int i = 0 ; i < lineStrings.length ; i ++) {
            lineStrings[i] = (LineString) transform( multiLineString.getGeometryN(i), transform);
        }
        return GEOMETRY_FACTORY.createMultiLineString(lineStrings);
    }

    /**
     * multipolygon 좌표 변환
     * @param multiPolygon 원본 multipolygon
     * @param transform 좌표변환기
     * @return 변환 multipolygon
     */
    protected static Geometry transform (MultiPolygon multiPolygon, CoordinateTransform transform) {
        Polygon[] polygons = new Polygon[multiPolygon.getNumGeometries()];
        for (int i = 0 ; i < polygons.length ; i ++) {
            polygons[i] = (Polygon) transform( multiPolygon.getGeometryN(i), transform);
        }
        return GEOMETRY_FACTORY.createMultiPolygon(polygons);
    }

    /**
     * geometryCollection 좌표 변환
     * @param geometryCollection 원본 goemetryCollection
     * @param transform 좌표변환기
     * @return 변환 geometryCollection
     */
    protected static Geometry transform (GeometryCollection geometryCollection, CoordinateTransform transform) {
        Geometry[] geometries = new Geometry[geometryCollection.getNumGeometries()];
        for (int i = 0 ; i < geometries.length ; i ++) {
            geometries[i] = transform( geometryCollection.getGeometryN(i), transform);
        }
        return GEOMETRY_FACTORY.createGeometryCollection(geometries);
    }

    /**
     * 좌표변환
     * @param coord 원본좌표
     * @param transform 좌표변환기
     * @return 변경좌표
     */
    protected static Coordinate transformCoordinate (Coordinate coord, CoordinateTransform transform) {
        ProjCoordinate src = new ProjCoordinate(coord.x, coord.y);
        ProjCoordinate dst = new ProjCoordinate();
        transform.transform(src, dst);
        return new Coordinate(dst.x, dst.y);
    }

    /**
     * 좌표목록 변환
     * @param coords 좌표목록
     * @param transform 좌표변환기
     * @return 변경좌표목록
     */
    protected static Coordinate[] transformCoordinates (Coordinate[] coords, CoordinateTransform transform) {
        Coordinate[] coordinates = new Coordinate[coords.length];
        for (int i = 0; i < coords.length; i ++) {
            coordinates[i] = transformCoordinate(coords[i], transform);
        }
        return coordinates;
    }

    /**
     * geometry to wkt 변환
     * @param geometry 대상 geometry
     * @return wkt 문자열
     */
    public static String convertGeometryToWkt (Geometry geometry) {
        if (geometry == null) return null;
        return new WKTWriter().write(geometry);
    }


    /**
     * wkt to geometry 변환
     * @param wkt wkt 문자열
     * @return geometry
     */
    public static Geometry convertWktToGeometry (String wkt) {
        if (wkt == null || wkt.isEmpty()) return null;
        try {
            return new WKTReader().read(wkt);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Wkt String... " + wkt, e);
        }
    }

    public static String getWktLineString (Number[] x, Number[] y) {
        checkNumberArrayLength(x,y);
        Coordinate[] coordinates = new Coordinate[x.length];
        for (int i = 0 ; i < x.length ; i ++) {
            coordinates[i] = new Coordinate(x[i].doubleValue(), y[i].doubleValue());
        }
        return convertGeometryToWkt(GEOMETRY_FACTORY.createLineString(coordinates));
    }

    public static String getWktPoint (Number x, Number y) {
        return convertGeometryToWkt(GEOMETRY_FACTORY.createPoint(new Coordinate(x.doubleValue(), y.doubleValue())));
    }

    public static void checkNumberArrayLength (Number[] x, Number[] y) {
        if (x.length != y.length) throw new IllegalArgumentException("x, y array length must be same.");
    }

    /* GeoJson 으로 형변환 */
    public static JSONObject listToGeoJSON (List<Map<String, Object>> dataList, String geometryKey) {
        return listToGeoJSON(dataList, geometryKey, "WKT");
    }
    /* JsonObject형변환 */
    public static JSONObject listToGeoJSON (List<Map<String, Object>> dataList, String geometryKey, String geometryType) {
        return featuresToGeoJSON(listToFeatures(dataList, geometryKey, geometryType));
    }
    /* listMap to features */
    public static List<Feature> listToFeatures (List<Map<String, Object>> dataList, String geometryKey, String geometryType){
        List<Feature> returnList = new ArrayList<Feature>();

        for (Map<String, Object> dataMap : dataList) {
            if(dataMap.get(geometryKey) != null) {
                Feature feature = mapToGeoJSONFeature(dataMap, geometryKey, geometryType);
                returnList.add(feature);
            }
        }
        return returnList;
    }

    /* map to GeoJson */
    public static Feature mapToGeoJSONFeature (Map<String, Object> featureMap, String geometryKey, String geometryType) {
        Map<String, Object> properties = new HashMap<String, Object>();
        /* 기하학 좌표 */
        Geometry geom = null;
        /* 좌표 오브젝트 */
        Object geometry = featureMap.get(geometryKey);
        featureMap.remove(geometryKey);
        properties.putAll(featureMap);
        try {
            switch (geometryType.toLowerCase()) {
                case "wkb" :
                    geom = new WKBReader().read((byte[]) geometry);
                    break;
                case "geojson" :
                    geom = new GeoJSONReader().read(geometry.toString());
                    break;
                case "wkt" :
                default :
                    geom = new WKTReader().read(geometry.toString());
                    break;
            }
        } catch (ParseException e) {
            throw new RuntimeException("Invalid Geometry String... " + geometry.toString(), e);
        }
        GeoJSONWriter writer = new GeoJSONWriter();
        Feature feature = new Feature(writer.write(geom), properties);
        return feature;
    }

    /* features to GeoJson */
    public static JSONObject featuresToGeoJSON(List<Feature> features) {
        GeoJSONWriter writer = new GeoJSONWriter();
        FeatureCollection collection = writer.write(features);
        JSONParser parser = new JSONParser();

        try {
            return (JSONObject) parser.parse(collection.toString());
        } catch (org.json.simple.parser.ParseException e) {
            throw new RuntimeException("Invalid GeoJson String... " + collection.toString(), e);
        }
    }

    /* Proj 이용한 x, y단순 변환 */
    public static ProjCoordinate transformPointByXy (double x, double y, CrsyType fromEpsg, CrsyType toEpsg) {
        CRSFactory crsFactory = new CRSFactory();

        CoordinateReferenceSystem sourceCrs = crsFactory.createFromName(fromEpsg.getEpsgName());
        CoordinateReferenceSystem targetCrs = crsFactory.createFromName(toEpsg.getEpsgName());

        BasicCoordinateTransform transform = new BasicCoordinateTransform(sourceCrs, targetCrs);

        return transform.transform(new ProjCoordinate( x, y), new ProjCoordinate());
    }

    public static Geometry readGeoJsonGeometryToGeometry(JSONObject geometry) {
        GeoJSONReader reader = new GeoJSONReader();
        return reader.read(geometry.toString());
    }

    public static String readGeoJsonGeometryToWkt(JSONObject geometry) {
        return convertGeometryToWkt(readGeoJsonGeometryToGeometry(geometry));
    }
}