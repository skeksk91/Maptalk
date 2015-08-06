package floor4.maptalk.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jethop on 2015-07-05.
 */
public class RoomInfo implements Parcelable{
    public static final String PARCELKEY_DATA_CREATE = "roomoption_data";
    public static final String PARCELKEY_DATA_PASSWORD = "roompassword_data";
    private String title;
    private String password;
    private int roomNumber = 0;
    private int curPersons = 0;
    private int maxPersons = 0;
    private int option;
    private int isPassword = 0;
    private String[] idList = new String[100];  //이거 크기 안정해도 될듯?

    public String[] getIdList() { return idList;}
    public RoomInfo(int roomNumber, String title, String password, int maxPersons) {
        this.roomNumber = roomNumber;
        this.title   = title;
        this.password = password;
        this.maxPersons = maxPersons;
    }
    public int getRoomnumber() { return roomNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) {this.title = title; }
    public String getPassword() { return password; }
    public void setPassword(String password) {this.password = password; }
    public int getCurpersons() { return curPersons;}
    public int getMaxpersons() { return maxPersons;}
    public String getPersons() { return getCurpersons() + "/" + getMaxpersons();}
    public void setmaxPerson(int maxPersons) {this.maxPersons = maxPersons; }
    public void setHavePassword() {this.isPassword = 1;}
    public int havePassword(){return isPassword;}
    // Parcel 처리-----------
    public RoomInfo(Parcel src) {
        // 순서 중요할듯..
        title = src.readString();
        password = src.readString();
        maxPersons = src.readInt();
    }
    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel in) {
            return new RoomInfo(in);
        }

        @Override
        public Object[] newArray(int size) {
            return new RoomInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(password);
        dest.writeInt(maxPersons);
    }

}
