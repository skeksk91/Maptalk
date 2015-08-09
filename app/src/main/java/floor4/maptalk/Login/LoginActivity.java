package floor4.maptalk.Login;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import floor4.maptalk.Lobby.LobbyActivity;
import floor4.maptalk.MSG.MSGRequestJoin;
import floor4.maptalk.MSG.MSGRequestLogin;
import floor4.maptalk.MSG.MSGResponseJoin;
import floor4.maptalk.MSG.MSGResponseLogin;
import floor4.maptalk.Network.Msg_Request_Thread;
import floor4.maptalk.Network.Network_Resource;
import floor4.maptalk.R;

public class LoginActivity extends ActionBarActivity {

    public static LoginActivity LoginClass;
    public static final int CONFIG_CODE_JOIN = 1;
    public static final String PARCELKEY_DATA_JOIN = "join";
    public LoginMassgeHandler handler = new LoginMassgeHandler();
    public Context context;
    public MSGRequestLogin account;
    public TextView textLogin;
    public TextView textFind;
    public TextView textJoin;
    public EditText id_text;

    public EditText pwd_text;
    private Thread.UncaughtExceptionHandler androidDefaultUEH;
    private String jsonResult;
    private Gson gson;

    public Thread.UncaughtExceptionHandler exHandler = new Thread.UncaughtExceptionHandler(){
        public void uncaughtException(Thread thread, Throwable ex){
            Log.e("Exception is: ", String.valueOf(ex));
            androidDefaultUEH.uncaughtException(thread, ex);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginClass = this;
        context = getApplicationContext();
        setContentView(R.layout.activity_login);

        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(exHandler);
        /*
        textLogin = (TextView)findViewById(R.id.loginBtn);
        textLogin.setOnClickListener(new View.OnClickListener() {  // 로그인 버튼
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Login Clicked", Toast.LENGTH_LONG).show();
            }
        });*/
        //load_image();
    }
    public void load_image(){
        ImageView imageView = (ImageView)findViewById(R.id.mainImage);
        Resources res = getResources();
        /*
        BitmapDrawable bitmap = (BitmapDrawable)res.getDrawable(R.drawable.imageMain);
        int bitmapWidth = bitmap.getIntrinsicWidth();
        int bitmapHeight = bitmap.getIntrinsicHeigth();
        imageView.setImageDrawable(bitmap);
        imageView.getLayoutParams().width = bitmapWidth;
        imageView.getLayoutParams().width = bitmapHeight;
        */
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void OnLoginClicked(View v){
        id_text = (EditText)findViewById(R.id.id_edit);
        pwd_text = (EditText)findViewById(R.id.pwd_edit);
        if(id_text.length() == 0 || id_text.length() > 20 ||
                pwd_text.length() == 0 || pwd_text.length() > 20) return;

        account = new MSGRequestLogin(id_text.getText().toString(), pwd_text.getText().toString());
        /*
        //#DEBUG
        //#################################################
        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        startActivity(intent);
        //#################################################
        */

        //#UNDEBUG
        (new Msg_Request_Thread("MSGRequestLogin", handler)).execute(account);
    }

    public void OnJoinClicked(View v){
        MSGRequestJoin join = new MSGRequestJoin("", "");
        Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, CONFIG_CODE_JOIN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONFIG_CODE_JOIN) {  // 방 만들기 다이얼로그 결과 받은 것
            if (resultCode == RESULT_OK) {   // -1
                Bundle bundle = data.getExtras();
                String id = bundle.getString("id");
                String pwd = bundle.getString("pwd");
                MSGRequestJoin msg = new MSGRequestJoin(id, pwd);
                msg.setKey(Network_Resource.key);
                (new Msg_Request_Thread("MSGRequestJoin", handler)).execute(msg); // 회원가입 요청
            }
        }
    }

    public class LoginMassgeHandler extends Handler {  // 메시지 핸들러

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case Network_Resource.MSGResponseLogin:  // 로그인 후 처리
                    if(msg.obj == null) {
                        Toast.makeText(context,
                                "네트워크 문제가 발생하였습니다 ㅠㅠ", Toast.LENGTH_LONG).show();
                        return;
                    }
                    MSGResponseLogin resultObj = (MSGResponseLogin)msg.obj;
                    if(resultObj.result == 1) { // 로그인 성공 //  1 : 성공, 2 : 이미 로그인 된 것 로그인.
                        Toast.makeText(context,
                                "로그인 성공~!", Toast.LENGTH_LONG).show();
                        Network_Resource.key = resultObj.key;  // 키 할당
                        Intent intent = new Intent(context, LobbyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                    else  if(resultObj.result == 2) {
                        Toast.makeText(context,
                                "이미 로그인되어있었습니다. 로그아웃시키며 로그인합니다.", Toast.LENGTH_LONG).show();
                    }
                    else if(resultObj.result == -1) {
                        Toast.makeText(context,
                                "아이디 혹은 비밀번호가 틀렸습니다.", Toast.LENGTH_LONG).show();
                    }
                    break;
                case Network_Resource.MSGResponseJoin:  // 회원가입 요청 답장
                    if(msg.obj == null) {
                        Toast.makeText(context,
                                "네트워크 문제가 발생하였습니다 ㅠㅠ", Toast.LENGTH_LONG).show();
                        return;
                    }
                    MSGResponseJoin resultObj2 = (MSGResponseJoin)msg.obj;
                    if(resultObj2.result == 1){
                        Toast.makeText(context,
                                "회원가입이 성공하였습니다.", Toast.LENGTH_LONG).show();
                    }
                    else if(resultObj2.result == -1) {
                        Toast.makeText(context,
                                "아이디가 중복되어 회원가입이 실패하였습니다.", Toast.LENGTH_LONG).show();
                    }
                default:
                    break;
            }
        }
    }
}
