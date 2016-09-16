package com.togglecorp.paiso;

public class Debt {
    public String by;       // Is either @me or contact-id
    public String to;       // Is either @me or contact-id
    public float amount;

    public Debt() {}

    public Debt(String by, String to, float amount) {
        this.by = by;
        this.to = to;
        this.amount = amount;
    }
}
