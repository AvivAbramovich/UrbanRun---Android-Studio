package team2.urbanrun;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aviv on 19/04/2015.
 */
public class ServletPlayerUnavailable extends AsyncTask<String,String,String> {
    private String name,GameID,lat,lng;

    @Override
    protected String doInBackground(String...params) {
        name = params[0];
        GameID = params[1];

        HttpClient httpClient = new DefaultHttpClient();
        //HttpPost request = new HttpPost("http://1-dot-team2urban.appspot.com/urbanrun");
        HttpPost request = new HttpPost("http://1-dot-team2urban.appspot.com/initZone");

        try {
            // Add name data to request
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username",name));
            nameValuePairs.add(new BasicNameValuePair("GameID",GameID));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            }
            return "Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();

        } catch (ClientProtocolException e) {
            return e.getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}