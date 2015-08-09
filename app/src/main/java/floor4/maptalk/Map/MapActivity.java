package floor4.maptalk.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
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
import java.util.List;

import floor4.maptalk.Chat.ChatItem;
import floor4.maptalk.Chat.ChatListAdapter;
import floor4.maptalk.Data.PlayerInfo;
import floor4.maptalk.Lobby.LobbyActivity;
import floor4.maptalk.MSG.MSGRequestChatInfo;
import floor4.maptalk.MSG.MSGRequestMapInfo;
import floor4.maptalk.MSG.MSGRequestRoomList;
import floor4.maptalk.MSG.MSGRequestSendChat;
import floor4.maptalk.MSG.MSGRequestSendPing;
import floor4.maptalk.MSG.MSGResponseChatInfo;
import floor4.maptalk.MSG.MSGResponseMapInfo;
import floor4.maptalk.Network.Msg_Request_Thread;
import floor4.maptalk.Network.Network_Resource;
import floor4.maptalk.Network.Request_and_Get;
import floor4.maptalk.R;

/**
 * 현재 위치의 지도를 보여주고 그 위에 오버레이를 추가하는 방법에 대해 알 수 있습니다.
 * 내 위치 표시를 해 줍니다.
 * 방향 센서를 이용해 나침반을 화면에 표시합니다.
 * <p/>
 * 구글맵 v2를 사용하기 위한 여러 가지 권한이 있어야 합니다.
 * 매니페스트 파일 안에 있는 키 값을 PC에 맞는 것으로 새로 발급받아서 넣어야 합니다.
 *
 * @author Mike
 */
public class MapActivity extends ActionBarActivity {
    public static MapActivity mapClass; //나중에 Activity Manager에 등록하는데 쓰임.. 모두 한번에 종료..
    static final int MAPUPDATETIME = 1000; // millisecond
    static final int CHATUPDATETIME = 500;
    private Context context;
    private FrameLayout mainLayout;
    private GoogleMap map;
    static public MapMessageHandler handler;
    MapUpdateThread mapUpdateThread;

    private CompassView mCompassView;
    private SensorManager mSensorManager;
    private boolean mCompassEnabled;
    private Location mylocation;
    private PlayerInfo[] playerList;
    private HashMap<String, Marker> markerList;
    private Button gpsStatusBtn;
    private String title;
    boolean isGpsEnabled = false;
    boolean isNetworkEnabled = false;
    LocationManager mLocationManager;
    MediaPlayer bgm;

    //Chat 관련 멤버변수
    private int chatNumber = 0;
    private ListView listView;
    private ChatListAdapter adapter;
    private Button sendTextButton;
    private Button sendVoiceButton;
    private String text;
    private ChatUpdateThread chatUpdateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //초기화
        mapClass = this;
        context = getApplicationContext();
        handler = new MapMessageHandler(); //Msg 송수신 쓰레드 생성시 전달.
        playerList = new PlayerInfo[0];
        markerList = new HashMap<String, Marker>();
        title = getIntent().getExtras().getString("title", "Room");
        chatNumber = getIntent().getIntExtra("chatNumber",0);
        bgm = MediaPlayer.create(this, R.raw.konan1);
        bgm.setLooping(true);
        // 메인 레이아웃 객체 참조
        mainLayout = (FrameLayout) findViewById(R.id.mapLayout);
        // 지도 객체 참조
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();


        // 센서 관리자 객체 참조
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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


