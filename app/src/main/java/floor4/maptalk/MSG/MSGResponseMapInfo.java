package floor4.maptalk.MSG;


import floor4.maptalk.Data.PlayerInfo;

/**
 * Created by Jethop on 2015-07-17.
 */
public class MSGResponseMapInfo {
    public int result;
    public PlayerInfo playerList[];

    public MSGResponseMapInfo(int result, PlayerInfo playerList[]) {
        this.result = result;
        this.playerList = playerList;
    }
}

