package com.company;

import java.io.Serializable;

public class UserData implements Serializable {

    private static final long serialVersionUID = 4088384793623750965L;



        private String username;
        private String password;

        public UserData(String usr, String pass)
        {
            username = usr;
            password = pass;
        }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
