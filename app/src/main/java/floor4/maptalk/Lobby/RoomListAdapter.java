package floor4.maptalk.Lobby;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import floor4.maptalk.Data.RoomInfo;

/**
 * Created by Administrator on 2015-06-28.
 */
public class RoomListAdapter extends BaseAdapter{
    private Context mContext;

    private List<RoomInfo> mItems = new ArrayList<RoomInfo>();

    public RoomListAdapter(Context context){
        mContext = context;
    }
    public int getCount(){
        return mItems.size();
    }

    public View getView(int position, View convertView, ViewGroup parent){
        RoomListView roomView;
        if(convertView == null){
            roomView = new RoomListView(mContext, mItems.get(position));
        }
        else{
            roomView = (RoomListView)convertView;
        }
        roomView.setText(0, ""+mItems.get(position).getRoomnumber());
        roomView.setText(1, mItems.get(position).getTitle());
        roomView.setText(2, mItems.get(position).getCurpersons() + "/" +
                            mItems.get(position).getMaxpersons());
        return roomView;
    }
    public RoomInfo getItem(int position){
        if(mItems == null || position >= mItems.size()){
            return null;
        }
        return mItems.get(position);
    }
    public long getItemId(int position){
        return position;
    }
    public void addItem(RoomInfo r1){
        mItems.add(r1);
    }
}
