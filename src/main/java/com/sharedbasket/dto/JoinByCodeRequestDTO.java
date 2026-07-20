package com.sharedbasket.dto;

import java.io.Serializable;

public class JoinByCodeRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String inviteCode;

    public JoinByCodeRequestDTO() {
    }

    public JoinByCodeRequestDTO(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
