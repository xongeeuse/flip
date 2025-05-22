package com.ssafy.flip.domain.log.service.error;

import com.ssafy.flip.domain.amr.service.AmrService;
import com.ssafy.flip.domain.log.dto.error.request.AddErrorRequestDTO;
import com.ssafy.flip.domain.log.entity.ErrorLog;
import com.ssafy.flip.domain.log.repository.error.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ErrorLogServiceImpl implements ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    private final AmrService amrService;

        @Override
        public ErrorLog save(ErrorLog errorLog) {
            return errorLogRepository.save(errorLog);
        }

        @Override
        public Optional<ErrorLog> findById(Long id) {
            return errorLogRepository.findById(id);
        }

        @Override
        public ErrorLog addErrorLog(AddErrorRequestDTO requestDTO) {
            ErrorLog errorLog = ErrorLog.builder()
                    .amr(amrService.getById(requestDTO.amrId()))
                    .errorCode(requestDTO.errorCode())
                    .x(requestDTO.x())
                    .y(requestDTO.y())
                    .build();

        return errorLogRepository.save(errorLog);
    }
}
