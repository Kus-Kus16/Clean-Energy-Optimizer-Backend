package com.cleanenergy.controller;

import com.cleanenergy.model.DayEnergyMix;
import com.cleanenergy.model.OptimalChargingWindow;
import com.cleanenergy.service.EnergyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EnergyController.class)
public class EnergyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnergyService energyService;

    @Test
    void testGetFututeEnergyMix() throws Exception {
        // given
        DayEnergyMix dayEnergyMix = new DayEnergyMix(
                LocalDate.now(),
                Set.of(),
                21.0
        );

        Mockito.when(energyService.get3DayEnergyMix())
                .thenReturn(List.of(dayEnergyMix, dayEnergyMix));

        // when then
        mockMvc.perform(get("/energy-mix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cleanEnergyPercentage").value(21));
    }

    @Test
    void testGetOptimalChangingWindow() throws Exception {
        // given
        OptimalChargingWindow optimalChargingWindow = new OptimalChargingWindow(
                LocalDateTime.now(),
                LocalDateTime.now(),
                21
        );

        Mockito.when(energyService.getOptimalChargingWindow(3))
                .thenReturn(optimalChargingWindow);

        // when then
        mockMvc.perform(get("/energy-mix/optimal-window")
                .param("chargingHours", String.valueOf(3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cleanEnergyPercentage").value(21));
    }

    @Test
    void testThrowsBadRequest() throws Exception {
        mockMvc.perform(get("/energy-mix/optimal-window")
                .param("chargingHours", String.valueOf(0)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/energy-mix/optimal-window")
                .param("chargingHours", String.valueOf(7)))
                .andExpect(status().isBadRequest());
    }
}
