package team2.urbanrun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class EndGameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        try {
            String res=(new ServletGameScores().execute(getIntent().getExtras().getString("GameID")).get());
            Log.d("Aviv", "Response from servlet: " + res);
            JSONArray array = new JSONArray(res);
            for(int i=0;i<array.length();i++) {
                JSONObject player = (JSONObject) array.get(i);
                if (player.getString("Username").equals(getIntent().getExtras().getString("myName"))) {
                    ((TextView) findViewById(R.id.myScore)).setText(Integer.toString(player.getInt("score")));
                    ((ImageView)findViewById(R.id.myImage)).setImageBitmap((new DownloadImageTask()
                            .execute(player.getString("ImageURL"))).get());
                    ((TextView) findViewById(R.id.myName)).setText(player.getString("FullName"));
                }
                else{
                    ((TextView) findViewById(R.id.oppScore)).setText(Integer.toString(player.getInt("score")));
                    ((ImageView)findViewById(R.id.oppImage)).setImageBitmap((new DownloadImageTask()
                            .execute(player.getString("ImageURL"))).get());
                    ((TextView) findViewById(R.id.oppName)).setText(player.getString("FullName"));
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        ((Button)findViewById(R.id.newGameButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(EndGameActivity.this,FriendChoosingActivity.class);
                //startActivity(intent);
                //returns to the FriendChoosingActivity
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_end_game, menu);
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
}
