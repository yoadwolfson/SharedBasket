package com.sharedbasket.dto;

import java.io.Serializable;

public class CreateGroupRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    public CreateGroupRequestDTO() {
    }

    public CreateGroupRequestDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
