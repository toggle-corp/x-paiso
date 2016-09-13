package com.togglecorp.paiso;

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

    public Database(User user) {
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
    }

    private void addTransaction(Transaction transaction, DatabaseReference user) {
        DatabaseReference t = user.child("transactions").push();
        t.child("title").setValue(transaction.title);

        for (Debt debt: transaction.debts) {
            DatabaseReference d = t.child("debts").push();

            d.child("by").setValue(debt.by);
            d.child("to").setValue(debt.to);
            d.child("amount").setValue(debt.amount);
            d.child("unit").setValue(debt.unit);

            if (!debt.by.equals("@me"))
                setTransactionOfContact(debt.by, transaction);
            if (!debt.to.equals("@me"))
                setTransactionOfContact(debt.to, transaction);
        }
    }

    private void setTransactionOfContact(String contact_id, Transaction transaction) {
        if (mContacts.containsKey(contact_id)) {
            Contact c = mContacts.get(contact_id);
            if (c.user_id != null) {
                addTransaction(transaction, mDatabase.child("users").child(c.user_id));
                return;
            }
            else if (c.contact_id != null){
                // TODO: Get contact's email and check if user with that email exists
                // TODO: if so, add transaction to that user
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
        if (contact.user_id != null) {
            c.child("user_id").setValue(contact.user_id);
        }
        if (contact.photo_uri != null) {
            c.child("photo_uri").setValue(contact.photo_uri.toString());
        }
    }

    private HashMap<String, Transaction> mTransactions = new HashMap<>();
    public void getTransactions(final DatabaseListener<HashMap<String, Transaction>> listener) {
        listener.handle(mTransactions);

        mUser.child("transactions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                for (DataSnapshot t: data.getChildren()) {
                    mTransactions.put(
                            data.getKey(),
                            new Transaction(t)
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

    private HashMap<String, Contact> mContacts = new HashMap<>();
    public void getContacts(final DatabaseListener<HashMap<String, Contact>> listener) {
        listener.handle(mContacts);

        mUser.child("contacts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                for (DataSnapshot c: data.getChildren()) {
                    mContacts.put(c.getKey(), new Contact(c));
                }
                listener.handle(mContacts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load transactions cancelled", databaseError.toException());
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
                                by: u1
                                to: u2
                                amount: xxx
                                unit: "Rs."
                            }
                            a2: {
                                by: u1
                                to: u3
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
