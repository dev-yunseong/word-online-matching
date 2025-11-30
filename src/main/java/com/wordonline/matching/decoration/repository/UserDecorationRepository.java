package com.wordonline.matching.decoration.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.wordonline.matching.decoration.entity.UserDecoration;

public interface UserDecorationRepository extends R2dbcRepository<UserDecoration, Long> {

}
