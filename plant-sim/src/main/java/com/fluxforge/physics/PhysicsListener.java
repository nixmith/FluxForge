package com.fluxforge.physics;

/** Push-style callback (functional interface → can be a λ). */
@FunctionalInterface
public interface PhysicsListener {
    void onCoreState(CoreState state);
}