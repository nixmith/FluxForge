package com.fluxforge.physics;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable snapshot of core neutronics and primary feedback channels.
 */
public record CoreState(
        Instant timestamp,
        double powerMWth,
        double kEff,
        double reactivityPcm,
        double peakPowerDensityWcm3,
        double averageFuelTempK,
        double coolantOutletTempK,
        Map<String, Double> rodBankPositionsCm,   // e.g. "A" → 140 cm
        double boronPpm
) {}
