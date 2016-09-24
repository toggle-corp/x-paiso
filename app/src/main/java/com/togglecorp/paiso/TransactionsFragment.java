package com.togglecorp.paiso;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment  {
    private static final String TAG = "Transactions Fragment";

    private TransactionAdapter mAdapter;
    private List<Transaction> mTransactions = new ArrayList<>();

    private Database mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transactions, parent, false);

        mAdapter = new TransactionAdapter(mTransactions);
        RecyclerView recyclerView = (RecyclerView)root.findViewById(R.id.transactions_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        // Initialize the database
        final User user = ((MainActivity)getActivity()).getUser();
        mDatabase = new Database(user, getActivity());

        updateTransactions();
        Database.TransactionsListeners.add(new DatabaseListener<Void>() {
            @Override
            public void handle(Void data) {
                if (mAdapter != null)
                    updateTransactions();
            }
        });

        return root;
    }

    private void updateTransactions() {
        // TODO: sort transactions by date

        mTransactions.clear();

        for (String t_key: Database.Transactions.keySet()) {
            Transaction t = Database.Transactions.get(t_key);
            mTransactions.add(t);
        }

        mAdapter.setTransactions(mTransactions);
    }
}
