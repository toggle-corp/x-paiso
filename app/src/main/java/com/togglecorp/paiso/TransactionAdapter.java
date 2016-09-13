package com.togglecorp.paiso;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int ITEM_GROUP_LABEL = 0;
    private static final int ITEM_TITLE = 1;
    private static final int ITEM_DEBT = 2;

    private List<Item> mItemMap;
    private List<Transaction> mTransactions;

    public class Item{
        public int type;
        public int position;
        public int offset;

        Item(int type, int position, int offset){
            this.type = type;
            this.position = position;
            this.offset = offset;
        }
    }

    public TransactionAdapter(List<Transaction> transactions){
        mTransactions = transactions;
        mItemMap = new ArrayList<>();
        mapItems();
    }

    public void setTransactions(List<Transaction> transactions){
        mTransactions = transactions;
        mapItems();
        notifyDataSetChanged();
    }

    private void mapItems(){
        mItemMap.clear();
        
        // TODO: implement logic for grouping
        for(int n=0; n < mTransactions.size(); n++){
            mItemMap.add(new Item(ITEM_TITLE, n, -1));
            for(int m=0; m<mTransactions.get(n).debts.size(); m++){
                mItemMap.add(new Item(ITEM_DEBT, n, m));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItemMap.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItemMap.get(position).type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        switch (viewType){
            case ITEM_GROUP_LABEL:
                holder = new TransactionTitleViewHolder(LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.layout_transaction_group_label, parent, false));
                break;
            case ITEM_TITLE:
                holder = new TransactionTitleViewHolder(LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.layout_transaction_title, parent, false));
                break;
            case ITEM_DEBT:
                holder = new TransactionDebtViewHolder(LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.layout_transaction_debt, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Item currentItem = mItemMap.get(position);
        switch(currentItem.type){
            case ITEM_GROUP_LABEL:
                ((TransactionGroupLabelViewHolder)holder).label.setText("26th December, 1992");
                break;
            case ITEM_TITLE:
                ((TransactionTitleViewHolder)holder).title.setText(mTransactions.get(currentItem.position).title);
                break;
            case ITEM_DEBT:
                TransactionDebtViewHolder viewHolder = (TransactionDebtViewHolder)holder;
                Debt currentDebt = mTransactions.get(currentItem.position).debts.get(currentItem.offset);

                if (currentDebt.by.equals("@me"))
                    viewHolder.debtTo.setText("To " + currentDebt.to);
                else
                    viewHolder.debtTo.setText("By " + currentDebt.by);

                viewHolder.unit.setText(currentDebt.unit);
                viewHolder.amount.setText(String.valueOf(currentDebt.amount));
                break;
        }
    }

    public class TransactionGroupLabelViewHolder extends RecyclerView.ViewHolder{
        protected TextView label;
        TransactionGroupLabelViewHolder(View v){
            super(v);
            label = (TextView)v.findViewById(R.id.transaction_group_label);
        }
    }

    public class TransactionTitleViewHolder extends RecyclerView.ViewHolder{
        protected TextView title;

        TransactionTitleViewHolder(View v){
            super(v);
            title = (TextView)v.findViewById(R.id.transaction_title);
        }
    }

    public class TransactionDebtViewHolder extends RecyclerView.ViewHolder{
        protected TextView debtTo;
        protected TextView unit;
        protected TextView amount;

        TransactionDebtViewHolder(View v){
            super(v);
            debtTo = (TextView)v.findViewById(R.id.debt_to);
            unit = (TextView)v.findViewById(R.id.unit);
            amount = (TextView)v.findViewById(R.id.amount);
        }
    }

}
