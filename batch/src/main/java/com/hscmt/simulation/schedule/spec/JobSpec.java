package com.hscmt.simulation.schedule.spec;

import com.hscmt.common.enumeration.JobTargetType;
import org.quartz.Job;

import java.util.Map;

public interface JobSpec {
    String getKey();
    JobTargetType getGroup();
    Class<? extends Job> getJobClass();
    String getCron();
    MisfirePolicy getMisfire();
    Map<String, Object> getJobData();

    enum MisfirePolicy {
        DO_NOTHING, FIRE_AND_PROCEED, IGNORE_MISFIRES
    }

}

