package com.ssafy.flip.domain.log.repository.error;

import com.ssafy.flip.domain.log.entity.ErrorLog;

import java.util.Optional;

public interface ErrorLogRepository {

    ErrorLog save(ErrorLog errorLog);

    Optional<ErrorLog> findById(Long id);

}
