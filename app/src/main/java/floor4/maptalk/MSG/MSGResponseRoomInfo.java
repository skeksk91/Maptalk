package floor4.maptalk.MSG;


import floor4.maptalk.Data.RoomInfo;

public class MSGResponseRoomInfo {
    public int result;
    public RoomInfo room;
    public MSGResponseRoomInfo(int result, RoomInfo room) {
        this.result=result;
        this.room = room;
    }
}
