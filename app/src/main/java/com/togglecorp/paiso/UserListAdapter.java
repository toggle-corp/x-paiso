package com.togglecorp.paiso;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private List<String> mSelections = new ArrayList<>();

    public UserListAdapter(Context context) {
        mContext = context;
        setContacts(new HashMap<String, Contact>());
    }

    public HashMap<String, Contact> getContacts() {
        return mContacts;
    }

    public List<String> getSelections() {
        return mSelections;
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
            holder.avatar.setImageDrawable(mContext.getDrawable(R.drawable.ic_avatar));
        }

        holder.root.setPressed(mSelections.contains(mContactKeys.get(position)));
    }

    @Override
    public int getItemCount() {
        return mContactKeys.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View root;
        public TextView name;
        public TextView extra;
        public CircleImageView avatar;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            name = (TextView)itemView.findViewById(R.id.name);
            extra = (TextView)itemView.findViewById(R.id.extra);
            avatar = (CircleImageView)itemView.findViewById(R.id.avatar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String cid = mContactKeys.get(getAdapterPosition());
                    if (mSelections.contains(cid))
                        mSelections.remove(cid);
                    else
                        mSelections.add(cid);
                    notifyDataSetChanged();
                }
            });
        }
    }
}
