package floor4.maptalk.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.HashMap;

import floor4.maptalk.Chat.ChatActivity;
import floor4.maptalk.Data.PlayerInfo;
import floor4.maptalk.Lobby.LobbyActivity;
import floor4.maptalk.MSG.MSGRequestMapInfo;
import floor4.maptalk.MSG.MSGRequestRoomList;
import floor4.maptalk.MSG.MSGResponseMapInfo;
import floor4.maptalk.Network.Msg_Request_Thread;
import floor4.maptalk.Network.Network_Resource;
import floor4.maptalk.Network.Request_and_Get;
import floor4.maptalk.R;

/**
 * 현재 위치의 지도를 보여주고 그 위에 오버레이를 추가하는 방법에 대해 알 수 있습니다.
 * 내 위치 표시를 해 줍니다.
 * 방향 센서를 이용해 나침반을 화면에 표시합니다.
 *
 * 구글맵 v2를 사용하기 위한 여러 가지 권한이 있어야 합니다.
 * 매니페스트 파일 안에 있는 키 값을 PC에 맞는 것으로 새로 발급받아서 넣어야 합니다.
 *
 * @author Mike
 */
public class MapActivity extends ActionBarActivity {
    public static MapActivity MapClass;
    static final int MAPUPDATETIME = 1000; // millisecond
    protected static Context context;
    private FrameLayout mainLayout;
    private GoogleMap map;
    MapMessageHandler handler;
    MapUpdateThread mapUpdateThread;

    private CompassView mCompassView;
    private SensorManager mSensorManager;
    private boolean mCompassEnabled;
    private Location mylocation;
    private PlayerInfo[] playerList;
    private HashMap<String, Marker> markerList;
    private Button sampleBut;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //초기화
        MapClass = this;
        context = getApplicationContext();
        handler = new MapMessageHandler(); //Msg 송수신 쓰레드 생성시 전달.
        playerList = new PlayerInfo[0];
        markerList = new HashMap<String, Marker>();
        //########################
        //#DEBUG
        getIntent().putExtra("title", "tmpTitle... for DEBUG");
        //########################
        title = getIntent().getExtras().getString("title");

