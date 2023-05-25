package ru.mirea.muravievvr.httpurlconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.mirea.muravievvr.httpurlconnection.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.button.setOnClickListener(view ->
        {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkinfo = null;
            if (connectivityManager != null) {
                networkinfo = connectivityManager.getActiveNetworkInfo();
            }
            if (networkinfo != null && networkinfo.isConnected()) {
                new DownloadPageTask().execute("https://ipinfo.io/json" );
            } else {
                Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class DownloadPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.ipTextView.setText("Загружаем...");
            binding.cityTextView.setText("Загружаем...");
            binding.regionTextView.setText("Загружаем...");
            binding.temperatureTextView.setText("Загружаем...");
            binding.dayPartTextView.setText("Загружаем...");
            binding.windspeedTextView.setText("Загружаем...");
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadIpInfo(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(MainActivity.class.getSimpleName(), result);
            try {
                JSONObject responseJson = new JSONObject(result);
                Log.d(MainActivity.class.getSimpleName(), "Response: " + responseJson);
                String ip = responseJson.getString("ip");
                String loc = responseJson.getString("loc");
                String address = "https://api.open-meteo.com/v1/forecast?latitude="
                        + loc.split(",")[0] + "&longitude=" +loc.split(",")[1] + "&current_weather=true";
                String city = responseJson.getString("city");
                String region = responseJson.getString("region");
                Log.d(MainActivity.class.getSimpleName(), "IP: " + ip);
                Log.d(MainActivity.class.getSimpleName(), "Location: " + loc);
                Log.d(MainActivity.class.getSimpleName(), "City: " + city);
                Log.d(MainActivity.class.getSimpleName(), "Region: " + region);
                binding.ipTextView.setText("Ip = " + ip);
                binding.cityTextView.setText("City: " + city);
                binding.regionTextView.setText("Region: " + region);
                new DownloadPageTask2().execute(address);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }

        private String downloadIpInfo(String address) throws IOException {
            InputStream inputStream = null;
            String data = "";
            try {
                URL url = new URL(address);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(100000);
                connection.setConnectTimeout(100000);
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(true);
                connection.setUseCaches(false);
                connection.setDoInput(true);
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                    inputStream = connection.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int read = 0;
                    while ((read = inputStream.read()) != -1) {
                        bos.write(read);
                    }
                    bos.close();
                    data = bos.toString();
                } else {
                    data = connection.getResponseMessage() + ". Error Code: " + responseCode;
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return data;
        }

        public class DownloadPageTask2 extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... urls) {
                try {
                    return downloadWeatherInfo(urls[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                    return "error";
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(MainActivity.class.getSimpleName(), result);
                try {
                    JSONObject responseJson = new JSONObject(result);
                    JSONObject weatherInfo =
                            new JSONObject(responseJson.getString("current_weather"));
                    Log.d(MainActivity.class.getSimpleName(), "Response: " + responseJson);
                    String temperature = weatherInfo.getString("temperature");
                    String windspeed = weatherInfo.getString("windspeed");
                    int isDay = weatherInfo.getInt("is_day");
                    String dayPart = "";
                    if (isDay == 1) dayPart = "День";
                    else dayPart = "Ночь";
                    Log.d(MainActivity.class.getSimpleName(), "Temperature: " + temperature);
                    Log.d(MainActivity.class.getSimpleName(), "Windspeed: " + windspeed);
                    Log.d(MainActivity.class.getSimpleName(), "DayPart: " + dayPart);
                    binding.temperatureTextView.setText("Temperature: " + temperature);
                    binding.windspeedTextView.setText("Wind speed: " + windspeed);
                    binding.dayPartTextView.setText("Day part: " + dayPart);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onPostExecute(result);
            }

            private String downloadWeatherInfo(String address) throws IOException {
                InputStream inputStream = null;
                String data = "";
                try {
                    URL url = new URL(address);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(100000);
                    connection.setConnectTimeout(100000);
                    connection.setRequestMethod("GET");
                    connection.setInstanceFollowRedirects(true);
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                        inputStream = connection.getInputStream();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        int read = 0;
                        while ((read = inputStream.read()) != -1) {
                            bos.write(read);
                        }
                        bos.close();
                        data = bos.toString();
                    } else {
                        data = connection.getResponseMessage() + ". Error Code: " + responseCode;
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
                return data;
            }
        }
    }
}