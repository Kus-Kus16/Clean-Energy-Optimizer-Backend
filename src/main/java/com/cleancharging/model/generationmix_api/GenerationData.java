package com.cleancharging.model.generationmix_api;

import java.time.LocalDateTime;
import java.util.List;

public record GenerationData(
        LocalDateTime from,
        LocalDateTime to,
        List<GenerationMixEntry> generationmix
) {
}
