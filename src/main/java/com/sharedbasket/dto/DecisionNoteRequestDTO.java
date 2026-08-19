package com.sharedbasket.dto;

import java.io.Serializable;

public class DecisionNoteRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String decisionNote;

    public DecisionNoteRequestDTO() {
    }

    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
}
