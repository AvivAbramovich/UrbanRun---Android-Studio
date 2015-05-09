package team2.urbanrun;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;


public class LaunchActivity extends Activity {
    private CallbackManager mCallBack;
    private FacebookCallback<LoginResult> mResultFromFaceBook = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            final AccessToken AcToekn = loginResult.getAccessToken();
            GraphRequest request = GraphRequest.newMeRequest(
                    AcToekn,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            try {
                                String id = object.getString("id");
                                String firstName = object.getString("first_name");
                                String lastName = object.getString("last_name");
                                String pic = object.getJSONObject("picture").getJSONObject("data").getString("url");

                                JSONObject temp = new JSONObject(object.getString("friends"));
                                Log.d("Aviv","Players: "+temp.toString());

                                Log.d("Aviv", "Result from loggin "+(new ServletLogin().execute(id, firstName, lastName, pic)).get());
                                Intent intent = new Intent(LaunchActivity.this, MainScreen.class);
                                intent.putExtra("friends", temp.toString());
                                startActivity(intent);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,first_name,picture,last_name,friends{id,first_name,last_name,picture}");
            request.setParameters(parameters);
            request.executeAsync();
        }
        @Override
        public void onCancel() {
        }
        @Override
        public void onError(FacebookException e) {
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_launch);

        try {
            /* PRINT TO THE LOG THIS COMPUTER HASH KEY _ ADD IT TO FACEBOOK DEVELOPER APP PAGE! */
            PackageInfo info = getPackageManager().getPackageInfo(
                    "team2.urbanrun", PackageManager.GET_SIGNATURES); //Your            package name here
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("Aviv:", "Your Computer Facebook's Hash key: "+Base64.encodeToString(md.digest(), Base64.DEFAULT));
                Log.d("Aviv", "If you cant connect to facebook, go to the developers site and add it to the hash keys");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        mCallBack = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.registerCallback(mCallBack,mResultFromFaceBook);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launch, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBack.onActivityResult(requestCode,resultCode,data);
    }
}