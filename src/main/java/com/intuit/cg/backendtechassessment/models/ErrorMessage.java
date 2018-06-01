package com.intuit.cg.backendtechassessment.models;

import java.util.ArrayList;
import java.util.List;

public class ErrorMessage {
    List<String> errors;
    public ErrorMessage(String error) {
        this.errors = new ArrayList<String>();
        errors.add(error);
    }

    public ErrorMessage(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}