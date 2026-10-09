package com.hscmt.common.util;

import org.springframework.batch.item.ExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PartitionerUtil {

    /* 실제 그리드 사이즈 조정 */
    public static int getActualGridSize (int gridSize, int targetSize) {
        return Math.min(gridSize, targetSize);
    }

    /* 파티션 사이즈 조정 */
    public static int getPartitionSize (int actualGridSize, int targetSize) {
        return (int) Math.ceil((double) targetSize / actualGridSize);
    }

    /* 파티션 나누기 */
    public static void splitPartition (Map<String, ExecutionContext> partition, int gridSize, int partitionSize, List<? extends Object> targetList){
        for (int i = 0; i < gridSize; i ++) {
            int fromIndex = i * partitionSize;
            int toIndex = Math.min(fromIndex + partitionSize, targetList.size());
            List<? extends Object> partitionItem = new ArrayList<>(targetList.subList(fromIndex, toIndex));
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.put("targetList", partitionItem);
            partition.put("partition" + i, executionContext);
        }
    }
}
