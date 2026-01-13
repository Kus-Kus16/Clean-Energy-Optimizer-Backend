package com.cleanenergy.model.generationmixapi;

import java.util.List;

public record GenerationResponse(
        List<GenerationData> data
) {
}
