package com.company;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class UDP_Server {

    static LinkedList<UserData> userList = new LinkedList<UserData>();

    private static final String ip = "127.0.0.1";
    private static final int port = 32000;

    private static final int STATE_OFFLINE = 0;
    private static final int STATE_LOGIN_SENT = 1;
    private static final int STATE_ONLINE = 2;

    private static final int EVENT_USER_LOGIN = 0;
    private static final int EVENT_USER_POST = 1;
    private static final int EVENT_USER_INVALID = 79;
    private static final int EVENT_NET_LOGIN_SUCCESSFUL = 80;
    private static final int EVENT_NET_POST_ACK = 81;
    private static final int EVENT_NET_INVALID = 255;

    private static final int OPCODE_RESET = 0x00;
    private static final int OPCODE_MUST_LOGIN_FIRST_ERROR = 0xF0;
    private static final int OPCODE_LOGIN = 0x10;


    public static void main(String[] args) throws IOException {
        System.out.println("Hello this is the UDP Server!");

        load();

        // create a socket to listen at port 32000
        DatagramSocket ds = new DatagramSocket(32000);
        byte[] receive = new byte[65535];

        while (true) {
            receivePacket(ds, receive);
            // Clear the buffer after every message.
            receive = new byte[65535];
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

    public static void sendPacket(String msg) throws IOException {

        // create the socket object for
        // carrying the data.
        DatagramSocket ds = new DatagramSocket();
        byte buf[] = null;

        // convert the String input into the byte array.
        buf = msg.getBytes();

        // create the datagramPacket for sending
        // the data.
        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);

        // invoke the send call to actually send
        // the data.
        ds.send(DpSend);
    }

    public static void receivePacket(DatagramSocket ds, byte[] receive) throws IOException {

        DatagramPacket DpReceive = null;
        // create a DatgramPacket to receive the data.
        DpReceive = new DatagramPacket(receive, receive.length);

        // revieve the data in byte buffer.
        ds.receive(DpReceive);
        StringBuilder clientMsg = data(receive);

        System.out.println("Client msg: " + clientMsg);
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
            System.out.println("logging out " + username);
        } else if (clientMsg.toString().contains("login#")) {
            if (login(clientMsg)) {
                System.out.println("Logged in!");
            } else {
                System.out.println("Log in failed...");
            }
        } else if (clientMsg.toString().contains("addusr#")) {
//            String username = clientMsg.substring(clientMsg.indexOf("#") + 1, clientMsg.indexOf("&"));
//            String pass = clientMsg.substring(clientMsg.indexOf("&") + 1);
            UserData usr = new UserData(username, pass);
            userList.add(usr);
            save();
            System.out.println("\n" + username + " has been added!\n");
        }



    }

    public static boolean login(StringBuilder msg) {
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
            if(userList.get(i).getUsername().equals(username) &&  userList.get(i).getPassword().equals(pass))
            {
                System.out.println("Log in Success!");
                loginSuccesful = true;
                break;
            }
//            System.out.println("Stored usr: " + userList.get(i).getUsername());
//            System.out.println("Stored pass: " + userList.get(i).getPassword());
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

        return;
    }

    public static void save() {

        try {
            FileOutputStream listFileOut = new FileOutputStream("savedUsers.txt");
            ObjectOutputStream listObjectOut = new ObjectOutputStream(listFileOut);

            listObjectOut.writeObject(userList);
            listObjectOut.close();

            System.out.println("userList was written to savedUsers.txt");

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
        }

    }


}
