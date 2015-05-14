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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;
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

    Profile myProfile;
    GoogleMap map;
    Circle Arena;
    String GameID; //TODO: send servlet in the begining of the game that gives you the GameID
    int numPlayers;

    /* Multiplayer start*/
    String[] firstNames;
    Bitmap[] icons;
    Marker[] players;
    Marker myLoc;
    /* Multiplayer end*/

    //prize location
    Marker[] elements;

    //scores
    int scores[];

    //temporary
    boolean endGame = true;

    //TextViews
    TextView countDown;
    TextView myScoreTV;
    TextView opp1scoreTV, opp1nameTV, opp2scoreTV, opp2nameTV, opp3scoreTV, opp3nameTV;
    ImageView opp1img, opp2img, opp3img;

    //clock
    Timer timer;

    long secondsLeft;

    //Markers icons
    Bitmap goldCoin, silverCoin, bronzeCoin, diamond, box;

    //sounds
    MediaPlayer CoinsSound;
    MediaPlayer whistle;

    String res;
    int stage;  //0 - wait to start , 1- start, 2- ended

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Log.d("Aviv", "GameActivity - onCreate");

        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        myProfile = Profile.getCurrentProfile();
        GameID= getIntent().getExtras().getString("GameID");
        int Radius=0;
        double centerLat=0,centerLng=0;
        try {
            JSONObject json = new JSONObject((new ServletGetGameProperties().execute(GameID,myProfile.getId(),"GameScreen")).get());
            Log.d("Aviv","json: "+json.toString());
            Radius = json.getInt("Radius");
            centerLat = json.getDouble("centerLat");
            centerLng = json.getDouble("centerLng");

            CircleOptions circOp = new CircleOptions().center(new LatLng(centerLat,centerLng)).radius(Radius);
            Arena = map.addCircle(circOp);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(circOp.getCenter(), 18));

            JSONArray playersInfo = json.getJSONArray("Players");

            numPlayers = playersInfo.length();
            Log.d("Aviv","Num players: "+numPlayers);
            players = new Marker[numPlayers];
            firstNames = new String[numPlayers];
            scores = new int[numPlayers];
            icons = new Bitmap[numPlayers];
            elements = new Marker[AppConstants.MAX_ELEMENTS*numPlayers];
            //TEMPORARY FOR 1vs1
            for(int i=0; i<playersInfo.length();i++) {
                players[i] = map.addMarker(new MarkerOptions().position(new LatLng(0,0)));
                firstNames[i] = ((JSONObject)playersInfo.get(i)).getString("FirstName");
                icons[i] =  new DownloadImageTask().execute(((JSONObject)playersInfo.get(i)).getString("ImageURL")).get();
                players[i].setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(icons[i],50,50,false)));
                scores[i] = 0;
            }

            Bitmap myImage = (new DownloadImageTask().execute(myProfile.getProfilePictureUri(40,40).toString())).get();
            ((ImageView)findViewById(R.id.myImage)).setImageBitmap(myImage);
            myLoc = map.addMarker(new MarkerOptions().position(new LatLng(0,0)));;
            myLoc.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(myImage,50,50,false)));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        goldCoin = (BitmapFactory.decodeResource(getResources(), R.drawable.gold40));
        silverCoin = (BitmapFactory.decodeResource(getResources(), R.drawable.silver40));
        bronzeCoin = (BitmapFactory.decodeResource(getResources(), R.drawable.bronze40));
        diamond = (BitmapFactory.decodeResource(getResources(), R.drawable.diamond40));

        //settings
        UiSettings settings = map.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setMyLocationButtonEnabled(true);

        countDown = (TextView)findViewById(R.id.clockTextView);
        myScoreTV = (TextView)findViewById(R.id.MyScore);

        opp1scoreTV = (TextView)findViewById(R.id.opp1score);
        opp2scoreTV = (TextView)findViewById(R.id.opp2score);
        opp3scoreTV = (TextView)findViewById(R.id.opp3score);

        opp1nameTV = (TextView)findViewById(R.id.opp1name);
        opp2nameTV = (TextView)findViewById(R.id.opp2name);
        opp3nameTV = (TextView)findViewById(R.id.opp3name);

        opp1img = (ImageView)findViewById(R.id.opp1img);
        opp2img = (ImageView)findViewById(R.id.opp2img);
        opp3img = (ImageView)findViewById(R.id.opp3img);

        //hide everything until the game starts
        opp1nameTV.setVisibility(View.INVISIBLE);
        opp2nameTV.setVisibility(View.INVISIBLE);
        opp3nameTV.setVisibility(View.INVISIBLE);
        opp1img.setVisibility(View.INVISIBLE);
        opp2img.setVisibility(View.INVISIBLE);
        opp3img.setVisibility(View.INVISIBLE);
        opp1scoreTV.setVisibility(View.INVISIBLE);
        opp2scoreTV.setVisibility(View.INVISIBLE);
        opp3scoreTV.setVisibility(View.INVISIBLE);

        if(numPlayers == 1)
        {
            Log.d("Aviv","1 opp");
            opp1img.setVisibility(View.VISIBLE);
            opp1nameTV.setVisibility(View.VISIBLE);
            opp1scoreTV.setVisibility(View.VISIBLE);
            opp1img.setImageBitmap(icons[0]);
            opp1nameTV.setText(firstNames[0]);
            opp1scoreTV.setText("0");

            opp2nameTV.setVisibility(View.GONE);
            opp2scoreTV.setVisibility(View.GONE);
            opp2img.setVisibility(View.GONE);
            opp3nameTV.setVisibility(View.GONE);
            opp3scoreTV.setVisibility(View.GONE);
            opp3img.setVisibility(View.GONE);
        }else{
            if(numPlayers == 2){
                Log.d("Aviv","2 opps");
                opp1img.setVisibility(View.VISIBLE);
                opp1nameTV.setVisibility(View.VISIBLE);
                opp1scoreTV.setVisibility(View.VISIBLE);
                opp2nameTV.setVisibility(View.VISIBLE);
                opp2scoreTV.setVisibility(View.VISIBLE);
                opp2img.setVisibility(View.VISIBLE);
                opp1img.setImageBitmap(icons[0]);
                opp1nameTV.setText(firstNames[0]);
                opp1scoreTV.setText("0");
                opp1img.setImageBitmap(icons[1]);
                opp1nameTV.setText(firstNames[1]);
                opp1scoreTV.setText("0");

                opp3nameTV.setVisibility(View.GONE);
                opp3scoreTV.setVisibility(View.GONE);
                opp3img.setVisibility(View.GONE);
            }
            else{
                Log.d("Aviv","3 opps");
                opp1img.setVisibility(View.VISIBLE);
                opp1nameTV.setVisibility(View.VISIBLE);
                opp1scoreTV.setVisibility(View.VISIBLE);
                opp2nameTV.setVisibility(View.VISIBLE);
                opp2scoreTV.setVisibility(View.VISIBLE);
                opp2img.setVisibility(View.VISIBLE);
                opp3nameTV.setVisibility(View.VISIBLE);
                opp3scoreTV.setVisibility(View.VISIBLE);
                opp3img.setVisibility(View.VISIBLE);
                opp1img.setImageBitmap(icons[0]);
                opp1nameTV.setText(firstNames[0]);
                opp1scoreTV.setText("0");
                opp1img.setImageBitmap(icons[1]);
                opp1nameTV.setText(firstNames[1]);
                opp1scoreTV.setText("0");
                opp1img.setImageBitmap(icons[2]);
                opp1nameTV.setText(firstNames[2]);
                opp1scoreTV.setText("0");
            }
        }

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

        CoinsSound = MediaPlayer.create(GameActivity.this, R.raw.coins);
        whistle = MediaPlayer.create(GameActivity.this,R.raw.coach_whistle);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String str;
                        try {
                            if (stage == 0) {
                                str = (new ServletReadySetGo().execute(GameID)).get();
                                Log.d("Aviv", "second till the game start: "+str);
                                JSONObject object = new JSONObject(str);
                                String status = object.getString("status");
                                if(status.equals("start")){
                                    Log.d("Aviv","Starting the game!!!");
                                    stage = 1;
                                    whistle.start();
                                }
                                else{
                                    countDown.setText(Integer.toString(object.getInt("time")) + " seconds to the game");
                                }
                            }
                            if(stage==1){
                                str = (new ServletSendLocation().execute(myProfile.getId(), GameID, Double.toString(
                                        myLoc.getPosition().latitude),Double.toString(myLoc.getPosition().longitude))).get();
                                Log.d("Aviv", str);
                                JSONObject object = new JSONObject(str);
                                int myScore, oppScore, sound;
                                JSONArray playersJson = object.getJSONArray("Players");
                                JSONArray elementsArr = object.getJSONArray("Elements");
                                secondsLeft = object.getLong("timeLeft");
                                sound = object.getInt("sound");
                                myScore = object.getInt("myScore");

                                if (sound == 1)
                                    CoinsSound.start();

                                if (secondsLeft <= 0) {
                                    stage = 3;  //so won't call EndOfGame twice (async shit...)
                                    EndOfGame();
                                }

                                //DEBUG
                                if (numPlayers != playersJson.length())
                                    Log.e("Aviv", "Num players form json (" + Integer.toString(playersJson.length()) +
                                            ") not equal to numPlayer from the game propoerties (" + Integer.toString(numPlayers) + ")");

                                //PLAYERS
                                for (int i = 0; i < numPlayers; i++) {
                                    JSONObject obj = (JSONObject) playersJson.get(i);
                                    scores[i] = obj.getInt("score");
                                    players[i].setPosition(new LatLng(obj.getDouble("Lat"), obj.getDouble("Lng")));
                                }

                                //find best 3 opponents
                                int firstIndex = -1, secondIndex = -1, thirdIndex = -1, firstScore = 0, secondScore = 0, thirdScore = 0;
                                for (int i = playersJson.length() - 1; i >= 0; i--) {
                                        if (scores[i] >= firstScore) {
                                            thirdScore = secondScore;
                                            secondScore = firstScore;
                                            firstScore = scores[i];

                                            thirdIndex = secondIndex;
                                            secondIndex = firstIndex;
                                            firstIndex = i;
                                        } else {
                                            if (scores[i] >= secondScore) {
                                                thirdScore = secondScore;
                                                secondScore = scores[i];

                                                thirdIndex = secondIndex;
                                                secondIndex = i;
                                            } else if (scores[i] >= thirdScore) {
                                                thirdScore = scores[i];
                                                thirdIndex = i;
                                            }
                                        }
                                }

                                myScoreTV.setText(Integer.toString(myScore));
                                opp1nameTV.setText(firstNames[firstIndex]);
                                opp1img.setImageBitmap(icons[firstIndex]);
                                opp1scoreTV.setText(Integer.toString(scores[firstIndex]));

                                if (numPlayers > 2) {
                                    opp2nameTV.setText(firstNames[secondIndex]);
                                    opp2img.setImageBitmap(icons[secondIndex]);
                                    opp2scoreTV.setText(Integer.toString(scores[secondIndex]));
                                }

                                if (numPlayers > 3) {
                                    opp3nameTV.setText(firstNames[thirdIndex]);
                                    opp3img.setImageBitmap(icons[thirdIndex]);
                                    opp3scoreTV.setText(Integer.toString(scores[thirdIndex]));
                                }

                                //ELEMENTS
                                int i = 0, type;
                                for (; i < elementsArr.length(); i++) {
                                    JSONObject elem;
                                    elem = (JSONObject) elementsArr.get(i);
                                    type = elem.getInt("type");
                                    elements[i].setPosition(new LatLng(elem.getDouble("Lat"), elem.getDouble("Lng")));

                                    switch (type) {
                                        case 0:
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(diamond));
                                            break;
                                        case 1:
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(box));
                                            break;
                                        case 2:
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(box));
                                            break;
                                        case 3:
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(goldCoin));
                                            break;
                                        case 4:
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(box));
                                            break;
                                        case 5:
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(box));
                                            break;
                                        case 6:
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(silverCoin));
                                            break;
                                        case 7:
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(bronzeCoin));
                                            break;
                                        default:
                                            elements[i].setIcon(BitmapDescriptorFactory.fromBitmap(box));
                                            break;
                                    }
                                    elements[i].setVisible(true);
                                }
                                for (; i < AppConstants.MAX_ELEMENTS; i++)   //hide all the unused elements
                                    elements[i].setVisible(false);

                                setTime(secondsLeft);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    };
                });
            };
        },0,1000);   //every second
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d("Aviv","GameActivity - onDestroy");
        //TODO: sending servlet to the server that user left the game or unavailable... (What do we do when user get phone call?)
        timer.cancel();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setMessage("Are you sure you want to quit the game?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: sending to the servlet that i left the game
                startActivity(new Intent(GameActivity.this, MainScreen.class));
                finish();
            }
        });
        builder.setNegativeButton("Stay", null);
        AlertDialog alert = builder.create();
        alert.show();
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
            intent.putExtra("myName", myProfile.getId());
            intent.putExtra("GameID", GameID);
            startActivity(intent);
            finish();
        }
    }
}