package com.togglecorp.paiso;

import android.net.Uri;

import com.google.firebase.database.DataSnapshot;

public class Contact {
    public String username;
    public String email;
    public String contactId = null;
    public String userId = null;
    public Uri photo_uri = null;

    public String contactUid = null;        // contact id in firebase

    public Contact() {}

    public Contact(String username) {
        this.username  = username;
    }

    public Contact(DataSnapshot data) {
        username = data.child("username").getValue(String.class);
        contactUid = data.getKey();

        if (data.child("email").exists())
            email = data.child("email").getValue(String.class);
        if (data.child("contact_id").exists())
            contactId = data.child("contact_id").getValue(String.class);
        if (data.child("user_id").exists())
            userId = data.child("user_id").getValue(String.class);
        if (data.child("photo_uri").exists())
            photo_uri = Uri.parse(data.child("photo_uri").getValue(String.class));
    }
}
