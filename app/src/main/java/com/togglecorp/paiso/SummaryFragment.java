package com.togglecorp.paiso;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fhx on 9/20/16.
 */
public class SummaryFragment extends Fragment{
    private static final String TAG = "Summary Fragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_summary, container, false);
        return root;
    }
}
