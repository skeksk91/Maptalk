package floor4.maptalk.Network;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by 정우 on 2015-07-11.
 */
public class Request_and_Get {
    StringEntity se;
    Gson gson = new Gson();
    String url;
    public Request_and_Get(String stringJson, String url){  // obj : 보낼 객체,  url : 보낼 주소
        this.url = url;
        try {
            se= new StringEntity(stringJson);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public String getResult(){ // 서버에서 받은 데이터(json형식 String) 반환
        Log.e("sendHttpWithMsg", "111");
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 3000);
        HttpConnectionParams.setSoTimeout(params, 3000);
        post.setHeader("Content-type", "application/json; charset=utf-8");
        HttpEntity he=se;
        post.setEntity(he);
        try {
            HttpResponse response = client.execute(post);
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(),
                    "utf-8"
            )
            );
            String line = null;
            String result = "";
            while ((line = bufReader.readLine())!=null){
                result +=line;
            }
            Log.e("test","responce result(json) :"+result); // 결과 테스트
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("test", "errer asdfsdkf"); // 결과 테스트
            return null;
        }
    }
}
