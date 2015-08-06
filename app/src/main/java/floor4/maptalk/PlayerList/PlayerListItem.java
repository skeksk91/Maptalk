package floor4.maptalk.PlayerList;

import android.graphics.drawable.Drawable;

/**
 * Created by 정우 on 2015-07-25.
 */
public class PlayerListItem {
    private Drawable mIcon;
    private String[] mData;

//      True if this item is selectable
    private boolean mSelectable = true;

    public PlayerListItem(Drawable icon, String[] obj){
        mIcon = icon;
        mData = obj;
    }

    public PlayerListItem(Drawable icon, String obj01, String obj02, String obj03) {
        mIcon = icon;
        mData = new String[3];
        mData[0] = obj01;
        mData[1] = obj02;
        mData[2] = obj03;
    }

    /**
     * True if this item is selectable
     */
    public boolean isSelectable() {
        return mSelectable;
    }

    /**
     * Set selectable flag
     */
    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }

    public String[] getData() {
        return mData;
    }

    /**
     * Get data
     */
    public String getData(int index) {
        if (mData == null || index >= mData.length) {
            return null;
        }

        return mData[index];
    }

    /**
     * Set data array
     *
     * @param obj
     */
    public void setData(String[] obj) {
        mData = obj;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    public Drawable getIcon() {
        return mIcon;
    }
}
