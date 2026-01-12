package com.cleancharging;

import com.cleancharging.model.generationmix_api.GenerationResponse;
import com.cleancharging.service.EnergyService;
import com.cleancharging.service.GenerationMixClient;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnergyConfig {
    private final GenerationMixClient generationMixClient;
    private final EnergyService energyService;

    public EnergyConfig(GenerationMixClient generationMixClient, EnergyService energyService) {
        this.generationMixClient = generationMixClient;
        this.energyService = energyService;
    }

    @PostConstruct
    public void init(){

    }
}
