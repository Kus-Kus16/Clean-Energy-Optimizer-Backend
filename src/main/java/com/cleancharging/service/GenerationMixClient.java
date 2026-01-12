package com.cleancharging.service;

import com.cleancharging.model.generationmixapi.GenerationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GenerationMixClient {
    private final static String BASE_URL = "https://api.carbonintensity.org.uk/generation";
    private final RestClient restClient = RestClient.create(BASE_URL);
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

    /**
     * @param from the minimum starting date of first GenerationData
     * @param to the minimum ending date for the last GenerationData
     */
    public GenerationResponse getGenerationMix(LocalDateTime from, LocalDateTime to) {
        String fromDate = toValidFromDate(from).format(FORMATTER);
        String toDate = roundToIntervalTime(to).format(FORMATTER);

        return restClient
                .get()
                .uri(String.format("/%s/%s", fromDate, toDate))
                .retrieve()
                .body(GenerationResponse.class);
    }

    /**
     * Avoids including the previous interval in GenerationResponse
     * @param from the minimum starting date of first GenerationData
     * @return the valid date for the api
     */
    private LocalDateTime toValidFromDate(LocalDateTime from) {
        return roundToIntervalTime(from).plusMinutes(1);
    }

    /**
     * Converts given time up to the nearest 30 minute interval time
     */
    private LocalDateTime roundToIntervalTime(LocalDateTime time) {
        int minute = time.getMinute();

        if (minute == 0 || minute == 30) {
            return time;
        } if (minute < 30) {
            return time.withMinute(0).plusMinutes(30);
        } else { // minute > 30
            return time.withMinute(0).plusHours(1);
        }
    }
}
