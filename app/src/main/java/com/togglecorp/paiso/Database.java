package com.togglecorp.paiso;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Database {
    private static final String TAG = "Database";

    private DatabaseReference mDatabase;
    private DatabaseReference mUser;
    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    public Database(User user) {
        mContext = user.getContext();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser f_user = user.getUser();
        mUser = mDatabase.child("users").child(f_user.getUid());

        // Create or update the user data
        mUser.child("username").setValue(f_user.getDisplayName());

        // Give fair warning to user that others will not discover them
        // without their email
        if (f_user.getEmail() != null) {
            mUser.child("email").setValue(f_user.getEmail());
        }
    }

    public void addTransaction(Transaction transaction) {
        addTransaction(transaction, mUser);

        for (Debt debt: transaction.debts) {
            if (!debt.by.equals("@me"))
                setTransactionOfContact(debt.by, transaction);
            if (!debt.to.equals("@me"))
                setTransactionOfContact(debt.to, transaction);
        }
    }

    private void addTransaction(Transaction transaction, DatabaseReference user) {
        DatabaseReference t = user.child("transactions").push();
        t.child("title").setValue(transaction.title);

        for (Debt debt: transaction.debts) {
            DatabaseReference d = t.child("debts").push();

            if (user.equals(mUser)) {
                d.child("by").setValue(debt.by);
                d.child("to").setValue(debt.to);
            } else {
                if (debt.by.equals("@me")) {
                    d.child("by_uid").setValue(mUser.getKey());
                    d.child("to").setValue("@me");
                } else {
                    d.child("by").setValue("@me");
                    d.child("to_uid").setValue(mUser.getKey());
                }
            }
            d.child("amount").setValue(debt.amount);
            d.child("unit").setValue(debt.unit);
        }
    }

    private void setTransactionOfContact(String contact_id, final Transaction transaction) {
        if (mContacts.containsKey(contact_id)) {
            Contact c = mContacts.get(contact_id);
            if (c.contact_id != null){
                // Get contact's email and check if user with that email exists
                // if so, add transaction to that user
                Cursor cursor = mContext.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.Contacts._ID + " = ?",
                        new String[]{c.contact_id},
                        null
                );

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        final String email = cursor.getString(cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Email.DATA
                        ));
                        mDatabase.child("users").orderByChild("email").equalTo(email)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot d: dataSnapshot.getChildren()) {
                                                addTransaction(transaction, d.getRef());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "load user's emails cancelled",
                                                databaseError.toException());
                                    }
                                });

                    }
                    cursor.close();
                }
            }
        }
    }

    public void addContact(Contact contact) {
        DatabaseReference c;

        if (contact.contact_id != null)
            // Using contact_id makes sure that same contact isn't added twice
            c = mUser.child("contacts").child(contact.contact_id);
        else
            c = mUser.child("contacts").push();

        c.child("username").setValue(contact.username);
        if (contact.contact_id != null) {
            c.child("contact_id").setValue(contact.contact_id);
        }
        if (contact.recent != null) {
            c.child("recent").setValue(contact.recent);
        }
        if (contact.photo_uri != null) {
            c.child("photo_uri").setValue(contact.photo_uri.toString());
        }
    }

    private static HashMap<String, Transaction> mTransactions = new HashMap<>();
    public void getTransactions(final DatabaseListener<HashMap<String, Transaction>> listener) {
        listener.handle(mTransactions);

        mUser.child("transactions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                for (DataSnapshot t: data.getChildren()) {
                    Transaction tt = new Transaction(Database.this, t);
                    if (!tt.invalid)
                        mTransactions.put(
                                data.getKey(),
                                tt
                        );
                }
                listener.handle(mTransactions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load transactions cancelled", databaseError.toException());
            }
        });
    }

    private static HashMap<String, Contact> mContacts = new HashMap<>();
    public void getContacts(final DatabaseListener<HashMap<String, Contact>> listener) {
        listener.handle(mContacts);

        mUser.child("contacts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                for (DataSnapshot c: data.getChildren()) {
                    Contact cc = new Contact(c);
                    if (!cc.invalid)
                        mContacts.put(c.getKey(), cc);
                }
                listener.handle(mContacts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load contacts cancelled", databaseError.toException());
            }
        });
    }


    /*
    An example database:

        users: {
            id1: {
                username: someone,
                email: adr1,
                contacts: {
                    c1: {
                        username: somebody,
                        recent: 1
                    },
                    c2: {
                        username: somebody else
                        contact_id: cid1
                        recent: 2
                    }
                },
                transactions: {
                    t1: {
                        title: "optional"
                        debts: {
                            a1: {
                                by: @me
                                to: u2 (contact_id)
                                amount: xxx
                                unit: "Rs."
                            }
                            a2: {
                                by_uid: u1 (user_id)
                                to: @me
                                amount: xxx
                            }
                        }
                    }
                },
            },
            ...
        },
     */

}
