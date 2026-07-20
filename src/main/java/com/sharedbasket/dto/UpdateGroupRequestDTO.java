package com.sharedbasket.dto;

import java.io.Serializable;

public class UpdateGroupRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    public UpdateGroupRequestDTO() {
    }

    public UpdateGroupRequestDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
