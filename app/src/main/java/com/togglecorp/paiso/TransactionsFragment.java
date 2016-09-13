package com.togglecorp.paiso;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransactionsFragment extends Fragment  {
    private static final String TAG = "Transactions Fragment";

    private TransactionAdapter mAdapter;
    private List<Transaction> mTransactions = new ArrayList<>();
    private HashMap<String, Contact> mContacts;

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
        mDatabase = new Database(user);
        mDatabase.getContacts(new DatabaseListener<HashMap<String, Contact>>() {
            @Override
            public void handle(HashMap<String, Contact> data) {
                mContacts = data;
                mContacts.put("@me", new Contact("@me"));
            }
        });
        mDatabase.getTransactions(new DatabaseListener<HashMap<String, Transaction>>() {
            @Override
            public void handle(HashMap<String, Transaction> data) {
                updateTransactions(data);
            }
        });

        return root;
    }

    private void updateTransactions(HashMap<String, Transaction> transactions) {
        // TODO: sort transactions by date

        mTransactions.clear();
        for (String t_key: transactions.keySet()) {
            Transaction t = transactions.get(t_key);
            Transaction t1 = new Transaction();

            t1.title = t.title;
            for (Debt d: t.debts) {
                t1.debts.add(new Debt(
                        mContacts.get(d.by).username,
                        mContacts.get(d.to).username,
                        d.amount,
                        d.unit
                ));
            }

            mTransactions.add(t1);
        }

        mAdapter.setTransactions(mTransactions);
    }
}
