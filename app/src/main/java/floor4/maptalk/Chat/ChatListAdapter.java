package floor4.maptalk.Chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015-06-28.
 */
public class ChatListAdapter extends BaseAdapter{
    private Context mContext;

    private List<ChatItem> mItems = new ArrayList<ChatItem>();

    public ChatListAdapter(Context context){
        mContext = context;
    }
    public int getCount(){
        return mItems.size();
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ChatListView chatView;
        if(convertView == null){
            chatView = new ChatListView(mContext, mItems.get(position));
        }
        else{
            chatView = (ChatListView)convertView;
        }
        chatView.setText(0, ""+mItems.get(position).getId());
        chatView.setText(1, mItems.get(position).getContent());
        return chatView;
    }
    public ChatItem getItem(int position){
        if(mItems == null || position >= mItems.size()){
            return null;
        }
        return mItems.get(position);
    }
    public long getItemId(int position){
        return position;
    }
    public void addItem(ChatItem r1){
        mItems.add(r1);
    }
}
