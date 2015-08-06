package floor4.maptalk.PlayerList;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import floor4.maptalk.R;

/**
 * Created by Administrator on 2015-06-28.
 */
public class PlayerListView extends LinearLayout{
    private TextView mText01, mText02, mText03;
    private ImageView mIcon;


    public PlayerListView(Context context, PlayerListItem aItem){
        super(context);

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.playerlistitem, this, true);

        mIcon = (ImageView)findViewById(R.id.playerlistitemimage);
        mIcon.setImageDrawable(aItem.getIcon());

        mText01 = (TextView) findViewById(R.id.playerlistitem01);
        mText01.setText(aItem.getData(0));

        // Set Text 02
        mText02 = (TextView) findViewById(R.id.playerlistitem02);
        mText02.setText(aItem.getData(1));

        // Set Text 03
        mText03 = (TextView) findViewById(R.id.playerlistitem03);
        mText03.setText(aItem.getData(2));
    }
    public void setText(int index, String data){
        if(index == 0)
            mText01.setText(data);
        else if(index == 1)
            mText02.setText(data);
        else if(index ==2)
            mText03.setText(data);
        else
            throw new IllegalArgumentException();
    }
}
