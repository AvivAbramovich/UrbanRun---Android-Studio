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
    Bitmap myIcon;
	
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
        Bitmap myImage=null;
        try {
            myImage = (new DownloadImageTask().execute(getIntent().getExtras().getString("pic"))).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        myIcon = Bitmap.createScaledBitmap(myImage,50,50,false);
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
                 //intent to the friends choosing activity
				Intent intent = new Intent(ArenaChoosingActivity.this, FriendChoosingActivity.class);
                intent.putExtra("firstName", getIntent().getExtras().getString("firstName"));
                intent.putExtra("lastName", getIntent().getExtras().getString("lasttName"));
                intent.putExtra("id",  getIntent().getExtras().getString("id"));
                intent.putExtra("pic", getIntent().getExtras().getString("pic"));
                intent.putExtra("Radius", (int)cir.getRadius());
                intent.putExtra("CenterLat", center.getPosition().latitude);
                intent.putExtra("CenterLng", center.getPosition().longitude);
                intent.putExtra("Time", getIntent().getExtras().getString("Time"));
                intent.putExtra("friends", getIntent().getExtras().getString("friends"));
                Log.d("Aviv", "Arena: radius: "+cir.getRadius()+", x: "+center.getPosition().latitude+", y: "+center.getPosition().longitude+", Time: "+getIntent().getExtras().getString("Time"));
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