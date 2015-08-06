package floor4.maptalk.MSG;

/**
 * Created by 정우 on 2015-07-20.
 */
public class MSGRequestIntoRoom {
    int key;
    int roomNumber;
    String password;
    public MSGRequestIntoRoom(int key, int roomNumber, String password){
        this.key = key;
        this.roomNumber = roomNumber;
        this.password = password;
    }
}
