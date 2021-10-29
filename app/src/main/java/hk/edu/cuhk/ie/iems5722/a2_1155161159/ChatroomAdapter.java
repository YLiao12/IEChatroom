package hk.edu.cuhk.ie.iems5722.a2_1155161159;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hk.ie.iems5722.a2_1155161159.R;


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
