package com.ssafy.flip.domain.log.repository.error;

import com.ssafy.flip.domain.log.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ErrorLogJpaRepository extends JpaRepository<ErrorLog, Long> {

    Optional<ErrorLog> findById(Long id);

}
