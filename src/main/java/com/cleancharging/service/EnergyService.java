package com.cleancharging.service;

import com.cleancharging.model.DayEnergyMix;
import com.cleancharging.model.EnergySource;
import com.cleancharging.model.EnergySourcePercentage;
import com.cleancharging.model.generationmix_api.GenerationData;
import com.cleancharging.model.generationmix_api.GenerationMixEntry;
import com.cleancharging.model.generationmix_api.GenerationResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnergyService {
    private final GenerationMixClient generationMixClient;
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

    public EnergyService(GenerationMixClient generationMixClient) {
        this.generationMixClient = generationMixClient;
    }

    public List<DayEnergyMix> get3DayEnergyMix() {
        LocalDateTime from = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIN)
                .plusMinutes(1);
        LocalDateTime to = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC).plusDays(2), LocalTime.MAX.truncatedTo(ChronoUnit.MINUTES))
                .plusMinutes(1);

        GenerationResponse response = generationMixClient.getGenerationMix(from.format(FORMATTER), to.format(FORMATTER));
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
                .collect(Collectors.toList());
    }

    private DayEnergyMix getDayEnergyMix(List<GenerationData> dayData) {
        if (dayData.isEmpty()) {
            return new DayEnergyMix();
        }

        LocalDate date = dayData.getFirst().from().toLocalDate();
        int totalData = dayData.size();
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
