package com.hscmt.simulation.user.repository;

import com.hscmt.simulation.user.dto.UserDto;

import java.util.List;

public interface UserCustomRepository {
    List<UserDto> findAllUsers();
    UserDto findUserById(String userId);
}
