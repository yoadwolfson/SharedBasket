package com.sharedbasket.dto;

import java.io.Serializable;

public class UpdateCategoryRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String color;
    private Integer sortOrder;
    private Boolean archived;
    private Long version;

    public UpdateCategoryRequestDTO() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
