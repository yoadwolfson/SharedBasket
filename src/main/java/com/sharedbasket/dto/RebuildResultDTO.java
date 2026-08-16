package com.sharedbasket.dto;

import java.io.Serializable;
import java.util.List;

public class RebuildResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int createdItemCount;
    private int skippedItemCount;
    private List<String> conflicts;

    public RebuildResultDTO() {}

    public RebuildResultDTO(int createdItemCount, int skippedItemCount, List<String> conflicts) {
        this.createdItemCount = createdItemCount;
        this.skippedItemCount = skippedItemCount;
        this.conflicts = conflicts;
    }

    public int getCreatedItemCount() { return createdItemCount; }
    public void setCreatedItemCount(int createdItemCount) { this.createdItemCount = createdItemCount; }
    public int getSkippedItemCount() { return skippedItemCount; }
    public void setSkippedItemCount(int skippedItemCount) { this.skippedItemCount = skippedItemCount; }
    public List<String> getConflicts() { return conflicts; }
    public void setConflicts(List<String> conflicts) { this.conflicts = conflicts; }
}
