package team2.urbanrun;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


public class WaitingScreen extends ListActivity {
    Profile myProfile = Profile.getCurrentProfile();
    GoogleMap map;
    String GameID;
    String statuses[]; //Wait, Accepted, Declined
    Timer timer;
    Bitmap accept, declined;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_screen);
        final Profile myProfile = Profile.getCurrentProfile();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        GameID = getIntent().getExtras().getString("GameID");
        accept = (BitmapFactory.decodeResource(getResources(), R.drawable.approved));
        declined = (BitmapFactory.decodeResource(getResources(), R.drawable.decline));
        int Radius = 0;
        double centerLat = 0, centerLng = 0;
        String[] names = {}, images = {}, ids = {};
        try {
            String res = (new ServletGetGameProperties().execute(GameID, myProfile.getId(), "WaitingScreen")).get();
            Log.d("Aviv", "Response from servlet: " + res);
            if (res.equals("started"))   //Game already begun, returns to the main screen
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(WaitingScreen.this);
                builder.setMessage("The game has already begun");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        startActivity(new Intent(WaitingScreen.this, MainScreen.class));
                        finish();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            else{
                JSONObject response = new JSONObject(res);
                int time = response.getInt("time");
                int radius = response.getInt("Radius");
                double CenterLat = response.getDouble("centerLat");
                double CenterLng = response.getDouble("centerLng");
                JSONArray array = response.getJSONArray("Players");

                //////////set time ////////
                String minutes, seconds;
                minutes = Integer.toString(time / 60);
                int secs = time % 60;
                if (secs > 10)
                    seconds = Integer.toString(secs);
                else
                    seconds = "0" + Integer.toString(secs);
                ((TextView) findViewById(R.id.GameTime)).setText(minutes + ":" + seconds);

                Circle arena = map.addCircle(new CircleOptions().center(new LatLng(CenterLat, CenterLng)));
                arena.setRadius(radius);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(arena.getCenter(), 16));

                int numPlayers = array.length();
                names = new String[numPlayers];
                images = new String[numPlayers];
                ids = new String[numPlayers];
                statuses = new String[numPlayers];
                ///////// set players //////////
                for (int i = 0; i < numPlayers; i++) {
                    JSONObject player = array.getJSONObject(i);
                    ids[i] = player.getString("ID");    //maybe dont need it, because the servlet return them sorting by the id's
                    names[i] = player.getString("FirstName") + " " + player.getString("LastName");
                    images[i] = player.getString("ImageURL");
                    statuses[i] = "wait";
                }


                setListAdapter(new FriendsListAdapter(this, android.R.layout.simple_list_item_1, R.id.textView, names, images, ids));

                final Button Startbtn = (Button) findViewById(R.id.startButton);
                final Button acceptBtn = (Button) findViewById(R.id.AcceptBtn);
                final Button declineBtn = (Button) findViewById(R.id.DeclineBtn);
                if (getIntent().getExtras().getBoolean("isCreator")) {
                    acceptBtn.setVisibility(View.GONE);
                    declineBtn.setVisibility(View.GONE);
                    (findViewById(R.id.startButton)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                    //first, check that at least 1 friend accepted the game
                    int countAccepted=0;
                    for(int i=0; i<statuses.length; i++){
                         if(statuses[i].equals("Accepted"))
                                    countAccepted++;
                         }
                         if(countAccepted==0)
                         {
                                AlertDialog.Builder builder = new AlertDialog.Builder(WaitingScreen.this);
                                builder.setMessage("You cant start a game alone, must at least 1 friend to accept");
                                builder.setCancelable(true);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                            else {
                                Intent intent = new Intent(WaitingScreen.this, GameActivity.class);
                                try {
                                    Log.d("Aviv", (new ServletGameStart().execute(GameID)).get());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                                intent.putExtra("GameID", GameID);
                                startActivity(intent);
                            }
                        }
                    });
                } else {
                    Startbtn.setVisibility(View.GONE);
                    declineBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(WaitingScreen.this);
                            builder.setMessage("Once you declined the invitation, you won't be able to re-join the game");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Decline", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new ServletDeclined().execute(myProfile.getId(), GameID);
                                    Intent intent = new Intent(WaitingScreen.this, MainScreen.class);
                                    dialog.cancel();
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            builder.setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
                    acceptBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ServletAccept().execute(myProfile.getId(), GameID);
                            //TODO: toast a message
                            acceptBtn.setVisibility(View.GONE);
                        }
                    });
                }

                final Bitmap accept, declined, waiting;
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String res = null;
                try {
                    res = (new ServletCheckStatuses().execute(GameID, myProfile.getId())).get();
                    Log.d("Aviv", "Result from servlet: " + res);
                    boolean isStart = new JSONObject(res).getBoolean("isStart");
                    if (isStart) {
                        Intent intent = new Intent(WaitingScreen.this, GameActivity.class);
                        intent.putExtra("GameID", GameID);
                        startActivity(intent);
                        finish();
                    }
                    JSONArray arr = new JSONObject(res).getJSONArray("status");
                    for (int i = 0; i < arr.length(); i++) {
                        String temp = arr.getString(i);
                        if (!statuses[i].equals(temp)) {
                            statuses[i] = temp;
                            if (temp.equals("Accepted")) {
                                Log.d("Aviv", "change " + i + " to Accepted");
                            }
                            if (temp.equals("Declined")) {
                                Log.d("Aviv", "change " + i + " to Declined");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 2000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        timer.cancel();
    }

    private class FriendsListAdapter extends ArrayAdapter<String> {
        private String[] names, images, ids;
        private Bitmap waiting, creator;


        public FriendsListAdapter(Context context, int resource, int textViewResourceId, String[] _names, String[] _img, String[] _ids) {
            super(context, resource, textViewResourceId, _names);
            names = _names;
            images = _img;
            ids = _ids;
            waiting = (BitmapFactory.decodeResource(getResources(), R.drawable.waiting));
            creator = (BitmapFactory.decodeResource(getResources(), R.drawable.inviter));
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View  row = inflator.inflate(R.layout.friend_status_list_item, parent, false);
            final ImageView iv = (ImageView) row.findViewById(R.id.userImage);
            final TextView tv = (TextView) row.findViewById(R.id.userName);
            final ImageView status = (ImageView) row.findViewById(R.id.userStatus);

            try {
                iv.setImageBitmap((new DownloadImageTask().execute(images[position])).get());
            }catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            tv.setText(names[position]);
            if(!getIntent().getExtras().getBoolean("isCreator") &&
                    getIntent().getExtras().getString("creator").equals(ids[position])) {
                status.setImageBitmap(creator);
                statuses[position] = "creator";
            }
            else
                status.setImageBitmap(waiting);

            return row;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_waiting_screen, menu);
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