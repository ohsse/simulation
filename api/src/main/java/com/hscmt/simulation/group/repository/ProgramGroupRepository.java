package com.hscmt.simulation.group.repository;

import com.hscmt.simulation.group.domain.LayerGroup;
import com.hscmt.simulation.group.domain.ProgramGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgramGroupRepository extends GroupBaseRepository<ProgramGroup>, ProgramGroupCustomRepository {
    @Query(value = """
    WITH RECURSIVE r AS (
        SELECT 
            g.grp_id,
            g.up_grp_id,
            g.grp_nm,
            g.grp_desc,
            g.sort_ord,
            g.rgst_id,
            g.rgst_dttm,
            g.mdf_id,
            g.mdf_dttm
        FROM pgm_grp_m g
        WHERE g.grp_id IN (:grpIds)

        UNION ALL

        SELECT 
            g.grp_id,
            g.up_grp_id,
            g.grp_nm,
            g.grp_desc,
            g.sort_ord,
            g.rgst_id,
            g.rgst_dttm,
            g.mdf_id,
            g.mdf_dttm
        FROM pgm_grp_m g
        JOIN r ON g.up_grp_id = r.grp_id
    )
    SELECT 
        grp_id,
        up_grp_id,
        grp_nm,
        grp_desc,
        sort_ord,
        rgst_id,
        rgst_dttm,
        mdf_id,
        mdf_dttm
    FROM r
    """, nativeQuery = true)
    List<ProgramGroup> findAllGroupRecursive(@Param("grpIds") List<String> grpIds);
}
