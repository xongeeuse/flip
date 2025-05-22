package com.ssafy.flip.domain.amr.repository;

import com.ssafy.flip.domain.amr.entity.AMR;

import java.util.List;
import java.util.Optional;

public interface AmrRepository {

    List<AMR> findAll();

    Optional<AMR> findById(String id);

}
