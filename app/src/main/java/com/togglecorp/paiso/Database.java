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
            mDatabase.child("emails").child(f_user.getEmail()).setValue(f_user.getUid());
        }
    }

    public void addTransaction(Transaction transaction) {
        DatabaseReference t = mDatabase.child("transactions").push();
        t.child("title").setValue(transaction.title);

        DatabaseReference u = mDatabase.child("users");

        for (Debt debt: transaction.debts) {
            DatabaseReference d = t.child("debts").push();

            d.child("by").setValue(debt.by);
            d.child("to").setValue(debt.to);
            d.child("amount").setValue(debt.amount);
            d.child("unit").setValue(debt.unit);

            u.child(debt.by).child("transactions").child(t.getKey()).setValue(true);
            u.child(debt.to).child("transactions").child(t.getKey()).setValue(true);
        }
    }

    public void addContact(Contact contact) {
        DatabaseReference c = mUser.child("contacts").push();
        c.child("username").setValue(contact.username);
        if (contact.contact_id != null) {
            c.child("contact_id").setValue(contact.contact_id);
        }

    }

    HashMap<String, Transaction> mTransactions;
    public void getTransactions(final DatabaseListener<HashMap<String, Transaction>> listener) {
        listener.handle(mTransactions);

        mUser.child("transactions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                for (DataSnapshot t: data.getChildren()) {
                    mDatabase.child("transactions").child(t.getKey())
                            .addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot data) {
                                    mTransactions.put(
                                            data.getKey(),
                                            new Transaction(data)
                                    );
                                    listener.handle(mTransactions);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "load transactions cancelled",
                                            databaseError.toException());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load transactions cancelled", databaseError.toException());
            }
        });
    }

    private HashMap<String, Contact> mContacts;
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
                        username: somebody
                    },
                    c2: {
                        username: somebody else
                        contact_id: cid1
                    }
                },
                transactions: {
                    t1: true
                    t2: true
                    ...
                },
            },
            ...
        },
        emails: {
            adr1: id1,
            adr2: id2,
            ...
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
        }
     */

}
