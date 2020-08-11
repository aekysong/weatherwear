package edu.skku.map.personalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.Objects;


public class MainPageActivity extends AppCompatActivity {
    private static final String TAG = "MainPageActivity";
    String username = "";
    TextView greeting;
    ImageButton newRecord;
    TextView weatherText, weatherCondition;
    ImageView weatherImage;
    float temperature;
    private DatabaseReference mWeatherReference;
    ImageView wearImage1, wearImage2, wearImage3;
    TextView wearText1, wearText2, wearText3, wearText11, wearText21, wearText31;
    String[] top3 = new String[3];
    long[] top3value = new long[3];
    String refChild = "";
    public static Handler threadHandler;
    getOpenWeatherAPI openWeatherAPI;
    double latitude, longitude;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        if(getIntent().getExtras() != null){
            Intent loginIntent = getIntent();
            username = loginIntent.getStringExtra("username");
            latitude = loginIntent.getDoubleExtra("latitude", 0.0);
            longitude = loginIntent.getDoubleExtra("longitude", 0.0);
        }

        GPSTracker gpsTracker = new GPSTracker(MainPageActivity.this);
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        // set greetings
        greeting = (TextView) findViewById(R.id.greeting);
        greeting.setText("Hello, " + username + "!");
        Shader textShader=new LinearGradient(0, 0, 0, greeting.getTextSize() * 2,
                new int[]{Color.rgb(209, 253, 255), Color.rgb(253, 219, 146)},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        greeting.getPaint().setShader(textShader);

        // new record
        newRecord = (ImageButton) findViewById(R.id.new_record);
        newRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newRecordIntent = new Intent(MainPageActivity.this, NewRecordActivity.class);
                newRecordIntent.putExtra("username", username);
                newRecordIntent.putExtra("temperature", temperature);
                startActivity(newRecordIntent);
            }
        });

        // weather
        weatherText = (TextView) findViewById(R.id.weather_text);
        weatherImage = (ImageView) findViewById(R.id.weather_image);
        weatherCondition = (TextView) findViewById(R.id.weather_condition);

        // set recommendation wear
        mWeatherReference = FirebaseDatabase.getInstance().getReference().child("weather");
        wearImage1 = (ImageView) findViewById(R.id.wear_image1);
        wearImage2 = (ImageView) findViewById(R.id.wear_image2);
        wearImage3 = (ImageView) findViewById(R.id.wear_image3);
        wearText1 = (TextView) findViewById(R.id.wear_image1_text);
        wearText2 = (TextView) findViewById(R.id.wear_image2_text);
        wearText3 = (TextView) findViewById(R.id.wear_image3_text);
        wearText11 = (TextView) findViewById(R.id.wear_image1_text2);
        wearText21 = (TextView) findViewById(R.id.wear_image2_text2);
        wearText31 = (TextView) findViewById(R.id.wear_image3_text2);

        threadHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Map<String, Object> result = (Map<String, Object>) msg.obj;

                String stringBuilder = Objects.requireNonNull(result.get("stringBuilder")).toString();
                String description = Objects.requireNonNull(result.get("description")).toString();
                String iconUrl = Objects.requireNonNull(result.get("icon")).toString();
                temperature = (float) result.get("temperature");

                weatherCondition.setText(description);
                weatherText.setText(stringBuilder);
                String iconUrlString = "http://openweathermap.org/img/wn/" + iconUrl + "@2x.png";
                Picasso.get().load(iconUrlString).into(weatherImage);

                if (temperature < 6.0f) {
                    // under5
                    refChild = "under5";
                }
                else if (6.0f <= temperature && temperature < 10.0f) {
                    // from6to9
                    refChild = "from6to9";
                }
                else if (10.0f <= temperature && temperature < 12.0f) {
                    // from10to11
                    refChild = "from10to11";
                }
                else if (12.0f <= temperature && temperature < 20.0f) {
                    // from12to19
                    refChild = "from12to19";
                }
                else if (20.0f <= temperature && temperature < 23.0f) {
                    // from20to22
                    refChild = "from20to22";
                }
                else if (23.0f <= temperature && temperature < 27.0f) {
                    // from23to26
                    refChild = "from23to26";
                }
                else if (27.0f <= temperature) {
                    // over27
                    refChild = "over27";
                }

                Log.d(TAG, "onCreate: " + mWeatherReference.child(refChild));
                Query top3Query = mWeatherReference.child(refChild).orderByValue().limitToLast(3);
                top3Query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int idx = 0;
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: " + data);
                            top3[idx] = data.getKey();
                            top3value[idx] = (long) data.getValue();
                            idx++;
                        }

                        // set wearImage
                        int[] top3resourceID = new int[3];
                        for (int i = 0; i < top3.length; i++) {
                            String imageName = top3[i];
                            assert imageName != null;
                            int resourceID = getResources().getIdentifier(imageName.toLowerCase(), "drawable", "edu.skku.map.personalproject");
                            top3resourceID[i] = resourceID;
                        }

                        wearImage1.setImageResource(top3resourceID[2]);
                        wearImage2.setImageResource(top3resourceID[1]);
                        wearImage3.setImageResource(top3resourceID[0]);

                        // set wearText
                        top3 = convertToItemName(top3);
                        wearText1.setText(top3[2]);
                        String wearText11StringBuilder = "Votes: " + top3value[2];
                        wearText11.setText(wearText11StringBuilder);
                        wearText2.setText(top3[1]);
                        String wearText21StringBuilder = "Votes: " + top3value[1];
                        wearText21.setText(wearText21StringBuilder);
                        wearText3.setText(top3[0]);
                        String wearText31StringBuilder = "Votes: " + top3value[0];
                        wearText31.setText(wearText31StringBuilder);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };

        openWeatherAPI = new getOpenWeatherAPI();
        openWeatherAPI.setParams(String.valueOf(latitude), String.valueOf(longitude));
        Thread openWeatherAPIthread = new Thread(openWeatherAPI);
        openWeatherAPIthread.start();
    }

    private String[] convertToItemName(String[] rankedItems) {
        for (int i=0; i < rankedItems.length; i++) {
            switch (rankedItems[i]) {
                case "padding":
                    rankedItems[i] = "Padded Coat";
                    break;
                case "jacket":
                    rankedItems[i] = "Jacket";
                    break;
                case "coat":
                    rankedItems[i] = "Coat";
                    break;
                case "leatherJacket":
                    rankedItems[i] = "Leather Jacket";
                    break;
                case "cardigan":
                    rankedItems[i] = "Cardigan";
                    break;
                case "shirt":
                    rankedItems[i] = "Shirt";
                    break;
                case "sweatShirt":
                    rankedItems[i] = "Sweat Shirt";
                    break;
                case "shortSleeved":
                    rankedItems[i] = "Short-sleeved T-shirt";
                    break;
                case "sleeveless":
                    rankedItems[i] = "Sleeveless T-shirt";
                    break;
                case "blouse":
                    rankedItems[i] = "Blouse";
                    break;
                case "vest":
                    rankedItems[i] = "Vest";
                    break;
                case "sweater":
                    rankedItems[i] = "Sweater";
                    break;
                case "jeans":
                    rankedItems[i] = "Jeans";
                    break;
                case "trouser":
                    rankedItems[i] = "Trouser";
                    break;
                case "shorts":
                    rankedItems[i] = "Shorts";
                    break;
                case "skirt":
                    rankedItems[i] = "Skirt";
                    break;
                case "onePiece":
                    rankedItems[i] = "One-piece Dress";
                    break;
                default:
                    rankedItems[i] = "None";
                    break;
            }
        }
        return rankedItems;
    }

}
