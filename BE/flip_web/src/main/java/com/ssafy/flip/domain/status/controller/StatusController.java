package com.ssafy.flip.domain.status.controller;

import com.ssafy.flip.domain.status.dto.request.*;
import com.ssafy.flip.domain.status.dto.response.*;
import com.ssafy.flip.domain.status.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/status")
@Tag(name = "Control Controller", description = "관제 관련 API")
@Slf4j
public class StatusController {

    private final StatusService statusService;

    @PostMapping("/amr/save")
    public ResponseEntity<Object> saveAmr(@RequestBody AmrSaveRequestDTO requestDTO){
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "amr mission status", description = "AMR이 Mission을 받았을 때의 최초 상태")
    @GetMapping("/amr/mission")
    public ResponseEntity<List<AmrMissionResponseDTO>> getAmrMissionStatus(@ModelAttribute AmrMissionRequestDTO requestDTO){
        return null;
    }

    @Operation(summary = "amr real time status", description = "AMR 실시간 상태")
    @GetMapping("/amr/realtime")
    public ResponseEntity<AmrRealTimeResponseDTO> getAmrRealTimeStatus(){
        return ResponseEntity.ok(statusService.getAmrRealTimeStatus());
    }

    @Operation(summary = "route status", description = "미션 세부 루트 확인")
    @GetMapping("amr/route/{amrId}")
    public ResponseEntity<MissionStatusResponseDTO> getRouteStatus(@PathVariable("amrId") String amrId){
        return ResponseEntity.ok(statusService.getRouteStatus(amrId));
    }

    @Operation(summary = "line status", description = "라인 상태")
    @GetMapping("/line")
    public ResponseEntity<LineStatusResponseDTO> getLineStatus(){
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "factory status", description = "공장 현재 상태 확인")
    @GetMapping("/factory")
    public ResponseEntity<FactoryStatusResponseDTO> getFactoryStatus(){
        return ResponseEntity.ok(statusService.getFactoryStatus());
    }

    @Operation(summary = "output status", description = "생산량 정보 확인")
    @GetMapping("/production")
    public ResponseEntity<ProductionResponseDTO> getProductionStatus(){
        return ResponseEntity.ok(statusService.getProductionStatus());
    }

    @Operation(summary = "heatmap status", description = "히트맵 정보 확인")
    @GetMapping("/heatmap")
    public ResponseEntity<HeatMapResponseDTO> getHeatMapStatus(){
        return ResponseEntity.ok(statusService.getHeatMapStatus());
    }

}
