package floor4.maptalk.Lobby;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import floor4.maptalk.Chat.ChatActivity;
import floor4.maptalk.Data.RoomInfo;
import floor4.maptalk.MSG.MSGRequestCreateRoom;
import floor4.maptalk.MSG.MSGRequestIntoRoom;
import floor4.maptalk.MSG.MSGRequestRoomList;
import floor4.maptalk.MSG.MSGResponseRoomInfo;
import floor4.maptalk.MSG.MSGResponseRoomList;
import floor4.maptalk.Map.MapActivity;
import floor4.maptalk.Network.Msg_Request_Thread;
import floor4.maptalk.Network.Network_Resource;
import floor4.maptalk.R;


/**
 * Created by Administrator on 2015-06-28.
 */
public class LobbyActivity extends ActionBarActivity{

    public static LobbyActivity LobbyClass;
    public static final int CONFIG_CODE_ROOM_CREATE_OPTION=1002;
    public static final int CONFIG_CODE_ROOM_PASSWORD=1003;
    public LobbyMassgeHandler handler = new LobbyMassgeHandler();
    public LobbyActivity refClass = this;
    private ListView listView;
    private RoomListAdapter adapter;
    private Button createRoomButton;
    private RoomInfo r;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        LobbyClass = this;

        createRoomButton = (Button)findViewById(R.id.addGroup);

        listView = (ListView)findViewById(R.id.listView);

        MSGRequestRoomList rq = new MSGRequestRoomList();
        rq.setKey(Network_Resource.key);
        (new Msg_Request_Thread("MSGRequestRoomList", handler)).execute(rq);

        //방 참가 요청 메시지 처리부분.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                r = (RoomInfo) parent.getAdapter().getItem(position);
                MSGRequestIntoRoom rq = new MSGRequestIntoRoom(Network_Resource.key, r.getRoomnumber(), r.getPassword());
                Log.e("join", "in Listner " + r.getTitle() + " " + r.havePassword());

