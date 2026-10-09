package com.hscmt.simulation.dashboard.repository;

import com.hscmt.simulation.dashboard.domain.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DashboardRepository extends JpaRepository <Dashboard, String>, DashboardCustomRepository {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE dsbd_m d
        SET dsbd_vis_items = (
          SELECT COALESCE(jsonb_agg(e), '[]'::jsonb)
          FROM jsonb_array_elements(d.dsbd_vis_items) e
          WHERE NOT (
            e->>'structType' = 'DYNAMIC'
            AND (e->>'structValue') = ANY(CAST(:structValues AS text[]))
          )
        )
        WHERE d.dsbd_vis_items IS NOT NULL
          AND EXISTS (
            SELECT 1
            FROM jsonb_array_elements(d.dsbd_vis_items) e
            WHERE e->>'structType' = 'DYNAMIC'
              AND (e->>'structValue') = ANY(CAST(:structValues AS text[]))
          )
        """, nativeQuery = true)
    int bulkRemoveDynamicItemsByVisIds(@Param("structValues") String[] structValues);

}
