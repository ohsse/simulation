package com.hscmt.common.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceUtil {
    public static Resource[] resolveMapperLocation(String ...mapperLocationPaths) throws Exception{
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<String> mapperLocations = new ArrayList<String>();
        mapperLocations.addAll(Arrays.asList(mapperLocationPaths));
        List<Resource> resources = new ArrayList<Resource>();
        for (String mapperLocation : mapperLocations) {
            Resource[] mappers = resourceResolver.getResources(mapperLocation);
            resources.addAll(Arrays.asList(mappers));
        }
        Resource [] result = new Resource[resources.size()];
        return resources.toArray(result);
    }
}
