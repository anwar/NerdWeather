package com.emiappnw.nerdweather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.bluejamesbond.text.DocumentView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    TextView currentLocView, weatherDescriptionView, currentTempView, WindView, HumidityView, PressureView, maxTempCView, maxTempFView, maxDetailTextView, minTempCView, minTempFView, minDetailTextView;
    DocumentView FactView;
    ImageView weatherBgImg;

    // Initialized for comparison with hourOfDayToCheck
    Calendar calendar = Calendar.getInstance();
    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        // Updates background on first run
        background_updater();

        // Accesses location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Checks for location changes
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                // Runs background_updater() every hour
                calendar = Calendar.getInstance();
                int hourOfDayToCheck = calendar.get(Calendar.HOUR_OF_DAY);
                if (hourOfDayToCheck != hourOfDay){

                    background_updater();
                }

                // Gets latitude and longitude and converts to String
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                final String latStr = String.valueOf(latitude);
                final String lonStr = String.valueOf(longitude);

                //-------------- Weather API Stuff --------------//
                currentLocView = findViewById(R.id.currentLoc);
                weatherDescriptionView = findViewById(R.id.weatherDescription);
                currentTempView = findViewById(R.id.currentTemp);
                WindView = findViewById(R.id.Wind);
                HumidityView = findViewById(R.id.Humidity);
                PressureView = findViewById(R.id.Pressure);

                WeatherAPI.placeIdTask asyncTask = new WeatherAPI.placeIdTask(new WeatherAPI.AsyncResponse() {
                    public void processFinish(String weather_location, String weather_description, String weather_temperature, String weather_wind, String weather_humidity, String weather_pressure) {
                        currentLocView.setText(weather_location);
                        weatherDescriptionView.setText(weather_description);
                        // Update the fact according to the current temperature
                        facts_parser(weather_temperature);

                        displayTempUnit(weather_temperature);
                        WindView.setText("W: "+weather_wind+ " m/s");
                        HumidityView.setText("H: "+weather_humidity+ "%");
                        PressureView.setText("P: "+weather_pressure+ " hPa");

                        // Save current Temp and Location in SharedPreferences
                        editor.putString("tempStored", weather_temperature);
                        editor.putString("locationStored", weather_location);
                        editor.apply();

                        // Checks if this is the first time the app is running and sets default values for the PopupWindow
                        if (sharedPref.getBoolean("firstRun", true)) {
                            editor.putString("MaxTCStored", sharedPref.getString("tempStored", ""));
                            editor.putString("MinTCStored", sharedPref.getString("tempStored", ""));
                            editor.putBoolean("firstRun", false);
                            editor.apply();
                        }

                    }
                });

                asyncTask.execute(latStr, lonStr); // ("Latitude", "Longitude")
                //-------------- Weather API Stuff --------------//

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                // No coding necessary
            }

            @Override
            public void onProviderEnabled(String s) {
                // No coding necessary
            }

            // Option to enable GPS if it is disabled
            @Override
            public void onProviderDisabled(String s) {
                Toast.makeText(getApplicationContext(), "Please turn on location to continue", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        request_location();

    }


    // Location permission check
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 420:
                request_location();
                break;
            default:
                break;
        }
    }

    void request_location(){

        // Permission check if API level >= 23
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET},420);
            }

            return;
        }

        // Location requested every second if permissions are granted using the GPS sensor
        locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);

    }

    public void background_updater(){

        weatherBgImg = findViewById(R.id.weatherBgImg);
        // repeated to get an updated hourOfDay if hourOfDay != hourOfDayToCheck
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay >= 6 && hourOfDay <= 12) {
            weatherBgImg.setImageResource(R.drawable.weatherimg_morning);
        } else if (hourOfDay >= 12 && hourOfDay <= 16) {
            weatherBgImg.setImageResource(R.drawable.weatherimg_afternoon);
        } else if (hourOfDay >= 17 && hourOfDay <= 20) {
            weatherBgImg.setImageResource(R.drawable.weatherimg_evening);
        } else if (hourOfDay >= 20 || hourOfDay < 6) {
            weatherBgImg.setImageResource(R.drawable.weatherimg_night);
        }
    }

    public void facts_parser(String weather_temperature){

        StringBuilder json = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("facts.json")));
            String temp;
            while ((temp=reader.readLine())!=null)
                json.append(temp).append("\n");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject data = new JSONObject(json.toString());

            JSONObject main = data.getJSONObject("temps");
            String name = main.getString(weather_temperature);

            FactView = findViewById(R.id.Fact);
            FactView.setText(name);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void onCurrentTempClick(View view) {
        if (sharedPref.getString("tempUnitStored", "").equals("C")) {
            editor.putString("tempUnitStored", "F");
            editor.apply();
        }
        else {
            editor.putString("tempUnitStored", "C");
            editor.apply();
        }
        displayTempUnit(sharedPref.getString("tempStored", ""));
    }

    public void displayTempUnit(String weather_temperature) {
        if (sharedPref.getString("tempUnitStored", "").equals("C")) {
            currentTempView.setText(weather_temperature + "C");
        }
        else {
            int fahrenheit = (int) Math.round(Integer.parseInt(weather_temperature)*1.8 + 32);
            currentTempView.setText(String.valueOf(fahrenheit) + "F");
        }
    }

    public void onButtonShowPopupWindowClick(View view) {

        //creates MP
        MediaPlayer mp = MediaPlayer.create(this, R.raw.krane_snare);
        try {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
                mp = MediaPlayer.create(this, R.raw.krane_snare);
            } mp.start();
        } catch(Exception e) { e.printStackTrace(); }

        // Reference main layout
        ConstraintLayout mainLayout = findViewById(R.id.mainLayout);

        // Set popup window size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x - 100;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        // Inflate popup window layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.dialog_temp_stats, null);

        // Create the popup window
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // Animation
        popupWindow.setBackgroundDrawable(new ColorDrawable( android.graphics.Color.TRANSPARENT));

        popupWindow.setAnimationStyle(R.style.Animation);

        // Show popup
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER_HORIZONTAL, 0, -300);


        //===================== PopupWindow Stuff =====================//
        // Initial Data for the loop
        int cTemp = Integer.parseInt(sharedPref.getString("tempStored", ""));
        int maxC = Integer.parseInt(sharedPref.getString("MaxTCStored", ""));
        int minC = Integer.parseInt(sharedPref.getString("MinTCStored", ""));

        // Get current Date and format it
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
        String today = formatter.format(date);

        // The wheels of the bus go round and round...
        if (cTemp >= maxC) {
            maxC = cTemp;
            int maxF = (int) (1.8*maxC)+32;
            String MaxTC = Integer.toString(maxC);
            String MaxTF = Integer.toString(maxF);
            editor.putString("MaxTCStored", MaxTC);
            editor.putString("MaxTFStored", MaxTF);
            editor.putString("MaxLocStored", sharedPref.getString("locationStored", "")+"  -  "+today);
            editor.apply();
        }
        if (cTemp <= minC) {
            minC = cTemp;
            int minF = (int) (1.8*minC)+32;
            String MinTC = Integer.toString(minC);
            String MinTF = Integer.toString(minF);
            editor.putString("MinTCStored", MinTC);
            editor.putString("MinTFStored", MinTF);
            editor.putString("MinLocStored", sharedPref.getString("locationStored", "")+"  -  "+today);
            editor.apply();
        }

        maxTempCView = popupWindow.getContentView().findViewById(R.id.maxTempC);
        maxTempFView = popupWindow.getContentView().findViewById(R.id.maxTempF);
        maxDetailTextView = popupWindow.getContentView().findViewById(R.id.maxDetailText);
        minTempCView = popupWindow.getContentView().findViewById(R.id.minTempC);
        minTempFView = popupWindow.getContentView().findViewById(R.id.minTempF);
        minDetailTextView = popupWindow.getContentView().findViewById(R.id.minDetailText);


        maxTempCView.setText(sharedPref.getString("MaxTCStored", "")+" ??C");
        maxTempFView.setText(" / "+sharedPref.getString("MaxTFStored", "")+" ??F");
        maxDetailTextView.setText(sharedPref.getString("MaxLocStored", ""));
        minTempCView.setText(sharedPref.getString("MinTCStored", "")+" ??C");
        minTempFView.setText(" / "+sharedPref.getString("MinTFStored", "")+" ??F");
        minDetailTextView.setText(sharedPref.getString("MinLocStored", ""));
        //===================== PopupWindow Stuff =====================//

    }

    // Inject Calligraphy into Context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void sharingIsCaring(View view) {

        String textToShare = "Extreme temperatures that I have survived:\nMIN: " +sharedPref.getString("MinTCStored", "")+ "??C\nMAX: " +sharedPref.getString("MaxTCStored", "")+ "??C\nIncrease your knowledge when you check the weather! Get Nerd Weather today: https://goo.gl/VR96S4";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Download Nerd Weather");
        sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

}
