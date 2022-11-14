package com.stano.homeapp.data;

import java.util.List;

public class EmployerRequest {
    private String g;
    private List<Double> l;
    private String EmployerId;

    public EmployerRequest(){}

    public EmployerRequest(String g, List<Double> l) {
        this.g = g;
        this.l = l;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public List<Double> getL() {
        return l;
    }

    public void setL(List<Double> l) {
        this.l = l;
    }

    public void setEmployerId(String EmployerId) {
        this.EmployerId = EmployerId;
    }

    public String getEmployerId() {
        return EmployerId;
    }
}
