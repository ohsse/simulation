package com.hscmt.simulation.user.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.user.domain.QUser;
import com.hscmt.simulation.user.dto.UserDto;
import com.hscmt.simulation.user.repository.UserCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserDto> findAllUsers() {
        QUser qUser = QUser.user;
        return queryFactory
                .select(QProjectionUtil.toQBean(UserDto.class, UserDto.projectionFields(qUser)))
                .from(qUser)
                .fetch();
    }

    @Override
    public UserDto findUserById(String userId) {
        QUser qUser = QUser.user;
        return queryFactory
                .select(QProjectionUtil.toQBean(UserDto.class, UserDto.projectionFields(qUser)))
                .from(qUser)
                .where(qUser.userId.eq(userId))
                .fetchOne();
    }
}
