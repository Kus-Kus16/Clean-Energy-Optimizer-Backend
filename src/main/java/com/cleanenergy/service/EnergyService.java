package com.cleanenergy.service;

import com.cleanenergy.model.DayEnergyMix;
import com.cleanenergy.model.EnergySource;
import com.cleanenergy.model.EnergySourcePercentage;
import com.cleanenergy.model.OptimalChargingWindow;
import com.cleanenergy.model.generationmixapi.GenerationData;
import com.cleanenergy.model.generationmixapi.GenerationMixEntry;
import com.cleanenergy.model.generationmixapi.GenerationResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnergyService {
    private final EnergyClient energyClient;

    public EnergyService(EnergyClient energyClient) {
        this.energyClient = energyClient;
    }

    public OptimalChargingWindow getOptimalChargingWindow(int nHours) {
        LocalDateTime from = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime to = from.plusDays(2); // 48 hrs

        GenerationResponse response = energyClient.getGenerationMix(from, to);
        List<GenerationData> dataList = response.data();

        int windowSize = nHours * 2;
        if (dataList.size() < windowSize) {
            throw new IllegalStateException("Not enough data");
        }

        // Sliding window
        int bestStartIndex = 0;
        double bestSum = 0;

        for (int i = 0; i < windowSize; i++) {
            GenerationData data = dataList.get(i);
            bestSum += getCleanEnergyPercentage(data);
        }

        double sum = bestSum;
        for (int j = windowSize; j < dataList.size(); j++) {
            int i = j - windowSize;
            sum -= getCleanEnergyPercentage(dataList.get(i));
            sum += getCleanEnergyPercentage(dataList.get(j));

            if (sum > bestSum) {
                bestSum = sum;
                bestStartIndex = i + 1;
            }
        }

        return new OptimalChargingWindow(
                dataList.get(bestStartIndex).from(),
                dataList.get(bestStartIndex + windowSize - 1).to(),
                bestSum / windowSize
        );
    }

    private double getCleanEnergyPercentage(GenerationData data) {
        double result = 0;

        for (GenerationMixEntry entry : data.generationmix()) {
            EnergySource energySource = EnergySource.valueOf(entry.fuel().toUpperCase());
            if (energySource.isCleanEnergy()) {
                result += entry.perc();
            }
        }

        return result;
    }

    public List<DayEnergyMix> get3DayEnergyMix() {
        LocalDateTime from = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIN);
        LocalDateTime to = from.plusDays(3);

        GenerationResponse response = energyClient.getGenerationMix(from, to);

        // Grouping by days
        List<List<GenerationData>> days = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            days.add(new LinkedList<>());
        }

        response.data()
                .forEach(generationData -> {
                    long dayIndex = daysFromTodayTo(generationData.from().toLocalDate());
                    if (dayIndex >= 0 && dayIndex < 3) {
                        days.get((int) dayIndex).add(generationData);
                    }
                });

        return days.stream()
                .map(this::getDayEnergyMix)
                .toList();
    }

    private DayEnergyMix getDayEnergyMix(List<GenerationData> dayData) {
        if (dayData.isEmpty()) {
            throw new IllegalArgumentException("dayData is empty");
        }

        LocalDate date = dayData.getFirst().from().toLocalDate();
        int totalData = dayData.size();

        // Total sum per EnergySource
        Map<EnergySource, Double> energySum = new HashMap<>();
        for (GenerationData generationData : dayData) {
            if (!generationData.from().toLocalDate().isEqual(date)) {
                throw new IllegalArgumentException("dayData contains different days");
            }

            for (GenerationMixEntry entry : generationData.generationmix()) {
                EnergySource energySource = EnergySource.valueOf(entry.fuel().toUpperCase());
                energySum.put(energySource, energySum.getOrDefault(energySource, 0.0) + entry.perc());
            }
        }

        // Mean of each EnergySource
        Set<EnergySourcePercentage> percentageSet = energySum.entrySet().stream()
                .map(entry -> {
                    EnergySource energySource = entry.getKey();
                    Double energyTotal = entry.getValue();

                    return new EnergySourcePercentage(energySource, energyTotal / totalData);
                })
                .collect(Collectors.toSet());

        double cleanEnergyPercentage = percentageSet.stream()
                .filter(source -> source.energySource().isCleanEnergy())
                .mapToDouble(EnergySourcePercentage::energyPercentage)
                .sum();

        return new DayEnergyMix(date, percentageSet, cleanEnergyPercentage);
    }

    private long daysFromTodayTo(LocalDate date) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return ChronoUnit.DAYS.between(today, date);
    }
}
