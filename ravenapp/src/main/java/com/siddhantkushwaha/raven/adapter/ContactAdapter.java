package com.siddhantkushwaha.raven.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.siddhantkushwaha.raven.R;
import com.siddhantkushwaha.raven.realm.entity.RavenUser;
import com.siddhantkushwaha.raven.utility.GlideUtilV2;

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
        if (contact.picUrl != null)
            GlideUtilV2.loadProfilePhotoCircle(context, imageView, contact.picUrl);
        else
            GlideUtilV2.loadProfilePhotoCircle(context, imageView, null);

        TextView name = listItem.findViewById(R.id.nameTextView);
        if (contact.contactName != null)
            name.setText(contact.contactName);
        else if (contact.displayName != null)
            name.setText(contact.displayName);
        else if (contact.phoneNumber != null)
            name.setText(contact.phoneNumber);
        else
            name.setText(R.string.default_name);

        TextView about = listItem.findViewById(R.id.aboutTextView);
        if (contact.about != null)
            about.setText(contact.about);
        else
            about.setText(R.string.default_about);

        return listItem;
    }
}
