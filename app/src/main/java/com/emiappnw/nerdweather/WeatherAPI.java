package com.emiappnw.nerdweather;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask; // AsyncTask enables proper and easy use of the UI thread.
import android.util.Log; // API for sending log output.

import org.json.JSONObject; // Self-explanatory.
import org.json.JSONException; // Thrown to indicate a problem with the JSON API.

import java.io.BufferedReader; // Read text from an input-stream and buffer the characters for efficient reading.
import java.io.InputStreamReader; // Reads bytes and decodes them into characters using a specified charset.
import java.net.HttpURLConnection; // A URLConnection with support for HTTP-specific features.
import java.net.URL; // Self-explanatory.
import java.util.List;
import java.util.Locale; // Self-explanatory.

public class WeatherAPI {

    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric";
    private static final String API_KEY = "your-openweathermap-api-key";


    public interface AsyncResponse {
        void processFinish(String output1, String output2, String output3, String output4, String output5, String output6);
    }

    public static class placeIdTask extends AsyncTask <String, Void, JSONObject> {

        public AsyncResponse delegate = null;

        public placeIdTask(AsyncResponse asyncResponse) {
            delegate = asyncResponse;
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject jsonWeather = null;

            try {
                jsonWeather = getWeatherJSON(params[0], params[1]);
            } catch (Exception e) {
                Log.d("Error", "Cannot process JSON results", e);
            }

            return jsonWeather;
        }

        // Get the weather data from JSON and assign it to our variables
        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if(json != null){
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");


                    String lat = json.getJSONObject("coord").getString("lat");
                    String lon = json.getJSONObject("coord").getString("lon");

                    String city = json.getString("name");
                    String country = json.getJSONObject("sys").getString("country");
                    String location;
                    if (country.equals("US")) {
                        location = city + ", " + country ;
                    } else {
                        location = city + ", " + country;
                    }
                    String description = details.getString("description").toUpperCase(Locale.US);

                    // Comment the line below for manual temperature input

                    String temperature = String.format(Locale.US,"%.0f", main.getDouble("temp"));
                    if (temperature.equals("-0")) {
                        temperature = "0";
                    }

                    // Uncomment the line below if you want to manually input temperatures
//                    String temperature = "-9";

                    String wind = String.format(Locale.US,"%.1f", json.getJSONObject("wind").getDouble("speed"));
                    String humidity = main.getString("humidity");
                    String pressure = main.getString("pressure");

                    delegate.processFinish(location, description, temperature, wind, humidity, pressure);
                }
            } catch (JSONException e) {
                Log.e("Error", "Cannot assign JSON result to variables", e);
            }
        }
    }

    // Get Weather Data from OpenWeatherMap API
    public static JSONObject getWeatherJSON(String lat, String lon){
        try {
            URL url = new URL(String.format(API_URL, lat, lon));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.addRequestProperty("x-api-key", API_KEY);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);
            String tmp;
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            if(data.getInt("cod") != 200){
                return null;
            }
            return data;
        } catch(Exception e) {
            return null;
        }
    }

}
