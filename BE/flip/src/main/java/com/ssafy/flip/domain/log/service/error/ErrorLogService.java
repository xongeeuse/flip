package com.ssafy.flip.domain.log.service.error;

import com.ssafy.flip.domain.log.dto.error.request.*;
import com.ssafy.flip.domain.log.entity.ErrorLog;

import java.util.Optional;

public interface ErrorLogService {

    ErrorLog save(ErrorLog errorLog);

    Optional<ErrorLog> findById(Long id);

    ErrorLog addErrorLog(AddErrorRequestDTO requestDTO);

}
