package com.togglecorp.paiso;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    public String title;
    public List<Debt> debts = new ArrayList<>();

    public Transaction() {}

    public Transaction(DataSnapshot data) {
        title = data.child("title").getValue(String.class);
        for (DataSnapshot d: data.child("debts").getChildren()) {
            debts.add(new Debt(
                    d.child("by").getValue(String.class),
                    d.child("to").getValue(String.class),
                    d.child("amount").getValue(Float.class),
                    d.child("unit").getValue(String.class)
            ));
        }
    }
}
