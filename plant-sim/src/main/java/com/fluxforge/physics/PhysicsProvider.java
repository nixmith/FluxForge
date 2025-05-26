package com.fluxforge.physics;

/**
 * Abstraction representing “something that produces CoreState snapshots”.
 * Could be an in-JVM emulator or a native ZeroMQ subscriber.
 */
public interface PhysicsProvider extends AutoCloseable {

    /** Begin emitting states (non-blocking). */
    void start();

    /** Latest value without waiting (may return null until first update). */
    CoreState latest();

    /** Register or unregister listeners. */
    void addListener(PhysicsListener l);
    void removeListener(PhysicsListener l);

    /** For AutoCloseable. */
    @Override void close();
}