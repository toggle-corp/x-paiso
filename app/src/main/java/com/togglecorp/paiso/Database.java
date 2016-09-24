package com.togglecorp.paiso;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Database {
    private static final String TAG = "Database";

    // Global database
    // -------------
    public static HashMap<String, Contact> Contacts = new HashMap<>();
    public static HashMap<String, String> UserContactMap = new HashMap<>();
    public static HashMap<String, Transaction> Transactions = new HashMap<>();

    public static List<DatabaseListener<Void>> ContactsListeners = new ArrayList<>();
    public static List<DatabaseListener<Void>> TransactionsListeners = new ArrayList<>();
    // -------------

    private Context mContext;

    private DatabaseReference mDatabase;
    private DatabaseReference mUser;

    public Database(User user, Context context) {
        mContext = context;

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser fbUser = user.getUser();
        mUser = mDatabase.child("users").child(fbUser.getUid());

        mUser.child("username").setValue(user.getUser().getDisplayName());
        mUser.child("email").setValue(user.getUser().getEmail());

    }

    private ValueEventListener mContactsListener = null;
    public void getContacts() {
        if (mContactsListener != null) {
            mUser.child("contacts").removeEventListener(mContactsListener);
            mContactsListener = null;
        }

        mContactsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data: dataSnapshot.getChildren()) {
                        if (!data.child("username").exists())
                            continue;

                        Contact contact = new Contact(data);
                        Contacts.put(data.getKey(), contact);

                        if (contact.userId != null)
                            UserContactMap.put(contact.userId, data.getKey());
                    }

                    if (ContactsListeners != null) {
                        for (DatabaseListener<Void> cl: ContactsListeners)
                            cl.handle(null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error getting contacts");
            }
        };
        mUser.child("contacts").addValueEventListener(mContactsListener);
    }

    public void addContact(Contact contact) {
        DatabaseReference ref;
        // Get contact reference
        if (contact.contactId == null)
            ref = mUser.child("contacts").push();
        else
            ref = mUser.child("contacts").child(contact.contactId);

        // Add contact details
        ref.child("username").setValue(contact.username);

        if (contact.contactId != null)
            ref.child("contact_id").setValue(contact.contactId);
        if (contact.email != null) {
            ref.child("email").setValue(contact.email);
            setContactUser(ref, contact);
        }
        if (contact.photo_uri != null)
            ref.child("photo_uri").setValue(contact.photo_uri.toString());

        // Refresh transactions to reload contact data
        getTransactions();
    }


    public Contact addContact(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        String username =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        String email =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
        String photoUrl =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_URI));


        Contact contact = new Contact();
        contact.contactId = id;
        contact.username = username;
        contact.email = email;
        if (photoUrl != null)
            contact.photo_uri = Uri.parse(photoUrl);
        addContact(contact);
        return contact;
    }

    // Check if user with email exists
    // and set user_id of contact
    private HashMap<String, ValueEventListener> mContactUserListener = new HashMap<>();
    public void setContactUser(final DatabaseReference ref, final Contact contact) {
        if (mContactUserListener.containsKey(contact.email)) {
            mDatabase.child("users").orderByChild("email").equalTo(contact.email)
                    .removeEventListener(mContactUserListener.get(contact.email));
            mContactUserListener.remove(contact.email);
        }

        ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        DataSnapshot user = dataSnapshot.getChildren().iterator().next();
                        ref.child("user_id").setValue(user.getKey());
                        contact.userId = user.getKey();

                        // Refresh transactions to reload contact data
                        getTransactions();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Error getting user with email: " + contact.email);
                }
            };

        mDatabase.child("users").orderByChild("email").equalTo(contact.email)
                .addValueEventListener(listener);
        mContactUserListener.put(contact.email, listener);
    }


    public void addTransaction(Transaction transaction) {
        DatabaseReference ref = mDatabase.child("transactions").push();
        ref.child("title").setValue(transaction.title);
        ref.child("amount").setValue(transaction.amount);

        String other;
        // Get other id as @uid or cid
        if (transaction.other.userId != null)
            other = "@" + transaction.other.userId;
        else
            other = transaction.other.contactUid;

        if (transaction.byOther) {
            ref.child("by").setValue(other);
            ref.child("to").setValue("@"+mUser.getKey());
        } else {
            ref.child("by").setValue("@"+mUser.getKey());
            ref.child("to").setValue(other);
        }

        // Set transaction to user and other (if exists)
        mUser.child("transactions").child(ref.getKey()).setValue(true);
        if (transaction.other.userId != null)
            mDatabase.child("users").child(transaction.other.userId).child("transactions")
                    .child(ref.getKey()).setValue(true);
    }

    private ValueEventListener mTransactionsListener = null;
    public void getTransactions() {
        // If there's a listener already, remove it
        if (mTransactionsListener != null) {
            mUser.child("transactions").removeEventListener(mTransactionsListener);
            mTransactionListeners = null;
        }

        mTransactionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                    for (DataSnapshot data: dataSnapshot.getChildren())
                        getTransaction(data.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error getting transactions");
            }
        };
        mUser.child("transactions").addValueEventListener(mTransactionsListener);
    }

    private HashMap<String, ValueEventListener> mTransactionListeners = new HashMap<>();
    private HashMap<String, List<Pair<String, ValueEventListener>>> mTransactionUserListeners
            = new HashMap<>();
    public void getTransaction(final String key) {
        if (key == null)
            return;

        // If there's a listener already, remove it
        if (mTransactionListeners.containsKey(key)) {
            ValueEventListener l = mTransactionListeners.get(key);
            mDatabase.child("transactions").child(key).removeEventListener(l);
            mTransactionListeners.remove(key);
        }
        if (mTransactionUserListeners.containsKey(key)) {
            List<Pair<String, ValueEventListener>> l = mTransactionUserListeners.get(key);
            if (l != null) {
                for (Pair<String, ValueEventListener> e: l)
                    mDatabase.child("users").child(e.first).removeEventListener(e.second);
            }
        }

        mTransactionUserListeners.put(key, new ArrayList<Pair<String, ValueEventListener>>());
        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (!dataSnapshot.child("by").exists()
                            || !dataSnapshot.child("to").exists()
                            || !dataSnapshot.child("amount").exists())
                        return;

                    Transaction transaction = new Transaction();

                    if (dataSnapshot.child("title").exists())
                        transaction.title = dataSnapshot.child("title").getValue(String.class);

                    transaction.amount = dataSnapshot.child("amount").getValue(Float.class);

                    String to = dataSnapshot.child("to").getValue(String.class);
                    String by = dataSnapshot.child("by").getValue(String.class);
                    transaction.byOther = to.startsWith("@") && to.substring(1).equals(mUser.getKey());

                    String other = transaction.byOther ? by : to;

                    if (other.startsWith("@")) {
                        String uid = other.substring(1);
                        if (UserContactMap.containsKey(uid))
                            transaction.other = Contacts.get(UserContactMap.get(uid));
                        else
                            setTransactionUser(key, transaction, uid);
                    } else {
                        if (!Contacts.containsKey(other))
                            return;
                        transaction.other = Contacts.get(other);
                    }

                    Transactions.put(key, transaction);

                    if (TransactionsListeners != null)
                        for (DatabaseListener<Void> tl: TransactionsListeners) {
                            tl.handle(null);
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error getting transaction with key: " + key);
            }
        };

        mDatabase.child("transactions").child(key).addValueEventListener(listener);
        mTransactionListeners.put(key, listener);
    }

    public void setTransactionUser(String key, final Transaction transaction, final String userId) {

        ValueEventListener listener = new ValueEventListener() {
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
                            transaction.other = addContact(cursor);
                        }
                        cursor.close();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error getting user with id: " + userId);
            }
        };
        mDatabase.child("users").child(userId).addValueEventListener(listener);

        mTransactionUserListeners.get(key).add(new Pair<>(userId, listener));
    }

    /*
    {
        users: {
            u1: {
                username:  ...
                email: ...
                contacts: {
                    c1: {
                        username: ...
                        email: ...
                        uid: ...
                        cid: ...
                    }
                }

                transactions: {
                    t1: true
                    t2: true
                }
            }
        }

        transactions: {
            t1: {
                title: ...
                remarks: ...
                amount: ...
                by: @uid/cn
                to: @uid/cn
            }
        }
	}
    */

}
