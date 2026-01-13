package com.cleanenergy.service;

import com.cleanenergy.model.generationmixapi.GenerationResponse;

import java.time.LocalDateTime;

public interface EnergyClient {
    GenerationResponse getGenerationMix(LocalDateTime from, LocalDateTime to);
}
