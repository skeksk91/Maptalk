package floor4.maptalk.Chat;

/**
 * Created by 정우 on 2015-07-25.
 */
public class ChatItem {
    private int number;
    private String id;
    private String text;
    public ChatItem(String id, String text){
        this.id = id;
        this.text = text;
    }

    public String getId(){
        return id;
    }

    public String getContent(){
        return text;
    }

    public int getNumber(){
        return number;
    }
}
