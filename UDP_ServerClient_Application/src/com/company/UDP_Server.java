package com.company;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class UDP_Server {

    static LinkedList<UserData> userList = new LinkedList<UserData>();

    private static final String ip = "127.0.0.1";
    private static final int portSend = 50000;
    private static final int portReceive = 32000;

    // The header = "magic1 + magic2 + OPCODE + payloadlength +
    //  + token + messageID + variable payload length

    private String header = "";
    private static final String magic1 = "M";
    private static final String magic2 = "B";

    private static final int STATE_OFFLINE = 0;
    private static final int STATE_LOGIN_SENT = 1;
    private static final int STATE_ONLINE = 2;

    private static final int EVENT_USER_LOGIN = 0;
    private static final int EVENT_USER_POST = 1;
    private static final int EVENT_USER_INVALID = 79;
    private static final int EVENT_NET_LOGIN_SUCCESSFUL = 80;
    private static final int EVENT_NET_POST_ACK = 81;
    private static final int EVENT_NET_INVALID = 255;

    private static final int OPCODE_SESSION_RESET = 0x00;
    private static final int OPCODE_MUST_LOGIN_FIRST = 0xF0;
    private static final int OPCODE_LOGIN_CLIENT = 0x10;
    private static final int OPCODE_SUCCESSFUL_LOGIN_ACK = 0x80;
    private static final int OPCODE_FAILED_LOGIN_ACK = 0x81;
    private static final int OPCODE_SUBSCRIBE_CLIENT = 0x20;
    private static final int OPCODE_SUCCESSFUL_SUBSCRIBE_ACK = 0x90;
    private static final int OPCODE_FAILED_SUBSCRIBE_ACK = 0x91;
    private static final int OPCODE_UNSUBSCRIBE_CLIENT = 0x21;
    private static final int OPCODE_SUCCESSFUL_UNSUBSCRIBE_ACK = 0xA0;
    private static final int OPCODE_FAILED_UNSUBSCRIBE_ACK = 0xA1;
    private static final int OPCODE_POST_CLIENT = 0x30;
    private static final int OPCODE_POST_ACK = 0xB0;
    private static final int OPCODE_FORWARD_SERVER = 0xB1;
    private static final int OPCODE_FORWARD_ACK = 0x31;
    private static final int OPCODE_RETRIEVE_CLIENT = 0x40;
    private static final int OPCODE_RETRIEVE_ACK = 0xC0;
    private static final int OPCODE_END_OF_RETRIEVE_ACK = 0xC1;
    private static final int OPCODE_LOGOUT_CLIENT = 0x1F;
    private static final int OPCODE_LOGOUT_ACK = 0x8F;


    public static void main(String[] args) throws IOException {
        System.out.println("Hello this is the UDP Server!");

        load();

        // create a socket to listen on port
        DatagramSocket dsSend = new DatagramSocket();
        DatagramSocket dsRecieve = new DatagramSocket(portReceive);



        byte[] receive = new byte[65535];

        while (true) {
            receivePacket(dsRecieve,dsSend, receive);
            // Clear the buffer after every message.
            receive = new byte[65535];

//            sendPacket("Test", dsRecieve);
        }

    }

    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a) {
        if (a == null)
            return null;

        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    public static void sendPacket(int OPCODE, DatagramSocket ds) throws IOException {


        String msg = "";
        if (OPCODE == OPCODE_SESSION_RESET) {

        } else if (OPCODE == OPCODE_MUST_LOGIN_FIRST) {

        } else if (OPCODE == OPCODE_LOGIN_CLIENT) {

        }


        // create the socket object for
        // carrying the data.
//        DatagramSocket ds = new DatagramSocket();
        byte buf[] = null;

        // convert the String input into the byte array.
        buf = msg.getBytes();

        // create the datagramPacket for sending
        // the data.
        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), portSend);

        // invoke the send call to actually send
        // the data.
        ds.send(DpSend);


    }

    public static void receivePacket(DatagramSocket dsReceive, DatagramSocket dsSend, byte[] receive) throws IOException {

        DatagramPacket DpReceive = null;
        // create a DatgramPacket to receive the data.
        DpReceive = new DatagramPacket(receive, receive.length);

        // revieve the data in byte buffer.
        dsReceive.receive(DpReceive);
        StringBuilder clientMsg = data(receive);

        String username = "";
        String pass = "";

        if (clientMsg.toString().contains("#") && clientMsg.toString().contains("&")) {
            username = clientMsg.substring(clientMsg.indexOf("#") + 1, clientMsg.indexOf("&"));
            pass = clientMsg.substring(clientMsg.indexOf("&") + 1);
        }


        if (clientMsg.toString().equals("clr")) {
            System.out.println("Clearing userList...");
            userList.clear();
            save();
        } else if (clientMsg.toString().equals("disp")) {
            dispUserList();
        } else if (clientMsg.toString().contains("logout#")) {
            sendPacket(OPCODE_LOGOUT_ACK, dsSend);
            System.out.println("logging out " + username);
        } else if (clientMsg.toString().contains("login#")) {
            login(clientMsg, dsSend);
        } else if (clientMsg.toString().contains("addusr#")) {
//            String username = clientMsg.substring(clientMsg.indexOf("#") + 1, clientMsg.indexOf("&"));
//            String pass = clientMsg.substring(clientMsg.indexOf("&") + 1);
            UserData usr = new UserData(username, pass);
            userList.add(usr);
            save();
            System.out.println("\n" + username + " has been added!\n");
        } else {
            System.out.println("Client msg: " + clientMsg);
        }

    }

    public static boolean login(StringBuilder msg, DatagramSocket ds) throws IOException {
        boolean loginSuccesful = false;
        String username = "";
        String pass = "";
        try {
            username = msg.substring(msg.indexOf("#") + 1, msg.indexOf("&"));
//            System.out.println("Given usr: " + userName);
            pass = msg.substring(msg.indexOf("&") + 1);
//            System.out.println("Given pass: " + pass);

        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("String index is out of bounds... :(");
            System.out.println("Please enter a valid username! thx\n\n");
//            e.printStackTrace();
        }


        // This loop iterates through the userList
        load();
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUsername().equals(username) && userList.get(i).getPassword().equals(pass)) {
                sendPacket(OPCODE_SUCCESSFUL_LOGIN_ACK, ds);
                loginSuccesful = true;
                userList.get(i).setLoggedin(true);
                save();
                break;
            }
        }

        if (!loginSuccesful) {
            sendPacket(OPCODE_FAILED_LOGIN_ACK, ds);
        }

        return  loginSuccesful;
    }

    public static void load() {
        try {
            FileInputStream fileIn = new FileInputStream(new File("savedUsers.txt"));
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            // Read objects
            userList = (LinkedList<UserData>) objectIn.readObject();

            // System.out.println(loadedList.toString());

            fileIn.close();
            objectIn.close();

//         return userList;

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void save() {

        try {
            FileOutputStream listFileOut = new FileOutputStream("savedUsers.txt");
            ObjectOutputStream listObjectOut = new ObjectOutputStream(listFileOut);

            listObjectOut.writeObject(userList);
            listObjectOut.close();

            System.out.println("\nuserList saved to savedUsers.txt");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void dispUserList() {
        load();
        System.out.println("\nDisplaying the list of all users!");
        if (userList.isEmpty()) {
            System.out.println("User list is empty!");
        }
        for (UserData userData : userList) {
            System.out.println("-----------------");
            System.out.println("User: " + userData.getUsername());
            System.out.println("Pass: " + userData.getPassword());
            System.out.println("Token: " + userData.getToken());
            System.out.println("Logged in? " + userData.isLoggedin());
        }

    }

    public static int getUserFromToken(int token) {
        int userIndex = -1;

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getToken() == token) {
                userIndex = i;
            }
        }

        return userIndex;
    }


}
