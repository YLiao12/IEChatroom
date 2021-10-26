package cuhk.edu.hk.iechatroom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //List<Chatroom> adapterData = new ArrayList<Chatroom>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setTitle("IEMS5722");

        //AsyncTask
        String url = "http://18.217.125.61/api/a3/get_chatrooms";
        GetChatroomsViaHttp getChatroomsViaHttp = new GetChatroomsViaHttp();
        getChatroomsViaHttp.execute(url);

        ListView chatroomList = (ListView) findViewById(R.id.chatroomList);
        chatroomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 通过getAdapter()方法取得MyAdapter对象，再调用getItem(int)返回一个Data对象
                //String button = (String) chatroomList.getAdapter().getItem(i);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                Chatroom chatroom = (Chatroom) chatroomList.getAdapter().getItem(i);
                intent.putExtra("chatroomName", chatroom.getChatroomName());
                intent.putExtra("chatroomId", chatroom.getChatroomId());
                startActivity(intent);
            }
        });
    }

    public class GetChatroomsViaHttp extends AsyncTask<String, String, List<Chatroom>> {

        List<Chatroom> adapterData = new ArrayList<Chatroom>();

        public GetChatroomsViaHttp() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Chatroom> doInBackground(String... url) {
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder().url(url[0]).build();
            Call call = client.newCall(request);
            List<Chatroom> chatroomList = new ArrayList<>();
            try {
                Response response=call.execute();
                if (response.code() == 200) {
                    String data = response.body().string();
                    publishProgress(data);
                    JSONObject json = null;
                    try {
                        json = new JSONObject(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONArray array = json.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            String chatroomName = array.getJSONObject(i).getString("name");
                            int chatroomId = array.getJSONObject(i).getInt("id");
                            Chatroom newChatroom = new Chatroom(chatroomId, chatroomName);
                            chatroomList.add(newChatroom);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return chatroomList;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... data) {

        }

        @Override
        protected void onPostExecute(List<Chatroom> result) {
            super.onPostExecute(result);
            for (int i = 0; i < result.size(); i++) {
                adapterData.add(result.get(i));

            }
            ListView chatroomList = (ListView) findViewById(R.id.chatroomList);
            ChatroomAdapter chatroomAdapter = new ChatroomAdapter(MainActivity.this, R.layout.chatroom_item, adapterData);
            chatroomList.setAdapter(chatroomAdapter);

        }
    }
}