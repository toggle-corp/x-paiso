package com.togglecorp.paiso;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.PeopleViewHolder> {
    private List<People> mPeoples;

    PeopleAdapter(){
        mPeoples = new ArrayList<>();
        mPeoples.add(new People("Ankit Mehta", "fren.ankit@gmail.com"));
        mPeoples.add(new People("Frozen Helium", "frozenhelium@togglecorp.com"));
        mPeoples.add(new People("Absolute Zero", "absolutezero@togglecorp.com"));
    }

    @Override
    public int getItemCount() {
        return mPeoples.size();
    }

    @Override
    public PeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PeopleViewHolder(LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.layout_people, parent, false));
    }

    @Override
    public void onBindViewHolder(PeopleViewHolder holder, int position) {
        People people = mPeoples.get(position);
        holder.name.setText(people.name);
        holder.extra.setText(people.extra);
    }

    public class PeopleViewHolder extends RecyclerView.ViewHolder{
        protected TextView name;
        protected TextView extra;
        PeopleViewHolder(View v){
            super(v);
            name = (TextView)v.findViewById(R.id.name);
            extra = (TextView)v.findViewById(R.id.extra);
        }
    }
}