        // 메인 레이아웃 객체 참조
        mainLayout = (FrameLayout)findViewById(R.id.mapLayout);
        // 지도 객체 참조
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        // 센서 관리자 객체 참조
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        // 나침반을 표시할 뷰 생성
        boolean sideBottom = true;
        mCompassView = new CompassView(this);
        mCompassView.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(sideBottom ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
        mainLayout.addView(mCompassView, params);
        mCompassEnabled = true;


        sampleBut = (Button)findViewById(R.id.sampleBut);
        sampleBut.setOnClickListener(new View.OnClickListener() { // 메시지 전송 요청
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class); // 채팅 액티비티 띄우기
                intent.putExtra("title", title);
                startActivity(intent);
            }
        });

        // 위치 확인하여 위치 표시 시작
        startLocationService();
        // 서버에 0.5초마다 반복 요청하는 쓰레드 생성
    }


    @Override
    public void onResume() {
        super.onResume();

        // 내 위치 자동 표시 enable
        map.setMyLocationEnabled(true);
        if(mCompassEnabled) {
            mSensorManager.registerListener(mListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        }

        Log.e("onResume", "start updateThread");
        mapUpdateThread = new MapUpdateThread();
        mapUpdateThread.execute();
        /*//#DEBUG
        MSGRequestMapInfo msgMapInfo;
        for(int i= 0; i<10; i++) {
            try {
                Thread.sleep(MAPUPDATETIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int TMPKEY = 142;
            double lat = mylocation.getLatitude()+ 0.0001 * i;
            double lng = mylocation.getLongitude();
            msgMapInfo = new MSGRequestMapInfo(TMPKEY, lat, lng);
            new Msg_Request_Thread("MSGRequestMapInfo", handler).execute(msgMapInfo);
        }*/
    }

    @Override
    public void onPause() { //액티비티가 제거되거나 일시 정지 됐을 경우 처리.
        super.onPause();

        // 내 위치 자동 표시 disable
        map.setMyLocationEnabled(false);
        if(mCompassEnabled) {
            mSensorManager.unregisterListener(mListener);
        }
        Log.i("onPause","updatethread 종료 시도");
        mapUpdateThread.setRunningState(false);
    }
    @Override
    protected void onStop() {
        Log.e("onStop()", "super.onStop() 호출 전.");
        super.onStop();
        Log.e("onStop()", "super.onStop() 호출 후.");
        // 결론1. onBackPressed 디폴트 시 onStop 실행됨
    }

    @Override
    public void onBackPressed() {
        Log.e("onBackPressed()", "디폴트로 액티비티 종료?");
        super.onBackPressed();
        //뒤로가기 버튼시 이전 액티비가 있으면 꺼내서 보여주고 없으면
        //내부에서 finsih 호출하여 어플종료.

        //finish(); //액티비티를 종료
        Log.e("onBackPressed()", "디폴트로 액티비티 종료2?");
    }

    /**
     * 현재 위치 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 리스너 객체 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 10000;
        float minDistance = 0;

        // GPS 기반 위치 요청
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        // 네트워크 기반 위치 요청
        manager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                gpsListener);


        mylocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // 어플 실행 시 현재 위치를 기준으로 보여줌
        LatLng startingPoint = new LatLng(mylocation.getLatitude(),mylocation.getLongitude()); //에뮬에서 에러. 설정안할시 에러
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint,16));
        // 줌 버튼 표시
        map.getUiSettings().setZoomControlsEnabled(true);
        /*
        //#DEBUG
        Toast.makeText(getApplicationContext(), "위치 확인 시작함. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();
        double latitude  = mylocation.getLatitude();
        double longitude = mylocation.getLongitude();
        playerList = new PlayerInfo[4];
        playerList[0] = new PlayerInfo("A",latitude+0.001,longitude+0.001);
        playerList[1] = new PlayerInfo("B",latitude+0.001,longitude-0.001);
        playerList[2] = new PlayerInfo("C",latitude-0.001,longitude+0.001);
        playerList[3] = new PlayerInfo("본인",latitude,longitude);
        Log.e("test","playerList.length : "+playerList.length);
        //응답 메시지에 본인것도 포함되서 도착.
        //본인 것인지 아이디로 검사할경우 같은 아이디가 존재해서는 안됨
        //GPS로 검사할경우 같은 GPS 일 수도 있음
        for(int i=0; i<playerList.length;i++) {
            //if(!playerList[i].id.equals("본인"))
            showPlayerMarker(playerList[i].id, playerList[i].latitude, playerList[i].longitude);
        }*/
    }

    /**
     * 리스너 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인되었을 때 호출되는 메소드
         */

        public void onLocationChanged(Location location) {
            mylocation = location;
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude : "+ latitude + "\nLongitude:"+ longitude;
            Log.i("GPSLocationService", msg);

            // 현재 위치의 지도를 보여주기 위해 정의한 메소드 호출
            showCurrentLocation(latitude, longitude);
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }


    }

    /**
     * 현재 위치의 지도를 보여주기 위해 정의한 메소드
     *
     * @param latitude
     * @param longitude
     */
    private void showCurrentLocation(Double latitude, Double longitude) {
        // 현재 위치를 이용해 LatLon 객체 생성
        LatLng curPoint = new LatLng(latitude, longitude);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

        // 지도 유형 설정. 지형도인 경우에는 GoogleMap.MAP_TYPE_TERRAIN, 위성 지도인 경우에는 GoogleMap.MAP_TYPE_SATELLITE
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // 현재 위치 주위에 아이콘을 표시하기 위해 정의한 메소드
    }





    /**
     * 센서의 정보를 받기 위한 리스너 객체 생성
     */
    private final SensorEventListener mListener = new SensorEventListener() {
        private int iOrientation = -1;

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        // 센서의 값을 받을 수 있도록 호출되는 메소드
        public void onSensorChanged(SensorEvent event) {
            if (iOrientation < 0) {
                iOrientation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            }

            mCompassView.setAzimuth(event.values[0] + 90 * iOrientation);
            mCompassView.invalidate();

        }
    };

    private void makePlayerMarker(String id, Double latitude, Double longitude) {
        MarkerOptions markerop = new MarkerOptions();
        markerop.position(new LatLng(latitude, longitude));
        markerop.title("id:" + id);
        markerop.snippet("경도/위도 : " + latitude + "/" + longitude);
        markerop.draggable(true);
        markerop.icon(BitmapDescriptorFactory.fromResource(R.drawable.player));

        Marker marker = map.addMarker(markerop);
        markerList.put(id, marker);
    }
    private void deletePlayerMarker(String id) {
        Marker marker = markerList.get(id);

        if (marker != null) {
            marker.remove();
            markerList.remove(id);
        }
        else Log.e("deletePlayerMarker()", "can't find id : " + id);
    }
    private void movePlayerMarker(String id, Double latitude, Double longitude) {
        Marker marker = markerList.get(id);
        if(marker != null)
            marker.setPosition(new LatLng(latitude, longitude));
        else Log.e("movePlayerMarker()", "can't find id : "+id);
    }
    /**
     * 플레이어를 표시하기 위해 정의한 메소드
     */
    public void updateMap(PlayerInfo[] newPlayerList) {
        int oldlen= playerList.length, newlen = newPlayerList.length;
        int oldidx=0,newidx=0;
        int compareRes;
        // 두 개의 리스트로부터 첫번째 항목 가져오기
        while (oldidx < oldlen || newidx < newlen)  { //하나라도 끝에 도달하지 않았다면 진행.
            if(oldidx >= oldlen)        compareRes = 1;
            else if(newidx >= newlen)   compareRes = -1;
            else  compareRes = playerList[oldidx].id.compareTo(newPlayerList[newidx].id);
            if(compareRes < 0) {
                // playerList[oldidx].id는 삭제됐음
                Log.e("updateMap()",playerList[oldidx].id + "삭제됨");
                deletePlayerMarker(playerList[oldidx].id);
                oldidx++;
            }
            else if(compareRes > 0){
                // newPlayerList[newidx].id는 추가됨
                Log.e("updateMap()",newPlayerList[newidx].id + "추가됨");
                makePlayerMarker(newPlayerList[newidx].id,
                        newPlayerList[newidx].latitude, newPlayerList[newidx].longitude);
                newidx++;
            }
            else {
                //기존에 이미 존재. move만 적용
                //Log.e("updateMap()",newPlayerList[newidx].id + "이동");
                movePlayerMarker(newPlayerList[newidx].id, newPlayerList[newidx].latitude,
                        newPlayerList[newidx].longitude);
                oldidx++;
                newidx++;
            }
        }
        playerList = newPlayerList;
    }
    public class MapMessageHandler extends Handler {  // 메시지 핸들러

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e("handlerMessage()","handleMessage실행됨");
            switch (msg.what) {
                case Network_Resource.MSGResponseMapInfo:  //
                    if(msg.obj == null) {
                        Toast.makeText(context,
                                "네트워크 문제일듯...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MSGResponseMapInfo resultObj = (MSGResponseMapInfo)msg.obj;
                    if(resultObj.result == 1) { // 서버로부터 MapInfo 정상 응답
                        Log.i("MapResponse", "mapinfo ok!!!!");
                        updateMap(resultObj.playerList);
                    }
                    else if(resultObj.result ==  -1){ // 현재 계정이 로그아웃 된 상태
                        Toast.makeText(context,
                                "다른 곳에서 접속하여 현 계정이 로그아웃 되었습니다.", Toast.LENGTH_LONG).show();
                        LobbyActivity.LobbyClass.finish(); // 로비 액티비티 종료
                        finish();  // 현재 맵 액티비티 종료  -> 로그인 액티비티로 이동.
                        Log.i("MapResponse", "mapinfo fail");
                    }
                    else{
                        Log.i("MapResponse", "mapinfo failed");
                    }
                    break;
                default:
                    Log.e("debug_error","잘못된 메시지 전달 msg.what : " + msg.what);
                    break;
            }
        }
    }

    public class MapUpdateThread extends AsyncTask<Void,Void,Void> {
        boolean isRunning;
        @Override
        protected Void doInBackground(Void... params) {
            MSGRequestMapInfo msgRequestMapInfo;
            MSGResponseMapInfo msgResponseMapInfo;
            Message msg = new Message();
            isRunning = true;

            //#DEBUG
            int debug_cnt = 0;

        /* 액티비티를 종료할 때까지 계속 반복해서 요청 작업 진행. */
            while(isRunning) {
                //#DEBUG
                debug_cnt++;
                try {
                    Thread.sleep(MAPUPDATETIME);  // 0.5초 간격.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //#DEBUG...
                int TMPKEY = 142; // 임시 키...
                double lat = mylocation.getLatitude() + 0.0001 * debug_cnt;
                //#UNDEBUG
                //double lat = mylocation.getLatitude();
                double lng = mylocation.getLongitude();
                msgRequestMapInfo = new MSGRequestMapInfo(Network_Resource.key, lat, lng);

                //#UNDEBUG
                // 요청 생성 ... 응답 처리는 MapMessageHandler 클래스에서 처리한다.
                Gson gson = new Gson();
                String result; // 받은 결과 문자열 (JSON)
                String msgType;
                String URL = Network_Resource.url + "MSGRequestMapInfo";
                String stringJson = gson.toJson(msgRequestMapInfo);   // 보낼 객체를 string JSON으로 변환
                Log.e("test", "send = " + stringJson + "url = " + URL);
                //--------------------------요청 전송 부분-----------------------------
                Request_and_Get rq = new Request_and_Get(stringJson, URL); // obj 객체를 서버에 보낸다.
                result = rq.getResult(); // 결과를 result에 저장한다. (JsonString 상태이다)
                if(result == null) {
                    Toast.makeText(getApplicationContext(), "네트워크 연결에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                    continue;
                }
                msgResponseMapInfo = gson.fromJson(result, MSGResponseMapInfo.class);
                msg = handler.obtainMessage();
                msg.what = Network_Resource.MSGResponseMapInfo;
                msg.obj  = msgResponseMapInfo;
                handler.sendMessage(msg);


                /*//#DEBUG....
                //##############################################################
                msg = new Message();
                double latitude  = mylocation.getLatitude();
                double longitude = mylocation.getLongitude();
                PlayerInfo[] debug_playerList = new PlayerInfo[4];
                String[] arrid = {"A","B","C","본인"};
                double[] arrlat = {0.0001, 0.0001, -0.0001, 0.0};
                double[] arrlng = {0.0001, -0.0001, +0.0001, 0.0};

                //초기화. 매번 debug_cnt에 따라 위치가 달라짐.
                for(int i=0; i<4; i++) {
                    debug_playerList[i] = new PlayerInfo(arrid[i],
                            latitude+ arrlat[i]*debug_cnt, longitude+ arrlng[i]*debug_cnt);
                }

                MSGResponseMapInfo mapmsg = new MSGResponseMapInfo(Network_Resource.NET_RESULT_OK,
                        debug_playerList);
                msg = handler.obtainMessage();  //what, obj 이외 속성값에 대한 초기화를 위함?
                msg.what = Network_Resource.MSGResponseMapInfo;
                msg.obj = mapmsg;
                handler.sendMessage(msg); // 인위적으로 만든 메시지 보냄
                //##############################################################
                */

            }
            Log.i("updateTHread","종료됨");
            return null;
        }
        public void setRunningState(boolean state) {
            isRunning = state;
        }
    }



    //--------------------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        MenuItem zoomin = menu.add(0, 1, Menu.NONE, "메뉴 추가 테스트용..");
// 메뉴에 항목 추가
        MenuItem zoonout = menu.add(0, 2, Menu.NONE, "지도 축소");
// 메뉴에 항목 추가
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);
// 맵 컨트롤러를 받아옵니다.
        Context context = getApplicationContext();
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id) {
            case 1:
            case 2:
        }

        return super.onOptionsItemSelected(item);
    }
}
