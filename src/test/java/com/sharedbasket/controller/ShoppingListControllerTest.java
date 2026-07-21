package com.sharedbasket.controller;

import com.sharedbasket.dto.RealtimeEventType;
import com.sharedbasket.dto.RealtimeShoppingEventDTO;
import com.sharedbasket.dto.ShoppingItemDTO;
import com.sharedbasket.dto.UserDTO;
import com.sharedbasket.service.realtime.RealtimeEventSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShoppingListControllerTest {

    @Test
    void ignoresDuplicateAndOutOfOrderEvents() {
        TestRealtimeEventSource source = new TestRealtimeEventSource();
        source.push(new RealtimeShoppingEventDTO("evt-1", 2, RealtimeEventType.ITEM_UPDATED, "Organic Apples", 9, 8.99, "fresh update"));
        source.push(new RealtimeShoppingEventDTO("evt-1", 2, RealtimeEventType.ITEM_UPDATED, "Organic Apples", 11, 8.99, "duplicate update"));
        source.push(new RealtimeShoppingEventDTO("evt-0", 1, RealtimeEventType.ITEM_UPDATED, "Organic Apples", 2, 8.99, "out of order update"));

        ShoppingListController controller = buildController(source);

        controller.connectRealtime();
        controller.processNextMockEvent();

        ShoppingItemDTO applesAfterFirst = findByName(controller, "Organic Apples");
        assertNotNull(applesAfterFirst);
        assertEquals(9, applesAfterFirst.getQuantity());
        assertEquals(2L, controller.getLastAppliedVersion());

        controller.processNextMockEvent();
        ShoppingItemDTO applesAfterDuplicate = findByName(controller, "Organic Apples");
        assertNotNull(applesAfterDuplicate);
        assertEquals(9, applesAfterDuplicate.getQuantity());
        assertEquals(2L, controller.getLastAppliedVersion());

        controller.processNextMockEvent();
        ShoppingItemDTO applesAfterOutOfOrder = findByName(controller, "Organic Apples");
        assertNotNull(applesAfterOutOfOrder);
        assertEquals(9, applesAfterOutOfOrder.getQuantity());
        assertTrue(controller.isDegraded());
    }

    @Test
    void refreshSnapshotRecoversFromDegradedStateAndResetsVersion() {
        TestRealtimeEventSource source = new TestRealtimeEventSource();
        source.push(new RealtimeShoppingEventDTO("evt-9", 9, RealtimeEventType.CONFLICT_DETECTED, null, 0, 0, "conflict"));

        ShoppingListController controller = buildController(source);

        controller.connectRealtime();
        controller.processNextMockEvent();
        assertTrue(controller.isDegraded());

        controller.refreshSnapshot();
        assertTrue(controller.isConnected());
        assertEquals(0L, controller.getLastAppliedVersion());
    }

    private ShoppingListController buildController(TestRealtimeEventSource source) {
        ShoppingListController controller = new ShoppingListController();
        ShellController shellController = new ShellController();
        shellController.setCurrentUser(new UserDTO(10L, "dev@sharedbasket.local", "Dev User", "MEMBER"));

        controller.setShellController(shellController);
        controller.setRealtimeEventSource(source);
        controller.init();
        return controller;
    }

    private ShoppingItemDTO findByName(ShoppingListController controller, String name) {
        for (ShoppingItemDTO item : controller.getShoppingItems()) {
            if (item.getName() != null && item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    private static final class TestRealtimeEventSource implements RealtimeEventSource {

        private final Queue<RealtimeShoppingEventDTO> queue = new ArrayDeque<>();
        private boolean connected;

        @Override
        public void setScenario(String scenarioName) {
            // No-op for tests.
        }

        @Override
        public void connect(Long userId, Long groupId) {
            connected = true;
        }

        @Override
        public void disconnect() {
            connected = false;
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public RealtimeShoppingEventDTO nextEvent() {
            return queue.poll();
        }

        void push(RealtimeShoppingEventDTO event) {
            queue.add(event);
        }
    }
}
