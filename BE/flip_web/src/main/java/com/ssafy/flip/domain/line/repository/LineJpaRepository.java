package com.ssafy.flip.domain.line.repository;

import com.ssafy.flip.domain.line.entity.Line;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LineJpaRepository extends JpaRepository<Line, Long> {

    List<Line> findAll();

    Optional<Line> findById(Long id);

}
