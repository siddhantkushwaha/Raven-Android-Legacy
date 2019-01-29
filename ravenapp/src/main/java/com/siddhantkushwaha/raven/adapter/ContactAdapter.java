package com.siddhantkushwaha.raven.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.siddhantkushwaha.raven.R;
import com.siddhantkushwaha.raven.utility.GlideUtilV2;
import com.siddhantkushwaha.raven.localEntity.RavenUser;

import androidx.annotation.Nullable;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

public class ContactAdapter extends RealmBaseAdapter<RavenUser> {

    private Context context;
    private OrderedRealmCollection<RavenUser> data;

    public ContactAdapter(Context context, @Nullable OrderedRealmCollection<RavenUser> data) {
        super(data);
        this.context = context;
        this.data = data;
    }

    @Nullable
    @Override
    public RavenUser getItem(int position) {
        return data.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.layout_user_profile, parent, false);

        RavenUser contact = data.get(position);

        ImageView imageView = listItem.findViewById(R.id.displayPicImageView);
        if (contact.getPicUrl() != null)
            GlideUtilV2.loadProfilePhotoCircle(context, imageView, contact.getPicUrl());
        else
            GlideUtilV2.loadProfilePhotoCircle(context, imageView, null);

        TextView name = listItem.findViewById(R.id.nameTextView);
        if(contact.getContactName() != null)
            name.setText(contact.getContactName());
        else if (contact.getDisplayName() != null)
            name.setText(contact.getDisplayName());
        else if(contact.getPhoneNumber() != null)
            name.setText(contact.getPhoneNumber());
        else
            name.setText(R.string.default_name);

        TextView about = listItem.findViewById(R.id.aboutTextView);
        if (contact.getAbout() != null)
            about.setText(contact.getAbout());
        else
            about.setText(R.string.default_about);

        return listItem;
    }
}
