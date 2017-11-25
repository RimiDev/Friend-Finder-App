package cs.dawson.dawsonelectriccurrents;
g
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is used to generate an httpURLConnection to the https://openweathermap.org/
 * website to gather information on the weather using the five day forecast feature.
 * Created by maximelacasse on 2017-11-22.
 */


public class FiveDayForecastActivity extends MenuActivity {

    TextView JSONresponse;
    //The API key that was genereated for my account on https://openweathermap.org/
    private String apiKey = "&APPID=5b62062bcde765f123614e4c944f8027";
    //The city that the user wants to check the weather for.
    private String city;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiveday_forecast);

        city = getIntent().getStringExtra("EXTRA_SESSION_ID");

        logIt("I am here" + city);

        Button btnHit = (Button) findViewById(R.id.btnHit);
        JSONresponse = (TextView) findViewById(R.id.JSONresponse);

        btnHit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherRequest request = new weatherRequest();
                weatherRequest.execute();
            }
        });

    } // end of onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * This is a subclass of FiveDayForecastActivity, for the sole purpose to do an AsyncTask
     * to be able to do an HttpURLConnection to the weather site, to gather information
     * about the weather that the user wants.
     */
    public class weatherRequest extends AsyncTask<Void, Void, String>{

        //The weather URL which includes the user the city inputted in the previous activity
        //as well as the API key that was generated for me.
        String weatherURL = "http://api.openweathermap.org/data/2.5/weather?q=" + city + apiKey;

        /**
         * This method is called whenever the subclass weatherRequest gets called by the method .execute()
         * It simply calls the its super class as we don't have much to do before the execution.
         */
        @Override
        protected void onPreExecute() {
            logIt("onPreExecute");
            super.onPreExecute();
        }

        /**
         * This method is called whenever onPreExecute() is finished.
         * doInBackground is needed when using HttpURLConnection because in Android, it is forbidden
         * to accomplish a HttpURLConnection in the UI thread. It has to be in a seperate thread, or
         * else it will throw an exception.
         * This method is using HttpURLConnection to go into the given weatherURL and using a
         * private method, httpResultsToString, will grab the JSON file and return a StringBuilder
         * with the JSON file.
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(Void... params) {
            //The results will be stored in this variable.
            String result = null;
            try {
                //Creating a URL with the weatherURL.
                URL url = new URL(weatherURL);
                //Opening the connection with the given url.
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //Creating an inputStream with the open connection.
                InputStream input = new BufferedInputStream(urlConnection.getInputStream());
                //Now that we have the Input stream, we use the private method httpResultsToString
                //To convert the information that we can obtain into a StringBuilder.
                result = httpResultsToString(input);
                logIt("result: " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Returns the StringBuilder JSON file results to onPostExecute().
            return result;
        }

        /**
         * This method is called whenever doInBackground is finished.
         * Since in this stage, we have the JSON information in a string, we create a JSONObject
         * to be able to manipulate and go around in the JSON file, using JSONArrays and JSONObjects.
         * @param s Whatever doInBackground returns is this parameter, in this case, JSON info in as a String.
         */
        @Override
        protected void onPostExecute(String s) {
            //Calls the super.
            super.onPostExecute(s);
            try {
                logIt("ResultInOnPostExecute: "+ s);
                //Create a JSONObject with the String JSON results from 'doInBackground' method.
                JSONObject jsonObject = new JSONObject(s);
                //Grabbing the first item to then grab the weather.
                JSONArray jsonItems = jsonObject.getJSONArray("weather");

                //Grabbing all the individual items associated to the weather.
                String id = jsonItems.getJSONObject(0).getString("id");
                String main = jsonItems.getJSONObject(0).getString("main");

                logIt("jsonItems: " + jsonItems);
                logIt("id: " + id);
                logIt("main: " + main);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * This method is used to convert the items received from an InputStream into
     * a Stringbuilder, so that it can be manipulated in another method (onPostExecute).
     * @param inStream The open connection done by the HttpURLConnection.
     * @return A string containing all the information from the InputStream
     */
    private String httpResultsToString(InputStream inStream) {
        //This variable will be used to read every line to be able to append from it.
        String line = "";
        //Creating a stringbuilder, which will be the result returned at the end of the method
        StringBuilder returningResults = new StringBuilder();
        //Creating a InputStreamReader using the inStream from doInBackground method.
        InputStreamReader inStreamResults = new InputStreamReader(inStream);
        //Buffered reading to read much bigger chuncks of data rather than a character at a time.
        BufferedReader readBuffer = new BufferedReader(inStreamResults);

        try {
            //Reading through all the information that the inStream can supply us.
            while ((line = readBuffer.readLine()) != null) {
                //Appending every line to the StringBuilder.
                returningResults.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Returns the string with all the JSON information in it.
        //It is now ready to be converted into a JSONObject.
        return returningResults.toString();
    }


    /**
     * Method to easily log to logcat
     *
     * @param msg to be printed to logcat
     */
    public static void logIt(String msg) {
        final String TAG = "---------WEATHER: ";
        Log.d(TAG, msg);
    }






}