        //GPS 상태 표시 버튼...
        gpsStatusBtn = (Button) findViewById(R.id.gpsStatusBtn);
        gpsStatusBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("gpsStatusBtn", "Clicked");
                AlertDialog.Builder builder = new AlertDialog.Builder(mapClass);
                builder.setTitle("gps on/off")
                        .setMessage("GPS가 꺼져있습니다.").setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .create().show();
            }
        });
        gpsStatusBtn.setVisibility(View.INVISIBLE);

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //현재 위도,경도의 화면 위치를 알려 준다.
                if (latLng == null) {
                    Log.e("onMapLongClick", "cannot get latLng... == null");
                    return;
                }
                String text = ":ping " + latLng.latitude + " " + latLng.longitude;
                Log.e("onMapLongClick", "text : " + text);

                MSGRequestSendPing rq = new MSGRequestSendPing(Network_Resource.key,
                        latLng.latitude, latLng.longitude); // 메시지 요청 생성
                (new Msg_Request_Thread("MSGRequestSendPing", handler)).execute(rq);  // 메시지 요청 보내기
            }
        });
        /*맵 클릭 이벤트 등록
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //현재 위도,경도의 화면 위치를 알려 준다.
                Point screenPt = map.getProjection().toScreenLocation(latLng);
            }
        });
        */
        // 위치 확인하여 위치 표시 시작
        startLocationService();

        //Chat 관련 초기화......###
        adapter = new ChatListAdapter(this);
        listView = (ListView) findViewById(R.id.chatlistview);
        sendTextButton = (Button) findViewById(R.id.sendtextbutton);
        sendVoiceButton = (Button) findViewById(R.id.sendvoicebutton);
        listView.setAdapter(adapter);
        sendTextButton.setOnClickListener(new View.OnClickListener() { // 메시지 전송 요청
            public void onClick(View v) {
                text = ((EditText) findViewById(R.id.chat_edit)).getText().toString();
                Log.e("sendTextButton", "clicked, text :" + text);
                if (text.length() == 0) return; // 채팅 내용이 없을 시 그냥 함수 리턴

                MSGRequestSendChat rq = new MSGRequestSendChat(Network_Resource.key, text); // 메시지 요청 생성
                (new Msg_Request_Thread("MSGRequestSendChat", handler)).execute(rq);  // 메시지 요청 보내기
                ((EditText) findViewById(R.id.chat_edit)).setText("");
            }
        });
        sendVoiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();
        // 내 위치 자동 표시 enable
        map.setMyLocationEnabled(true);
        if (mCompassEnabled) {
            mSensorManager.registerListener(mListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        }

        Log.e("onResume", "start updateThread");
        mapUpdateThread = new MapUpdateThread();
        mapUpdateThread.setRunningState(true);
        mapUpdateThread.start();
        chatUpdateThread = new ChatUpdateThread();
        chatUpdateThread.setRunningState(true);
        chatUpdateThread.start(); // 채팅 리스트 요청 스레드 시작.
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
        if (mCompassEnabled) {
            mSensorManager.unregisterListener(mListener);
        }
        Log.i("onPause", "updatethread 종료 시도");
        mapUpdateThread.setRunningState(false);
        chatUpdateThread.setRunningState(false); // 스레드 종료
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
        //super.onBackPressed();
        //뒤로가기 버튼시 이전 액티비가 있으면 꺼내서 보여주고 없으면 어플이 종료됨.
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(mapClass);
        gsDialog.setTitle("Exit room");
        gsDialog.setMessage("Are you sure you want to get out from room?");
        gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_OK);
                mapUpdateThread.setRunningState(false);
                chatUpdateThread.setRunningState(false);
                mapClass.finish();
            }
        })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).create().show();
        Log.e("onBackPressed()", "디폴트로 액티비티 종료2?");
    }

    /**
     * 현재 위치 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        // 위치 관리자 객체 참조
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        // 리스너 객체 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 1000; //1초마다 호출
        float minDistance = 0;

        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        // 네트워크 기반 위치 요청
        mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        mylocation = null;
        chkGpsService();
        Log.e("mylocation", "getLastKnownLocation");
        mylocation = getLastKnownLocation();
        if (mylocation == null) {
            Toast.makeText(getApplicationContext(), "현재 위치를 불러올 수 없습니다. GPS를 켜 주세요.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        while (mylocation == null) {

        }

        // 어플 실행 시 현재 위치를 기준으로 보여줌
        LatLng startingPoint = new LatLng(mylocation.getLatitude(), mylocation.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, 16));

        // 줌 버튼 표시
        //map.getUiSettings().setZoomControlsEnabled(true);
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


    //Chat 관련...

    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private boolean chkGpsService() {
        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Log.e(gps, "gps 체크...");
        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {
            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(mapClass);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
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

            String msg = "Latitude : " + latitude + "\nLongitude:" + longitude;
            Log.i("GPSLocationService", msg);

            // 현재 위치의 지도를 보여주기 위해 정의한 메소드 호출
            //showCurrentLocation(latitude, longitude);
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
        //markerop.draggable(true);
        markerop.draggable(false);
        markerop.icon(BitmapDescriptorFactory.fromResource(R.drawable.playermark));

        Marker marker = map.addMarker(markerop);
        markerList.put(id, marker);
    }

    private void deletePlayerMarker(String id) {
        Marker marker = markerList.get(id);
        if (marker != null) {
            marker.remove();
            markerList.remove(id);
        } else Log.e("deletePlayerMarker()", "can't find id : " + id);
    }

    private void movePlayerMarker(String id, Double latitude, Double longitude) {
        Marker marker = markerList.get(id);
        if (marker != null)
            marker.setPosition(new LatLng(latitude, longitude));
        else Log.e("movePlayerMarker()", "can't find id : " + id);
    }

    /**
     * 플레이어를 표시하기 위해 정의한 메소드
     */
    public void updateMap(PlayerInfo[] newPlayerList) {
        int oldlen = playerList.length, newlen = newPlayerList.length;
        int oldidx = 0, newidx = 0;
        int compareRes;
        // 두 개의 리스트로부터 첫번째 항목 가져오기
        while (oldidx < oldlen || newidx < newlen) { //하나라도 끝에 도달하지 않았다면 진행.
            if (oldidx >= oldlen) compareRes = 1;
            else if (newidx >= newlen) compareRes = -1;
            else compareRes = playerList[oldidx].id.compareTo(newPlayerList[newidx].id);
            if (compareRes < 0) {
                // playerList[oldidx].id는 삭제됐음
                Log.e("updateMap()", playerList[oldidx].id + "삭제됨");
                deletePlayerMarker(playerList[oldidx].id);
                oldidx++;
            } else if (compareRes > 0) {
                // newPlayerList[newidx].id는 추가됨
                Log.e("updateMap()", newPlayerList[newidx].id + "추가됨");
                makePlayerMarker(newPlayerList[newidx].id,
                        newPlayerList[newidx].latitude, newPlayerList[newidx].longitude);
                newidx++;
            } else {
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
            if (isGpsEnabled) gpsStatusBtn.setVisibility(View.INVISIBLE);
            else gpsStatusBtn.setVisibility(View.VISIBLE);
            switch (msg.what) {
                case Network_Resource.MSGResponseMapInfo:  //mpaUpdateThread의 매 요청에 대한 응답
                    MSGResponseMapInfo resultObj = (MSGResponseMapInfo) msg.obj;
                    if (resultObj.result == 1) { // 서버로부터 MapInfo 정상 응답
                        Log.i("MapResponse", "mapinfo ok!!!!");
                        updateMap(resultObj.playerList);
                    } else if (resultObj.result == -1) { // 서버로부터 로그아웃 된 상태
                        Toast.makeText(context, "다른 사용자가 로그인하여 로그아웃 되었습니다. " +
                                        "나중에 ActivityManger클래스 정의하여" +
                                        "모두 종료 후 LoginActivity실행",
                                Toast.LENGTH_SHORT).show();
                        LobbyActivity.lobbyClass.finish();
                        finish();
                    }
                    break;
                case Network_Resource.MSGResponseChatInfo:   // requestChatInfo_Thread 요청에 대한 응답
                    MSGResponseChatInfo ret = (MSGResponseChatInfo) msg.obj;
                    if (ret.result == -1) {
                        Toast.makeText(getApplicationContext(),
                                "다른 곳에서 로그인하여 현재 계정이 로그아웃 되었습니다.",
                                Toast.LENGTH_LONG).show();
                        //이전 액티비티들을 먼저 지우고 현재 액티비티 종료
                        LobbyActivity.lobbyClass.finish();
                        finish();
                        break;
                    }
                    for (int i = 0; i < ret.chatList.length; i++) {
                        ChatItem item = ret.chatList[i];
                        chatNumber = item.getNumber();  // number 최신화
                        adapter.addItem(new ChatItem(item.getId(), item.getContent())); // 추가
                        adapter.notifyDataSetChanged();
                        if (ret.chatList[i].getContent().startsWith(":")) {
                            String[] str = ret.chatList[i].getContent().split(" ");
                            String cmd = str[0];
                            Message pingmsg = handler.obtainMessage();
                            switch (cmd) {
                                case ":ping": {
                                    Double lat, lng;
                                    if (str.length != 3)
                                        break;
                                    try {
                                        lat = Double.parseDouble(str[1]);
                                        lng = Double.parseDouble(str[2]);
                                    } catch (NumberFormatException nfe) {
                                        Log.e("ping", "NumberFormatException");
                                        break;
                                    }
                                    pingmsg.what = Network_Resource.MapPrintPing;
                                    pingmsg.obj = new LatLng(lat, lng);
                                    handler.sendMessage(pingmsg);
                                    break;
                                }
                                default:
                                    break;
                            }
                        }
                    }
                    break;
                case Network_Resource.MSGResponseSendChat:
                    Log.e("MapMessageHandler", "MSGResponseSendChat");
                    break;
                case Network_Resource.MSGResponseSendPing:
                    Log.e("MapMessageHandler", "MSGResponseSendPing");
                    break;

                case Network_Resource.MapPrintPing: //채팅에서 Ping 출력 요청이 들어옴
                {
                    Log.e("MapMessageHandler", "MapPrintPing");
                    LatLng latlng = (LatLng) msg.obj;
                    if (latlng == null) {
                        Log.e("MapMessageHandler", "MSGHandlerPing latlng==null error");
                        break;
                    }
                    MarkerOptions markerop = new MarkerOptions();
                    markerop.position(latlng);
//                    markerop.title("id:" + id);
//                    markerop.snippet("경도/위도 : " + latitude + "/" + longitude);
                    markerop.draggable(false); // 드래그 안되도록
                    markerop.icon(BitmapDescriptorFactory.fromResource(R.drawable.pingmark));
                    Marker marker = map.addMarker(markerop);
                    MediaPlayer pingsound = MediaPlayer.create(context, R.raw.pingsound);
                    pingsound.setLooping(false);
                    pingsound.start();
                    // ping 을 일정시간 동안만 보여주기 때문에 일정시간 후 없애줄 필요가 있음....
                    break;
                }

                case Network_Resource.ErrUnstableNetwork: {
                    Toast.makeText(context,
                            "MSGResponseSendPing 네트워크 연결 상태 나쁨", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    Log.e("debug_error", "잘못된 메시지 전달 msg.what : " + msg.what);
                    break;
            }
        }
    }

    public class ChatUpdateThread extends Thread {
        MSGRequestChatInfo send_object;
        MSGResponseChatInfo result_object;
        String URL = Network_Resource.url + "MSGRequestChatInfo";
        Message msg;
        private boolean is_running = true;

        public void setRunningState(boolean flag) {
            is_running = flag;
        }

        public void run() {
            Gson gson = new Gson();
            while (is_running) {
                try {
                    sleep(CHATUPDATETIME); // 채팅 리스트 요청 간격
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                send_object = new MSGRequestChatInfo(Network_Resource.key, chatNumber); // 채팅 리스트 요청 메시지 생성

                String stringJson = gson.toJson(send_object);
                Request_and_Get rq = new Request_and_Get(stringJson, URL); // obj 객체를 서버에 보낸다.
                String result = rq.getResult(); // 결과를 result에 저장한다. (JsonString 상태이다)

                if (result != null) {
                    result_object = gson.fromJson(result, MSGResponseChatInfo.class);
                    Log.e("Network", "Success req/res... in ChatUpdateThread");
                } else {
                    /* Can't create handler inside thread that has not called Looper.prepare()
                    Toast.makeText(getApplicationContext(),
                            "네트워크 연결을 확인하십시오.",
                            Toast.LENGTH_SHORT).show();
                    */
                    Log.e("Network", "fail req/res... in ChatUpdateThread");
                    continue;
                }
                handler.obtainMessage(Network_Resource.MSGResponseChatInfo, result_object)
                        .sendToTarget();
            }
        }
    }

    public class MapUpdateThread extends Thread {
        boolean isRunning;

        @Override
        public void run() {
            MSGRequestMapInfo msgRequestMapInfo;
            MSGResponseMapInfo msgResponseMapInfo;
            isRunning = true;

            //#DEBUG
            int debug_cnt = 0;

        /* 액티비티를 종료할 때까지 계속 반복해서 요청 작업 진행. */
            while (isRunning) {
                //#DEBUG
                debug_cnt++;

                try {
                    Thread.sleep(MAPUPDATETIME);  // 0.5초 간격.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // GPS On/Off check
                String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                Log.e("MapUpdateThread", "gps 체크..." + gps);
                if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) { //꺼져있는 경우
                    isGpsEnabled = false;
                    isNetworkEnabled = false;
                } else {
                    isGpsEnabled = true;
                    isNetworkEnabled = true;
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
                String URL = Network_Resource.url + "MSGRequestMapInfo";
                String stringJson = gson.toJson(msgRequestMapInfo);   // 보낼 객체를 string JSON으로 변환
                //--------------------------요청 전송 부분-----------------------------
                Request_and_Get rq = new Request_and_Get(stringJson, URL); // obj 객체를 서버에 보낸다.
                result = rq.getResult(); // 결과를 result에 저장한다. (JsonString 상태이다)
                if (result != null) {
                    msgResponseMapInfo = gson.fromJson(result, MSGResponseMapInfo.class);
                    Log.e("Network", "Success req/res... in MapUpdateThread");
                } else {
                    Log.e("Network", "fail req/res... in MapUpdateThread");
                    continue;
                }
                handler.obtainMessage(Network_Resource.MSGResponseMapInfo, msgResponseMapInfo)
                        .sendToTarget();
                /*
                Meesgae msg = handler.obtainMessage();
                msg.what = Network_Resource.MSGResponseMapInfo;
                msg.obj  = msgResponseMapInfo;
                handler.sendMessage(msg);
                */


                /*//#DEBUG....
                //##############################################################
                msg = handler.obtainMessage();
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
            Log.i("updateTHread", "종료됨");
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
        switch (id) {
            case 1:
            case 2:
        }

        return super.onOptionsItemSelected(item);
    }
}
