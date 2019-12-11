package company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;

public class UserData implements Serializable {

    private static final long serialVersionUID = 4088384793623750965L;


    private static final int STATE_OFFLINE = 0;
    private static final int STATE_LOGIN_SENT = 1;
    private static final int STATE_ONLINE = 2;

    private String username;
    private String password;
    private ArrayList<String> clientMessages = new ArrayList<>();
    private ArrayList<String> subscriberList = new ArrayList<>();


    private int state;
    private int token;
    private Random rand = new Random();
    private boolean isLoggedin = false;

    public UserData(String usr, String pass)
    {
        username = usr;
        password = pass;
        state = STATE_OFFLINE;
        token = abs(rand.nextInt(61000 - 32768 + 1) + 32768);
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public int getToken() {
        return token;
    }

    public void setLoggedin(boolean loggedIn) {
            isLoggedin = loggedIn;
    }

    public boolean isLoggedin() {
            return isLoggedin;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void addClientMessages(String msg) {
        clientMessages.add(msg);
    }

    public void addSubscriber(String name) {
        subscriberList.add(name);
    }

    public ArrayList<String> getSubscriberList() {
        return subscriberList;
    }

    public ArrayList<String> getClientMessages() {
        return clientMessages;
    }
}
