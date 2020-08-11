package edu.skku.map.personalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewRecordActivity extends AppCompatActivity {
    private static final String TAG = "NewRecordActivity";
    String username = "";
    Button recordButton;
    Spinner recordSpinner1, recordSpinner2, recordSpinner3, recordSpinner4, recordSpinner5;
    String[] select = new String[5];
    float temperature;
    private DatabaseReference mWeatherReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);

        if(getIntent().getExtras() != null){
            Intent newRecordIntent = getIntent();
            username = newRecordIntent.getStringExtra("username");
            temperature = newRecordIntent.getFloatExtra("temperature", 0.0f);
        }

        mWeatherReference = FirebaseDatabase.getInstance().getReference().child("weather");

        recordSpinner1 = (Spinner) findViewById(R.id.record_spinner1);
        recordSpinner2 = (Spinner) findViewById(R.id.record_spinner2);
        recordSpinner3 = (Spinner) findViewById(R.id.record_spinner3);
        recordSpinner4 = (Spinner) findViewById(R.id.record_spinner4);
        recordSpinner5 = (Spinner) findViewById(R.id.record_spinner5);

        recordSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                select[0] = recordSpinner1.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                select[0] = "None";
            }
        });

        recordSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                select[1] = recordSpinner2.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                select[1] = "None";
            }
        });

        recordSpinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                select[2] = recordSpinner3.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                select[2] = "None";
            }
        });

        recordSpinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                select[3] = recordSpinner4.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                select[3] = "None";
            }
        });

        recordSpinner5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                select[4] = recordSpinner5.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                select[4] = "None";
            }
        });

        recordButton = (Button) findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select = convertToKey(select);
                Log.d(TAG, "onClick: " + select[0] + " " + select[1] + " " + select[2] + " " + select[3] + " " + select[4]);

                String refChild = "";
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

                Log.d(TAG, "onClick: " + mWeatherReference.child(refChild));

                mWeatherReference.child(refChild).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isPosted = false;

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            for (String item : select) {
                                if (item.equals(data.getKey())) {
                                    isPosted = true;
                                    long dataValue = (long) data.getValue();
                                    dataValue = dataValue + 1;
                                    data.getRef().setValue(dataValue);
                                    Log.d(TAG, "onDataChange: " + data.getKey() + " => " + dataValue);
                                }
                            }
                        }

                        if (isPosted) {
                            Intent newRecordIntent = new Intent(NewRecordActivity.this, MainPageActivity.class);
                            newRecordIntent.putExtra("username", username);
                            startActivity(newRecordIntent);
                        } else {
                            Toast.makeText(NewRecordActivity.this, "Something went wrong! Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private String[] convertToKey(String[] selectedItems) {
        for (int i=0; i < selectedItems.length; i++) {
            switch (selectedItems[i]) {
                case "Padded Coat":
                    selectedItems[i] = "padding";
                    break;
                case "Jacket":
                    selectedItems[i] = "jacket";
                    break;
                case "Coat":
                    selectedItems[i] = "coat";
                    break;
                case "Leather Jacket":
                    selectedItems[i] = "leatherJacket";
                    break;
                case "Cardigan":
                    selectedItems[i] = "cardigan";
                    break;
                case "Shirt":
                    selectedItems[i] = "shirt";
                    break;
                case "Sweat Shirt":
                    selectedItems[i] = "sweatShirt";
                    break;
                case "Short-sleeved T-shirt":
                    selectedItems[i] = "shortSleeved";
                    break;
                case "Sleeveless T-shirt":
                    selectedItems[i] = "sleeveless";
                    break;
                case "Blouse":
                    selectedItems[i] = "blouse";
                    break;
                case "Vest":
                    selectedItems[i] = "vest";
                    break;
                case "Sweater":
                    selectedItems[i] = "sweater";
                    break;
                case "Jeans":
                    selectedItems[i] = "jeans";
                    break;
                case "Trouser":
                    selectedItems[i] = "trouser";
                    break;
                case "Shorts":
                    selectedItems[i] = "shorts";
                    break;
                case "Skirt":
                    selectedItems[i] = "skirt";
                    break;
                case "One-piece Dress":
                    selectedItems[i] = "onePiece";
                    break;
                default:
                    selectedItems[i] = "None";
                    break;
            }
        }
        return selectedItems;
    }
}
