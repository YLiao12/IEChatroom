package cuhk.edu.hk.iechatroom;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private List<Msg> msgList = new ArrayList<Msg>();
    private int page = 1;
    private int chatroomId = 0;
    private int totalPage = 1;
    private int firstItem;
    private int statusCode = 0;

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

        GetMsgViaHttp getMsgViaHttp = new GetMsgViaHttp(page, chatroomId);
        getMsgViaHttp.execute();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ListView msgListView = (ListView) findViewById(R.id.message_list);
        msgListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int first, int visible, int total) {
                // Your code here
                firstItem = first;
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                statusCode = scrollState;
                if (statusCode != 0 && firstItem == 0) {
                    if (totalPage > page) {

                        page++;
                        ChatActivity.GetMsgViaHttp getMsgViaHttp = new ChatActivity
                                .GetMsgViaHttp(page, chatroomId);
                        getMsgViaHttp.execute();


                    }

                }
            }
        });
    }

    public void send_msg(View view) {

        EditText text = (EditText) findViewById(R.id.MessageText);
        String msgString = text.getText().toString();
        StringBuffer messageBuffer = new StringBuffer();
        messageBuffer.append("User: Leo");
        messageBuffer.append("\r\n");
        messageBuffer.append(msgString);
        if (msgString.isEmpty() || msgString.trim().length() == 0)
            return;
        text.setText("");

        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm");
        Date date = new Date();

        ListView myListView = (ListView) findViewById(R.id.message_list);
        Msg msg = new Msg(messageBuffer.toString(), 1, sdf.format(date));
        msgList.add(msg);
        MsgAdapter msgAdapter = new MsgAdapter(ChatActivity.this, R.layout.msg_item, msgList);
        myListView.setAdapter(msgAdapter);

        PostMsgToServer post = new PostMsgToServer(chatroomId, "Leo", 1155161159, msgString);
        post.execute();

    }

    public void refresh_msg(View view) {
        ListView myListView = (ListView) findViewById(R.id.message_list);
        List<Msg> msgList = new ArrayList<>();
        MsgAdapter msgAdapter = new MsgAdapter(ChatActivity.this, R.layout.msg_item, msgList);
        myListView.setAdapter(msgAdapter);
        GetMsgViaHttp getMsgViaHttp = new GetMsgViaHttp(1, chatroomId);
        getMsgViaHttp.execute();
    }

    private class GetMsgViaHttp extends AsyncTask<String, Void, Integer> {

        private int msgPage;
        private int chatroomId;

        public GetMsgViaHttp(int msgPage, int chatroomId) {
            this.msgPage = msgPage;
            this.chatroomId = chatroomId;
        }

        @Override
        protected Integer doInBackground(String... strings) {
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
                        totalPage = totalPages;
                        List<Msg> msgListAsny = new ArrayList<>();
                        if(status.equals("OK") && totalPages >= page) {
                            for (int i = 0; i < messageArray.length(); i++) {
                                String msg = messageArray.getJSONObject(i).getString("message");
                                String user = messageArray.getJSONObject(i).getString("name");
                                String time = messageArray.getJSONObject(i).getString("message_time");
                                String id = messageArray.getJSONObject(i).getString("user_id");
                                int type = Msg.TYPE_RECEIVED;
                                if (Integer.parseInt(id) == 1155161159)
                                    type = Msg.TYPE_SENT;
                                StringBuffer messageBuffer = new StringBuffer();
                                messageBuffer.append("User: ");
                                messageBuffer.append(user);
                                messageBuffer.append("\r\n");
                                messageBuffer.append(msg);
                                Msg newMsg = new Msg(messageBuffer.toString(), type, time);
                                msgListAsny.add(newMsg);
                            }
                        }
                        Collections.reverse(msgListAsny);
                        List<Msg> updateMsgList = new ArrayList<Msg>();
                        updateMsgList.addAll(msgListAsny);
                        updateMsgList.addAll(msgList);
                        msgList = updateMsgList;
                        return msgListAsny.size();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                return 0;
            }
            return 0;
        }


        @Override
        protected void onPostExecute(Integer position) {
            super.onPostExecute(position);
            ListView msgListView = (ListView) findViewById(R.id.message_list);
            //int fv = msgListView.getFirstVisiblePosition();
            MsgAdapter msgAdapter = new MsgAdapter(ChatActivity.this, R.layout.msg_item, msgList);

            msgListView.setAdapter(msgAdapter);
            msgListView.setSelection(position);
        }
    }

    private class PostMsgToServer extends AsyncTask<String, Void, Void> {

        private int chatroomId;
        private String userName;
        private int userId;
        private String msg;

        public PostMsgToServer(int chatroomId, String userName, int userId, String msg) {
            this.chatroomId = chatroomId;
            this.userName = userName;
            this.userId = userId;
            this.msg = msg;
        }

        @Override
        protected Void doInBackground(String... strings) {

            FormBody.Builder builder = new FormBody.Builder()
                    .add("chatroom_id", String.valueOf(chatroomId))
                    .add("user_id", String.valueOf(userId))
                    .add("name", userName)
                    .add("message", msg);
            RequestBody formBody=builder.build();
            OkHttpClient okHttpClient=new OkHttpClient();
            Request request=new Request.Builder().url("http://18.217.125.61/api/a3/send_message").post(formBody).build();
            try {
                okHttpClient.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}