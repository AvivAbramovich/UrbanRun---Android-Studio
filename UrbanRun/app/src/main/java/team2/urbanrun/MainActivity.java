package team2.urbanrun;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class MainActivity extends Activity {

	GoogleMap map;
	Circle cir;
	Marker usersLoc;
	Marker center;
	TextView tv;
	
	String oppName;
	Bitmap oppImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Log.d("Aviv", "MainActivity - onCreate");

        //sending http post servlet
        new ServletPostAsyncTask().execute(new Pair<Context, String>(this, "Aviv"));
		
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

		oppName = getIntent().getExtras().getString("oppName");
		oppImage= (Bitmap)getIntent().getExtras().getParcelable("oppImage");

		//oppName = "Oren";
        //oppImage = BitmapFactory.decodeResource(getResources(),R.drawable.oren);

        //((ImageView)findViewById(R.id.opponentImg)).setImageDrawable(oppImage);
		//end of the fixing
		
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
						cir.setCenter(point);
					}
				});
				
		findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
					
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, GameActivity.class);
				intent.putExtra("Radius", cir.getRadius());
				intent.putExtra("CenterLat", center.getPosition().latitude);
				intent.putExtra("CenterLng", center.getPosition().longitude);
				intent.putExtra("oppName", oppName);			//from the former activity intent
				intent.putExtra("oppImage", (android.os.Parcelable) oppImage); 	//from the former activity intent
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();

        Log.d("Aviv", "MainActivity - onStart");
	}

	@Override
	protected void onResume(){
		super.onResume();
		Log.d("Aviv", "MainActivity - onResume");
		
		/*
		AlertDialog.Builder buil = new AlertDialog.Builder(MainActivity.this);
		buil.setMessage("onResume!");
		AlertDialog alert = buil.create();
		alert.show();
		*/
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Log.d("Aviv", "MainActivity - onDestroy");
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		Log.d("Aviv", "MainActivity - onPause");
	}
	
	@Override
	protected void onRestart()
	{
		super.onRestart();
		Log.d("Aviv", "MainActivity - onRestart");
	}
	
	void update_game_size_text(TextView tv, double radius)
	{
		tv.setText("Game Arena Radius: "+Integer.toString((int)radius)+" meters");
	}
}