                if (r.havePassword() == 1) {  // 방의 암호가 존재하면 암호 입력띄우고 결과 받은 뒤 보내줌.
                    String pwd = "";
                    Intent intent = new Intent(getApplicationContext(), RoomPasswordActivity.class);
                    intent.putExtra(RoomInfo.PARCELKEY_DATA_CREATE, pwd);
                    startActivityForResult(intent, CONFIG_CODE_ROOM_PASSWORD);  // 다이얼로그 띄우고 결과 따로 처리
                } else {
                    // 방의 암호가 없으면 바로 참가요청 보냄.
                    //UNDEBUG
                    (new Msg_Request_Thread("MSGRequestIntoRoom", handler)).execute(rq);
                }
            }
        });

        adapter = new RoomListAdapter(this);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //#UNDEBUG

    }

    public void onCreateRoomClicked(View v){
        RoomInfo roomInfo = new RoomInfo(0,"","",0);
        Intent intent = new Intent(getApplicationContext(), RoomCreateOptionActivity.class);
        intent.putExtra(RoomInfo.PARCELKEY_DATA_CREATE, roomInfo);
        startActivityForResult(intent, CONFIG_CODE_ROOM_CREATE_OPTION);
    }

    public void onRefreshClicked(View v){
        MSGRequestRoomList rq = new MSGRequestRoomList();
        rq.setKey(Network_Resource.key);

        //#UNDEBUG
        (new Msg_Request_Thread("MSGRequestRoomList", handler)).execute(rq);
    }

    // 방 만들기 액티비티와 통신.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONFIG_CODE_ROOM_CREATE_OPTION) {  // 방 만들기 다이얼로그 결과 받은 것
            Toast toast = Toast.makeText(getBaseContext(),
                    "okok" + requestCode + "결과코드 : " + resultCode, Toast.LENGTH_LONG);
            toast.show();

            if (resultCode == RESULT_OK) {   // -1
                    Bundle bundle = data.getExtras();
                    RoomInfo roomInfo = (RoomInfo) bundle.getParcelable(RoomInfo.PARCELKEY_DATA_CREATE);
                    if(roomInfo.getPassword().length() > 0) roomInfo.setHavePassword();
                    Log.e("join", "result-> " + roomInfo.getTitle() + " " + roomInfo.havePassword());
                    toast = Toast.makeText(getBaseContext(),
                            "Main으로 전달된 값 : title : "
                                    + roomInfo.getTitle() + " password : "
                                    + roomInfo.getPassword()
                            , Toast.LENGTH_LONG);
                    toast.show();


                /*
                // ############################################################
                //#DEBUG -- 테스트할때 통신없이 응답을 받았다고 가정.
                Message msg;
                msg = Message.obtain();
                MSGResponseRoomInfo responseRoomInfo = new MSGResponseRoomInfo(1, roomInfo);
                msg.what = Network_Resource.MSGResponseRoomCreate;
                msg.obj  = responseRoomInfo;
                handler.sendMessage(msg);
                // ############################################################
                */

                //#UNDEBUG
                // 방만들기 요청 메시지 생성
                MSGRequestCreateRoom msg_roomCreate = new MSGRequestCreateRoom(
                        roomInfo.getTitle(), roomInfo.getPassword(), roomInfo.getMaxpersons());
                msg_roomCreate.setKey(Network_Resource.key);
                (new Msg_Request_Thread("MSGRequestCreateRoom", handler)).execute(msg_roomCreate); // 방 만들기 요청

            }
        }
        else if(requestCode == CONFIG_CODE_ROOM_PASSWORD){  // 방 참가 요청시 암호 입력 다이얼로그 결과
            if (resultCode == RESULT_OK){
                Bundle bundle = data.getExtras();
                String pwd = bundle.getString(RoomInfo.PARCELKEY_DATA_PASSWORD);
                Log.e("join", pwd +" " + r.getTitle());
                MSGRequestIntoRoom rq = new MSGRequestIntoRoom(Network_Resource.key, r.getRoomnumber(), pwd);

                /*
                // ############################################################
                //#DEBUG -- 테스트할때 통신없이 응답을 받았다고 가정.
                RoomInfo debug_roomInfo = new RoomInfo(10,"debug_room","123",12);
                Message msg;
                msg = Message.obtain();
                MSGResponseRoomInfo responseRoomInfo = new MSGResponseRoomInfo(1, debug_roomInfo);
                msg.what = Network_Resource.MSGResponseIntoRoom;
                msg.obj = responseRoomInfo;
                handler.sendMessage(msg);
                // ############################################################
                */
                //#UNDEBUG
                (new Msg_Request_Thread("MSGRequestIntoRoom", handler)).execute(rq); // 방 창가 요청
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        // | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return true;
    }

    public class LobbyMassgeHandler extends Handler {  // 메시지 핸들러

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent;

            switch (msg.what) {
                case Network_Resource.MSGResponseRoomList:   // 방 리스트 받은 것에 대한 처리
                    if(msg.obj == null){
                        return;
                    }
                    MSGResponseRoomList responseRoomList = (MSGResponseRoomList)msg.obj;
                    if(responseRoomList.result != 1) {
                        errorHandling(msg.what, responseRoomList.result);
                        return; //  결과 거부면 함수 종료
                    }
                    adapter = new RoomListAdapter(refClass);  // adapter 초기화
                    for(int i = 0; i < responseRoomList.roomList.length; i++){  // <- length로 될지?
                        Log.e("roomList", "방 제목 : "+responseRoomList.roomList[i].getTitle());
                        adapter.addItem(responseRoomList.roomList[i]);
                        listView.setAdapter(adapter);   //그때 그때 바로 추가
                    }
                    break;

                case Network_Resource.MSGResponseRoomCreate:   // 방 만들기에 대한 응답처리.
                    MSGResponseRoomInfo responseRoomCreate = (MSGResponseRoomInfo)msg.obj;
                    RoomInfo roomInfo = responseRoomCreate.room;
                    if(responseRoomCreate.result != Network_Resource.NET_RESULT_OK) {
                        errorHandling(msg.what, responseRoomCreate.result);
                        return; // 결과가 거부이면 함수 종료
                    }

                    //#UNDEBUG
                    Log.i("create", "start MapActivity ");
                    enterRoom(roomInfo.getTitle());

                    /*
                    //#DEBUG_CHAT
                    Intent intent2 = new Intent(getApplicationContext(), ChatActivity.class);
                    intent2.putExtra("title", roomInfo.getTitle());
                    startActivity(intent2);
                    /*
                    #DEBUG
                    //########################################
                    Log.e("create", roomInfo.getTitle() + " " + roomInfo.getMaxpersons());
                    adapter.addItem(response.room);
                    listView.setAdapter(adapter);
                    //########################################
                    */
                    break;

                case Network_Resource.MSGResponseIntoRoom:   // 방 참가 요청에 대한 처리
                    MSGResponseRoomInfo responseIntoRoom = (MSGResponseRoomInfo)msg.obj;
                    Log.i("IntoRoom핸들링", "start...");

                    if(responseIntoRoom.result == 1){  // 방 참가 성공
                        enterRoom(responseIntoRoom.room.getTitle());
                    }
                    else{   // 방 참가 실패!!!!!!!! ( result가 0 : 암호틀림,  -1 : 방 꽉참)
                        errorHandling(msg.what,responseIntoRoom.result);
                        return;
                    }
                    break;
                default:
                    break;
            }
        }
        public void enterRoom(String title) {
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            intent.putExtra("title", title);
            startActivity(intent);
        }
        void errorHandling(int type, int result) {
            Toast toast = new Toast(getBaseContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            switch(type) {
                case Network_Resource.MSGResponseRoomList :
                    if(result == 0)
                    toast.setText("get room List Failed");
                    break;
                case Network_Resource.MSGResponseRoomCreate:
                    if(result == -1) { // 로그아웃된 상태. 초기 로그인 액티비티로 이동
                        Toast.makeText(getApplicationContext(),
                                "다른 곳에서 접속하여 현 계정이 로그아웃 되었습니다.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    break;
                case Network_Resource.MSGResponseIntoRoom :
                    if(result == -1)         finish(); // 로그아웃된 상태. 초기 로그인 액티비티로 이동
                    else if(result == -2)   toast.setText("비밀번호가 틀렸습니다.");
                    else if(result == -3)   toast.setText("방의 인원이 꽉 찼습니다.");
                    else if(result == -4)   toast.setText("존재하지 않는 방입니다.");
                    else                    toast.setText("Unknown Error");
                    break;
            }
            toast.show();
        }
    }
}
