package com.togglecorp.paiso;

import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    public String title;
    public List<Debt> debts = new ArrayList<>();

    public boolean invalid = false;

    public Transaction() {}

    public Transaction(final Database db, final DataSnapshot data) {
        title = data.child("title").getValue(String.class);

        if (!data.child("debts").exists()) {
            invalid = true;
            return;
        }

        for (final DataSnapshot d: data.child("debts").getChildren()) {

            if ((!d.child("by").exists() && !d.child("by_uid").exists()) ||
                    (!d.child("to").exists() && !d.child("to_uid").exists()) ||
                    !d.child("amount").exists() ||
                    !d.child("unit").exists()) {

                invalid = true;
                return;
            }

            // Check if to_uid and by_uid are present instead of to and by
            // then add contact for that user, then add debt

            String uid = null;
            if (!d.child("to").exists() && d.child("to_uid").exists())
                uid = d.child("to_uid").getValue(String.class);
            else if (!d.child("by").exists() && d.child("by_uid").exists())
                uid = d.child("by_uid").getValue(String.class);

            if (uid == null) {
                debts.add(new Debt(
                        d.child("by").getValue(String.class),
                        d.child("to").getValue(String.class),
                        d.child("amount").getValue(Float.class),
                        d.child("unit").getValue(String.class)
                ));
            }

            else {
                // First get the user
                data.getRef().getDatabase().getReference()
                        .child("users").child(uid).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    // Get the email and get contact with that email
                                    String email =
                                            dataSnapshot.child("email").getValue(String.class);

                                    Cursor cursor = db.getContext().getContentResolver().query(
                                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Email.DATA + " = ?",
                                            new String[]{email},
                                            null
                                    );

                                    if (cursor != null) {
                                        if (cursor.moveToFirst()) {

                                            // Add this contact
                                            Contact c = SelectPeopleActivity.addContact(db, cursor);

                                            if (c.contact_id != null) {

                                                String to = "@me", by = "@me";
                                                if (d.child("to_uid").exists()) {
                                                    d.getRef().child("to_uid").setValue(null);
                                                    d.getRef().child("to").setValue(c.contact_id);
                                                    to = c.contact_id;
                                                }
                                                if (d.child("by_uid").exists()) {
                                                    d.getRef().child("by_uid").setValue(null);
                                                    d.getRef().child("by").setValue(c.contact_id);
                                                    by = c.contact_id;
                                                }

                                                debts.add(new Debt(
                                                        by,
                                                        to,
                                                        d.child("amount").getValue(Float.class),
                                                        d.child("unit").getValue(String.class)
                                                ));
                                            }

                                        }
                                        cursor.close();
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // TODO: log
                            }
                        }
                );
            }
        }
    }
}
