package com.ssafy.flip.domain.line.service;

import com.ssafy.flip.domain.line.entity.Line;
import com.ssafy.flip.domain.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LineServiceImpl implements LineService {

    private final LineRepository lineRepository;

    @Override
    public List<Line> findAll() {
        return lineRepository.findAll();
    }

    @Override
    public Optional<Line> findById(Long id) {
        return lineRepository.findById(id);
    }

    @Override
    public Line getLine(Long id) {
        return lineRepository.findById(id)
                .orElseThrow();
    }

}
