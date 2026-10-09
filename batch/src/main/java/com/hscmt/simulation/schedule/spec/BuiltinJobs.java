package com.hscmt.simulation.schedule.spec;

import com.hscmt.common.enumeration.JobTargetType;
import com.hscmt.simulation.collect.job.TagAutoCollectJobRunner;
import com.hscmt.simulation.partition.job.PartitionTableCheckRunner;
import com.hscmt.simulation.tag.job.TagInfoCleanUpRunner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;

import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum BuiltinJobs implements JobSpec{
    /* 1분태그 수집 */
    AUTO_TAG_COLLECT(
            "autoTagCollect"
            , JobTargetType.STATIC
            , TagAutoCollectJobRunner.class
            , "50 * * * * ?"
            , MisfirePolicy.DO_NOTHING
            , Map.of (
                    "timeGapMinutes", 3,
                    "jobExecutor", "AUTO_TAG_COLLECT"
            )
    ),
    /* 월말파티션 생성 */
    MONTHLY_PARTITION(
            "monthlyPartition"
            , JobTargetType.STATIC
            , PartitionTableCheckRunner.class
            , "30 50 23 L * ?"
            , MisfirePolicy.FIRE_AND_PROCEED
            , Map.of("jobExecutor", "PARTITION_TABS_CHK")
    ),
    /* 태그데이터 정리 */
    TAG_INFO_CLEANUP (
            "tagInfoCleanUp"
            , JobTargetType.STATIC
            , TagInfoCleanUpRunner.class
            , "40 5 0/2 * * ?"
            , MisfirePolicy.DO_NOTHING
            , Map.of("jobExecutor", "TAG_INFO_CLEANUP")
    )
    ;

    private final String key;
    private final JobTargetType group;
    private final Class<? extends Job> jobClass;
    private final String cron;
    private final MisfirePolicy misfire;
    private final Map<String, Object> jobData;
}
