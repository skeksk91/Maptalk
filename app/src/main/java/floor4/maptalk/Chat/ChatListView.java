package floor4.maptalk.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import floor4.maptalk.R;

/**
 * Created by Administrator on 2015-06-28.
 */
public class ChatListView extends LinearLayout{
    private TextView chat_id, chat_content;
    public ChatListView(Context context, ChatItem aItem){
        super(context);

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.listitem_chat, this, true);

        chat_id = (TextView)findViewById(R.id.chat_id);
        chat_id.setText(aItem.getId());
        chat_content = (TextView)findViewById(R.id.chat_content);
        chat_content.setText(aItem.getContent());
    }
    public void setText(int index, String data){
        if(index == 0)
            chat_id.setText(data);
        else if(index == 1)
            chat_content.setText(data);
        else
            throw new IllegalArgumentException();
    }
}
