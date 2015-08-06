package floor4.maptalk.MSG;

/**
 * Created by 정우 on 2015-07-16.
 */
public class MSGRequestCreateRoom {
    public MSGRequestCreateRoom(String title, String password, int maxPersons){
        this.title = title;
        this.password = password;
        this.maxPersons = maxPersons;
    }
    private int key;
    private String title;
    private String password;
    private int maxPersons;
    public void setKey(int key){
        this.key = key;
    }
}
