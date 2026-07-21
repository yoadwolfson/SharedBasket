package com.sharedbasket.service.realtime;

import com.sharedbasket.dto.RealtimeEventType;
import com.sharedbasket.dto.RealtimeShoppingEventDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Queue;

@Service
public class MockRealtimeEventSource implements RealtimeEventSource {

    private final Queue<RealtimeShoppingEventDTO> scriptedEvents = new ArrayDeque<>();
    private boolean connected;
    private RealtimeScenarioType scenarioType = RealtimeScenarioType.ORDERING_EDGE_CASE;

    @Override
    public synchronized void setScenario(String scenarioName) {
        try {
            this.scenarioType = RealtimeScenarioType.valueOf(scenarioName);
        } catch (Exception ignored) {
            this.scenarioType = RealtimeScenarioType.ORDERING_EDGE_CASE;
        }
    }

    @Override
    public synchronized void connect(Long userId, Long groupId) {
        connected = true;
        scriptedEvents.clear();
        scriptedEvents.addAll(EventScenarioCatalog.build(scenarioType));
    }

    @Override
    public synchronized void disconnect() {
        connected = false;
        scriptedEvents.clear();
    }

    @Override
    public synchronized boolean isConnected() {
        return connected;
    }

    @Override
    public synchronized RealtimeShoppingEventDTO nextEvent() {
        if (!connected) {
            return null;
        }

        RealtimeShoppingEventDTO next = scriptedEvents.poll();
        if (next != null) {
            return next;
        }

        return new RealtimeShoppingEventDTO(
            "keepalive-idle",
            Long.MAX_VALUE,
            RealtimeEventType.KEEPALIVE,
            null,
            0,
            0,
            "No new list changes"
        );
    }
}
