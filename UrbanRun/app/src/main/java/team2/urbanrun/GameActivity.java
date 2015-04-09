package team2.urbanrun;

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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class GameActivity extends Activity {

	GoogleMap map;
	Circle Arena;
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		Log.d("Aviv", "GameActivity - onCreate");
		
		map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		flag = false;

		//settings
		UiSettings settings = map.getUiSettings();
		settings.setZoomControlsEnabled(true);
		settings.setMyLocationButtonEnabled(true);
		
		oppScore = 0;
		myScore = 0;
		((TextView)findViewById(R.id.opponentName)).setText(getIntent().getExtras().getString("oppName"));
		((ImageView)findViewById(R.id.opponentImg)).setImageBitmap((Bitmap)getIntent().getExtras().getParcelable("oppImage"));
		((TextView)findViewById(R.id.opponentScore)).setText(Integer.toString(oppScore));
		((TextView)findViewById(R.id.MyScore)).setText(Integer.toString(myScore));
		
		LatLng loc = new LatLng(getIntent().getExtras().getDouble("CenterLat"),getIntent().getExtras().getDouble("CenterLng"));
		double Radius = getIntent().getExtras().getDouble("Radius");
		CircleOptions circOp = new CircleOptions();
		circOp.center(loc);
		circOp.radius(Radius);
		myLoc = map.addMarker(new MarkerOptions().position(loc));
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
						myLoc.setPosition(new LatLng((double)location.getLatitude(),(double)location.getLongitude()));
						if(flag)
						{
							double dist = diff_in_meters_between_two_points(myLoc.getPosition(), prize.getPosition());
							if(dist>AppConstants.PRIZE_RADIUS)
								((TextView)findViewById(R.id.textDebugDist)).setText("Meters from the prize "+Integer.toString((int)dist));
							else
							{
								myScore+=100;
								((TextView)findViewById(R.id.MyScore)).setText(Integer.toString(myScore));
								prize.remove();
								prize = map.addMarker(new MarkerOptions().position(generateNewCoin()));
								prize.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
								flag = true;
							}
						}
					}
				});
	}
	
	
	@Override
	protected void onStart()
	{
		super.onStart();
		Log.d("Aviv", "GameActivity - onStart");
		//TODO: what should happen here...
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.d("Aviv", "GameActivity - onResume");
		/*suppose to get send every second his location from the gps to the server, and get in the respone:
			1. the location of the opponent
			2. the locations of the coins and weapons
			3. the location of activated weapons
			4. statistics like scores, time to end of the game, etc..
		*/
		
		//for example, now generate new coin
		final TextView tv = (TextView) findViewById(R.id.textDebugDist);
		
		findViewById(R.id.button_new_prize).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(flag)
					prize.remove();
				prize = map.addMarker(new MarkerOptions().position(generateNewCoin()));
				prize.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
				tv.setText("Meters from the prize "+Integer.toString((int)diff_in_meters_between_two_points(myLoc.getPosition(), prize.getPosition())));
				flag = true;
			}
		});
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
}
