package cuhk.edu.hk.iechatroom;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    List<Msg> msgList = new ArrayList<Msg>();
    int page = 1;
    int chatroomId = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        Bundle extras = getIntent().getExtras();
        String chatroomName = extras.getString("chatroomName");
        chatroomId = extras.getInt("chatroomId");
        getSupportActionBar().setTitle(chatroomName);

        ChatActivity.GetMsgViaHttp getMsgViaHttp = new ChatActivity.GetMsgViaHttp(page, chatroomId, msgList);
        getMsgViaHttp.execute();

//        ListView msgListView = (ListView) findViewById(R.id.list);
//        msgListView.setOnScrollListener(new AbsListView.OnScrollListener() {
//
//            int totalPages;
//            @Override
//            public void onScroll(AbsListView view, int first, int visible, int total) {
//                // Your code here
//                page++;
//                ChatActivity.GetMsgViaHttp getMsgViaHttp = new ChatActivity.GetMsgViaHttp(page, chatroomId, msgList);
//                getMsgViaHttp.execute();
//            }
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//            }
//        });
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
        msgList.add(msg);
        MsgAdapter msgAdapter = new MsgAdapter(ChatActivity.this, R.layout.msg_item, msgList);
        myListView.setAdapter(msgAdapter);

    }

    public class GetMsgViaHttp extends AsyncTask<String, Void, List<Msg>> {

        private int msgPage;
        private int chatroomId;
        private List<Msg> msgList;

        public GetMsgViaHttp(int msgPage, int chatroomId, List<Msg> msgList) {
            this.msgPage = msgPage;
            this.chatroomId = chatroomId;
            this.msgList = msgList;
        }

        @Override
        protected List<Msg> doInBackground(String... strings) {
            String url = "http://18.217.125.61/api/a3/get_messages";
            Request.Builder builder = new Request.Builder();
            Request request = builder.url(url).build();
            HttpUrl.Builder urlBuilder = request.url().newBuilder();
            urlBuilder.addQueryParameter("chatroom_id", String.valueOf(this.chatroomId));
            urlBuilder.addQueryParameter("page", String.valueOf(this.msgPage));
            builder.url(urlBuilder.build());
            try {
                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(builder.build()).execute();
                if (response.code() == 200) {
                    String jsonStringData = response.body().string();
                    publishProgress();
                    JSONObject json = null;
                    try {
                        json = new JSONObject(jsonStringData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONObject messageData = json.getJSONObject("data");
                        JSONArray messageArray = messageData.getJSONArray("messages");
                        String status = json.getString("status");
                        String totalPagesString = messageData.getString("total_pages");
                        int totalPages = Integer.parseInt(totalPagesString);
                        if(status.equals("OK") && totalPages >= page) {
                            for (int i = 0; i < messageArray.length(); i++) {
                                String msg = messageArray.getJSONObject(i).getString("message");
                                String user = messageArray.getJSONObject(i).getString("name");
                                String time = messageArray.getJSONObject(i).getString("message_time");
                                int type = Msg.TYPE_RECEIVED;
                                StringBuffer messageBuffer = new StringBuffer();
                                messageBuffer.append("User: ");
                                messageBuffer.append(user);
                                messageBuffer.append("\r\n");
                                messageBuffer.append(msg);
                                Msg newMsg = new Msg(messageBuffer.toString(), type, time);
                                this.msgList.add(newMsg);
                            }
                        }
                        return msgList;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Msg> msgList) {
            super.onPostExecute(msgList);
            ListView msgListView = (ListView) findViewById(R.id.list);
            MsgAdapter msgAdapter = new MsgAdapter(ChatActivity.this, R.layout.msg_item, msgList);
            msgListView.setAdapter(msgAdapter);
        }
    }
}