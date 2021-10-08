package cuhk.edu.hk.iechatroom;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChatActivity extends AppCompatActivity {

    List<Msg> adapterData = new ArrayList<Msg>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setTitle("Chatroom");

    }

    public void send_msg(View view) {

        EditText text = (EditText) findViewById(R.id.MessageText);
        String textString = text.getText().toString();
        if (textString.isEmpty() || textString.trim().length() == 0)
            return;
        text.setText("");

        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("HH:mm");
        Date date = new Date();

        ListView myListView = (ListView) findViewById(R.id.list);
        Msg msg = new Msg(textString, 1, sdf.format(date));
        adapterData.add(msg);
        MsgAdapter msgAdapter = new MsgAdapter(ChatActivity.this, R.layout.msg_item, adapterData);
        myListView.setAdapter(msgAdapter);

    }
}