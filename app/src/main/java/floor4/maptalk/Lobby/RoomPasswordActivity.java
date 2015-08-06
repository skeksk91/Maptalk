package floor4.maptalk.Lobby;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import floor4.maptalk.Data.RoomInfo;
import floor4.maptalk.R;


public class RoomPasswordActivity extends ActionBarActivity{
    String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roompassword);
        processIntent();
    }

    //적용 버튼
    public void onApplyClicked(View v) {
        EditText password_text = (EditText)findViewById(R.id.password_room_text);
        if(password_text.length() > 20 || password_text.length() == 0){
            Toast toast = Toast.makeText(getBaseContext(),
                    "다시 입력하여 주십시오.",Toast.LENGTH_LONG);
            return;
        }
        password = password_text.getText().toString();
        Log.e("join", "pwd = " + password);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RoomInfo.PARCELKEY_DATA_PASSWORD, password);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    //취소 버튼
    public void onCancelClicked(View v) {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //사용자 정의 메소드----------------------
    private void processIntent() {
        // 인텐트 안의 번들 객체를 참조합니다.
        Bundle bundle = getIntent().getExtras();

        // 번들 객체 안의 SimpleData 객체를 참조합니다.
        password = bundle.getParcelable(RoomInfo.PARCELKEY_DATA_PASSWORD);

        Toast toast = Toast.makeText(getBaseContext(),
                "password : " + password,Toast.LENGTH_LONG);
        toast.show();
    }
}
