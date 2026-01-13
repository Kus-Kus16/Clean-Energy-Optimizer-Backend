package com.cleanenergy.model.generationmixapi;

import java.time.LocalDateTime;
import java.util.List;

public record GenerationData(
        LocalDateTime from,
        LocalDateTime to,
        List<GenerationMixEntry> generationmix
) {
}
