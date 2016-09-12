package com.togglecorp.paiso;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private User mUser;
    private Database mDatabase;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get logged in user or start Login Activity
        mUser = new User(this);
        if (mUser.getUser() == null) {
            // Not signed in, launch the Log In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize the database
        mDatabase = new Database(mUser);

        // Initialize the nav drawer
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        mNavigationView = (NavigationView)findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                }
        );

        // Set the nav drawer header
        View header = mNavigationView.getHeaderView(0);
        ((TextView)header.findViewById(R.id.username)).setText(mUser.getUser().getDisplayName());
        ((TextView)header.findViewById(R.id.email)).setText(mUser.getUser().getEmail());
        Picasso.with(this)
                .load(mUser.getUser().getPhotoUrl())
                .into((CircleImageView)header.findViewById(R.id.user_image));

        // Initialize the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Add button
        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SelectPeopleActivity.class));
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
