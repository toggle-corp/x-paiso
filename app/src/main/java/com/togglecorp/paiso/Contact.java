package com.togglecorp.paiso;

import android.net.Uri;

import com.google.firebase.database.DataSnapshot;

public class Contact {
    public String username;
    public String contact_id = null;
    public Integer recent = null;
    public Uri photo_uri = null;

    public Contact() {}

    public Contact(String username) {
        this.username  = username;
    }

    public Contact(DataSnapshot data) {
        username = data.child("username").getValue(String.class);

        if (data.child("contact_id").exists())
            contact_id = data.child("contact_id").getValue(String.class);
        if (data.child("recent").exists())
            recent = data.child("recent").getValue(Integer.class);
        if (data.child("photo_uri").exists())
            photo_uri = Uri.parse(data.child("photo_uri").getValue(String.class));
    }
}
