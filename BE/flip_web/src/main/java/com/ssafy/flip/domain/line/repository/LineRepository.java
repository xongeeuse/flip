package com.ssafy.flip.domain.line.repository;

import com.ssafy.flip.domain.line.entity.Line;

import java.util.List;
import java.util.Optional;

public interface LineRepository {

    List<Line> findAll();

    Optional<Line> findById(Long id);

}
