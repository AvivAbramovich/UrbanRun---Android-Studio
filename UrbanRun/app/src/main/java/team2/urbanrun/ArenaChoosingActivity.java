package team2.urbanrun;

import com.facebook.Profile;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class ArenaChoosingActivity extends Activity {

	GoogleMap map;
	Circle cir;
	Marker usersLoc;
	Marker center;
	TextView tv;
    NumberPicker minutes = null;
    TextView timeDis = null;
    int time;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        Profile myProfile = Profile.getCurrentProfile();

        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();

        CircleOptions circOp = new CircleOptions();
		center = map.addMarker(new MarkerOptions().position(new LatLng(32.761872,35.018299)));
		center.setDraggable(true);
		circOp.center(center.getPosition());
		circOp.radius(AppConstants.DEFAULT_RADIUS);
		usersLoc = map.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
		cir = map.addCircle(circOp);

		//from the previous intent, right now, this is the launched intent, so we use in Oren's image and name
        try {
            Log.d("Aviv", "imageURL: " + myProfile.getProfilePictureUri(50, 50).toString());
             Bitmap myImage = (new DownloadImageTask().execute(myProfile.getProfilePictureUri(50,50).toString())).get();
             usersLoc.setIcon(BitmapDescriptorFactory.fromBitmap(getCroppedBitmap(myImage)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
		
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
                            cir.setRadius(cir.getRadius()+20);
                            Toast.makeText(getApplicationContext(), "Arena Radius is "+
                                    Integer.toString((int)cir.getRadius())+" meters", Toast.LENGTH_SHORT).show();
						}
					}
				});

		findViewById(R.id.button_minus).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if(cir.getRadius()>AppConstants.MIN_RADIUS)
                        {
						    cir.setRadius(cir.getRadius()-20);
                            Toast.makeText(getApplicationContext(), "Arena Radius is "+
                                    Integer.toString((int)cir.getRadius())+" meters", Toast.LENGTH_SHORT).show();
						}
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
                 Intent intent = new Intent(ArenaChoosingActivity.this, FriendsChoosingActivity.class);
                 intent.putExtra("Radius", (int) cir.getRadius());
                 intent.putExtra("CenterLat", center.getPosition().latitude);
                 intent.putExtra("CenterLng", center.getPosition().longitude);
                 intent.putExtra("Time", time);
                 startActivity(intent);
                 finish();
			}
		});

        findViewById(R.id.button_me).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(usersLoc.getPosition(), 18));
            }
        });

        findViewById(R.id.button_help).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ArenaChoosingActivity.this, tutorial.class));
            }
        });

        time = 60;
        minutes = (NumberPicker)findViewById(R.id.PickMin);
        minutes.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutes.setMinValue(1);
        minutes.setMaxValue(100);
        minutes.setWrapSelectorWheel(true);
        timeDis = (TextView) findViewById(R.id.TimeDisplay);
        minutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                String Old = "";
                String New = "New Value : ";
                time = newVal*60;
                timeDis.setText(Old.concat(String.valueOf(newVal)+":"+"00"));
            }
        });

        if(getIntent().getExtras().getString("From").equals("FriendChoosingActivity")){

            center.setPosition(new LatLng(getIntent().getExtras().getDouble("CenterLat"),getIntent().getExtras().getDouble("CenterLng")));
            cir.setRadius(getIntent().getExtras().getInt("Radius"));
            cir.setCenter(center.getPosition());
            time = getIntent().getExtras().getInt("Time");
        }
	}

    @Override
    public void onBackPressed(){
        startActivity(new Intent(ArenaChoosingActivity.this, MainScreen.class));
        finish();
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}