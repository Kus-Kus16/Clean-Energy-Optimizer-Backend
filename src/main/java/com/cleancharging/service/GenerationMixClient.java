package com.cleancharging.service;

import com.cleancharging.model.generationmix_api.GenerationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GenerationMixClient {
    private final static String BASE_URL = "https://api.carbonintensity.org.uk/generation";
    private final RestClient restClient = RestClient.create(BASE_URL);

    public GenerationResponse getGenerationMix(String from, String to) {
        return restClient
                .get()
                .uri(String.format("/%s/%s", from, to))
                .retrieve()
                .body(GenerationResponse.class);
    }
}
