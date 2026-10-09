package com.hscmt.simulation.user.repository;

import com.hscmt.simulation.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String>, UserCustomRepository{
}
