package com.togglecorp.paiso;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    public String title;
    public List<Debt> debts = new ArrayList<>();

    public Transaction() {}
}
