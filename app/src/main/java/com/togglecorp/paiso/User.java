package com.togglecorp.paiso;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class User implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "User Class";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private static GoogleApiClient mGoogleApiClient;

    public User(FragmentActivity activity) {
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            return;
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .enableAutoManage(activity, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    public FirebaseUser getUser() {
        return mFirebaseUser;
    }

    public void logout() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        mFirebaseUser = null;
    }
}
