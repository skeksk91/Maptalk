package floor4.maptalk.MSG;

/**
 * Created by 정우 on 2015-07-25.
 */
public class MSGRequestSendChat {
    private int key;
    private String text;
    public MSGRequestSendChat(int key, String text){
        this.key = key;
        this.text = text;
    }
}
