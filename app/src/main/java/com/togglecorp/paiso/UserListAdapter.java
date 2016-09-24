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
    private List<String> mContactKeys;
    private String mSelection = null;

    public UserListAdapter(Context context) {
        mContext = context;
        refresh();
    }

    public String getSelection() {
        return mSelection;
    }

    public void refresh() {
        HashMap<String, Contact> contacts = Database.Contacts;
        notifyDataSetChanged();

        mContactKeys = new ArrayList<>();
        if (contacts.size() > 0) {
            for (String c : contacts.keySet()) {
                mContactKeys.add(c);
            }
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
        Contact contact = Database.Contacts.get(mContactKeys.get(position));
        holder.name.setText(contact.username);
        if (contact.photo_uri != null) {
            holder.avatar.setImageURI(contact.photo_uri);
        } else {
            holder.avatar.setImageDrawable(mContext.getDrawable(R.drawable.ic_avatar));
        }

        holder.root.setPressed(mSelection != null &&
                mSelection.equals(mContactKeys.get(position)));
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
                    mSelection = mContactKeys.get(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });
        }
    }
}
