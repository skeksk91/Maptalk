package floor4.maptalk.PlayerList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import floor4.maptalk.Chat.ChatItem;


/**
 * Created by Administrator on 2015-06-28.
 */
public class PlayerListAdapter extends BaseAdapter{
    private Context mContext;

    private List<PlayerListItem> mItems = new ArrayList<PlayerListItem>();

    public PlayerListAdapter(Context context){
        mContext = context;
    }
    public void addItem(PlayerListItem it) {
        mItems.add(it);
    }

    public void setListItems(List<PlayerListItem> lit) {
        mItems = lit;
    }
    public int getCount(){
        return mItems.size();
    }
    public Object getItem(int position) {
        return mItems.get(position);
    }

    public boolean areAllItemsSelectable() {
        return false;
    }
    public boolean isSelectable(int position) {
        try {
            return mItems.get(position).isSelectable();
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
    }

    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        PlayerListView playerListView;
        if(convertView == null){
            playerListView = new PlayerListView(mContext, mItems.get(position));
        }
        else{
            playerListView = (PlayerListView)convertView;
        }
        return playerListView;
    }
}
