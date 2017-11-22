package cs.dawson.dawsonelectriccurrents;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class WeatherActivity extends MenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    public void startUvIndex(View view)
    {
        Intent intent = new Intent(this, UvIndexActivity.class);
        startActivity(intent);
    }


    public void startFiveDayForecast(View view)
    {
        Intent intent = new Intent(this, FiveDayForecastActivity.class);
        startActivity(intent);
    }

}
