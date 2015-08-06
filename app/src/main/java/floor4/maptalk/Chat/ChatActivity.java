package floor4.maptalk.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import floor4.maptalk.Lobby.LobbyActivity;
import floor4.maptalk.MSG.MSGRequestChatInfo;
import floor4.maptalk.MSG.MSGRequestSendChat;
import floor4.maptalk.MSG.MSGResponseChatInfo;
import floor4.maptalk.Map.MapActivity;
import floor4.maptalk.Network.Msg_Request_Thread;
import floor4.maptalk.Network.Network_Resource;
import floor4.maptalk.Network.Request_and_Get;
import floor4.maptalk.R;

/**
 * Created by 정우 on 2015-07-25.
 */
public class ChatActivity extends ActionBarActivity{
    private Gson gson = new Gson();
    private int chatNumber = 0;
    private ListView listView;
    private ChatListAdapter adapter;
    private Button sendButton;
    private String text, id;
    private TextView title_view;
    private requestChatInfo_Thread thread;
    private ChatMassgeHandler handler = new ChatMassgeHandler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        listView = (ListView)findViewById(R.id.chatlistview);
        adapter = new ChatListAdapter(this);
        listView.setAdapter(adapter);
        sendButton = (Button)findViewById(R.id.send_button);
        title_view = (TextView)findViewById(R.id.chat_title);
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("title");
        title_view.setText(title); // 채팅 타이틀 설정
        //id = Network_Resource.id;   // id 설정
        Log.e("chat", "" + title);
        sendButton.setOnClickListener(new View.OnClickListener() { // 메시지 전송 요청
            public void onClick(View v) {
                text = ((EditText) findViewById(R.id.chat_edit)).getText().toString();
                if (text.length() == 0) return; // 채팅 내용이 없을 시 그냥 함수 리턴

                MSGRequestSendChat rq = new MSGRequestSendChat(Network_Resource.key, text); // 메시지 요청 생성

                (new Msg_Request_Thread("MSGRequestSendChat", handler)).execute(rq);  // 메시지 요청 보내기
                ((EditText) findViewById(R.id.chat_edit)).setText("");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        thread.setRunningState(false); // 스레드 종료
    }

    protected void onResume() {
        super.onResume();
        thread = new requestChatInfo_Thread();
        thread.setRunningState(true);
        thread.start(); // 채팅 리스트 요청 스레드 시작.
    }

    public class ChatMassgeHandler extends Handler {  // 메시지 핸들러

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case Network_Resource.MSGResponseChatInfo:   // 메시지 전송 요청에 대한 응답
                    MSGResponseChatInfo ret = (MSGResponseChatInfo)msg.obj;
                    if(ret == null || ret.result != 1){
                        Toast.makeText(getApplicationContext(),
                                "메시지 수신이 실패하였습니다.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(ret.result == -1){
                        Toast.makeText(getApplicationContext(),
                                "다른 곳에서 로그인하여 현재 계정이 로그아웃 되었습니다.",
                                Toast.LENGTH_LONG).show();
                        MapActivity.MapClass.finish();
                        LobbyActivity.LobbyClass.finish();
                        finish();
                    }
                    for(int i = 0; i < ret.chatList.length; i++) {
                        ChatItem item = ret.chatList[i];
                        chatNumber = item.getNumber();  // number 최신화
                        adapter.addItem(new ChatItem(item.getId(), item.getContent())); // 추가
                        adapter.notifyDataSetChanged();
                    }
                    break;

                case Network_Resource.MSGResponseSendChat:
                    break;

                default:
                    break;
            }
        }
    }

    public class requestChatInfo_Thread extends Thread{
        MSGRequestChatInfo send_object;
        MSGResponseChatInfo result_object;
        String URL = Network_Resource.url + "MSGRequestChatInfo";
        Message msg = new Message();
        private boolean is_running = true;
        public void setRunningState(boolean flag){
            is_running = flag;
        }
        public void run() {
            while(is_running) {
                send_object = new MSGRequestChatInfo(Network_Resource.key, chatNumber); // 채팅 리스트 요청 메시지 생성

                String stringJson = gson.toJson(send_object);
                Request_and_Get rq = new Request_and_Get(stringJson, URL); // obj 객체를 서버에 보낸다.
                String result = rq.getResult(); // 결과를 result에 저장한다. (JsonString 상태이다)

                if (result != null)
                    result_object = gson.fromJson(result, MSGResponseChatInfo.class);
                else{
                    Toast.makeText(getApplicationContext(),
                            "네트워크 연결을 확인하십시오.",
                            Toast.LENGTH_LONG).show();
                }
                msg = handler.obtainMessage();
                msg.what = Network_Resource.MSGResponseChatInfo;
                msg.obj = result_object;

                if(handler != null && msg != null)
                    handler.sendMessage(msg);

                try {
                    sleep(500); // 채팅 리스트 요청 간격
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
