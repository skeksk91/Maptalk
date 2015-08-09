package floor4.maptalk.MSG;

/**
 * Created by 정우 on 2015-08-02.
 */
public class MSGRequestJoin {
    private int key;
    private String id;
    private String password;
    public MSGRequestJoin(String id, String password){
        this.id = id;
        this.password = password;
    }
    public void setKey(int key){
        this.key = key;
    }
}
