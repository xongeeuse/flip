package com.ssafy.flip.domain.status.service;

import com.ssafy.flip.domain.status.dto.response.*;


public interface StatusService {

    AmrRealTimeResponseDTO getAmrRealTimeStatus();

    MissionStatusResponseDTO getRouteStatus(String amrId);

    LineStatusResponseDTO getLineStatus();

    FactoryStatusResponseDTO getFactoryStatus();

    ProductionResponseDTO getProductionStatus();

    HeatMapResponseDTO getHeatMapStatus();

}
