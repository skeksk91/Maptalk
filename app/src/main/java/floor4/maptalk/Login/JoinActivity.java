package floor4.maptalk.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import floor4.maptalk.Data.RoomInfo;
import floor4.maptalk.MSG.MSGRequestJoin;
import floor4.maptalk.R;


public class JoinActivity extends ActionBarActivity  {
    public MSGRequestJoin msg;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
    }

    //적용 버튼
    public void onJoinCompleteClicked(View v) {
        String id, pwd;
        EditText id_text = (EditText)findViewById(R.id.join_id_text);
        EditText password_text = (EditText)findViewById(R.id.join_pwd_text);
        if(id_text.length() == 0 || id_text.length() > 20 ||
                password_text.length() == 0 || password_text.length() > 20){
            Toast toast = Toast.makeText(getBaseContext(),
                    "다시 입력하여 주십시오.",Toast.LENGTH_LONG);
            return;
        }
        id = id_text.getText().toString();
        pwd = password_text.getText().toString();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("id", id);
        resultIntent.putExtra("pwd", pwd);
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
}
