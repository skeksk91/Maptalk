package floor4.maptalk.Lobby;

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
import floor4.maptalk.R;


public class RoomCreateOptionActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {
    Integer [] spin_personsItem = {2,4,6,8,10,12,14,16};
    Spinner spinner;
    RoomInfo roomInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomoption);
        spinner = (Spinner)findViewById(R.id.spinner_maxPerson);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<Integer> adapter1 = new ArrayAdapter<Integer>(
                this, android.R.layout.simple_spinner_dropdown_item, spin_personsItem);
        spinner.setAdapter(adapter1);
        processIntent();
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        if(parent.getId() == R.id.spinner_maxPerson) {
            Log.e("person", "aa" + spin_personsItem[position]);
            roomInfo.setmaxPerson(spin_personsItem[position]);
        }
    }
    public void onNothingSelected(AdapterView<?> parent) {
    }

    //적용 버튼
    public void onApplyClicked(View v) {
        EditText title_text = (EditText)findViewById(R.id.room_title_text);
        EditText password_text = (EditText)findViewById(R.id.password_title_text);
        if(title_text.length() == 0 || title_text.length() > 20 ||
                 password_text.length() > 20){
            Toast toast = Toast.makeText(getBaseContext(),
                    "다시 입력하여 주십시오.",Toast.LENGTH_LONG);
            return;
        }
        roomInfo.setTitle(title_text.getText().toString());
        roomInfo.setPassword(password_text.getText().toString());
        if(password_text.getText().length() > 0) roomInfo.setHavePassword(); // 방에 암호가 존재.
        Log.e("join", "" + roomInfo.getTitle() + " " + roomInfo.havePassword());
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RoomInfo.PARCELKEY_DATA_CREATE, roomInfo);
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

    //사용자 정의 메소드----------------------
    private void processIntent() {
        // 인텐트 안의 번들 객체를 참조합니다.
        Bundle bundle = getIntent().getExtras();

        // 번들 객체 안의 SimpleData 객체를 참조합니다.
        roomInfo = (RoomInfo)bundle.getParcelable(RoomInfo.PARCELKEY_DATA_CREATE);

        Toast toast = Toast.makeText(getBaseContext(),
                "Parcelable 객체로 전달된 값 : maxteam : "
                        + roomInfo.getTitle() + " isChangeable : " + roomInfo.getPassword()
                ,Toast.LENGTH_LONG);
        toast.show();
        spinner.setSelection(roomInfo.getMaxpersons()/2-1);
    }
}
