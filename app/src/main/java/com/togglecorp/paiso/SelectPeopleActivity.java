package com.togglecorp.paiso;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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

        // Initialize the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        setTitle("Select people");

        // Initialize user and database.
        mUser = new User(this);
        if (mUser.getUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mDatabase = new Database(mUser, this);

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

        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdapter.getSelection() == null) {
                    Toast.makeText(SelectPeopleActivity.this, "You haven't selected any contact",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(SelectPeopleActivity.this, AddTransactionActivity.class);
                intent.putExtra("contact", mAdapter.getSelection());
                startActivity(intent);
                finish();
            }
        });

        // Initialize the recycler view
        mAdapter = new UserListAdapter(this);
        RecyclerView contactsList = (RecyclerView)findViewById(R.id.contacts_list);
        contactsList.setLayoutManager(new LinearLayoutManager(this));
        contactsList.setAdapter(mAdapter);

        // Get contacts
        Database.ContactsListeners.add(new DatabaseListener<Void>() {
            @Override
            public void handle(Void data) {
                if (mAdapter != null)
                    mAdapter.refresh();
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
        mDatabase.addContact(cursor);
    }

    public void addContact(String username) {
        if (username.isEmpty())
            return;
        Contact contact = new Contact();
        contact.username = username;
        mDatabase.addContact(contact);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

}
