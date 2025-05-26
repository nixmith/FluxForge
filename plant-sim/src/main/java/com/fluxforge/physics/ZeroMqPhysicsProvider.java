package com.fluxforge.physics;

import org.zeromq.ZMQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;

public final class ZeroMqPhysicsProvider implements PhysicsProvider {

    private final ZMQ.Context ctx = ZMQ.context(1);
    private final ZMQ.Socket sub  = ctx.socket(ZMQ.SUB);
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<PhysicsListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicReference<CoreState> latest = new AtomicReference<>();
    private Thread rxThread;

    public ZeroMqPhysicsProvider(String endpoint, String topic) {
        sub.connect(endpoint);
        sub.subscribe(topic.getBytes());
    }

    @Override public void start() {
        rxThread = Thread.ofVirtual().start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                String topic = sub.recvStr();
                String payload = sub.recvStr();
                CoreState state = mapper.readValue(payload, CoreState.class);
                latest.set(state);
                listeners.forEach(l -> l.onCoreState(state));
            }
        });
    }

    @Override public CoreState latest() { return latest.get(); }
    @Override public void addListener(PhysicsListener l) { listeners.add(l); }
    @Override public void removeListener(PhysicsListener l) { listeners.remove(l); }
    @Override public void close() {
        rxThread.interrupt();
        sub.close(); ctx.close();
    }
}
