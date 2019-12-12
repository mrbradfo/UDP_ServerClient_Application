package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class UDP_Server {

    private static LinkedList<UserData> userList = new LinkedList<UserData>();

    private static final String ip = "127.0.0.1";
    private static final int portReceive = 32000;

    private int messageID = 0;  // The ​ message ID is an ID used to identify an individual message.
                                // It is monotonically increasing

    private static final int OPCODE_SESSION_RESET = 0x00;
    private static final int OPCODE_MUST_LOGIN_FIRST = 0xF0;
    private static final int OPCODE_LOGIN_CLIENT = 0x10;
    private static final int OPCODE_SUCCESSFUL_LOGIN_ACK = 0x80;
    private static final int OPCODE_FAILED_LOGIN_ACK = 0x81;
    private static final int OPCODE_FAILED_LOGOUT_ACK = 0x82;
    private static final int OPCODE_SUBSCRIBE_CLIENT = 0x20;
    private static final int OPCODE_SUCCESSFUL_SUBSCRIBE_ACK = 0x90;
    private static final int OPCODE_FAILED_SUBSCRIBE_ACK = 0x91;
    private static final int OPCODE_UNSUBSCRIBE_CLIENT = 0x21;
    private static final int OPCODE_SUCCESSFUL_UNSUBSCRIBE_ACK = 0xA0;
    private static final int OPCODE_FAILED_UNSUBSCRIBE_ACK = 0xA1;
    private static final int OPCODE_POST_CLIENT = 0x30;
    private static final int OPCODE_POST_ACK = 0xB0;
    private static final int OPCODE_POST_FAILED = 0xB2;
    private static final int OPCODE_FORWARD_SERVER = 0xB1;
    private static final int OPCODE_FORWARD_ACK = 0x31;
    private static final int OPCODE_RETRIEVE_CLIENT = 0x40;
    private static final int OPCODE_RETRIEVE_ACK = 0xC0;
    private static final int OPCODE_END_OF_RETRIEVE_ACK = 0xC1;
    private static final int OPCODE_LOGOUT_CLIENT = 0x1F;
    private static final int OPCODE_LOGOUT_ACK = 0x8F;


    public static void main(String[] args) throws IOException {
        System.out.println("Hello this is the UDP Server!");

        loadClientList();

        // create a socket to listen on port
        DatagramSocket dsSend = new DatagramSocket();
        DatagramSocket dsReceive = new DatagramSocket(portReceive);

        byte[] receive = new byte[65535];

        while (true) {
            receivePacket(dsReceive,dsSend, receive);
            // Clear the buffer after every message.
            receive = new byte[65535];

        }

    } // END OF MAIN

    // A utility method to convert the byte array
    // data into a string representation.
    static StringBuilder data(byte[] a) {
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

    private static void sendPacket(int OPCODE, String message, DatagramSocket ds, int port) throws IOException {

        String msg = "";
        if (OPCODE == OPCODE_SUCCESSFUL_LOGIN_ACK) {
            msg = "login_ack#successful";
        } else if (OPCODE == OPCODE_MUST_LOGIN_FIRST) {
            msg = "error#must_login_first";
        } else if (OPCODE == OPCODE_FAILED_LOGIN_ACK) {
            msg = "login_ack#failed";
        } else if (OPCODE == OPCODE_LOGOUT_ACK) {
            msg = "logout_ack#successful";
        } else if (OPCODE == OPCODE_FAILED_LOGOUT_ACK) {
            msg = "logout_ack#failed";
        } else if (OPCODE == OPCODE_POST_ACK) {
            msg = "post_ack#successful";
        } else if (OPCODE == OPCODE_SUCCESSFUL_SUBSCRIBE_ACK) {
            msg = "subscribe_ack#successful";
        } else if (OPCODE == OPCODE_FAILED_SUBSCRIBE_ACK) {
            msg = "subscribe_ack#failed";
        } else if (OPCODE == OPCODE_SUCCESSFUL_UNSUBSCRIBE_ACK) {
            msg = "unsubscribe_ack#successful";
        } else if (OPCODE == OPCODE_FAILED_UNSUBSCRIBE_ACK) {
            msg = "unsubscribe_ack#failed";
        } else if (OPCODE == OPCODE_FORWARD_SERVER) {
            msg = message;
        }

//        System.out.println(msg);

        // create the socket object for
        // carrying the data.
        // DatagramSocket ds = new DatagramSocket();
        byte[] buf;

        // convert the String input into the byte array.
        buf = msg.getBytes();

        // create the datagramPacket for sending
        // the data.

        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);

        // invoke the send call to actually send
        // the data.
        ds.send(DpSend);

    }

    private static void receivePacket(DatagramSocket dsReceive, DatagramSocket dsSend, byte[] receive) throws IOException {

        DatagramPacket DpReceive = null;
        // create a DatgramPacket to receive the data.
        DpReceive = new DatagramPacket(receive, receive.length);

        // revieve the data in byte buffer.
        dsReceive.receive(DpReceive);
        StringBuilder clientMsg = data(receive);
        if(clientMsg.equals("")) {
            return;
        }

        String username = "";
        String pass = "";

        if (clientMsg.toString().contains("#") && clientMsg.toString().contains("&")) {
            username = clientMsg.substring(clientMsg.indexOf("#") + 1, clientMsg.indexOf("&"));
            pass = clientMsg.substring(clientMsg.indexOf("&") + 1, clientMsg.indexOf("*"));
        }

        int op = -1;
        int clientPort = 0;
        try {
            String opcode = clientMsg.substring(0, 2);
//            System.out.println("received opcode: " + opcode);
            op = Integer.parseInt(opcode);
            String cPort = clientMsg.substring(clientMsg.indexOf("$") + 1);
            clientPort = Integer.parseInt(cPort);

        } catch (NumberFormatException e) {
            System.out.println("\njava.lang.NumberFormatException".toUpperCase());
        } catch (StringIndexOutOfBoundsException o) {
            System.out.println("\nString index out of range\n");
        }

        int userIndex = -1;
        try {
            userIndex = Integer.parseInt(clientMsg.substring(clientMsg.indexOf("*") + 1, clientMsg.indexOf("$"))); // get the user index
        } catch (java.lang.NumberFormatException e) {
            System.out.println("\njava.lang.NumberFormatException");
        } catch (StringIndexOutOfBoundsException ee) {
            System.out.println("StringIndexOutOfBoundsException".toUpperCase());
        }

        if (userIndex == -1) {
//                System.out.println("Failed to locate user".toUpperCase());
            sendPacket(OPCODE_MUST_LOGIN_FIRST, "",  dsSend, clientPort);
            return;
        }

        if (op == OPCODE_LOGIN_CLIENT) {
            if (login(username, pass)) {
                System.out.println(username + " logged in");
                sendPacket(OPCODE_SUCCESSFUL_LOGIN_ACK, "", dsSend, clientPort);
            } else {
                sendPacket(OPCODE_FAILED_LOGIN_ACK, "", dsSend, clientPort);
            }
        } else if (op == OPCODE_LOGOUT_CLIENT) {
            if (logout(username, pass)) {
                System.out.println(username + " logged out");
                sendPacket(OPCODE_LOGOUT_ACK, "", dsSend, clientPort);
            } else {
                sendPacket(OPCODE_FAILED_LOGOUT_ACK, "", dsSend, clientPort);
            }
        } else if (op == OPCODE_POST_CLIENT) {
//            int token = clientMsg.substring(clientMsg.indexOf("#") + 1, clientMsg.indexOf("&"))
//            getUserFromToken()

                if (userList.get(userIndex).isLoggedin()) {
                    String msg = clientMsg.substring(clientMsg.indexOf("#") + 1, clientMsg.indexOf("*"));
//                    System.out.println("size of subscriber list: " + numOfSubScribers);
                    System.out.println(userList.get(userIndex).getUsername() + ": " + msg);
                    sendPacket(OPCODE_POST_ACK, "", dsSend, clientPort);

                    ArrayList<String> subList = userList.get(userIndex).getSubscriberList();
                    int numOfSubScribers = subList.size();
                    for (int i = 0; i < numOfSubScribers; i++) {
                        username = subList.get(i);
                        sendPacket(OPCODE_FORWARD_SERVER, msg, dsSend, userList.get(getUserIndex(username)).getToken()); //Forward message to subscribers
                    }

                } else {
                    sendPacket(OPCODE_POST_FAILED, "", dsSend, clientPort);
                }

        } else if (op == OPCODE_SUBSCRIBE_CLIENT) {
            String clientName = clientMsg.substring(clientMsg.indexOf("#") + 1, clientMsg.indexOf("*")); // This is the client that is being subscribed to
            // need to get the current user that is logged in and add them to the subscriber list.
           String user = userList.get(userIndex).getUsername();
           System.out.println(user + " is subscribing to " + clientName);
           userList.get(getUserIndex(clientName)).addSubscriber(user);
           saveClientList();
           sendPacket(OPCODE_SUCCESSFUL_SUBSCRIBE_ACK, "", dsSend, clientPort);

        } else if (op == OPCODE_UNSUBSCRIBE_CLIENT) {

        }
    }

    private static boolean logout(String user, String pass) {
        boolean logoutSuccessful = false;

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUsername().equals(user) && userList.get(i).getPassword().equals(pass)) {
                logoutSuccessful = true;
                userList.get(i).setLoggedin(false);
                userList.get(i).setState(0);
                saveClientList();
                break;
            }
        }

        return logoutSuccessful;
    }

    private static boolean login(String user, String pass) {
        boolean loginSuccesful = false;

        // This loop iterates through the userList
        loadClientList();
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUsername().equals(user) && userList.get(i).getPassword().equals(pass)) {
                loginSuccesful = true;
                userList.get(i).setLoggedin(true);
                userList.get(i).setState(1);
                saveClientList();
                break;
            }
        }

        return  loginSuccesful;
    }

    private static void loadClientList() {
        try {
            FileInputStream fileIn = new FileInputStream(new File("savedUsers.txt"));
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            // Read objects
            userList = (LinkedList<UserData>) objectIn.readObject();

            // System.out.println(loadedList.toString());

            fileIn.close();
            objectIn.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void saveClientList() {

        try {
            FileOutputStream listFileOut = new FileOutputStream("savedUsers.txt");
            ObjectOutputStream listObjectOut = new ObjectOutputStream(listFileOut);

            listObjectOut.writeObject(userList);
            listObjectOut.close();

//            System.out.println("\nuserList saved to savedUsers.txt");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void dispUserList() {
        loadClientList();
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
            System.out.println("State: " + userData.getState());
        }

    }

    public static int getUserIndexFromToken(int token) {
        int userIndex = -1;

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getToken() == token) {
                userIndex = i;
            }
        }

        return userIndex;
    }

    public static int getUserIndex(String username) {
        int userIndex = -1;

        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUsername().equals(username)) {
                userIndex = i;
            }
        }

        return userIndex;
    }

} // END OF UDP_Server
