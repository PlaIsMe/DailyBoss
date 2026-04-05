package com.pla.pladailyboss.data;

public class CustomLootEntry {
    public final String itemId;
    public final int count;

    public CustomLootEntry(String itemId, int count) {
        this.itemId = itemId;
        this.count = Math.max(1, count);
    }
}