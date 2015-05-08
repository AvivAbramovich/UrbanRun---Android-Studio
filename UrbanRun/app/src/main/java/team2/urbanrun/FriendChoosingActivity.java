package team2.urbanrun;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class FriendChoosingActivity extends ListActivity {
    private String myName,FB;
    String res;
    private boolean hasInvitation = false;
    Timer timer;
    List<String> friendChosen = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_choosing);

        //should take the array from servlet, now i make it static..
        //setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.friendsList)));
        //FB = getIntent().getExtras().getString("json");
        //JSONArray arr = getIntent().getExtras().getString("json");
        JSONArray friends = null;
        String[] images={}, names={},ID={};
        try {
            JSONObject temp = new JSONObject(getIntent().getExtras().getString("friends"));
            friends = temp.getJSONArray("data");
            myName = getIntent().getExtras().getString("id");

            //String response = (new ServletGetUsers().execute(myName)).get(); TODO: replace it with get the friends from the Facebook/server instead all users
            images = new String[friends.length()];
            names = new String[friends.length()];
            ID = new String[friends.length()];
            ////////////// fetch picture, first and name last name from friends////////////
            for(int i=0; i<friends.length();i++)
            {
                JSONObject user = (JSONObject) friends.get(i);
                names[i]= user.getString("first_name")+" "+user.getString("last_name");
                ID[i]= user.getString("id");
                images[i] = user.getJSONObject("picture").getJSONObject("data").getString("url");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        setListAdapter(new MyAdapter(this, android.R.layout.simple_list_item_1, R.id.textView , ID, names, images));

        //checking if were invited to game by another player
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                FriendChoosingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!hasInvitation){
                            try {
                                res = (new ServletAmIInvited().execute(myName)).get();
                                Log.d("Aviv","Result from servlet: "+res);

                                if(!res.equals("Not Invited")){
                                    hasInvitation=true;
                                    final JSONObject json = new JSONObject(res);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendChoosingActivity.this);
                                    final String invitor = json.getString("invitor");
                                    final String GameID = json.getString("gameID");
                                    builder.setMessage(invitor+" Invited you to play");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("Accept",new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(FriendChoosingActivity.this,GameActivity.class);
                                            intent.putExtra("myName",getIntent().getExtras().getString("myName"));
                                            intent.putExtra("GameID",GameID);
                                            dialog.cancel();
                                            startActivity(intent);
                                        }
                                    });
                                    builder.setNegativeButton("Decline",new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new ServletDeclined().execute(myName,GameID);
                                            dialog.cancel();
                                            hasInvitation = false;
                                        }
                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                                else {
                                    Log.d("Aviv","No invitations yet...");
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(tt,0,5000);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        timer.cancel();
    }

    public void FriendsChosenToServer(View view) throws ExecutionException, InterruptedException {
        String myID = getIntent().getExtras().getString("id");
        //opponents
        String opponents ="[";
        for(int i=0; i<friendChosen.size()-1; i++)
            opponents+=friendChosen.get(i)+",";
        opponents+=friendChosen.get(friendChosen.size()-1)+"]";
        //Arena properties
        String radius = Integer.toString(getIntent().getExtras().getInt("Radius"));
        String CenterLat = Double.toString(getIntent().getExtras().getDouble("CenterLat"));
        String CenterLng = Double.toString(getIntent().getExtras().getDouble("CenterLng"));
        String TimeInSeconds = getIntent().getExtras().getString("Time");
        Log.d("Aviv","Game properties: radius: "+getIntent().getExtras().getInt("Radius")+", x: "+getIntent().getExtras().getDouble("CenterLat")+", y: "+getIntent().getExtras().getDouble("CenterLng"));
        Log.d("Aviv","Game properties: radius: "+radius+", x: "+CenterLat+", y: "+CenterLng+", myID: "+myID+", opps: "+opponents);
        String res = (new ServletInitGame().execute(myID, opponents, radius, CenterLat, CenterLng, TimeInSeconds)).get();
        Log.d("Aviv", "GameID: "+res); //TODO: check the answer from the post request

        Intent intent = new Intent(FriendChoosingActivity.this, ArenaChoosingActivity.class);
        intent.putExtra("id",myID);
        intent.putExtra("GameID",Integer.parseInt(res));
        startActivity(intent);
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
                    Log.d("Aviv", names[position]);
                    //check if already in the list
                    if(friendChosen.contains(IDs[position])){
                        Log.d("Aviv","Remove "+names[position]+" from the list");
                        friendChosen.remove(IDs[position]);
                    }
                    else{
                        Log.d("Aviv", "Adding "+names[position]+" to the game");
                        friendChosen.add(IDs[position]);
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