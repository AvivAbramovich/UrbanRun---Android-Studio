package team2.urbanrun;

import android.os.AsyncTask;

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
 * Created by Aviv on 20/04/2015.
 */
public class ServletInitGame extends AsyncTask<String,String,String> {

    @Override
    protected String doInBackground(String...params) {
        String username = params[0];
        String oppName = params[1];
        String radius = params[2];
        String centerLat = params[3];
        String centerLng = params[4];
        String type = params[5];
        String limit = params[6];

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost("http://1-dot-team2urban.appspot.com/initZone");

        try {
            // Add name data to request
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
            nameValuePairs.add(new BasicNameValuePair("username",username));
            nameValuePairs.add(new BasicNameValuePair("oppName",oppName));
            nameValuePairs.add(new BasicNameValuePair("radius",radius));
            nameValuePairs.add(new BasicNameValuePair("centerLat",centerLat));
            nameValuePairs.add(new BasicNameValuePair("centerLng",centerLng));
            nameValuePairs.add(new BasicNameValuePair("type",type));
            nameValuePairs.add(new BasicNameValuePair("limit",limit));

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
