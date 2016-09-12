package com.togglecorp.paiso;

import com.google.firebase.database.DataSnapshot;

public class Contact {
    public String username;
    public String contact_id = null;

    public Contact() {}

    public Contact(DataSnapshot data) {
        username = data.child("username").getValue(String.class);
        if (data.child("contact_id").exists())
            contact_id = data.child("contact_id").getValue(String.class);
    }
}
