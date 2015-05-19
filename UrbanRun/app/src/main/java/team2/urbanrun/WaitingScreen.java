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
    String ids[]={}, names[]={}, statuses[]={}; //Wait, Accepted, Declined
    Bitmap[] images={};
    Timer timer;
    Bitmap accept, declined, creator, waiting;
    boolean isAlreadyBegun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_screen);
        final Profile myProfile = Profile.getCurrentProfile();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        GameID = getIntent().getExtras().getString("GameID");
        accept = (BitmapFactory.decodeResource(getResources(), R.drawable.approved));
        declined = (BitmapFactory.decodeResource(getResources(), R.drawable.decline));
        waiting = (BitmapFactory.decodeResource(getResources(), R.drawable.waiting));
        creator = (BitmapFactory.decodeResource(getResources(), R.drawable.inviter));
        timer = null;
        int Radius = 0;
        double centerLat = 0, centerLng = 0;
        try {
            String res = (new ServletGetGameProperties().execute(GameID, myProfile.getId(), "WaitingScreen")).get();
            if (res.equals("started"))   //Game already begun, returns to the main screen
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(WaitingScreen.this);
                builder.setMessage("The game has already begun");
                isAlreadyBegun = true;
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
                images = new Bitmap[numPlayers];
                ids = new String[numPlayers];
                statuses = new String[numPlayers];
                ///////// set players //////////
                for (int i = 0; i < numPlayers; i++) {
                    JSONObject player = array.getJSONObject(i);
                    ids[i] = player.getString("ID");    //maybe dont need it, because the servlet return them sorting by the id's
                    names[i] = player.getString("FirstName") + " " + player.getString("LastName");
                    images[i] = (new DownloadImageTask().execute(player.getString("ImageURL"))).get();
                    statuses[i] = "";
                }

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
                                new ServletGameStart().execute(GameID);
                                intent.putExtra("GameID", GameID);
                                startActivity(intent);
                                finish();
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
                            acceptBtn.setVisibility(View.INVISIBLE);
                            acceptBtn.setOnClickListener(null);
                        }
                    });

                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(timer!=null)
            timer.cancel();
    }

    @Override
    protected  void onResume(){
        super.onResume();
        if(!isAlreadyBegun){
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String res = (new ServletCheckStatuses().execute(GameID, myProfile.getId())).get();
                        Log.d("Aviv", "Result from servlet: " + res);
                        String isStart = new JSONObject(res).getString("isStart");  //"true", "false" or "canceled"
                        if (isStart.equals("true")) {
                            Intent intent = new Intent(WaitingScreen.this, GameActivity.class);
                            intent.putExtra("GameID", GameID);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            if (isStart.equals("canceled")) {
                                WaitingScreen.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new ServletDeclined().execute();    //so the user won't get anymore the game invitations
                                        AlertDialog.Builder builder = new AlertDialog.Builder(WaitingScreen.this);
                                        builder.setMessage("The game was canceled, return to the main screen");
                                        builder.setCancelable(false);
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                startActivity(new Intent(WaitingScreen.this, MainScreen.class));
                                                finish();
                                                timer.cancel();
                                            }
                                        });
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    }
                                });
                            }
                            JSONArray arr = new JSONObject(res).getJSONArray("status");
                            boolean flag = false;
                            for (int i = 0; i < arr.length(); i++) {
                                String temp = arr.getString(i);
                                if (!statuses[i].equals(temp)) {
                                    statuses[i] = temp;
                                    flag = true;
                                }
                            }
                            if (flag) {
                                WaitingScreen.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setListAdapter(new FriendsListAdapter(WaitingScreen.this, android.R.layout.simple_list_item_1, R.id.textView, ids));
                                    }
                                });
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
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WaitingScreen.this);
        if(getIntent().getExtras().getBoolean("isCreator")){
            builder.setMessage("If you choose to leave, the game will be canceled. Are you sure?");
            builder.setCancelable(false);
            builder.setPositiveButton("Cancel game", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new ServletCancelGame().execute(GameID);
                    startActivity(new Intent(WaitingScreen.this, MainScreen.class));
                    finish();
                }
            });
            builder.setNegativeButton("Stay", null);
            AlertDialog alert = builder.create();
            alert.show();
        }
        else{
            builder.setMessage("Decline the invitation?");
            builder.setCancelable(false);
            builder.setPositiveButton("Decline", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new ServletDeclined().execute(myProfile.getId(), GameID);
                    startActivity(new Intent(WaitingScreen.this, MainScreen.class));
                    finish();
                }
            });
            builder.setNegativeButton("Stay", null);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private class FriendsListAdapter extends ArrayAdapter<String> {

        public FriendsListAdapter(Context context, int resource, int textViewResourceId, String[] _ids) {
            super(context, resource, textViewResourceId, _ids);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View  row = inflator.inflate(R.layout.friend_status_list_item, parent, false);
            ((ImageView) row.findViewById(R.id.userImage)).setImageBitmap(images[position]);
            ((TextView) row.findViewById(R.id.userName)).setText(names[position]);
            final ImageView status = (ImageView) row.findViewById(R.id.userStatus);

            if(statuses[position].equals("wait"))
                status.setImageBitmap(waiting);
            else{
                if(statuses[position].equals("Accepted"))
                    status.setImageBitmap(accept);
                else{
                    if(statuses[position].equals("Declined"))
                        status.setImageBitmap(declined);
                    else
                        status.setImageBitmap(creator);
                }
            }
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