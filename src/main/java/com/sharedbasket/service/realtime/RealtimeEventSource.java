package com.sharedbasket.service.realtime;

import com.sharedbasket.dto.RealtimeShoppingEventDTO;

public interface RealtimeEventSource {

    default void setScenario(String scenarioName) {
        // Optional hook for test and mock adapters.
    }

    void connect(Long userId, Long groupId);

    void disconnect();

    boolean isConnected();

    RealtimeShoppingEventDTO nextEvent();
}
