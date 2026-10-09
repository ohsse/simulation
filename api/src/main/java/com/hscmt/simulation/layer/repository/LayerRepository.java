package com.hscmt.simulation.layer.repository;

import com.hscmt.simulation.layer.domain.Layer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LayerRepository extends JpaRepository<Layer, String>, LayerCustomRepository {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
  INSERT INTO layer_m
    (layer_id, layer_nm, layer_desc, init_dspy_yn, crsy_type_cd, use_able_yn, rgst_id, rgst_dttm, mdf_id, mdf_dttm)
  VALUES
    (:layerId, :layerNm, :layerDesc, 'N', 'EPSG5186', 'N', :rgstId, :rgstDttm, :mdfId, :mdfDttm)
  ON CONFLICT (layer_id) DO UPDATE SET
    layer_id        = EXCLUDED.layer_id,
    layer_nm        = EXCLUDED.layer_nm,
    layer_desc      = EXCLUDED.layer_desc,
    init_dspy_yn    = EXCLUDED.init_dspy_yn,
    crsy_type_cd    = EXCLUDED.crsy_type_cd,
    use_able_yn    = EXCLUDED.use_able_yn,
    rgst_id       = EXCLUDED.rgst_id,
    rgst_dttm     = EXCLUDED.rgst_dttm,
    mdf_id        = EXCLUDED.mdf_id,
    mdf_dttm      = EXCLUDED.mdf_dttm       
  """, nativeQuery = true)
    int upsertLayerByResult(
            @Param("layerId") String layerId,
            @Param("layerNm") String layerNm,
            @Param("layerDesc") String layerDesc,
            @Param("rgstId") String rgstId,
            @Param("rgstDttm") LocalDateTime rgstDttm,
            @Param("mdfId") String mdfId,
            @Param("mdfDttm") LocalDateTime mdfDttm
    );
}
