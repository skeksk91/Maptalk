package floor4.maptalk.Lobby;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import floor4.maptalk.Data.RoomInfo;
import floor4.maptalk.R;


/**
 * Created by Administrator on 2015-06-28.
 */
public class RoomListView extends LinearLayout{
    private TextView roomNumber_text, title_text, persons_text;
    public RoomListView(Context context, RoomInfo aItem){
        super(context);

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.listitem, this, true);

        roomNumber_text = (TextView)findViewById(R.id.roomNumber);
        roomNumber_text.setText(""+aItem.getRoomnumber());
        title_text = (TextView)findViewById(R.id.roomName);
        title_text.setText(aItem.getTitle());
        persons_text = (TextView)findViewById(R.id.roomPeople);
        persons_text.setText(aItem.getCurpersons() + "/" + aItem.getMaxpersons());
    }
    public void setText(int index, String data){
        if(index == 0)
            roomNumber_text.setText(data);
        else if(index == 1)
            title_text.setText(data);
        else if(index == 2)
            persons_text.setText(data);
        else
            throw new IllegalArgumentException();
    }
}
