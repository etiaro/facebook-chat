package com.etiaro.facebook;

import android.util.Log;

import com.etiaro.facebook.functions.Login;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by jakub on 15.03.18.
 */

public class Facebook {
    //Thats the singlethon stuff
    private static Facebook instance = null;
    protected Facebook() {}
    public static Facebook getInstance() {
        if(instance == null) {
            instance = new Facebook();
        }
        return instance;
    }

    public HashMap<String, Account> accounts = new HashMap<>();
}
