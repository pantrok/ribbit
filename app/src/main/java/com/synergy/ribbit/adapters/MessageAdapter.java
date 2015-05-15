package com.synergy.ribbit.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.synergy.ribbit.utils.ParseConstants;
import com.synergy.ribbit.R;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

/**
 * Created by daniel on 26/03/15.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {

    private Context mContext;
    private List<ParseObject> mMessages;


    public MessageAdapter(Context context, List<ParseObject> objects) {
        super(context, R.layout.message_item, objects);
        mContext = context;
        mMessages = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.messageIcon);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.senderLabel);
            holder.timeLabel = (TextView) convertView.findViewById(R.id.timeLabel);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ParseObject message = mMessages.get(position);
        Date createdAt = message.getCreatedAt();
        long now = new Date().getTime();
        String convertDate = DateUtils.getRelativeTimeSpanString(createdAt.getTime(), now, DateUtils.SECOND_IN_MILLIS).toString();
        holder.timeLabel.setText(convertDate);
        if (message.get(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
            holder.iconImageView.setImageResource(R.mipmap.ic_menu_camera);
        } else {
            holder.iconImageView.setImageResource(R.mipmap.ic_video);
        }
        holder.nameLabel.setText(message.getString(ParseConstants.KEY_SENDER_NAME));

        return convertView;
    }

    private static class ViewHolder {
        ImageView iconImageView;
        TextView nameLabel;
        TextView timeLabel;
    }

    public void refill(List<ParseObject> messages) {
        mMessages.clear();
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }

}

