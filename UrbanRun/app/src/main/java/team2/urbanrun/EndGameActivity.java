package team2.urbanrun;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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


public class EndGameActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);
        int[] scores={};
        int myIndex=0;
        String[] names={},images={};
        try {
            String res=(new ServletGameScores().execute(getIntent().getExtras().getString("GameID"),
                    getIntent().getExtras().getString("myName")).get());
            Log.d("Aviv", "Response from servlet: " + res);
            JSONArray array = new JSONArray(res);

            int numPlayers = array.length();
            scores = new int[numPlayers];
            names = new String[numPlayers];
            images = new String[numPlayers];
            for(int i=0; i<numPlayers; i++){
                JSONObject player = array.getJSONObject(i);
                names[i]=player.getString("FullName");
                images[i]= player.getString("ImageURL");
                scores[i]=player.getInt("score");
                if(player.getString("Username").equals(getIntent().getExtras().getString("myName")))
                    myIndex=i;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        setListAdapter(new EndGameAdapter(this, android.R.layout.simple_list_item_1, R.id.textView ,
                names, images ,scores, myIndex));

        ((Button)findViewById(R.id.newGameButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //return to the friend choosing activity
            }
        });
    }

    private class EndGameAdapter extends ArrayAdapter<String> {
        private String[] names, images;
        int[] scores;
        int myIndex;
        public EndGameAdapter(Context context, int resource, int textViewResourceId
                         , String[] _names, String[] _img, int _scores[], int _myIndex) {
            super(context, resource, textViewResourceId, _names);
            names = _names;
            images = _img;
            scores = _scores;
            myIndex = _myIndex;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View  row = inflator.inflate(R.layout.end_game_list_item, parent, false);
            final ImageView iv = (ImageView) row.findViewById(R.id.userImage);
            final ImageView label = (ImageView) row.findViewById(R.id.imgLabel);
            final TextView tv = (TextView) row.findViewById(R.id.name);
            final TextView scr = (TextView) row.findViewById(R.id.score);

            if(position==myIndex)
                tv.setText("You");
            else
                tv.setText(names[position]);
            scr.setText(Integer.toString(scores[position]));
            try {
                URL url = new URL(images[position]);
                Bitmap img = new DownloadImageTask().execute(images[position]).get();
                iv.setImageBitmap(img);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            //adding a image for each place
            if(position==0)
                label.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.first_place));
            if(position==1)
                label.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.second_place));
            if(position==2)
                label.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.third_place));
            if(position>2)
                label.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.sad));
            return row;
        }
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
