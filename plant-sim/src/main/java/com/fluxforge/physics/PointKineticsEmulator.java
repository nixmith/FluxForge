package com.fluxforge.physics;

import com.fluxforge.physics.config.CoreCoefficients;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * One-delayed-group point-kinetics + basic Doppler & moderator feedback.
 * Runs on a ScheduledExecutor (virtual thread friendly).
 */
public final class PointKineticsEmulator implements PhysicsProvider {

    /* ===== Runtime knobs ===== */
    private final double targetPowerMW;        // 3400 MWth, etc.
    private final double dt = 0.001;           // 1 ms integration
    private final CoreCoefficients coeffs;

    /* ===== State ===== */
    private volatile double P;   // instantaneous power (relative)
    private volatile double C;   // precursor concentration
    private volatile double fuelTempK = 820.0;
    private volatile double coolantOutK = 590.0;
    private volatile double reactivity;  // ρ (fraction)
    private final Map<String, Double> rodPos = new ConcurrentHashMap<>();
    private final AtomicReference<CoreState> latest = new AtomicReference<>();
    private final List<PhysicsListener> listeners = new CopyOnWriteArrayList<>();

    /* ===== Infra ===== */
    private final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());

    public PointKineticsEmulator(double targetPowerMW, CoreCoefficients coeffs) {
        this.targetPowerMW = targetPowerMW;
        this.coeffs = coeffs;
        // initialise 100 percent power steady state
        this.P = 1.0;
        this.C = coeffs.beta() / (coeffs.lambda() * coeffs.neutronGenTime());
    }

    /* API methods -------------------------------------------------------- */

    @Override public void start() {
        exec.scheduleAtFixedRate(this::step, 0, (long)(dt * 1000), TimeUnit.MILLISECONDS);
    }

    @Override public CoreState latest() {
        return latest.get();
    }

    @Override public void addListener(PhysicsListener l) {
        listeners.add(l);
    }
    @Override public void removeListener(PhysicsListener l) {
        listeners.remove(l);
    }

    @Override public void close() {
        exec.shutdownNow();
    }

    /* ===== Integration loop ===== */
    private void step() {
        // Very simplified feedbacks (tunable constants for quick testing)
        double alphaDoppler = -4e-5;      // Δρ per K
        double alphaModerator = 7e-5;     // Δρ per K (density proxy)

        // Example rod worth (pcm/cm) * position
        double rodWorth = rodPos.values().stream().mapToDouble(v -> -7.5e-5 * v).sum();

        // Compute ρ
        reactivity = rodWorth
                + alphaDoppler * (fuelTempK - 820)
                + alphaModerator * (coolantOutK - 590);

        /* ---- Semi-implicit Euler integration ---- */
        double dP = ((reactivity - coeffs.beta()) / coeffs.neutronGenTime()) * P
                + coeffs.lambda() * C;
        double dC = (coeffs.beta() / coeffs.neutronGenTime()) * P - coeffs.lambda() * C;

        P += dP * dt;
        C += dC * dt;

        /* ---- Crude temp update (lumped mass) ---- */
        double heatCapMJperK = 8.0e5;  // fake lumped Cp (fuel+clad)
        fuelTempK += (P * targetPowerMW * 1e6 * dt) / (heatCapMJperK * 1e6);

        /* ---- Build CoreState and publish ---- */
        CoreState state = new CoreState(
                Instant.now(),
                P * targetPowerMW,
                1 + reactivity,           // k_eff ≈ 1+ρ for |ρ|≪1
                reactivity * 1e5,         // convert to pcm
                P * 87.0,                 // fake peak W/cm³
                fuelTempK,
                coolantOutK,
                Map.copyOf(rodPos),
                580                        // constant boron for now
        );
        latest.set(state);
        listeners.forEach(l -> l.onCoreState(state));
    }

    /* Public rod-motion helper (called by Operator UI or auto-control) */
    public void setRodPosition(String bankId, double posCm) {
        rodPos.put(bankId, posCm);
    }
}
