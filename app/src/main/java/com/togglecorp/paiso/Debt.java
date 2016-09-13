package com.togglecorp.paiso;

public class Debt {
    public String by;       // Is either @me or contact-id
    public String to;       // Is either @me or contact-id
    public float amount;
    public String unit = "Rs.";

    public Debt() {}

    public Debt(String by, String to, float amount, String unit) {
        this.by = by;
        this.to = to;
        this.amount = amount;
        this.unit = unit;
    }
}
