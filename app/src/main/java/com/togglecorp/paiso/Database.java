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
    private String mUserId;

//    public Context getContext() {
//        return mContext;
//    }

    public Database(User user) {
        mContext = user.getContext();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser f_user = user.getUser();
        mUser = mDatabase.child("users").child(f_user.getUid());
        mUserId = f_user.getUid();

        // Create or update the user data
        mUser.child("username").setValue(f_user.getDisplayName());

        // Give fair warning to user that others will not discover them
        // without their email
        if (f_user.getEmail() != null) {
            mUser.child("email").setValue(f_user.getEmail());
        }
    }


    // Make sure getContacts has been called before calling this method
    public void addTransaction(Transaction transaction) {
        DatabaseReference t = mDatabase.child("transactions").push();
        mUser.child("transactions").child(t.getKey()).setValue(true);

        t.child("title").setValue(transaction.title);
        t.child("created_by").setValue(mUserId);

        for (Debt debt: transaction.debts) {
            DatabaseReference d = t.child("debts").push();
            d.child("amount").setValue(debt.amount);
            setContactToDebt(d.child("to"), debt.to, t.getKey(),
                    d.child("to_cid"));
            setContactToDebt(d.child("by"), debt.by, t.getKey(),
                    d.child("by_cid"));
        }
    }

    // Make sure getContacts has been called before calling this method
    private void setContactToDebt(final DatabaseReference d, final String contactId,
                                  final String transactionId,
                                  final DatabaseReference alteranteD) {
        if (contactId.equals("@me")) {
            d.setValue(mUserId);
        }
        else {
            getUserWithContact(contactId, new DatabaseListener<String>() {
                @Override
                public void handle(String uid) {
                    if (uid == null)
                        alteranteD.setValue(contactId);
                    else {
                        d.setValue(uid);

                        mDatabase.child("users").child(uid).child("transactions")
                                .child(transactionId).setValue(true);
                    }
                }
            });
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
                if (data.exists()) {
                    for (DataSnapshot t : data.getChildren()) {
                        if (t.getValue(Boolean.class)) {
                            getTransaction(t.getKey(), listener);
                        }
                    }
                }
//                listener.handle(mTransactions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "load transactions cancelled", databaseError.toException());
            }
        });
    }

    private void getTransaction(final String tid,
                                final DatabaseListener<HashMap<String, Transaction>> listener) {

        mDatabase.child("transactions").child(tid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot t) {
                        if (t.exists()) {
                            Transaction transaction = new Transaction();

                            if (t.child("title").exists())
                                transaction.title = t.child("title").getValue(String.class);

                            if (!t.child("created_by").exists() ||
                                    !t.child("debts").exists())
                                return;

                            boolean creator = t.child("created_by").getValue(String.class)
                                    .equals(mUserId);

                            DataSnapshot debts = t.child("debts");
                            for (DataSnapshot d: debts.getChildren()) {

                                if ((!d.child("to_cid").exists() && !d.child("to").exists())
                                        || (!d.child("by_cid").exists() && !d.child("by").exists())
                                        || (!d.child("amount").exists())) {
                                    continue;
                                }

                                Debt debt = new Debt();
                                debt.amount = d.child("amount").getValue(Float.class);

                                debt.to = "";
                                debt.by = "";

                                if (d.child("to_cid").exists()) {
                                    if (!creator)
                                        return;
                                    debt.to = d.child("to_cid").getValue(String.class);
                                }
                                else
                                    setDebtContact(d.child("to"), debt, true, listener);

                                if (d.child("by_cid").exists()) {
                                    if (!creator)
                                        return;
                                    debt.by = d.child("by_cid").getValue(String.class);
                                }
                                else
                                    setDebtContact(d.child("by"), debt, false,listener);

                                transaction.debts.add(debt);
                            }

                            mTransactions.put(tid, transaction);
                            listener.handle(mTransactions);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "cancelled transaction",
                                databaseError.toException());
                    }
                });
    }

    private void setDebtContact(DataSnapshot d, final Debt debt, final boolean to,
                                final DatabaseListener<HashMap<String, Transaction>> listener) {

        if (d.getValue(String.class).equals(mUserId)) {
            if (to)
                debt.to = "@me";
            else
                debt.by = "@me";
        }
        else {
            String uid = d.getValue(String.class);
            getContactWithUser(uid, new DatabaseListener<Cursor>() {
                @Override
                public void handle(Cursor data) {
                    if (data != null) {
                        Contact c = SelectPeopleActivity.addContact(
                                Database.this, data
                        );
                        if (to)
                            debt.to = c.contact_id;
                        else
                            debt.by = c.contact_id;
                        listener.handle(mTransactions);
                    }
                }
            });
        }
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
                    t1: true,
                    t2: true,
                },
            },
            ...
        },
        transactions: {
            t1 {
                title: "optional"
                created_by: u1
                debts: {
                    a1: {
                        by: u1
                        to: u2
                        amount: xxx
                    }
                    a2: {
                        by_cid: c1
                        to: u1          // cid is id of contact belonging to creator (u1)
                        amount: xxx     // for other users, ignore this debt
                    }
                }
            }
        }
     */




    // Make sure getContacts has been called before calling this method
    public void getUserWithContact(String contactId, final DatabaseListener<String> handler) {
        if (mContacts.containsKey(contactId)) {
            Contact c = mContacts.get(contactId);
            if (c.contact_id != null) {
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
                                            if (dataSnapshot.getChildrenCount() > 0) {
                                                handler.handle(
                                                        dataSnapshot
                                                                .getChildren()
                                                                .iterator().next().getKey()
                                                );
                                                return;
                                            }
                                        }
                                        handler.handle(null);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "load user's emails cancelled",
                                                databaseError.toException());
                                        handler.handle(null);
                                    }
                                });

                    }
                    cursor.close();
                }
            }
        }
    }

    // Make sure getContacts has been called before calling this method
    public void getContactWithUser(final String userId, final DatabaseListener<Cursor> handler) {
        mDatabase.child("users").orderByKey().equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String email = dataSnapshot.child(userId)
                                    .child("email").getValue(String.class);

                            // Get contact with same email
                            Cursor cursor = mContext.getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Email.DATA + " = ?",
                                    new String[]{email},
                                    null
                            );

                            if (cursor != null) {
                                if (cursor.moveToFirst()) {
                                    handler.handle(cursor);
                                    return;
                                }
                                cursor.close();
                            }
                        }
                        handler.handle(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "load user's emails cancelled",
                                databaseError.toException());
                        handler.handle(null);
                    }
                });
    }

}
