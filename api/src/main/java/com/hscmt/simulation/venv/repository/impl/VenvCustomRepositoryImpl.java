package com.hscmt.simulation.venv.repository.impl;

import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.venv.domain.QVirtualEnvironment;
import com.hscmt.simulation.venv.dto.VenvDto;
import com.hscmt.simulation.venv.repository.VenvCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class VenvCustomRepositoryImpl implements VenvCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPQLQueryFactory queryFactory;


    @Override
    public List<VenvDto> findAllVenvs(String pyVrsn) {
        QVirtualEnvironment qVirtualEnvironment = QVirtualEnvironment.virtualEnvironment;

        BooleanBuilder builder = new BooleanBuilder();

        if (pyVrsn != null && !pyVrsn.isEmpty() ) {
            builder.and(qVirtualEnvironment.pyVrsn.eq(pyVrsn));
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(VenvDto.class, VenvDto.projectionFields(qVirtualEnvironment))
                )
                .from(qVirtualEnvironment)
                .where(builder)
                .fetch();
    }

    @Override
    public VenvDto findVenvById(String venvId) {
        QVirtualEnvironment qVirtualEnvironment = QVirtualEnvironment.virtualEnvironment;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(VenvDto.class, VenvDto.projectionFields(qVirtualEnvironment))
                )
                .from(qVirtualEnvironment)
                .where(qVirtualEnvironment.venvId.eq(venvId))
                .fetchOne();
    }

    @Override
    public void createComplete(String venvId) {
        QVirtualEnvironment qVirtualEnvironment = QVirtualEnvironment.virtualEnvironment;
        queryFactory.update(qVirtualEnvironment)
                .set(qVirtualEnvironment.useAbleYn, YesOrNo.Y)
                .where(qVirtualEnvironment.venvId.eq(venvId))
                .execute();
    }
}
