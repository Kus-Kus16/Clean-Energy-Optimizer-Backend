package com.cleancharging.model.generationmix_api;

import java.util.List;

public record GenerationResponse(
        List<GenerationData> data
) {
}
