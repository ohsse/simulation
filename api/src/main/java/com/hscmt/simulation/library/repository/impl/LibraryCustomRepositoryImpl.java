package com.hscmt.simulation.library.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.library.domain.QLibrary;
import com.hscmt.simulation.library.dto.LibraryDto;
import com.hscmt.simulation.library.repository.LibraryCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LibraryCustomRepositoryImpl implements LibraryCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;


    @Override
    public void updateModifier(String lbrId, String userId) {
        QLibrary qLibrary = QLibrary.library;
        queryFactory.update(qLibrary)
                .set(qLibrary.mdfId, userId)
                .set(qLibrary.mdfDttm, LocalDateTime.now())
                .where(qLibrary.lbrId.eq(lbrId))
                .execute();
    }

    @Override
    public List<LibraryDto> findAllLibraries() {
        QLibrary qLibrary = QLibrary.library;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(LibraryDto.class, LibraryDto.projectionFields(qLibrary))
                )
                .from(qLibrary)
                .fetch();
    }
}
