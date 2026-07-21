package com.sharedbasket.service.realtime;

import com.sharedbasket.dto.RealtimeEventType;
import com.sharedbasket.dto.RealtimeShoppingEventDTO;

import java.util.ArrayList;
import java.util.List;

public final class EventScenarioCatalog {

    private EventScenarioCatalog() {
    }

    public static List<RealtimeShoppingEventDTO> build(RealtimeScenarioType scenarioType) {
        List<RealtimeShoppingEventDTO> events = new ArrayList<>();

        if (RealtimeScenarioType.ORDERING_EDGE_CASE.equals(scenarioType)) {
            events.add(new RealtimeShoppingEventDTO("e-101", 101, RealtimeEventType.ITEM_ADDED, "Tomatoes", 3, 7.20, "A group member added Tomatoes"));
            events.add(new RealtimeShoppingEventDTO("e-102", 102, RealtimeEventType.ITEM_UPDATED, "Organic Apples", 6, 8.99, "Quantity was updated by another member"));
            events.add(new RealtimeShoppingEventDTO("e-102", 102, RealtimeEventType.ITEM_UPDATED, "Organic Apples", 6, 8.99, "Duplicate update event"));
            events.add(new RealtimeShoppingEventDTO("e-100", 100, RealtimeEventType.ITEM_REMOVED, "Greek Yogurt", 0, 0, "Out-of-order remove event"));
            events.add(new RealtimeShoppingEventDTO("e-103", 103, RealtimeEventType.CONFLICT_DETECTED, null, 0, 0, "Conflict detected while editing the same item"));
            events.add(new RealtimeShoppingEventDTO("e-104", 104, RealtimeEventType.SNAPSHOT_REQUIRED, null, 0, 0, "Server requested a full snapshot refresh"));
            events.add(new RealtimeShoppingEventDTO("e-105", 105, RealtimeEventType.KEEPALIVE, null, 0, 0, "Connection is healthy"));
            return events;
        }

        events.add(new RealtimeShoppingEventDTO("e-1", 1, RealtimeEventType.ITEM_ADDED, "Tomatoes", 3, 7.20, "A group member added Tomatoes"));
        events.add(new RealtimeShoppingEventDTO("e-2", 2, RealtimeEventType.ITEM_UPDATED, "Organic Apples", 6, 8.99, "Quantity was updated by another member"));
        events.add(new RealtimeShoppingEventDTO("e-3", 3, RealtimeEventType.KEEPALIVE, null, 0, 0, "Connection is healthy"));
        return events;
    }
}
