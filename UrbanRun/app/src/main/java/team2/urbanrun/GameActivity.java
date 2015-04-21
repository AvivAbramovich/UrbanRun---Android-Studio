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
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;

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

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GameActivity extends Activity {

    GoogleMap map;
    Circle Arena;

    String GameID; //TODO: send servlet in the begining of the game that gives you the GameID

    String myName;
    String oppName;

    String isCreator;

    //prize location
    Marker prize;

    //scores
    int oppScore;
    int myScore;

    //my and opponent locations
    Marker myLoc;
    Marker oppLoc;

    //temporary
    boolean flag;

    //TextViews
    TextView countDown;
    TextView myScoreTV;
    TextView oppScoreTV;

    //clock
    CounterClock myClock;
    long secondsLeft;

    //Markers icons
    Bitmap appleIcon;
    Bitmap myIcon;
    Bitmap oppIcon;

    String res;
    boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Log.d("Aviv", "GameActivity - onCreate");

        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        flag = false;

        //TODO: send servlet in the begining of the game that gives you the GameID

        appleIcon = BitmapFactory.decodeResource(getResources(), R.drawable.apple_icon);
        //my icon for the marker and resize it to 20x20
        //myIcon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.aviv),50,50,false);
        myIcon = Bitmap.createScaledBitmap((Bitmap)getIntent().getExtras().getParcelable("myImage"),50,50,false);
        oppIcon = Bitmap.createScaledBitmap((Bitmap)getIntent().getExtras().getParcelable("oppImage"),50,50,false);

        //settings
        UiSettings settings = map.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setMyLocationButtonEnabled(true);

        oppScore = 0;
        myScore = 0;
        myName = getIntent().getExtras().getString("myName");
        oppName = getIntent().getExtras().getString("oppName");

        ((TextView)findViewById(R.id.opponentName)).setText(oppName);
        ((ImageView)findViewById(R.id.opponentImg)).setImageBitmap((Bitmap)getIntent().getExtras().getParcelable("oppImage"));
        oppScoreTV = (TextView)findViewById(R.id.opponentScore);
        myScoreTV = (TextView)findViewById(R.id.MyScore);

        oppScoreTV.setText("0");
        myScoreTV.setText("0");

        countDown = (TextView)findViewById(R.id.clockTextView);
        countDown.setText("Waiting for "+oppName+"...");
        myClock = new CounterClock(60*60*1000,500);

        LatLng loc = new LatLng(getIntent().getExtras().getDouble("CenterLat"),getIntent().getExtras().getDouble("CenterLng"));
        double Radius = getIntent().getExtras().getDouble("Radius");
        CircleOptions circOp = new CircleOptions();
        circOp.center(loc);
        circOp.radius(Radius);

        Log.d("Aviv","check: radius"+Radius+", centerLat: "+circOp.getCenter().latitude+", lng: "+circOp.getCenter().longitude);

        myLoc = map.addMarker(new MarkerOptions().position(loc));
        myLoc.setIcon(BitmapDescriptorFactory.fromBitmap(myIcon));

        oppLoc = map.addMarker(new MarkerOptions().position(new LatLng(0,0)));
        oppLoc.setIcon(BitmapDescriptorFactory.fromBitmap(oppIcon));

        Arena = map.addCircle(circOp);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {

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
                /* trnasfered for the server
                if(flag)
                {

                    double dist = diff_in_meters_between_two_points(myLoc.getPosition(), prize.getPosition());
                    if(dist>AppConstants.PRIZE_RADIUS)
                        ((TextView)findViewById(R.id.textDebugDist)).setText("Meters from the prize "+Integer.toString((int)dist));
                    else
                    {
                        myScore+=100;
                        ((TextView)findViewById(R.id.MyScore)).setText(Integer.toString(myScore));
                        prize.setPosition(generateNewCoin());
                    }
                }*/
            }
        });

        //for example, now generate new coin
        final TextView tv = (TextView) findViewById(R.id.textDebugDist);
        /*
        findViewById(R.id.button_new_prize).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!flag)
                {
                    myClock.start(); //starting the game
                    flag=true;
                    prize = map.addMarker(new MarkerOptions().position(generateNewCoin()));
                    prize.setIcon(BitmapDescriptorFactory.fromBitmap(appleIcon));
                    tv.setText("Meters from the prize "+Integer.toString((int)diff_in_meters_between_two_points(myLoc.getPosition(), prize.getPosition())));
                }
                else
                    prize.setPosition(generateNewCoin());
            }
        });*/

        GameID= getIntent().getExtras().getString("GameID");
        started = false;
        myClock.start();
        //when get the GameID from the server, starting the game!!!!!
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d("Aviv","GameActivity - onPause");
        //TODO: sending servlet to the server that user left the game or unavailable... (What do we do when user get phone call?)

        myClock.cancel();
    }

    LatLng generateNewCoin()
    {

        //PROBLEM: not mach between LatLng to meters, don't know how to do this conversation

        double r = Math.random()*Arena.getRadius(); 	//geneate a number between 0 to the radius
        double angle = Math.random()*360;			//generate a number between 0 to 360

        //little bit trigonometry...
        double a,b;
        a = Math.sin(angle)*r;
        b = Math.cos(angle)*r;

        //a and b are in meters and we need to change it into lat lng diffrences, so
        // the difference in meters between 2 LatLng points when Lng1==Lng2 and |Lat1-Lat2|== 1 is 111.23KM, so 1m = 1/111230 Lat
        // the difference in meters between 2 LatLng points when Lat1==Lat2 and |Lng1-Lng2|== 1 is 87.65KM, so 1m = 1/87650 Lng
        a/=111230;
        b/=87650;

        return new LatLng(Arena.getCenter().latitude+b, Arena.getCenter().longitude+a); //NOT GOOD
    }


    double diff_in_meters_between_two_points(LatLng p1, LatLng p2)
    {
        double R = 6371000; //earth's radius metres
        double lat1 = Math.toRadians(p1.latitude);
        double lat2 = Math.toRadians(p2.latitude);
        double diff_lat = Math.toRadians((p2.latitude-p1.latitude));
        double diff_lng = Math.toRadians((p2.longitude-p1.longitude));

        double a = Math.pow(Math.sin(diff_lat/2),2)+Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin(diff_lng/2),2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }

    public class CounterClock extends CountDownTimer {
        public CounterClock(long milisInFuture, long countDownInterval)
        {
            super(milisInFuture,countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if(started) {
                //sending to the server the location
                String str;
                try {
                    str = (new ServletSendLocation().execute(myName, GameID, Double.toString(myLoc.getPosition().latitude), Double.toString(myLoc.getPosition().longitude))).get();
                    Log.d("Aviv","str: "+str);
                    JSONObject object = new JSONObject(str);
                    double oppLat,oppLng,prizeLat,prizeLng;
                    int myScore, oppScore;
                    oppLat = object.getDouble("oppLat");
                    oppLng = object.getDouble("oppLng");
                    prizeLat =object.getDouble("prizeLat");
                    prizeLng =object.getDouble("prizeLng");
                    myScore = object.getInt("myScore");
                    oppScore = object.getInt("oppScore");
                    secondsLeft = object.getLong("timeLeft");
                    /*Log.d("Aviv","check: oppLat: "+oppLat+", oppLng: "+oppLng+", prizeLat: "+prizeLat+", prizeLng: "+prizeLng);
                    Log.d("Aviv","cont. myScore: "+myScore+", oppScore: "+oppScore+", time: "+secondsLeft);*/


                    oppLoc.setPosition(new LatLng(oppLat,oppLng));

                    if(flag)
                        prize.setPosition(new LatLng(prizeLat,prizeLng));
                    else
                    {
                        flag=true;
                        prize = map.addMarker(new MarkerOptions().position(new LatLng(prizeLat,prizeLng)));
                        prize.setIcon(BitmapDescriptorFactory.fromBitmap(appleIcon));
                    }
                    setTime(secondsLeft);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (secondsLeft <= 0) {
                    EndOfGame();
                }
            }
            else{
                try {
                    res = (new ServletGameStart().execute(myName, GameID)).get();
                    Log.d("Aviv", "res: " + res);
                    if (res.equals("Wait"))   //still wait to the opp...
                    {
                        Log.d("Aviv", "still waiting for " + oppName);
                        //TODO: waiting "circle" (like in YouTube) and message to the user, meanwhile in the counterClock TextView
                    } else if (res.equals("Declined")) {
                        Log.d("Aviv", "Declined");
                        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                        builder.setMessage(oppName + " declined your invitation to play :(");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            /*
                            Intent intent = new Intent(GameActivity.this, FriendChoosingActivity.class);
                            intent.putExtra("myName",myName);
                            intent.putExtra("myImage",getIntent().getExtras().getParcelable("myImage"));
                            startActivity(intent);*/
                                finish();
                            }
                        });
                    } else {               //opponent is ready, starting the gmae
                        //TODO: adding sound
                        Log.d("Aviv","Start game!!");
                            started = true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFinish() {
            //end of the game!!!
            EndOfGame();
        }
    }

    void setTime(long secondsLeft)
    {
        int minutes = (int)(secondsLeft/60);
        int seconds = (int)(secondsLeft%60);
        Log.d("Aviv","min: "+minutes+", seconds: "+seconds);
        countDown.setText(String.format("%02d:%02d",minutes,seconds));
    }

    void EndOfGame()
    {
        Intent intent = new Intent(GameActivity.this, EndGameActivity.class);
        intent.putExtra("myScore",myScore);
        intent.putExtra("oppScore", oppScore);
        intent.putExtra("myName", myName);
        intent.putExtra("myImage",getIntent().getExtras().getParcelable("myImage"));
        intent.putExtra("oppName", oppName);
        intent.putExtra("oppImage",getIntent().getExtras().getParcelable("oppImage"));
        startActivity(intent);
        finish();
    }
}