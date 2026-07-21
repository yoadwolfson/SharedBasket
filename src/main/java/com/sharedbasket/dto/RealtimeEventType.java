package com.sharedbasket.dto;

public enum RealtimeEventType {
    ITEM_ADDED,
    ITEM_UPDATED,
    ITEM_REMOVED,
    SNAPSHOT_REQUIRED,
    CONFLICT_DETECTED,
    KEEPALIVE
}
