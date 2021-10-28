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

    List<Msg> msgList = new ArrayList<Msg>();
    int page = 1;
    int chatroomId = 0;
    int totalPage = 1;
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

        GetMsgViaHttp getMsgViaHttp = new GetMsgViaHttp(page, chatroomId, msgList, this);
        getMsgViaHttp.execute();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(totalPage > page) {
            ListView msgListView = (ListView) findViewById(R.id.message_list);
            msgListView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScroll(AbsListView view, int first, int visible, int total) {
                    // Your code here
                    page++;
                    ChatActivity.GetMsgViaHttp getMsgViaHttp = new ChatActivity
                            .GetMsgViaHttp(page, chatroomId, msgList, 1);
                    getMsgViaHttp.execute();
                }

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }
            });
        }
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
        GetMsgViaHttp getMsgViaHttp = new GetMsgViaHttp(page, chatroomId, msgList, this);
        getMsgViaHttp.execute();
    }

    private class GetMsgViaHttp extends AsyncTask<String, Void, List<Msg>> {

        private int msgPage;
        private int chatroomId;
        private List<Msg> msgListAsny;
        private ChatActivity chatActivity;
        private int scrollFlag = 0;

        public GetMsgViaHttp(int msgPage, int chatroomId, List<Msg> msgList, ChatActivity chatActivity) {
            this.msgPage = msgPage;
            this.chatroomId = chatroomId;
            this.msgListAsny = msgList;
            this.chatActivity = chatActivity;
            this.scrollFlag = 0;
        }

        public GetMsgViaHttp(int msgPage, int chatroomId, List<Msg> msgList, int i) {
            this.msgPage = msgPage;
            this.chatroomId = chatroomId;
            this.msgListAsny = msgList;
            this.scrollFlag = i;
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
                        totalPage = totalPages;
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
                        chatActivity.msgList = msgListAsny;
                        return msgListAsny;
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
        protected void onPostExecute(List<Msg> msgListAsny) {
            if(scrollFlag == 2) {
                msgListAsny.clear();
            }
            if(scrollFlag != 2) {
                super.onPostExecute(msgListAsny);
                ListView msgListView = (ListView) findViewById(R.id.message_list);
                List<Msg> allMsgs = new ArrayList<Msg>();
                if (scrollFlag == 1) {
                    allMsgs.addAll(msgListAsny);
                    allMsgs.addAll(chatActivity.msgList);
                }
                else
                    allMsgs.addAll(msgListAsny);
                MsgAdapter msgAdapter = new MsgAdapter(ChatActivity.this, R.layout.msg_item, allMsgs);
                msgListView.setAdapter(msgAdapter);
            }
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
                Response response = okHttpClient.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}