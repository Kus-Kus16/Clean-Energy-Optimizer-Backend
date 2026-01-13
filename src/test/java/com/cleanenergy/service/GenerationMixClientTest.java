package com.cleanenergy.service;

import com.cleanenergy.model.generationmixapi.GenerationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(GenerationMixClient.class)
public class GenerationMixClientTest {
    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private GenerationMixClient generationMixClient;


    @Test
    void testCallsApi() {
        // given
        server.expect(requestTo(startsWith("https://api.carbonintensity.org.uk/generation")))
                .andRespond(withSuccess("""
                        {
                            "data":[
                            {
                              "from": "2018-01-20T12:00Z",
                              "to": "2018-01-20T12:30Z",
                              "generationmix": [
                                {
                                  "fuel": "gas",
                                  "perc": 43.6
                                },
                                {
                                  "fuel": "coal",
                                  "perc": 0.7
                                },
                                {
                                  "fuel": "biomass",
                                  "perc": 4.2
                                },
                                {
                                  "fuel": "nuclear",
                                  "perc": 17.6
                                },
                                {
                                  "fuel": "hydro",
                                  "perc": 2.2
                                }
                              ]
                            }]
                          }
                        """, MediaType.APPLICATION_JSON));

        // when
        GenerationResponse response = generationMixClient.getGenerationMix(
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1)
        );

        //then
        assertEquals(1, response.data().size());
        assertEquals(5, response.data().getFirst().generationmix().size());
    }

    @Test
    void testValidFromDate() throws Exception {
        Method toValidFromDate = GenerationMixClient.class.getDeclaredMethod("toValidFromDate", LocalDateTime.class);
        toValidFromDate.setAccessible(true);

        LocalDateTime time1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime expected = time1.plusMinutes(1);
        assertEquals(expected, toValidFromDate.invoke(generationMixClient, time1));

        LocalDateTime time2 = LocalDateTime.of(2025, 1, 1, 10, 5);
        LocalDateTime expected2 = time2.withMinute(30).plusMinutes(1);
        assertEquals(expected2, toValidFromDate.invoke(generationMixClient, time2));

        LocalDateTime time3 = LocalDateTime.of(2025, 1, 1, 10, 30);
        LocalDateTime expected3 = time3.withMinute(30).plusMinutes(1);
        assertEquals(expected3, toValidFromDate.invoke(generationMixClient, time3));

        LocalDateTime time4 = LocalDateTime.of(2025, 1, 1, 10, 35);
        LocalDateTime expected4 = time4.withHour(11).withMinute(0).plusMinutes(1);
        assertEquals(expected4, toValidFromDate.invoke(generationMixClient, time4));
    }

    @Test
    void testRoundToIntervalTime() throws Exception {
        Method roundToIntervalTime = GenerationMixClient.class.getDeclaredMethod("roundToIntervalTime", LocalDateTime.class);
        roundToIntervalTime.setAccessible(true);

        LocalDateTime time1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        assertEquals(time1, roundToIntervalTime.invoke(generationMixClient, time1));

        LocalDateTime time2 = LocalDateTime.of(2025, 1, 1, 10, 30);
        assertEquals(time2, roundToIntervalTime.invoke(generationMixClient, time2));

        LocalDateTime time3 = LocalDateTime.of(2025, 1, 1, 10, 15);
        LocalDateTime expected3 = time3.withMinute(30);
        assertEquals(expected3, roundToIntervalTime.invoke(generationMixClient, time3));

        LocalDateTime time4 = LocalDateTime.of(2025, 1, 1, 10, 45);
        LocalDateTime expected4 = time4.withHour(11).withMinute(0);
        assertEquals(expected4, roundToIntervalTime.invoke(generationMixClient, time4));

        LocalDateTime time5 = LocalDateTime.of(2025, 1, 1, 10, 29);
        LocalDateTime expected5 = time5.withMinute(30);
        assertEquals(expected5, roundToIntervalTime.invoke(generationMixClient, time5));

        LocalDateTime time6 = LocalDateTime.of(2025, 1, 1, 10, 31);
        LocalDateTime expected6 = time6.withHour(11).withMinute(0);
        assertEquals(expected6, roundToIntervalTime.invoke(generationMixClient, time6));
    }


}
