package cuhk.edu.hk.iechatroom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;


public class ChatroomAdapter extends ArrayAdapter<Chatroom> {

    public int getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(int chatroomId) {
        this.chatroomId = chatroomId;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public void setChatroomName(String chatroomName) {
        this.chatroomName = chatroomName;
    }

    private int resourceId;
    private int chatroomId;
    private String chatroomName;
    public ChatroomAdapter(Context context, int resource, List<Chatroom> objects) {
        super(context, resource, objects);
        resourceId = resource;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Chatroom chatroom = getItem(position);
        View view;
        ChatroomAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ChatroomAdapter.ViewHolder();
            viewHolder.chatroomName = view.findViewById(R.id.chatroomName);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ChatroomAdapter.ViewHolder) view.getTag();
        }
        viewHolder.chatroomName.setText(chatroom.getChatroomName());
        return view;
    }

    class ViewHolder {
        TextView chatroomName;
    }

}
