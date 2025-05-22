package com.ssafy.flip.domain.line.service;

import com.ssafy.flip.domain.line.entity.Line;

import java.util.List;
import java.util.Optional;

public interface LineService {

    List<Line> findAll();

    Optional<Line> findById(Long id);

    Line getLine(Long id);

    void disableMissionAssignment(String missionId);

    void markMissionBlockedNow(String missionId);

    void updateMissionAssignment(String missionId);

    void brokeLine(Long id);

    void repairLine(Long id);
}
