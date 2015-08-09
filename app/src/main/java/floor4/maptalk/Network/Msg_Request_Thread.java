package floor4.maptalk.Network;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import floor4.maptalk.MSG.MSGResponseJoin;
import floor4.maptalk.MSG.MSGResponseLogin;
import floor4.maptalk.MSG.MSGResponseRoomInfo;
import floor4.maptalk.MSG.MSGResponseRoomList;
import floor4.maptalk.MSG.MSGResponseSendChat;
import floor4.maptalk.MSG.MSGResponseSendPing;

/**
 * Created by Jethop on 2015-07-08.
 */
public class Msg_Request_Thread extends AsyncTask<Object, Void, Void>{
    Gson gson = new Gson();
    Object send_object; // 보낼 객체
    String result; // 받은 결과 문자열 (JSON)
    String msgType;
    Handler handler;
    public Msg_Request_Thread(String msgType, Handler handler){
        this.msgType = msgType;
        this.handler = handler;
    }
    protected Void doInBackground(Object... params) {
        if(msgType == null) {
            msgType = "Invalid type";
            return null;
        }
        send_object = params[0];
        String URL = Network_Resource.url + msgType; // MSGRequestLogin 헤더이다.
        String stringJson = gson.toJson(send_object);   // 보낼 객체를 string JSON으로 변환
        Log.e("send json", "msgType : " + msgType + "\nsend json = " + stringJson + "url = " + URL);

        //------------------------------------요청 부분------------------------------------------/

        Request_and_Get rq = new Request_and_Get(stringJson, URL); // obj 객체를 서버에 보낸다.
        result = rq.getResult(); // 결과를 result에 저장한다. (JsonString 상태이다)
        Log.e("receive json","msgType : " + msgType + "\nreceive json : " + result);
        //--------------------------------------------------------------------------------------/

        return null;
    }

    protected void onPostExecute(Void aVoid) { // 스레드가 끝나고 할 일 지정.
        super.onPostExecute(aVoid);
        Message msg;
        if( (msg = handler.obtainMessage()) == null) {
            Log.e("Msg_Request_Thread", " obtainMessage error");
            return;
        }
        //네트워크 상태가 안좋은 경우 result에 null값이 들어옴
        //그에 대한 에러 메시지 포맷을 보내주자.
        msg.obj = null;
        if(result == null) {
            msg.what = Network_Resource.ErrUnstableNetwork;
            msg.obj = msgType; //요청 메시지 타입을 보내줌.
        }
        else if(msgType.equals("MSGRequestLogin")){  // 로그인 요청
            msg.what = Network_Resource.MSGResponseLogin;
            msg.obj = gson.fromJson(result, MSGResponseLogin.class);
        }
        else if(msgType.equals("MSGRequestJoin")){ // 회원가입 요청
            msg.what = Network_Resource.MSGResponseJoin;
            msg.obj = gson.fromJson(result, MSGResponseJoin.class);
        }
        else if(msgType.equals("MSGRequestRoomList")){ // 방 리스트 요청
            msg.what = Network_Resource.MSGResponseRoomList;
            msg.obj = gson.fromJson(result, MSGResponseRoomList.class);
        }
        else if(msgType.equals("MSGRequestCreateRoom")){  // 방 만들기 요청
            // LobbyActiviy로 처리를 보내야함
            msg.what = Network_Resource.MSGResponseRoomCreate;
            msg.obj = gson.fromJson(result, MSGResponseRoomInfo.class);
        }

        else if(msgType.equals("MSGRequestIntoRoom")){ // 방 참가 요청
            msg.what = Network_Resource.MSGResponseIntoRoom;
            msg.obj = gson.fromJson(result, MSGResponseRoomInfo.class);
        }
        else if(msgType.equals("MSGRequestSendChat")) {
            msg.what = Network_Resource.MSGResponseSendChat;
            msg.obj = gson.fromJson(result, MSGResponseSendChat.class);
        }
        else if(msgType.equals("MSGRequestSendPing")) {
            msg.what = Network_Resource.MSGResponseSendPing;
            msg.obj = gson.fromJson(result, MSGResponseSendPing.class);
        }
        else{
            int a= 10/0;
            // msgType 제대로 설정 하시오.
            return;
        }
        if(msg.obj == null) {
            Log.e("Msg_Request_Thread", "gson error");
            return;
        }
        handler.sendMessage(msg);
    }
}
