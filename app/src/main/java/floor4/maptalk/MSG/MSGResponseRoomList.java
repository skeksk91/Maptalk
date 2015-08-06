package floor4.maptalk.MSG;

import floor4.maptalk.Data.RoomInfo;

/**
 * Created by 정우 on 2015-07-19.
 */
public class MSGResponseRoomList { // 디버그 후 private으로
    public int result;
    public RoomInfo roomList[]; // 일단 배열로. ArrayList 변환 과정이 따로있음
}
