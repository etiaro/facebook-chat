package com.etiaro.facebook;

import android.util.Log;

import com.etiaro.facebook.functions.Login;

import java.util.HashMap;

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

    private static HashMap<String, Account> accounts = new HashMap<>();
    public void addAccount(String id, Account ac){  accounts.put(id, ac);   }
    public void getAccount(String id){ accounts.get(id);    }


}
