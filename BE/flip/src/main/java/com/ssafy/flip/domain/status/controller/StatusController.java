package com.ssafy.flip.domain.status.controller;

import com.ssafy.flip.domain.status.dto.request.*;
import com.ssafy.flip.domain.status.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/status")
@Slf4j
public class StatusController {

    private final StatusService statusService;

    @PostMapping("/amr/test")
    public ResponseEntity<Object> saveAmr(@RequestBody AmrSaveRequestDTO amrSaveRequestDTO){
        //카프카 들고올
        String missionId = amrSaveRequestDTO.body().missionId();
        //misionId로 알고리즘 서버를 통해서 경로를 들고와야함
//        MissionRequestDto missionRequestDto = statusService.Algorithim(missionId);  // 더미 호출


//        statusService.saveAmr(amrSaveRequestDTO,missionRequestDto, null);
        return ResponseEntity.ok(null);
        //return ResponseEntity.ok(new AmrMissionResponseDTO("AMR001", 0, 1, "MOVING", 2, "ERROR01", LocalDateTime.now(), "TYPE001", (float) 3.14, (float) 3.14, (float) 71.5, (float) 65.5, 620));
    }

    @GetMapping("/line/broken")
    public ResponseEntity<String> brokeLine(){
        statusService.brokeLine();
        return ResponseEntity.ok(null);
    }

    @GetMapping("/line/repair")
    public ResponseEntity<String> repairLine(){
        statusService.repairLine();
        return ResponseEntity.ok(null);
    }
}
