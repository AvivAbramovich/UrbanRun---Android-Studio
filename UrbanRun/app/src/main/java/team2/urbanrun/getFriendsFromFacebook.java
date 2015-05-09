package team2.urbanrun;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aviv on 08/05/2015.
 */
public class getFriendsFromFacebook extends AsyncTask<Void, Void, JSONObject> {
JSONObject friendsJson;
    protected JSONObject doInBackground(Void...params) {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        friendsJson = object;
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "friends{id,first_name,last_name,picture}");
        request.setParameters(parameters);
        request.executeAndWait();
        return friendsJson;
    }
}