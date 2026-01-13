package com.cleanenergy.service;

import com.cleanenergy.model.DayEnergyMix;
import com.cleanenergy.model.OptimalChargingWindow;
import com.cleanenergy.model.generationmixapi.GenerationData;
import com.cleanenergy.model.generationmixapi.GenerationMixEntry;
import com.cleanenergy.model.generationmixapi.GenerationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class EnergyServiceTest {
    @Mock
    private EnergyClient energyClient;

    @InjectMocks
    private EnergyService energyService;

    private GenerationData createGenerationData(LocalDateTime from, LocalDateTime to, Map<String, Double> generationMix ) {
        List<GenerationMixEntry> generationMixEntries = generationMix
                .entrySet().stream()
                .map(e -> new GenerationMixEntry(
                        e.getKey(),
                        e.getValue()
                ))
                .toList();

        return new GenerationData(from, to, generationMixEntries);
    }

    @Test
    void testGetOptimalChargingWindow() {
        // given
        int chargingHours = 2;
        LocalDateTime from = LocalDateTime.now();

        List<GenerationData> generationDataList = new ArrayList<>();

        // 0, 0, 0 0
        for (int i = 0; i < 4; i++) {
            Map<String, Double> generationMix = Map.of(
                    "wind", 0.0,
                    "solar", 0.0,
                    "coal", 100.0
            );

            generationDataList.add(createGenerationData(
                    from.plusMinutes(i * 30),
                    from.plusMinutes((i + 1) * 30),
                    generationMix
            ));
        }

        // 30, 40, 50, 60
        for (int i = 4; i < 8; i++) {
            double cleanPerc = 30 + (i - 4) * 10;
            Map<String, Double> generationMix = Map.of(
                    "wind", cleanPerc / 2,
                    "solar", cleanPerc / 2,
                    "coal", 100 - cleanPerc
            );

            generationDataList.add(createGenerationData(
                    from.plusMinutes(i * 30),
                    from.plusMinutes((i + 1) * 30),
                    generationMix
            ));
        }

        // 70, 50, 30, 10
        for (int i = 8; i < 12; i++) {
            double cleanPerc = 70 - (i - 8) * 20;
            Map<String, Double> generationMix = Map.of(
                    "wind", cleanPerc / 2,
                    "solar", cleanPerc / 2,
                    "coal", 100 - cleanPerc
            );
            generationDataList.add(createGenerationData(
                    from.plusMinutes(i * 30),
                    from.plusMinutes((i + 1) * 30),
                    generationMix
            ));
        }

        Mockito.when(energyClient.getGenerationMix(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new GenerationResponse(generationDataList));

        // when
        OptimalChargingWindow result = energyService.getOptimalChargingWindow(chargingHours);

        // then
        assertNotNull(result);
        assertEquals(57.5, result.cleanEnergyPercentage(), 0.01);
        assertEquals(generationDataList.get(6).from(), result.startDate());
        assertEquals(generationDataList.get(9).to(), result.endDate());
    }

    @Test
    void testGetOptimalChargingWindow_ThrowsException() {
        // given
        int chargingHours = 2;
        List<GenerationData> generationDataList = new ArrayList<>();

        // 0, 0, 0
        for (int i = 0; i < 3; i++) {
            Map<String, Double> generationMix = Map.of(
                    "wind", 0.0,
                    "solar", 0.0,
                    "coal", 100.0
            );

            generationDataList.add(createGenerationData(
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    generationMix
            ));
        }

        Mockito.when(energyClient.getGenerationMix(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new GenerationResponse(generationDataList));

        // when, then
        assertThrows(IllegalStateException.class, () -> energyService.getOptimalChargingWindow(chargingHours));
    }

    @Test
    void testCleanEnergyPercentage() throws Exception {
        // given
        Method getCleanEnergyPercentage = EnergyService.class.getDeclaredMethod("getCleanEnergyPercentage", GenerationData.class);
        getCleanEnergyPercentage.setAccessible(true);

        Map<String, Double> generationMix = Map.of(
                "wind", 10.0,
                "solar", 15.0,
                "coal", 75.0
        );
        GenerationData generationData = createGenerationData(
                LocalDateTime.now(),
                LocalDateTime.now(),
                generationMix
        );

        //when, then
        assertEquals(25.0, (Double) getCleanEnergyPercentage.invoke(energyService, generationData), 0.001);
    }

    @Test
    void test3DayEnergyMix() {
        // given
        LocalDateTime from = LocalDateTime.now().withHour(0).withMinute(0);

        List<GenerationData> generationDataList = new ArrayList<>();
        for (int day = 0; day < 3; day++) {
            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalDateTime intervalStart = from.plusDays(day).plusHours(hour).plusMinutes(minute);
                    LocalDateTime intervalEnd = intervalStart.plusMinutes(30);

                    Map<String, Double> generationMix = Map.of(
                            "wind", 30.0,
                            "solar", 20.0,
                            "coal", 50.0
                    );

                    generationDataList.add(createGenerationData(intervalStart, intervalEnd, generationMix));
                }
            }
        }

        Mockito.when(energyClient.getGenerationMix(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new GenerationResponse(generationDataList));

        // when
        List<DayEnergyMix> result = energyService.get3DayEnergyMix();

        // then
        assertNotNull(result);
        assertEquals(3, result.size());

        for (int i = 0; i < result.size(); i++) {
            DayEnergyMix dayEnergyMix = result.get(i);

            assertEquals(from.plusDays(i).toLocalDate(), dayEnergyMix.date());
            assertEquals(50.0, dayEnergyMix.cleanEnergyPercentage(), 0.001);
        }
    }

    @Test
    void testGetDayEnergyMix() throws Exception {
        // given
        Method getDayEnergyMix = EnergyService.class.getDeclaredMethod("getDayEnergyMix", List.class);
        getDayEnergyMix.setAccessible(true);

        LocalDateTime today = LocalDateTime.now();

        Map<String, Double> m1 = Map.of(
                "wind", 10.0,
                "solar", 15.0,
                "coal", 75.0
        );
        Map<String, Double> m2 = Map.of(
                "wind", 25.0,
                "solar", 70.0,
                "coal", 5.0
        );

        List<GenerationData> generationDataList = Stream.of(m1, m2)
                .map(map -> createGenerationData(
                        today,
                        today,
                        map
                ))
                .toList();

        // when
        DayEnergyMix dayEnergyMix = (DayEnergyMix) getDayEnergyMix.invoke(energyService, generationDataList);

        //then
        assertEquals(today.toLocalDate(), dayEnergyMix.date());
        assertEquals(60.0, dayEnergyMix.cleanEnergyPercentage(), 0.001);
    }

    @Test
    void testGetDayEnergyMix_ThrowsEmptyException() throws Exception {
        Method getDayEnergyMix = EnergyService.class.getDeclaredMethod("getDayEnergyMix", List.class);
        getDayEnergyMix.setAccessible(true);

        InvocationTargetException e =  assertThrows(InvocationTargetException.class, () ->
                    getDayEnergyMix.invoke(energyService, List.of())
            );

        assertInstanceOf(IllegalArgumentException.class, e.getCause());
        assertEquals("dayData is empty", e.getCause().getMessage());
    }

    @Test
    void testGetDayEnergyMix_ThrowsDifferentDaysException() throws Exception {
        // given
        Method getDayEnergyMix = EnergyService.class.getDeclaredMethod("getDayEnergyMix", List.class);
        getDayEnergyMix.setAccessible(true);

        LocalDateTime today = LocalDateTime.now();
        Map<String, Double> m1 = Map.of(
                "wind", 10.0,
                "solar", 15.0,
                "coal", 75.0
        );

        List<GenerationData> generationDataList = List.of(
                createGenerationData(today, today, m1),
                createGenerationData(today.plusDays(1), today.plusDays(1), m1)
        );

        // when then
        InvocationTargetException e = assertThrows(InvocationTargetException.class, () ->
                getDayEnergyMix.invoke(energyService, generationDataList)
        );

        assertInstanceOf(IllegalArgumentException.class, e.getCause());
        assertEquals("dayData contains different days", e.getCause().getMessage());
    }

    @Test
    void testDaysFromTodayTo() throws Exception {
        // given
        Method method = EnergyService.class.getDeclaredMethod("daysFromTodayTo", LocalDate.class);
        method.setAccessible(true);

        EnergyService service = new EnergyService(null);

        LocalDate now = LocalDate.now();

        // when
        long today = (long) method.invoke(service, now);
        long tomorrow = (long) method.invoke(service, now.plusDays(1));
        long yesterday = (long) method.invoke(service, now.minusDays(1));
        long threeDays = (long) method.invoke(service, now.plusDays(3));

        // then
        assertEquals(0, today);
        assertEquals(1, tomorrow);
        assertEquals(-1, yesterday);
        assertEquals(3, threeDays);
    }

}
