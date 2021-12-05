package hk.edu.cuhk.ie.iems5722.a2_1155161159;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hk.ie.iems5722.a2_1155161159.R;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //List<Chatroom> adapterData = new ArrayList<Chatroom>();
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setTitle("IEMS5722");

        //Firebase
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        // Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        //AsyncTask
        String url = "http://34.92.209.154/api/a3/get_chatrooms";
        GetChatroomsViaHttp getChatroomsViaHttp = new GetChatroomsViaHttp();
        getChatroomsViaHttp.execute(url);

        ListView chatroomList = (ListView) findViewById(R.id.chatroomList);
        chatroomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 通过getAdapter()方法取得MyAdapter对象，再调用getItem(int)返回一个Data对象
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
                        String arrayString = json.getString("data");
                        JSONArray array = JSONArray.parseArray(arrayString);
                        for (int i = 0; i < array.size(); i++) {
                            String chatroomName = array.getJSONObject(i).getObject("name", String.class);
                            int chatroomId = array.getJSONObject(i).getObject("id", Integer.class);
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