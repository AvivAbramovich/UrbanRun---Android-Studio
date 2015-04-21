package team2.urbanrun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class EndGameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        ((TextView)findViewById(R.id.oppName)).setText(getIntent().getExtras().getString("oppName"));
        ((TextView)findViewById(R.id.myScore)).setText(Integer.toString(getIntent().getExtras().getInt("myScore")));
        ((TextView)findViewById(R.id.oppScore)).setText(Integer.toString(getIntent().getExtras().getInt("oppScore")));

        ((ImageView)findViewById(R.id.myImage)).setImageBitmap((Bitmap)getIntent().getExtras().getParcelable("myImage"));
        ((ImageView)findViewById(R.id.oppImage)).setImageBitmap((Bitmap)getIntent().getExtras().getParcelable("oppImage"));

        ((Button)findViewById(R.id.newGameButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(EndGameActivity.this,FriendChoosingActivity.class);
                //startActivity(intent);
                //returns to the FriendChoosingActivity
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_end_game, menu);
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
