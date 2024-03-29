package com.togglecorp.paiso;

import android.app.Fragment;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";

    private User mUser;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private static FirebaseDatabase mFDb;

    private TransactionsFragment mTransactionsFragment = new TransactionsFragment();
    private PeoplesFragment mPeoplesFragment = new PeoplesFragment();
    private SummaryFragment mSummaryFragment = new SummaryFragment();

    private FloatingActionButton mAddButton;
    private boolean mToggleAddButton = false;

    public User getUser() {
        return mUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mFDb == null) {
            mFDb = FirebaseDatabase.getInstance();
            mFDb.setPersistenceEnabled(true);
        }

        // Get logged in user or start Login Activity
        mUser = new User(this);
        if (mUser.getUser() == null) {
            // Not signed in, launch the Log In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        final Database database = new Database(mUser, this);
        Database.ContactsListeners.add(new DatabaseListener<Void>() {
            @Override
            public void handle(Void data) {
                if (database != null)
                    database.getTransactions();
            }
        });
        database.getContacts();

        // Initialize the nav drawer
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        mNavigationView = (NavigationView)findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_content, mSummaryFragment)
                .commit();
        mNavigationView.setCheckedItem(R.id.nav_summary);


        mAddButton = (FloatingActionButton)findViewById(R.id.add_button);

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
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // The hamburger icon
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // Add button
        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SelectPeopleActivity.class));
            }
        });

    }


    private NavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch(item.getItemId()){
                case R.id.nav_transactions:
                    fragment = mTransactionsFragment;
                    mToggleAddButton = true;
                    break;
                case R.id.nav_peoples:
                    fragment = mPeoplesFragment;
                    mToggleAddButton = true;
                    break;
                case R.id.nav_summary:
                    fragment = mSummaryFragment;
                    mToggleAddButton = true;
            }
            if(fragment != null){
                mAddButton.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        if(mToggleAddButton){
                            fab.show();
                        }

                    }
                });

                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_content, fragment)
                        .commit();
                item.setChecked(true);

            }
            mDrawerLayout.closeDrawers();
            return true;
        }
    };

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
