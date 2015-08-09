package floor4.maptalk.Network;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by 정우 on 2015-07-11.
 */
public class Request_and_Get {
    Gson gson = new Gson();
    URL url;
    String stringJson;
    public Request_and_Get(String stringJson, String url){  // obj : 보낼 객체,  url : 보낼 주소
        try {
            this.url = new URL(url);
            this.stringJson = stringJson;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    public String getResult(){ // 서버에서 받은 데이터(json형식 String) 반환
        Log.e("sendHttpWithMsg", "start...");
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-type", "application/json; charset=utf-8");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            OutputStream os = conn.getOutputStream();
            os.write(stringJson.getBytes("UTF-8"));
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String line = null;
            String result = "";
            while ((line = br.readLine()) != null) {
                result+= line;
            }
            conn.disconnect();
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("test", "Request_and_Get error"); // 결과 테스트
            return null;
        }
    }
}
