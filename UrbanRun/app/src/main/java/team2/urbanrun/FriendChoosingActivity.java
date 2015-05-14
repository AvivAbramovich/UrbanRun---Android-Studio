package team2.urbanrun;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class FriendChoosingActivity extends ListActivity {
    Profile myProfile = Profile.getCurrentProfile();
    JSONArray friendsJson;
    JSONObject obj;
    String res;
    boolean wait=true;
    private boolean hasInvitation = false;
    List<String> friendChosen = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_choosing);
        String[] images={}, names={},ID={};

        try {
            obj = (new getFriendsFromFacebook().execute()).get();
            friendsJson = obj.getJSONObject("friends").getJSONArray("data");
            images = new String[friendsJson.length()];
            names = new String[friendsJson.length()];
            ID = new String[friendsJson.length()];
            ////////////// fetch picture, first and name last name from friends////////////
            for(int i=0; i<friendsJson.length();i++)
            {
                JSONObject user = (JSONObject) friendsJson.get(i);
                names[i]= user.getString("first_name")+" "+user.getString("last_name");
                ID[i]= user.getString("id");
                images[i] = user.getJSONObject("picture").getJSONObject("data").getString("url");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        setListAdapter(new MyAdapter(this, android.R.layout.simple_list_item_1, R.id.textView , ID, names, images));

        ((Button)findViewById(R.id.doneButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friendChosen.size() == 0) {
                    FriendChoosingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FriendChoosingActivity.this);
                            builder.setMessage("You must invite at least 1 friend to play");
                            builder.setCancelable(true);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                        }
                    });
                } else {
                    String myID = myProfile.getId();
                    //opponents
                    String opponents = "[";
                    for (int i = 0; i < friendChosen.size() - 1; i++)
                        opponents += friendChosen.get(i) + ",";
                    opponents += friendChosen.get(friendChosen.size() - 1) + "]";
                    //Arena properties
                    String radius = Integer.toString(getIntent().getExtras().getInt("Radius"));
                    String CenterLat = Double.toString(getIntent().getExtras().getDouble("CenterLat"));
                    String CenterLng = Double.toString(getIntent().getExtras().getDouble("CenterLng"));
                    String TimeInSeconds = getIntent().getExtras().getString("Time");
                    String res = null;
                    try {
                        res = (new ServletInitGame().execute(myID, opponents, radius, CenterLat, CenterLng, TimeInSeconds)).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    Log.d("Aviv", "GameID: " + res); //TODO: check the answer from the post request

                    Intent intent = new Intent(FriendChoosingActivity.this, WaitingScreen.class);
                    intent.putExtra("id", myID);
                    intent.putExtra("GameID", res);
                    intent.putExtra("isCreator", true);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(FriendChoosingActivity.this);

        builder.setMessage("Cancel the game?");
        builder.setCancelable(false);
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(FriendChoosingActivity.this, MainScreen.class));
                finish();
            }
        });
        builder.setNegativeButton("Stay", null);
        AlertDialog alert = builder.create();
        alert.show();
    }


    private class MyAdapter extends ArrayAdapter<String> {
        private String[] IDs, names, images;
        public MyAdapter(Context context, int resource, int textViewResourceId, String[] _IDs, String[] _names, String[] _img) {
            super(context, resource, textViewResourceId, _IDs);
            names = _names;
            images = _img;
            IDs = _IDs;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View  row = inflator.inflate(R.layout.friends_list_item, parent, false);
            final ImageView iv = (ImageView) row.findViewById(R.id.userImage);
            final TextView tv = (TextView) row.findViewById(R.id.textView);
            tv.setText(names[position]);
            try {
                iv.setImageBitmap((new DownloadImageTask().execute(images[position])).get());
            }catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v.findViewById(R.id.cbBox);

                    if(friendChosen.contains(IDs[position])){
                        friendChosen.remove(IDs[position]);
                        cb.setChecked(false);
                    }
                    else{
                        friendChosen.add(IDs[position]);
                        cb.setChecked(true);
                    }
                }
            });

            return row;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend_choosing, menu);
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