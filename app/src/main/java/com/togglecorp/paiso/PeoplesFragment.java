package com.togglecorp.paiso;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fhx on 9/19/16.
 */
public class PeoplesFragment extends Fragment {
    private static final String TAG = "Peoples Fragment";

    private PeopleAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_peoples, container, false);

        mAdapter = new PeopleAdapter();
        RecyclerView recyclerView = (RecyclerView)root.findViewById(R.id.people_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        return root;
    }
}
