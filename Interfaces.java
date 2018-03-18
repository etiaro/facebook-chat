package com.etiaro.facebook;


import com.etiaro.facebook.Account;

import java.util.HashMap;

public class Interfaces {
    public interface LoginCallback{
        void success(Account ac);
        void fail();
        void cancelled();
    }
    public static class UserInfo{
        public String name, firstName, vanity, thumbSrc, profileUrl, gender, type;
        public boolean isFriend;
    }
    public interface UserInfoCallback{
        void success(UserInfo info);
        void fail();
        void cancelled();
    }
}

