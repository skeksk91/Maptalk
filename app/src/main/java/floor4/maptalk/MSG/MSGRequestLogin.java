package floor4.maptalk.MSG;

/**
 * Created by 정우 on 2015-07-10.
 */
public class MSGRequestLogin {
    private String id;
    private String password;
    public MSGRequestLogin(String id, String password){
        this.id = id;
        this.password = password;
    }
}
