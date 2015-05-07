package team2.urbanrun;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ListActivity;
import android.content.Context;
import android.util.Log;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class TrophyScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy_screen);

/*
        int[] scores = {};
        int myIndex = 0;
        String[] names = {}, images = {};
        try {
            String res = (new ServletGameScores().execute(getIntent().getExtras().getString("GameID"),
                    getIntent().getExtras().getString("myName")).get());
            Log.d("Aviv", "Response from servlet: " + res);
            JSONArray array = new JSONArray(res);

            int numPlayers = array.length();
            scores = new int[numPlayers];
            names = new String[numPlayers];
            images = new String[numPlayers];
            for (int i = 0; i < numPlayers; i++) {
                JSONObject player = array.getJSONObject(i);
                names[i] = player.getString("FullName");
                images[i] = player.getString("ImageURL");
                scores[i] = player.getInt("score");
                if (player.getString("Username").equals(getIntent().getExtras().getString("myName")))
                    myIndex = i;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        setListAdapter(new ScroesListAdapter(this, android.R.layout.simple_list_item_1, R.id.textView ,
                names, images ,scores, myIndex));
*/
    }


    private class ScoresListAdapter extends ArrayAdapter<String> {
        private String[] names, images;
        int[] scores;
        int myIndex;

        public ScoresListAdapter(Context context, int resource, int textViewResourceId
                , String[] _names, String[] _img, int _scores[], int _myIndex) {
            super(context, resource, textViewResourceId, _names);
            names = _names;
            images = _img;
            scores = _scores;
            myIndex = _myIndex;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trophy_screen, menu);
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