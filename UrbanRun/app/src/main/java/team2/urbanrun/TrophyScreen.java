package team2.urbanrun;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ListActivity;
import android.content.Context;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class TrophyScreen extends ListActivity {
    private String[] GlobalNames={}, FriendsNames={};
    Bitmap[] GlobalImages={}, FriendsImages={};
    private int[] GlobalNumGames={}, GlobalTotalScores={}, FriendsNumGames={}, FriendsTotalScores={};
    Profile myProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophy_screen);
        myProfile = Profile.getCurrentProfile();

        try {
            //get the friends from facebook
            JSONArray fb_friends = new getFriendsFromFacebook().execute().get().getJSONObject("friends").getJSONArray("data");
            //creating array includes ONLY the ID!
            String friendsJson = "[";
            for(int i=0; i<fb_friends.length(); i++)
                friendsJson+= ((JSONObject)fb_friends.get(i)).getString("id")+",";
            friendsJson += myProfile.getId()+"]";
            String res = (new ServletTrophy().execute(friendsJson)).get(); //TODO: sed friendsList
            Log.d("Aviv","res: "+res);
            JSONArray global = (new JSONObject(res)).getJSONArray("Global");
            JSONArray friends = (new JSONObject(res)).getJSONArray("Friends");

            GlobalNames = new String[global.length()];
            GlobalImages = new Bitmap[global.length()];
            GlobalNumGames = new int[global.length()];
            GlobalTotalScores = new int[global.length()];

            for(int i=0; i<global.length(); i++){
                JSONObject user = (JSONObject) global.get(i);
                GlobalNames[i] = user.getString("FirstName");
                GlobalImages[i] = new DownloadImageTask().execute(user.getString("ImageURL")).get();
                GlobalNumGames[i] = user.getInt("numGames");
                GlobalTotalScores[i] = user.getInt("totalScore");
            }

            FriendsNames = new String[friends.length()];
            FriendsImages = new Bitmap[friends.length()];
            FriendsNumGames = new int[friends.length()];
            FriendsTotalScores = new int[friends.length()];

            for(int i=0; i<friends.length(); i++){
                JSONObject user = (JSONObject) friends.get(i);
                FriendsNames[i] = user.getString("FirstName");
                FriendsImages[i] =  new DownloadImageTask().execute(user.getString("ImageURL")).get();
                FriendsNumGames[i] = user.getInt("numGames");
                FriendsTotalScores[i] = user.getInt("totalScore");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((Button)findViewById(R.id.FriendsBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setListAdapter(new ScoresListAdapter(TrophyScreen.this, android.R.layout.simple_list_item_1, R.id.textView,
                        FriendsNames, FriendsImages, FriendsTotalScores, FriendsNumGames));
            }
        });

        ((Button)findViewById(R.id.GlobalBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setListAdapter(new ScoresListAdapter(TrophyScreen.this, android.R.layout.simple_list_item_1, R.id.textView,
                        GlobalNames, GlobalImages, GlobalTotalScores, GlobalNumGames));
            }
        });

        //By default, calling the friends hisg scores
        setListAdapter(new ScoresListAdapter(TrophyScreen.this, android.R.layout.simple_list_item_1, R.id.textView,
                GlobalNames, GlobalImages, GlobalTotalScores, GlobalNumGames));
    }


    private class ScoresListAdapter extends ArrayAdapter<String> {
        private String[] names;
        Bitmap[] images;
        private int[] totalScores, numGames;
        public ScoresListAdapter(Context context, int resource, int textViewResourceId, String[] _names, Bitmap[] _images,
                                 int[] _totalScores, int[] _numGames) {
            super(context, resource, textViewResourceId, _names);
            names = _names;
            images = _images;
            totalScores = _totalScores;
            numGames = _numGames;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View  row = inflator.inflate(R.layout.trophy_list_item, parent, false);
            final ImageView image = (ImageView) row.findViewById(R.id.userImage);

            if(names[position].equals(myProfile.getFirstName()))
                ((TextView) row.findViewById(R.id.NameTV)).setText("You");
            else
                ((TextView) row.findViewById(R.id.NameTV)).setText(names[position]);

            ((TextView) row.findViewById(R.id.place)).setText(Integer.toString(position+1));
            ((TextView) row.findViewById(R.id.TotalScoresTV)).setText(Integer.toString(totalScores[position]));
            ((TextView) row.findViewById(R.id.numGamesTV)).setText(Integer.toString(numGames[position]));
            ((ImageView)row.findViewById(R.id.userImage)).setImageBitmap(images[position]);

            return row;
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