package com.sharedbasket.dto;

import java.io.Serializable;
import java.util.List;

public class RebuildRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long groupId;
    private List<String> itemNames;

    public RebuildRequestDTO() {}

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public List<String> getItemNames() { return itemNames; }
    public void setItemNames(List<String> itemNames) { this.itemNames = itemNames; }
}
