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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class FriendChoosingActivity extends ListActivity {
    private String myName;
    String res;
    private boolean hasInvitation = false;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_choosing);

        //should take the array from servlet, now i make it static..
        //setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.friendsList)));

        myName = getIntent().getExtras().getString("myName");
        String[] names={}, images={}, usernames={};
        try {
            String response = (new ServletGetUsers().execute(myName)).get();
            JSONArray arr = new JSONArray(response);
            names = new String[arr.length()];
            images = new String[arr.length()];
            usernames = new String[arr.length()];
            for(int i=0; i<arr.length();i++)
            {
                JSONObject user = (JSONObject) arr.get(i);
                names[i] = user.getString("FullName");
                images[i] = user.getString("ImageURL");
                usernames[i]= user.getString("Username");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setListAdapter(new MyAdapter(this, android.R.layout.simple_list_item_1, R.id.textView , usernames, names, images));

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
                                            finish();
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

   private class MyAdapter extends ArrayAdapter<String> {
   private String[] usernames, names, images;
       public MyAdapter(Context context, int resource, int textViewResourceId, String[] _users, String[] _names, String[] _img) {
           super(context, resource, textViewResourceId, _users);
           usernames = _users;
           names = _names;
           images = _img;
       }

       @Override
       public View getView(final int position, View convertView, ViewGroup parent) {
           LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
           View  row = inflator.inflate(R.layout.friends_list_item, parent, false);
           final ImageView iv = (ImageView) row.findViewById(R.id.imageView);
           final TextView tv = (TextView) row.findViewById(R.id.textView);
           tv.setText(names[position]);
           try {
               URL url = new URL(images[position]);
               Bitmap img = new DownloadImageTask().execute(images[position]).get();
               iv.setImageBitmap(img);
           } catch (MalformedURLException e) {
               e.printStackTrace();
           } catch (InterruptedException e) {
               e.printStackTrace();
           } catch (ExecutionException e) {
               e.printStackTrace();
           }

           row.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(FriendChoosingActivity.this, ArenaChoosingActivity.class);
                   intent.putExtra("myName",getIntent().getExtras().getString("myName"));
                   intent.putExtra("myImage", getIntent().getExtras().getParcelable("myImage"));
                   intent.putExtra("oppName", usernames[position]);
                   startActivity(intent);
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


