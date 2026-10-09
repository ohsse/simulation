package com.hscmt.simulation.program.repository;

import com.hscmt.simulation.program.domain.ProgramVisualization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ProgramVisualizationRepository extends JpaRepository<ProgramVisualization, String>,ProgramVisualizationCustomRepository {
    @Modifying
    @Query(value = """
  INSERT INTO pgm_vis_m
    (vis_id, pgm_id, vis_nm, vis_type_cd, vis_setup_text, rgst_id, rgst_dttm, mdf_id, mdf_dttm)
  VALUES
    (:visId, :pgmId, :visNm, :visTypeCd, CAST(:visSetupText AS jsonb), :rgstId, :rgstDttm, :rgstId, :mdfDttm)
  ON CONFLICT (vis_id) DO UPDATE SET
    pgm_id        = EXCLUDED.pgm_id,
    vis_nm        = EXCLUDED.vis_nm,
    vis_type_cd   = EXCLUDED.vis_type_cd,
    vis_setup_text= EXCLUDED.vis_setup_text,
    rgst_id       = EXCLUDED.rgst_id,
    rgst_dttm     = EXCLUDED.rgst_dttm,
    mdf_id        = EXCLUDED.mdf_id,
    mdf_dttm      = EXCLUDED.mdf_dttm       
  """, nativeQuery = true)
    int upsertVisByResult(@Param("visId") String visId,
                   @Param("pgmId") String pgmId,
                   @Param("visNm") String visNm,
                   @Param("visTypeCd") String visTypeCd,
                   @Param("visSetupText") String visSetupTextJsonb,
                   @Param("rgstId") String rgstId,
                   @Param("rgstDttm") LocalDateTime rgstDttm,
                   @Param("mdfId") String mdfId,
                   @Param("mdfDttm") LocalDateTime mdfDttm
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE pgm_vis_m t
            SET vis_setup_text = jsonb_set(
              t.vis_setup_text,
              '{layerIds}',
              (
                SELECT COALESCE(jsonb_agg(x), '[]'::jsonb)
                FROM jsonb_array_elements_text(t.vis_setup_text->'layerIds') AS x
                WHERE x <> :layerId
              ),
              true
            )
            WHERE jsonb_exists(t.vis_setup_text, 'layerIds')
              AND EXISTS (
                SELECT 1
                FROM jsonb_array_elements_text(t.vis_setup_text->'layerIds') AS x
                WHERE x = :layerId
          )
    """, nativeQuery = true)
    int bulkUpdateLayerIdsByResultId(@Param("layerId") String layerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
      delete from pgm_vis_m pv
      where jsonb_path_exists(
        pv.vis_setup_text,
        cast(:jsonPath as jsonpath),
        jsonb_build_object('target', :fileName),
        true
      )
      """, nativeQuery = true)
    int deleteAllByFileName(@Param("jsonPath") String jsonPath,@Param("fileName") String fileName);

}
