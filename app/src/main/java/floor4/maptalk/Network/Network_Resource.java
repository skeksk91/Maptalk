package floor4.maptalk.Network;

import floor4.maptalk.MSG.MSGResponseChatInfo;
import floor4.maptalk.MSG.MSGResponseSendChat;

/**
 * Created by 정우 on 2015-07-11.
 */
public class Network_Resource {
    public static final String url = "http://203.253.25.102:6789/";
    //public static final String url = "http://192.168.25.27:8080/";
    public static final int MSGResponseRoomInfo = 1;
    public static final int MSGResponseLogin = 2;
    public static final int MSGResponseRoomList = 3;
    public static final int MSGResponseRoomCreate = 4;
    public static final int MSGResponseIntoRoom = 5;
    public static final int MSGResponseMapInfo = 6;
    public static final int MSGResponseChatInfo = 7;
    public static final int MSGResponseSendChat = 8;
    public static final int MSGResponseJoin = 9;

    public static final int NET_RESULT_OK = 1;
    public static final int NET_RESULT_FAIL = 0;
    public static int key;
}
