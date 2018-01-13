package com.emiappnw.nerdweather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    TextView currentLocationField, weatherDescriptionField, currentTempField, minTempCField, maxTempCField, WindField, HumidityField, PressureField;
    private LocationManager locationManager;
    private LocationListener locationListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // accesses location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //checks for location changes
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //              Begin API Stuff              //
                currentLocationField = findViewById(R.id.currentLoc);
                weatherDescriptionField = findViewById(R.id.weatherDescription);
                currentTempField = findViewById(R.id.currentTemp);
                // minTempCField = findViewById(R.id.minTempC); // We don't need minimum temp at the moment
                // maxTempCField = findViewById(R.id.maxTempC); // We don't need maximum temp at the moment
                WindField = findViewById(R.id.Wind);
                HumidityField = findViewById(R.id.Humidity);
                PressureField = findViewById(R.id.Pressure);

                WeatherAPI.placeIdTask asyncTask = new WeatherAPI.placeIdTask(new WeatherAPI.AsyncResponse() {
                    public void processFinish(String weather_location, String weather_description, String weather_temperature, String weather_temperature_min, String weather_temperature_max, String weather_wind, String weather_humidity, String weather_pressure) {

                        currentLocationField.setText(weather_location);
                        weatherDescriptionField.setText(weather_description);
                        currentTempField.setText(weather_temperature);
                        // minTempCField.setText(weather_temperature_min); // We don't need minimum temp at the moment
                        // maxTempCField.setText(weather_temperature_max); // We don't need maximum temp at the moment
                        WindField.setText("W: "+weather_wind);
                        HumidityField.setText("H: "+weather_humidity);
                        PressureField.setText("P: "+weather_pressure);

                    }
                });

                //              End API Stuff              //

                // gets latitude and longitude and converts to String
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String latStr = String.valueOf(latitude);
                String lonStr = String.valueOf(longitude);

                //sends lat and long to API
                asyncTask.execute(latStr, lonStr); // ("Latitude", "Longitude")

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            //not needed
            }

            @Override
            public void onProviderEnabled(String s) {
            //not needed
            }

            @Override
            public void onProviderDisabled(String s) {
                // option to enable GPS if it is disabled
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        };

        request_location();

    }

    @Override
    // permission check
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
        // permission check if API level >= 23
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,420);
            }
            return;
        }
        // location gets requested every second
        locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);

    }

    public void onButtonShowPopupWindowClick(View view) {

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
        boolean focusable = true; // Lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // Animation
        popupWindow.setBackgroundDrawable(new ColorDrawable( android.graphics.Color.TRANSPARENT));

        popupWindow.setAnimationStyle(R.style.Animation);

        // Show popup
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER_HORIZONTAL, 0, -300);

    }

    // Inject Calligraphy into Context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
