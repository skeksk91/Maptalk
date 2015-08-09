package floor4.maptalk.Network;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import floor4.maptalk.MSG.MSGResponseJoin;
import floor4.maptalk.MSG.MSGResponseLogin;
import floor4.maptalk.MSG.MSGResponseRoomInfo;
import floor4.maptalk.MSG.MSGResponseRoomList;

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
        send_object = params[0];
        String URL = Network_Resource.url + msgType; // MSGRequestLogin 헤더이다.
        String stringJson = gson.toJson(send_object);   // 보낼 객체를 string JSON으로 변환
        Log.e("test", "send = " + stringJson + "url = " + URL);

        //------------------------------------요청 부분------------------------------------------/

        Request_and_Get rq = new Request_and_Get(stringJson, URL); // obj 객체를 서버에 보낸다.
        result = rq.getResult(); // 결과를 result에 저장한다. (JsonString 상태이다)
        //--------------------------------------------------------------------------------------/

        return null;
    }

    protected void onPostExecute(Void aVoid) { // 스레드가 끝나고 할 일 지정.
        super.onPostExecute(aVoid);
        if(result == null){
            return;
        }
        Message msg = new Message();
        Object result_object = null;
        if(msgType.equals("MSGRequestLogin")){  // 로그인 요청
            if(result != null)
                result_object = gson.fromJson(result, MSGResponseLogin.class);
            msg = handler.obtainMessage();
            msg.what = Network_Resource.MSGResponseLogin;
            msg.obj = result_object;
        }
        else if(msgType.equals("MSGRequestRoomList")){ // 방 리스트 요청
            if(result != null)
                result_object = gson.fromJson(result, MSGResponseRoomList.class);
            msg = handler.obtainMessage();
            msg.what = Network_Resource.MSGResponseRoomList;
            msg.obj = result_object;
        }
        else if(msgType.equals("MSGRequestCreateRoom")){  // 방 만들기 요청
            // LobbyActiviy로 처리를 보내야함
            if(result != null)
                result_object = gson.fromJson(result, MSGResponseRoomInfo.class);
            msg = handler.obtainMessage();
            msg.what = Network_Resource.MSGResponseRoomCreate;
            msg.obj = result_object;
        }
        else if(msgType.equals("MSGRequestIntoRoom")){ // 방 참가 요청
            if(result != null)
                result_object = gson.fromJson(result, MSGResponseRoomInfo.class);
            msg = handler.obtainMessage();
            msg.what = Network_Resource.MSGResponseIntoRoom;
            msg.obj = result_object;
        }
        else if(msgType.equals("MSGRequestJoin")){ // 회원가입 요청
            if(result != null)
                result_object = gson.fromJson(result, MSGResponseJoin.class);
            msg = handler.obtainMessage();
            msg.what = Network_Resource.MSGResponseJoin;
            msg.obj = result_object;
        }
        else{
            return;
        }
        if(handler != null && msg != null)
            handler.sendMessage(msg);
    }
}
