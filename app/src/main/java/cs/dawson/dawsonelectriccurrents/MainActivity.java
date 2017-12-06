package cs.dawson.dawsonelectriccurrents;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.test.mock.MockPackageManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import cs.dawson.dawsonelectriccurrents.cancelled.CancelledActivity;
import cs.dawson.dawsonelectriccurrents.database.FriendFinderDBHelper;

import cs.dawson.dawsonelectriccurrents.notes.NotesActivity;
import cs.dawson.dawsonelectriccurrents.weatherrequest.GPSTracker;
import cs.dawson.dawsonelectriccurrents.weatherrequest.WeatherRequest;

public class MainActivity extends MenuActivity
{
    private final static String TAG = MainActivity.class.getName();
    private FriendFinderDBHelper database;
    private final String USERS_PREFS = "user";
    private ImageView dawsonLogo;

    //Current weather variables
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;

    // GPSTracker class
    GPSTracker gps;

    //Current temperature text view
    TextView currentTempTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initalizing current temperature textView
        currentTempTextView = findViewById(R.id.currentTempTextView);

        //Current weather start up, display the temperature depending on device's location.
        //If the device doesn't have location on, it will ask the user to turn it on.
        onCurrentWeatherStartUp();


        database = new FriendFinderDBHelper(this);
        database.getWritableDatabase();

        SharedPreferences prefs = getSharedPreferences(USERS_PREFS, MODE_PRIVATE);
        String fullName = prefs.getString("firstName", "") + " " + prefs.getString("lastName", "");
        Log.i(TAG, "Full name is: " + fullName);

        if (fullName == null || fullName.trim().equals("")){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    public void startDawsonPage(View view)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.dawsoncollege.qc.ca/computer-science-technology/"));
        startActivity(intent);
    }

    public void startAbout(View view)
    {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void startClassCancelled(View view)
    {
        Intent intent = new Intent(this, CancelledActivity.class);
        startActivity(intent);
    }

    public void startFindTeacher(View view)
    {
        Intent intent = new Intent(this, FindTeacherActivity.class);
        startActivity(intent);
    }

    public void startAddCalendar(View view)
    {
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }

    public void startNotes(View view)
    {
        Intent intent = new Intent(this, NotesActivity.class);
        startActivity(intent);
    }

    public void startWeather(View view)
    {
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
    }

    public void startAcademicCalendar(View view) {
        Intent intent = new Intent(this,AcademicCalendarActivity.class);
        startActivity(intent);
    }

    public void startFindFriends(View view) {
        Intent intent = new Intent(this,FindFriendCourseActivity.class);
        startActivity(intent);
    }

    public void startFindFreeFriends(View view) {
        Intent intent = new Intent(this,FindFreeFriends.class);
        startActivity(intent);
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

    @Override
    public void onStart()
    {
        super.onStart();
    }


    public void onCurrentWeatherStartUp(){

        //The API key that was genereated for my account on https://openweathermap.org/
        String apiKey = "&APPID=5b62062bcde765f123614e4c944f8027";

        String currentTemperature;

        WeatherRequest currentWeatherRequest = new WeatherRequest(null, apiKey, "3");

        //Current weather startup
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != MockPackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);

            } else {
                gps = new GPSTracker(MainActivity.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    //Grabbing the current weather from OpenWeatherApi.com
                    currentTemperature = parseJSONandReturnCurrentTemperature(
                            currentWeatherRequest.execute(apiKey,String.valueOf(latitude),String.valueOf(longitude)).get());


                    currentTempTextView.setText(currentTemperature);
                }else{
                    // GPS or Network is not enabled
                    // Ask user to enable through a dialog.
                    gps.showSettingsAlert();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String parseJSONandReturnCurrentTemperature(String jsonCurTempResults) {


        String currentTemperature = null;

        if (jsonCurTempResults != null) {

            try {
                //Create a JSONArray with the String JSON results from 'doInBackground' method.
                JSONObject jsonObject = new JSONObject(jsonCurTempResults);

                    //Grabbing the weather value
//                     JSONArray weather = jsonObject.getJSONArray("weather");
//                     JSONObject weatherDesc = weather.getJSONObject(0);
//                     weatherDetails[0] = weatherDesc.getString("description");

                    //Grabbing the current temperature
                    JSONObject main = jsonObject.getJSONObject("main");
                    currentTemperature = main.getString("temp");

                //Grabbing the first item to then grab the weather.
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertKelvtoCelcius(currentTemperature);

        }

        return convertKelvtoCelcius(currentTemperature);

    } // end of parseCurrentTemperature



    /**
     * This method is used to convert the information that is given to us by the weather api (kelvin)
     * to celcius since not everyone is comfortable reading kevlin temperature when they want to know
     * what they have to wear in the morning.
     * @param kelv
     * @return
     */
    public String convertKelvtoCelcius(String kelv){
        Double celcius = Double.valueOf(kelv);
        celcius -= 273.15;
        NumberFormat formatter = new DecimalFormat("#0.00");
        return String.valueOf(formatter.format(celcius) + "C°");

    }


    /**
     * Method to easily log to logcat
     *
     * @param msg to be printed to logcat
     */
    public static void logIt(String msg) {
        final String TAG = "---------MAIN: ";
        Log.d(TAG, msg);
    }



}
