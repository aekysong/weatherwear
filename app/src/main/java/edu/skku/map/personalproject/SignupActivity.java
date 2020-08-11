package edu.skku.map.personalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private DatabaseReference mUserReference;
    String username = "";
    EditText usernameET;
    Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mUserReference = FirebaseDatabase.getInstance().getReference();
        signupButton = (Button) findViewById(R.id.signup_button);
        usernameET = (EditText) findViewById(R.id.signup_username);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameET.getText().toString();

                if (username.length() == 0) {
                    Toast.makeText(SignupActivity.this, "Username is empty", Toast.LENGTH_SHORT).show();
                } else {
                    mUserReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean isExist = false;

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                if (data.getValue(FirebaseUser.class).username.equals(username)) {
                                    isExist = true;
                                    Toast.makeText(SignupActivity.this, "Please use another username", Toast.LENGTH_SHORT).show();
                                }
                            }

                            if (!isExist) {
                                postFirebaseDatabase(true);
                                Intent loginIntent = new Intent(SignupActivity.this, MainActivity.class);
                                startActivity(loginIntent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    private void postFirebaseDatabase(boolean add) {
        Map<String, Object> postValues = null;
        if (add) {
            FirebaseUser post = new FirebaseUser(username);
            postValues = post.toMap();
        }

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + username, postValues);

        mUserReference.updateChildren(childUpdates);
    }
}
