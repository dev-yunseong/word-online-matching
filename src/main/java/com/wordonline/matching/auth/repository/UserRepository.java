package com.wordonline.matching.auth.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.wordonline.matching.auth.domain.User;

public interface UserRepository extends R2dbcRepository<User, Long> {

}
