package org.kebn.firealarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.kebn.firealarm.Config;
import org.kebn.firealarm.FireAlarmApp;
import org.kebn.firealarm.R;


public class MainActivity extends BaseActivity {


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (FireAlarmApp.config.getAppType().equals(Config.Type.CLIENT.toString())) {
      startActivity(new Intent(this, SendAlarmActivity.class));
    } else if (FireAlarmApp.config.getAppType().equals(Config.Type.HOST.toString())) {
      startActivity(new Intent(this, MonitorActivity.class));
    }
    finish();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
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
