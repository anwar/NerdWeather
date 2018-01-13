package com.emiappnw.nerdweather;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        asyncTask.execute("51.05", "13.74"); // ("Latitude", "Longitude")
        //              End API Stuff              //
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
