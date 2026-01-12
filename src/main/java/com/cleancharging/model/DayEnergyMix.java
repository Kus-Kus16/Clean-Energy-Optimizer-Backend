package com.cleancharging.model;


import java.time.LocalDate;
import java.util.Set;

public record DayEnergyMix (
    LocalDate date,
    Set<EnergySourcePercentage> energySources,
    double cleanEnergyPercentage
){
}
