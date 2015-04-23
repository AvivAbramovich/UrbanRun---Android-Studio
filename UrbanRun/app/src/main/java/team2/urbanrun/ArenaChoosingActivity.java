package team2.urbanrun;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class ArenaChoosingActivity extends Activity {

	GoogleMap map;
	Circle cir;
	Marker usersLoc;
	Marker center;
	TextView tv;
	
	String oppName;
    Bitmap myIcon;
	Bitmap oppImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Log.d("Aviv", "MainActivity - onCreate");

        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		CircleOptions circOp = new CircleOptions();
		center = map.addMarker(new MarkerOptions().position(new LatLng(32.761872,35.018299)));   //HAIFA UNIV.
		center.setDraggable(true);
		circOp.center(center.getPosition());
		circOp.radius(AppConstants.DEFAULT_RADIUS);
		usersLoc = map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
		cir = map.addCircle(circOp);

        tv = (TextView) findViewById(R.id.textView_size_arena);
		update_game_size_text(tv,cir.getRadius());

		//from the previous intent, right now, this is the launched intent, so we use in Oren's image and name
        myIcon = Bitmap.createScaledBitmap((Bitmap)getIntent().getExtras().getParcelable("myImage"),50,50,false);
        usersLoc.setIcon(BitmapDescriptorFactory.fromBitmap(myIcon));
		
		//settings
		UiSettings settings = map.getUiSettings();
		settings.setZoomControlsEnabled(true);
		settings.setMyLocationButtonEnabled(true);
		
		//user's locations
		LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, new LocationListener() {
					
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
						usersLoc.setPosition(new LatLng((double)location.getLatitude(),(double)location.getLongitude()));
					}
				});
			
		map.setOnMarkerDragListener(new OnMarkerDragListener() {
					
					@Override
					public void onMarkerDragStart(Marker marker) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onMarkerDragEnd(Marker marker) {
                        cir.setCenter(marker.getPosition());
					}
					
					@Override
					public void onMarkerDrag(Marker marker) {
						cir.setCenter(marker.getPosition());
					}
				});

		findViewById(R.id.button_plus).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(cir.getRadius()<AppConstants.MAX_RADIUS)
                        {
                            cir.setRadius(2*cir.getRadius());
                            update_game_size_text(tv,cir.getRadius());
						}
					}
				});

		findViewById(R.id.button_minus).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if(cir.getRadius()>AppConstants.MIN_RADIUS)
                        {
						    cir.setRadius(cir.getRadius()/2);
                            update_game_size_text(tv,cir.getRadius());
						}
					}
				});

		findViewById(R.id.button_me).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(usersLoc.getPosition(), 16));
			}
		});

		map.setOnMapLongClickListener(new OnMapLongClickListener() {

					@Override
					public void onMapLongClick(LatLng point) {
                        center.setPosition(point);
						cir.setCenter(point);
					}
				});
				
		findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
					
			@Override
			public void onClick(View v) {
                String myName, oppName, radius, centerLat, centerLng,type,limit;
                myName= getIntent().getExtras().getString("myName");
                radius = Integer.toString((int)cir.getRadius());
                centerLat = Double.toString(cir.getCenter().latitude);
                centerLng = Double.toString(cir.getCenter().longitude);
                type = "0";
                limit = "180000";   //3 minutes TODO: choosing game limit
                //first - send servlet for open a game and get a gameID
                String GameID = "";
                try {
                    GameID = (new ServletInitGame().execute(myName,getIntent().getExtras().getString("oppName"),radius,centerLat,centerLng,type,limit)).get();
                    Log.d("Aviv","Game ID: "+GameID);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                //intent to the game activity
				Intent intent = new Intent(ArenaChoosingActivity.this, GameActivity.class);
                intent.putExtra("myName", myName);			//from the former activity intent
                intent.putExtra("myImage", getIntent().getExtras().getParcelable("myImage")); 	//from the former activity intent
                intent.putExtra("GameID",GameID);
				startActivity(intent);
                finish();
			}
		});
	}

	void update_game_size_text(TextView tv, double radius)
	{
		tv.setText("Game Arena Radius: "+Integer.toString((int)radius)+" meters");
	}
}
