package team2.urbanrun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class GameActivity extends Activity {


    GoogleMap map;
    Circle Arena;
    String GameID; //TODO: send servlet in the begining of the game that gives you the GameID

    String myName;

    JSONArray players;
    //JSONObject[] opps;
    //temporary
    JSONObject opp,me;

    //prize location
    Marker[] elements = new Marker[AppConstants.MAX_ELEMENTS];

    //scores
    int oppScore;
    int myScore;

    //my and opponent locations
    Marker myLoc;
    Marker oppLoc;

    //temporary
    boolean endGame = true;

    //TextViews
    TextView countDown;
    TextView myScoreTV;
    TextView oppScoreTV;

    //clock
    Timer timer;
    TimerTask task;
    long secondsLeft;

    //Markers icons
    Bitmap goldCoin, silverCoin, bronzeCoin;

    //sounds
    MediaPlayer CoinsSound;
    MediaPlayer whistle;

    String res;
    int stage;  //0 - wait for response, 1- start, 2-declined

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Log.d("Aviv", "GameActivity - onCreate");

        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        myName = getIntent().getExtras().getString("myName");

        GameID= getIntent().getExtras().getString("GameID");
        int Radius=0;
        double centerLat=0,centerLng=0;
        try {
            JSONObject json = new JSONObject((new ServletGetGameProperties().execute(GameID)).get());
            Log.d("Aviv","json: "+json.toString());
            Radius = json.getInt("Radius");
            centerLat = json.getDouble("centerLat");
            centerLng = json.getDouble("centerLng");

            CircleOptions circOp = new CircleOptions().center(new LatLng(centerLat,centerLng)).radius(Radius);
            Arena = map.addCircle(circOp);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(circOp.getCenter(), 16));

            players = json.getJSONArray("Players");

            //TEMPORARY FOR 1vs1
            for(int i=0; i<players.length();i++) {
                if(((JSONObject)players.get(i)).getString("Username").equals(myName))
                    me = (JSONObject) players.get(i);
                else
                    opp = (JSONObject) players.get(i);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //appleIcon = BitmapFactory.decodeResource(getResources(), R.drawable.apple_icon);
        goldCoin = BitmapFactory.decodeResource(getResources(), R.drawable.gold_coin);
        silverCoin = BitmapFactory.decodeResource(getResources(), R.drawable.silver_coin);
        bronzeCoin = BitmapFactory.decodeResource(getResources(), R.drawable.bronze_coin);

        //settings
        UiSettings settings = map.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setMyLocationButtonEnabled(true);

        oppScore = 0;
        myScore = 0;
        oppLoc = map.addMarker(new MarkerOptions().position(new LatLng(0,0)));
        myLoc = map.addMarker(new MarkerOptions().position(new LatLng(0,0)));
        try {
            ((TextView)findViewById(R.id.opponentName)).setText(opp.getString("FirstName"));
            Bitmap oppImg = new DownloadImageTask().execute(opp.getString("ImageURL")).get();
            Bitmap myImg = new DownloadImageTask().execute(me.getString("ImageURL")).get();
            oppLoc.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(oppImg,50,50,false)));
            myLoc.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(myImg,50,50,false)));
            ((ImageView)findViewById(R.id.opponentImg)).setImageBitmap(oppImg);
            ((ImageView)findViewById(R.id.myImage)).setImageBitmap(myImg);
            countDown = (TextView)findViewById(R.id.clockTextView);
            countDown.setText("Waiting for "+opp.getString("FirstName")+"...");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        oppScoreTV = (TextView)findViewById(R.id.opponentScore);
        myScoreTV = (TextView)findViewById(R.id.MyScore);

        oppScoreTV.setText("0");
        myScoreTV.setText("0");

        //initialize elements array
        for(int i=0; i<AppConstants.MAX_ELEMENTS; i++)
        {
            elements[i] = map.addMarker(new MarkerOptions().position(new LatLng(0,0)));
            elements[i].setVisible(false);
        }

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onLocationChanged(Location location) {
                myLoc.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
            }
        });

        stage = 0;

        task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(stage==1) {
                            //sending to the server the location
                            String str;
                            try {
                                str = (new ServletSendLocation().execute(myName, GameID, Double.toString(myLoc.getPosition().latitude), Double.toString(myLoc.getPosition().longitude))).get();
                                Log.d("Aviv",str);
                                JSONObject object = new JSONObject(str);
                                double oppLat,oppLng,prizeLat,prizeLng;
                                int myScore, oppScore, sound;
                                oppLat = object.getDouble("oppLat");
                                oppLng = object.getDouble("oppLng");
                                JSONArray elementsArr = object.getJSONArray("Elements");
                                myScore = object.getInt("myScore");
                                oppScore = object.getInt("oppScore");
                                secondsLeft = object.getLong("timeLeft");
                                sound = object.getInt("sound");

                                if(sound==1)
                                    CoinsSound.start();

                                if(secondsLeft <= 0){
                                    stage = 3;  //so won't call EndOfGame twice (async shit...)
                                    EndOfGame();
                                }
                                myScoreTV.setText(Integer.toString(myScore));
                                oppScoreTV.setText(Integer.toString(oppScore));

                                oppLoc.setPosition(new LatLng(oppLat,oppLng));

                                int i=0, type;
                                JSONObject elem;
                                for(; i<elementsArr.length(); i++)
                                {
                                    elem = (JSONObject) elementsArr.get(i);
                                    type = elem.getInt("type");
                                    elements[i].setPosition(new LatLng(elem.getDouble("Lat"),elem.getDouble("Lng")));
                                    if(type==0) //bronze coin
                                        elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(bronzeCoin));
                                    else if(type==1)    //silver coin
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(silverCoin));
                                         else   //gold coin
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(goldCoin));
                                    //TODO: weapons
                                    elements[i].setVisible(true);
                                }
                                for(;i<AppConstants.MAX_ELEMENTS;i++)   //hide all the unused elements
                                    elements[i].setVisible(false);

                                setTime(secondsLeft);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if(stage==0){
                            try {
                                res = (new ServletGameStart().execute(myName, GameID)).get();
                                if (res.equals("Wait"))   //still wait to the opp...
                                {
                                    Log.d("Aviv", "still waiting for " + opp.getString("FirstName"));
                                    //TODO: waiting "circle" (like in YouTube) and message to the user, meanwhile in the counterClock TextView
                                } else if (res.equals("Declined")) {
                                    stage=2;
                                } else {               //opponent is ready, starting the gmae
                                    whistle.start();
                                    stage=1;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                       if(stage==2) //opp declined
                       {
                           AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                           try {
                               builder.setMessage(opp.getString("FirstName") + " declined your invitation to play :(");
                           } catch (JSONException e) {
                               e.printStackTrace();
                           }
                           builder.setCancelable(false);
                           builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog, int which) {
                                   finish();
                               }
                           });
                           AlertDialog alert = builder.create();
                           alert.show();
                       }
                    }
                });
            }
        };
        //CoinsSound = MediaPlayer.create(GameActivity.this,R.raw.eating_an_apple_loudly_);
        CoinsSound = MediaPlayer.create(GameActivity.this, R.raw.coins);
        whistle = MediaPlayer.create(GameActivity.this,R.raw.coach_whistle);

        timer = new Timer();
        timer.scheduleAtFixedRate(task,0,1000);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d("Aviv","GameActivity - onDestroy");
        //TODO: sending servlet to the server that user left the game or unavailable... (What do we do when user get phone call?)
        timer.cancel();
    }


    void setTime(long secondsLeft)
    {
        int minutes = (int)(secondsLeft/60);
        int seconds = (int)(secondsLeft%60);
        countDown.setText(String.format("%02d:%02d",minutes,seconds));
    }

    void EndOfGame()
    {
        if(endGame) {
            endGame = false; //so EndOfGame will active only once (async servlets call it some times and opens some EndScreen activities)
            whistle.start();
            Intent intent = new Intent(GameActivity.this, EndGameActivity.class);
            intent.putExtra("myName", myName);
            intent.putExtra("GameID", GameID);
            startActivity(intent);
            finish();
        }
    }
}