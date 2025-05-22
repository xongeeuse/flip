package com.ssafy.flip.domain.log.repository.error;

import com.ssafy.flip.domain.log.entity.ErrorLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ErrorLogRepositoryImpl implements ErrorLogRepository {

    private ErrorLogJpaRepository errorLogJpaRepository;

    @Override
    public ErrorLog save(ErrorLog errorLog) {
        return errorLogJpaRepository.save(errorLog);
    }

    @Override
    public Optional<ErrorLog> findById(Long id) {
        return errorLogJpaRepository.findById(id);
    }
}
