package floor4.maptalk.MSG;

public class MSGRequestSendPing {
    private int key;
    private double latitude;
    private double longitude;
    public MSGRequestSendPing(int key, double latitude, double longitude) {
        this.key = key;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
