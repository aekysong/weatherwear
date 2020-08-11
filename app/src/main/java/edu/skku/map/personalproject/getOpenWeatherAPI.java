package edu.skku.map.personalproject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static edu.skku.map.personalproject.MainPageActivity.threadHandler;


public class getOpenWeatherAPI implements weatherRunnable {

    private static final String TAG = "getOpenWeatherAPI";
    public static String BaseUrl = "http://api.openweathermap.org/";
    public static String AppId = "cccc0692ad8173bb613e53d0bbab3462";
    public static String lat;
    public static String lon;
    public static String units = "metric";
    Map<String, Object> result = new HashMap<>();

    @Override
    public void setParams(String lat, String lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: " + "latitude is " + lat + " / longitude is " + lon);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeatherData(lat, lon, AppId, units);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.code() == 200) {
                    WeatherResponse weatherResponse = response.body();
//                    Log.d(TAG, "onResponse: " + weatherResponse);
                    assert weatherResponse != null;

                    String stringBuilder = "Location: "
                            + weatherResponse.name + " in " + weatherResponse.sys.country +
                            "\n" +
                            "Temperature: "
                            + weatherResponse.main.temp +
                            "\n" +
                            "Humidity: " +
                            weatherResponse.main.humidity;

                    result.put("description", weatherResponse.weather.get(0).description);
                    result.put("stringBuilder", stringBuilder);
                    result.put("icon", weatherResponse.weather.get(0).icon);
                    result.put("temperature", weatherResponse.main.temp);

                    Message msg = new Message();
                    msg.obj = result;
                    threadHandler.sendMessage(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                result.put("msg", t.getMessage());

                Message msg = new Message();
                msg.obj = result;
                threadHandler.sendMessage(msg);
            }
        });
    }
}
