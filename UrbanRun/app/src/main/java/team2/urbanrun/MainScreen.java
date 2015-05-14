package team2.urbanrun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


public class MainScreen extends Activity {
    boolean hasInvitation = false;
    Timer timer;
    Profile myProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
         myProfile = Profile.getCurrentProfile();

        ((ImageButton)findViewById(R.id.PlayBut)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainScreen.this, ArenaChoosingActivity.class);
                startActivity(intent);
            }
        });

        ((ImageButton)findViewById(R.id.TrophyBut)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainScreen.this, TrophyScreen.class);
                startActivity(intent);
            }
        });

        new ServletCleanDB().execute(); //help keeping the DB in the server clean
    }

    @Override
    protected void onResume(){
        super.onResume();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MainScreen.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!hasInvitation){
                            try {
                                String res = (new ServletAmIInvited().execute(myProfile.getId())).get();
                                Log.d("Aviv","Result from servlet: "+res);

                                if(!res.equals("Not Invited")){
                                    hasInvitation=true;
                                    final JSONObject json = new JSONObject(res);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainScreen.this);
                                    final String invitor = json.getString("FirstName")+" "+json.getString("LastName");
                                    final String GameID = json.getString("GameID");
                                    final String invitorID = json.getString("ID");
                                    builder.setMessage(invitor+" Invited you to play");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("Enter",new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(MainScreen.this,WaitingScreen.class);
                                            intent.putExtra("GameID",GameID);
                                            intent.putExtra("isCreator",false);
                                            intent.putExtra("creator", invitorID);
                                            dialog.cancel();
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                    builder.setNegativeButton("Decline",new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new ServletDeclined().execute(myProfile.getId(),GameID);
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
        },0,5000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        timer.cancel();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
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