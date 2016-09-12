package com.togglecorp.paiso;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private Context mContext;
    private HashMap<String, Contact> mContacts;
    private List<String> mContactKeys;

    public UserListAdapter(Context context) {
        mContext = context;
        setContacts(new HashMap<String, Contact>());
    }

    public void setContacts(HashMap<String, Contact> contacts) {
        mContacts = contacts;
        notifyDataSetChanged();

        mContactKeys = new ArrayList<>();
        if (mContacts.size() > 0) {
            for (String c : mContacts.keySet()) {
                mContactKeys.add(c);
            }
            Collections.sort(mContactKeys, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    if (mContacts.get(s1).recent == null)
                        return 1;
                    if (mContacts.get(s2).recent == null)
                        return -1;
                    return mContacts.get(s1).recent.compareTo(mContacts.get(s2).recent);
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.layout_listitem_people, parent, false
        ));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact contact = mContacts.get(mContactKeys.get(position));
        holder.name.setText(contact.username);
        if (contact.photo_uri != null) {
            holder.avatar.setImageURI(contact.photo_uri);
        } else {
            holder.avatar.setImageDrawable(mContext.getDrawable(R.mipmap.ic_avatar));
        }
    }

    @Override
    public int getItemCount() {
        return mContactKeys.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView extra;
        public CircleImageView avatar;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.name);
            extra = (TextView)itemView.findViewById(R.id.extra);
            avatar = (CircleImageView)itemView.findViewById(R.id.avatar);
        }
    }
}
