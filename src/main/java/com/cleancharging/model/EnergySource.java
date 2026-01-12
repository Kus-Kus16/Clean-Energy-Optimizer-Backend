package com.cleancharging.model;

import java.util.Objects;

public enum EnergySource {
    GAS(false),
    COAL(false),
    BIOMASS(true),
    NUCLEAR(true),
    HYDRO(true),
    WIND(true),
    SOLAR(true),
    IMPORTS(false),
    OTHER(false);

    private final boolean clean;

    EnergySource(boolean clean) {
        this.clean = clean;
    }

    public boolean isCleanEnergy() {
        return clean;
    }
}

