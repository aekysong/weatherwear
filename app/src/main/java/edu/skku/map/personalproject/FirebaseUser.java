package edu.skku.map.personalproject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUser {
    public String username;

    public FirebaseUser() {
    }

    public FirebaseUser(String username) {
        this.username = username;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        return result;
    }
}
