package team2.urbanrun;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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

import java.util.List;

import team2.urbanrun.R;

public class FriendChoosingActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_choosing);

        //should take the array from servlet, now i make it static...
        setListAdapter(new MyAdapter(this, android.R.layout.simple_list_item_1, R.id.textView , getResources().getStringArray(R.array.friendsList)));
        //setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.friendsList)));
    }

   private class MyAdapter extends ArrayAdapter<String> {

       public MyAdapter(Context context, int resource, int textViewResourceId, String[] strings) {
           super(context, resource, textViewResourceId, strings);
       }

       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
           LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
           View  row = inflator.inflate(R.layout.friends_list_item, parent, false);
           String[] items = getResources().getStringArray(R.array.friendsList);
           final ImageView iv = (ImageView) row.findViewById(R.id.imageView);
           final TextView tv = (TextView) row.findViewById(R.id.textView);

           tv.setText(items[position]);


           //just for now
           if(items[position].equals("Aviv Abramovich"))
           {
               iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.aviv));
           }
           if(items[position].equals("Asaf Slilat"))
           {
               iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.asaf));
           }
           if(items[position].equals("Oren Tvila"))
           {
               iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.oren));
           }
           if(items[position].equals("Omri Cohen"))
           {
               iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.omri));
           }
           if(items[position].equals("Rotem Klorin"))
           {
               iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.rotem));
           }
           if(items[position].equals("Yuval Brave"))
           {
               iv.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.yuval));
           }

           row.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(FriendChoosingActivity.this, MainActivity.class);
                   intent.putExtra("oppName",tv.getText());
                   intent.putExtra("oppImage", ((BitmapDrawable)iv.getDrawable()).getBitmap());
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


