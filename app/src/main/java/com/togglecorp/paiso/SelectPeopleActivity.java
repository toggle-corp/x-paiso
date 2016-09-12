package com.togglecorp.paiso;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class SelectPeopleActivity extends AppCompatActivity {
    private static final int PICK_CONTACT_REQUEST = 9002;

    private User mUser;
    private Database mDatabase;

    private UserListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_people);

        // Initialize user and database.
        mUser = new User(this);
        if (mUser.getUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mDatabase = new Database(mUser);

        // Initialize buttons
        findViewById(R.id.add_contact_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickContactIntent =
                        new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE);
                startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
            }
        });

        findViewById(R.id.add_user_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText username = (EditText)findViewById(R.id.new_username);
                addContact(username.getText().toString());
                username.setText("");
            }
        });

        // Initialize the recycler view
        mAdapter = new UserListAdapter(this);
        RecyclerView contactsList = (RecyclerView)findViewById(R.id.contacts_list);
        contactsList.setLayoutManager(new LinearLayoutManager(this));
        contactsList.setAdapter(mAdapter);

        // Get contacts
        mDatabase.getContacts(new DatabaseListener<HashMap<String, Contact>>() {
            @Override
            public void handle(HashMap<String, Contact> data) {
                mAdapter.setContacts(data);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == PICK_CONTACT_REQUEST) {
            // Contact picked from pick contact intent
            if (resultCode == RESULT_OK) {
                Uri pickedData = intent.getData();

                Cursor cursor = getContentResolver().query(pickedData, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    addContact(cursor);
                    cursor.close();
                }
            }
        }
    }

    private void addContact(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        String username =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        Uri photoUrl = Uri.parse(
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_URI))
        );


        // TODO: Add only if id is not already added

        Contact contact = new Contact();
        contact.user_id = id;
        contact.username = username;
        contact.photo_uri = photoUrl;
        mDatabase.addContact(contact);
    }

    public void addContact(String username) {
        Contact contact = new Contact();
        contact.username = username;
        mDatabase.addContact(contact);
    }

}
