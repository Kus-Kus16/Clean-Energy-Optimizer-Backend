package com.cleanenergy.model;

import java.time.LocalDateTime;

public record OptimalChargingWindow (
    LocalDateTime startDate,
    LocalDateTime endDate,
    double cleanEnergyPercentage
){
}
