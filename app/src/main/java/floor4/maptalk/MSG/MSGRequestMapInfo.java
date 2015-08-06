package floor4.maptalk.MSG;

/**
 * Created by Jethop on 2015-07-17.
 */
public class MSGRequestMapInfo {
    int key;
    double latitude;
    double longitude;

    public MSGRequestMapInfo(int key, double latitude, double longitude) {
        this.key = key;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
