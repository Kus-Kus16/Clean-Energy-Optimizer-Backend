package com.cleancharging.controller;

import com.cleancharging.model.DayEnergyMix;
import com.cleancharging.model.OptimalChargingWindow;
import com.cleancharging.service.EnergyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("energy-mix")
public class EnergyController {
    private final EnergyService energyService;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    @GetMapping
    public List<DayEnergyMix> getFutureEnergyMix() {
        return energyService.get3DayEnergyMix();
    }

    @GetMapping("/optimal-window")
    public OptimalChargingWindow getOptimalChargingWindow(@RequestParam int chargingHours) {
        if (chargingHours < 0 || chargingHours > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Charging hours must be between 1 and 6");
        }

        return energyService.getOptimalChargingWindow(chargingHours);
    }
}
