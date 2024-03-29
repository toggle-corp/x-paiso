package com.togglecorp.paiso;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.List;

public class AddTransactionActivity extends AppCompatActivity {

    private Database mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        final String selectedContact = getIntent().getExtras().getString("contact");

        // Initialize the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        setTitle("Add transaction");

        // Initialize user and database.
        User user = new User(this);
        if (user.getUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mDatabase = new Database(user, this);

        // Set the done handler
        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Transaction transaction = new Transaction();
                transaction.title = ((EditText)findViewById(R.id.title)).getText().toString();

                transaction.amount = Float.parseFloat(((EditText)findViewById(R.id.amount))
                        .getText().toString());

                transaction.byOther = !((ToggleButton)findViewById(R.id.debt_by)).isChecked();

                transaction.other = Database.Contacts.get(selectedContact);

                mDatabase.addTransaction(transaction);
                finish();
            }
        });

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
