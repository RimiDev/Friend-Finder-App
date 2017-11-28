package cs.dawson.dawsonelectriccurrents;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import cs.dawson.dawsonelectriccurrents.weatherrequest.WeatherRequest;

/**
 * This class is used to generate an httpURLConnection to the https://openweathermap.org/
 * website to gather information on the weather using the five day forecast feature.
 * Created by maximelacasse on 2017-11-22.
 */

public class FiveDayForecastActivity extends MenuActivity {

    TextView JSONresponse;
    //The API key that was genereated for my account on https://openweathermap.org/
    public String apiKey = "&APPID=5b62062bcde765f123614e4c944f8027";
    //The city that the user wants to check the weather for.
    public String city = "Paris";

    //Latitude and longitude of the device.
    public String longitude;
    public String latitude;


    //Weather details for 5 day range.
    TextView weatherDay1;
    TextView weatherDay2;
    TextView weatherDay3;
    TextView weatherDay4;
    TextView weatherDay5;
    TextView cityname;
    TextView uvIndex;

    //weatherRequest class objects in global scope.
    WeatherRequest forecastRequest = new WeatherRequest(city, apiKey, "1");
    WeatherRequest uvIndexRequest = new WeatherRequest(city, apiKey, "2");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiveday_forecast);

        Bundle b = getIntent().getExtras();
        city = b.getString("city");

        setUpWeatherDisplays();

        //Calling the WeatherRequest class to demand a request with the weather forecast with user input.
        try {
            parseJSONandDisplayForecast(forecastRequest.execute(city, apiKey).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

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
     * onPostExecute in the WeatherRequest class passed us the information that
     * doInBackground() passed him (The JSON weather information that we need to parse)
     * we now have the JSON information in a string, we create a JSONObject with that string
     * to be able to manipulate and go around in the JSON file, using JSONArrays and JSONObjects.
     * Whilst parsing the JSONObject, we will be setting the textfields to display the results to the user.
     * @param jsonForecastResult Whatever doInBackground returns is this parameter, in this case, JSON info as a String.
     */
    @SuppressLint("SetTextI18n")
    public void parseJSONandDisplayForecast(String jsonForecastResult){
        //Checks if s is null, if it is, then the user typed a bad city name and we did not
        //get any results from the website, which results in a crash when trying to use
        //bufferreader on a bad HttpURLConnection. This will redirect to a error page for user.
        if (jsonForecastResult != null) {

            try {
                //Create a JSONObject with the String JSON results from 'doInBackground' method.
                JSONObject jsonObject = new JSONObject(jsonForecastResult);


                //Grabbing the latitude and longitude to then ship it to another weatherRequest
                //For the UV index.
                JSONObject jsonCity = jsonObject.getJSONObject("city");
                String lat = jsonCity.getJSONObject("coord").getString("lat");
                String lon = jsonCity.getJSONObject("coord").getString("lon");

                this.latitude = lat;
                this.longitude = lon;

                String[] uvIndexes = parseJSONandDisplayUV(uvIndexRequest.execute(city,apiKey,latitude,longitude).get());

                //Grabbing the first item to then grab the weather.
                JSONArray jsonItems = jsonObject.getJSONArray("list");

                /**
                 * Checking and pushing condiitons that will grab only the next weather
                 * information in a 3 hour range depending on our current time.
                 * 1. Create a SimpleDateFormat to create the format that we need and get the
                 * current time which grabs the hour.
                 * 2. For loop that will iterate through checking if current time is LESS
                 * than the current time's hour. The for loop will increase by 3 every iteration
                 * since the openWeatherChannel sends out new information every 3 hours.
                 * This will ensure that we are grabbing the next future weather information.
                 * 3. Grab the information from dt_txt to aquire the hour and day for step 4 condition.
                 * 4. In the for loop for actual grabbing of the information, make sure to
                 * create a condition that it will only grab information from the sections that
                 * have the time that we established in this for loop.
                 */

                //1. Creating a SimpleDateFormat to format the current date/time with ease.
                SimpleDateFormat sdf = new SimpleDateFormat("HHdd");
                String currentHourDay = sdf.format(new Date()); //Output: 01 || 11 -> weather: 01 || 11

                //2. For loop to grab the correct time that we want to have for our user.
                String weatherHourWeNeedToGrab = currentHourDay.substring(0,2); //Grabbing the current hour.
                String weatherDayWeNeedToGrab = currentHourDay.substring(2,4); //Grabbing current day.

                //Day counter to display the information gathered depending on which day it is.
                int dayCounter = 0;
                //Set the city that the user inputted.
                cityname.setText(city);
                //Set the firstDay to true, so it goes into the loop immeditely and grab the information.
                //Then we set the hour to be the hour that we got from the 'firstDay' and we keep it for the
                //rest of the forecast. example: firstDay: 00:00:00, then the rest will be 00:00:00 but with increase of day.
                boolean firstDay = true;
                /**
                 * 'firstDayOfTheMonth' string
                 * This string is used to know whenever the month changes.
                * With my technique, i increase the day of the month every time we grab information.
                * If it's at 31 and i increase it to 32, then i will never get information.
                * Thus, if the information that the JSONObject provides us is at day 01, then we will grab it.
                * This string will turn null whenever we grab the 01, or else it will grab all the 3 hour weather info every iteration.
                 **/
                String firstDayOfTheMonth = "01";

                //Iterate through all the JSONObjects inside the list JSONArray.
                for (int i = 0; i < jsonItems.length(); i++) {

                    //3. Grabbing the dt_txt to see if it's the results that we want to display.
                    String weatherTimeAndDay = jsonItems.getJSONObject(i).getString("dt_txt");
                    String weatherHour = weatherTimeAndDay.substring(11,13); //Grabbing the hour.
                    String weatherDay = weatherTimeAndDay.substring(8,10); //Grabbing the day.
                    String weatherDate = weatherTimeAndDay.substring(0,10); //Grabbing the date.
                    String weatherTime = weatherTimeAndDay.substring(11,19); //Grabbing the time.
                    //Change the hour we need to grab to the first one that came out closest to our time.

                    logIt("WeatherDayGRAB: " + weatherDayWeNeedToGrab);
                    logIt("WeatherDay: " + weatherDay);
                    logIt("WeatherHour: " + weatherHour);
                    logIt("weatherHourGRAB: " + weatherHourWeNeedToGrab);

                    //4. A condition to check to check if we are grabbing the same hour
                    //And incrementing the day counter until we hit 5 (5-day forecast).
                    if ((Integer.valueOf(weatherHour) == Integer.valueOf(weatherHourWeNeedToGrab) &&
                            (weatherDay.equals(weatherDayWeNeedToGrab) || weatherDay.equals(firstDayOfTheMonth))) || firstDay){
                        weatherHourWeNeedToGrab = weatherHour;
                        //The same hour and fits the day counter -> GRAB RESULTS!
                        dayCounter++;
                        //Turn to false because there can only be one first day.
                        firstDay = false;

                        logIt("Inside the weatherTime condition");

                        //Grabbing the main branch and all it's components.
                        String mainTemp = jsonItems.getJSONObject(i).getJSONObject("main").getString("temp");
                        String mainMinTemp= jsonItems.getJSONObject(i).getJSONObject("main").getString("temp_min");
                        String mainMaxTemp = jsonItems.getJSONObject(i).getJSONObject("main").getString("temp_max");
                        String mainHumidity = jsonItems.getJSONObject(i).getJSONObject("main").getString("humidity");
                        logIt("mainTemp: " + mainTemp);

                        //Grabbing the weather branch and all it's components.
                        String weatherMain = jsonItems.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("main");
                        String weatherDescription = jsonItems.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description");

                        //Grabbing the winds.
                        String windSpeed = jsonItems.getJSONObject(i).getJSONObject("wind").getString("speed");

                        //Displaying the results
                        //CONVERT TEMPERATURE TO CELCIUS
                        switch (dayCounter){
                            case 1:
                                weatherDay1.setText(weatherDate + "\n" + weatherTime + "\n" +  weatherMain + "\n" + weatherDescription + "\n" + mainTemp + "\n" + mainMinTemp +
                                        "\n" + mainMaxTemp + "\n" + mainHumidity + "\n" + windSpeed + "\n\n" + uvIndexes[0]);
                            case 2:
                                weatherDay2.setText(weatherDate + "\n" + weatherTime + "\n" +  weatherMain + "\n" + weatherDescription + "\n" + mainTemp + "\n" + mainMinTemp +
                                        "\n" + mainMaxTemp + "\n" + mainHumidity + "\n" + windSpeed + "\n\n" + uvIndexes[1]);
                            case 3:
                                weatherDay3.setText(weatherDate + "\n" + weatherTime + "\n" +  weatherMain + "\n" + weatherDescription + "\n" + mainTemp + "\n" + mainMinTemp +
                                        "\n" + mainMaxTemp + "\n" + mainHumidity + "\n" + windSpeed + "\n\n" + uvIndexes[2]);
                            case 4:
                                weatherDay4.setText(weatherDate + "\n" + weatherTime + "\n" +  weatherMain + "\n" + weatherDescription + "\n" + mainTemp + "\n" + mainMinTemp +
                                        "\n" + mainMaxTemp + "\n" + mainHumidity + "\n" + windSpeed + "\n\n" + uvIndexes[3]);
                            case 5:
                                weatherDay5.setText(weatherDate + "\n" + weatherTime + "\n" +  weatherMain + "\n" + weatherDescription + "\n" + mainTemp + "\n" + mainMinTemp +
                                        "\n" + mainMaxTemp + "\n" + mainHumidity + "\n" + windSpeed + "\n\n" + uvIndexes[4]);
                        }



                        logIt("WeatherDayBeforeIncrease: " + weatherDayWeNeedToGrab);

                        int weatherIncrease;
                            //Increase the weather day counter.
                            weatherIncrease = Integer.valueOf(weatherDay);
                            weatherIncrease++;
                            if (weatherIncrease < 10) {
                                //Adds a zero at the start of the string or else it will search for a '1' or '2', which is invalid.
                                weatherDayWeNeedToGrab = String.valueOf("0" + weatherIncrease);
                            } else {
                                //Simply adds it since it is already a double digit number.
                                weatherDayWeNeedToGrab = String.valueOf(weatherIncrease);
                            }

                            //This prevents having duplicates for the first day of the month
                            if (weatherDay.equals(firstDayOfTheMonth)){
                                firstDayOfTheMonth = null;
                            }


                        logIt("WeatherDayAfterIncrease: " + weatherDayWeNeedToGrab);

                    } else {
                        logIt("Continue...");
                        continue;
                    }

                } //End of JSON information grabbing and displaying for loop.

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            //Alert user that input is invalid and that no response is given to us.
        }

    } // end of parseJSONandDisplayForecast


    public String[] parseJSONandDisplayUV(String jsonUvIndexResults) {

        String[] uvIndexes = new String[5];

        //Checks if s is null, if it is, then the user typed a bad city name and we did not
        //get any results from the website, which results in a crash when trying to use
        //bufferreader on a bad HttpURLConnection. This will redirect to a error page for user.
        if (jsonUvIndexResults != null) {

//            StringBuilder uvValues = new StringBuilder();
//            uvValues.append("UV index value: ");

            try {
                //Create a JSONArray with the String JSON results from 'doInBackground' method.
                JSONArray jsonArray = new JSONArray(jsonUvIndexResults);


                //Iterate through the JSONArray to grab all the uv values for 5 day range.
                for (int i = 0; i < 5; i++){
                    //Grabbing the uv value for each day.
                    uvIndexes[i] = jsonArray.getJSONObject(i).getString("value");
//                    uvValues.append("                    " + jsonArray.getJSONObject(i).getString("value"));
                }

                //Grabbing the first item to then grab the weather.
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return uvIndexes;

        }

        return uvIndexes;

    } // end of parseJSONandDisplayUV

    /**
     * This method is used to set up all the resources for the UI associated with displaying the
     * results from the JSON file that we grabbed from https://openweathermap.org/.
     * Displaying in a 5 day forecast fashion.
     */
    public void setUpWeatherDisplays(){

        //Finding resources for weather type.
        weatherDay1 = (TextView) findViewById(R.id.weatherDay1);
        weatherDay2 = (TextView) findViewById(R.id.weatherDay2);
        weatherDay3 = (TextView) findViewById(R.id.weatherDay3);
        weatherDay4 = (TextView) findViewById(R.id.weatherDay4);
        weatherDay5 = (TextView) findViewById(R.id.weatherDay5);
        cityname = (TextView) findViewById(R.id.cityname);
        uvIndex = (TextView) findViewById(R.id.uvIndex);
    }

    private String get3HourRangeCondition(String currentHourDay){
        String hour = "Hour?";

        for (int i = 0; i <= 24; i += 3) {
            if (i <= Integer.valueOf(currentHourDay)) {
                //Not in the correct range.
                continue;
            } else {
                //Correct range.
                if (i <= 9) {
                    hour = String.valueOf("0" + i); //Get the number out of the loop.

                } else {
                    hour = String.valueOf(i); //Get the number out of the loop.
                }
                break;
            }
        }
        return hour;
    } // end of get3HourRangeCondition






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
