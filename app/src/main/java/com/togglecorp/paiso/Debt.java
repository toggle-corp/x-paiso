package com.togglecorp.paiso;

public class Debt {
    public String by;
    public String to;
    public int amount;
    public String unit = "Rs.";

    public Debt() {}

    public Debt(String by, String to, int amount, String unit) {
        this.by = by;
        this.to = to;
        this.amount = amount;
        this.unit = unit;
    }
}
