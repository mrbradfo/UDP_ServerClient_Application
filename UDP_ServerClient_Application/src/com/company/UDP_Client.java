package com.company;

// Java program to illustrate Client side
// Implementation using DatagramSocket
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Math.abs;

/*
    UDP_Client
 */

public class UDP_Client implements Serializable {

    private static final long serialVersionUID = 4088384793623750965L;

    static LinkedList<UserData> userList = new LinkedList<UserData>();

    private static final String ip = "127.0.0.1";
    private static final int portSend = 32000;
//    private static final int portReceive = 50000;

    // The header = OPCODE + payload + client index + client port
    // header + inp + "*" + userIndex + "$" + portReceive;
    private static String header = "";

    private static final int STATE_OFFLINE = 0;
    private static final int STATE_LOGIN_SENT = 1;
    private static final int STATE_ONLINE = 2;

    private static final int EVENT_USER_LOGIN = 0;// User types login. Client sends login message
    private static final int EVENT_LOGIN_SUCCESSFUL = 1;
    private static final int EVENT_LOGIN_FAILED = 2;
    private static final int EVENT_USER_LOGOUT = 3; // User types logout. Client sends logoff message
    private static final int EVENT_USER_POST = 4;
    private static final int EVENT_USER_SUBSCRIBE = 5;
    private static final int EVENT_USER_INVALID = 79;
    private static final int EVENT_POST_ACK = 81;
    private static final int EVENT_INVALID = 255;

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
    private static final int OPCODE_POST_FAILED = 0xB2;
    private static final int OPCODE_POST_ACK = 0xB0;
    private static final int OPCODE_FORWARD_SERVER = 0xB1;
    private static final int OPCODE_FORWARD_ACK = 0x31;
    private static final int OPCODE_RETRIEVE_CLIENT = 0x40;
    private static final int OPCODE_RETRIEVE_ACK = 0xC0;
    private static final int OPCODE_END_OF_RETRIEVE_ACK = 0xC1;
    private static final int OPCODE_LOGOUT_CLIENT = 0x1F;
    private static final int OPCODE_LOGOUT_ACK = 0x8F;

    private static int state = STATE_OFFLINE;
    private static int userIndex = -1;

    public static void main(String[] args) throws IOException {

        System.out.println("Hello this is the UDP Client!");
        loadClientList(); // Load the stored list of Users
        Scanner sc = new Scanner(System.in);

        Random rand = new Random();
        int portReceive = abs(rand.nextInt(61000 - 32768 + 1) + 32768); // Generates a rand # between 32768 and 61000

        // create the socket object for
        // carrying the data.
        DatagramSocket dsSend = new DatagramSocket();
        DatagramSocket dsReceive = new DatagramSocket(portReceive);
//        dsReceive.setSoTimeout(200);

        int event = -1;

        while (true)
        {
            // String to get input from the user
            String inp = "";



            if (state == STATE_OFFLINE) {
                System.out.print("\nPlease login: ");
            } else if (state == STATE_ONLINE) {
                if (userIndex != -1) {
                    System.out.print(userList.get(userIndex).getUsername() + "~$ ");
                }
            }


            inp = sc.nextLine();

            // if user logs in
            if(inp.contains("login#")) {
                event = EVENT_USER_LOGIN;
                String username = inp.substring(inp.indexOf("#") + 1, inp.indexOf("&"));
                userIndex = getUserIndex(username);
                } else if (inp.contains("logout#")) {
                    event = EVENT_USER_LOGOUT;
/*                    String username = inp.substring(inp.indexOf("#") + 1, inp.indexOf("&"));
                    int rm = getUserIndex(username);
                    if (rm == -1) {
                        System.out.println(username + " was not found and was not logged out");
                    } else {
                        userList.remove(rm);
                        saveClientList();
                        byte[] receive = new byte[65535];
                        receivePacket(dsReceive, receive);
    //                    System.out.println(username + " has been successfully logged out");
                        System.out.println();
                    }*/
                }  else if (inp.contains("addusr#")) {
                    String username = inp.substring(inp.indexOf("#") + 1, inp.indexOf("&"));
                    String password = inp.substring(inp.indexOf("&") + 1);
                    UserData usr = new UserData(username, password);
                    userList.add(usr);
                    saveClientList();
                    System.out.println(username + " has been added");
                } else if (inp.contains("post#")) {
                    event = EVENT_USER_POST;
                } else if (inp.contains("subscribe#")) {
                    event = EVENT_USER_SUBSCRIBE;
                } else if (inp.equals("disp")) {
                    dispUserList();
                }  else if (inp.equals("clr")) {
                    System.out.println("Clearing userList...");
                    userList.clear();
                    saveClientList();
                } else {
    //                sendPacket(inp, dsSend);
                    System.out.println("Command invalid".toUpperCase());
                }

                if (event == EVENT_USER_LOGIN) { // SEND LOGIN MESSAGE TO SERVER
                    header = OPCODE_LOGIN_CLIENT + "";
                } else if (event == EVENT_USER_LOGOUT) { // SEND LOGOUT MESSAGE TO SERVER
                    header = OPCODE_LOGOUT_CLIENT + "";
                } else if (event == EVENT_USER_POST) {
                    header = OPCODE_POST_CLIENT + "";
                } else if (event == EVENT_USER_SUBSCRIBE) {
                    header = OPCODE_SUBSCRIBE_CLIENT + "";
                }

            if (event != -1) {
                header = header + inp + "*" + userIndex + "$" + portReceive; // SEND HEADER
                sendPacket(header, dsSend);

                byte[] receive = new byte[65535];                            // RECEIVE RESPONSE
                receivePacket(dsReceive,receive);
            }

//            } // END OF STATE CONDITIONAL

            event = -1; // RESET EVENT AFTER PROCESSING
            header = "";// RESET HEADER AFTER PROCESSING
//            byte[] receive = new byte[65535];                            // RECEIVE RESPONSE
//            receivePacket(dsReceive,receive);

        } // END OF WHILE

    } // END OF MAIN

    public static void sendPacket(String msg, DatagramSocket ds) throws IOException {
        byte[] buf;
        buf = msg.getBytes();

        // create the datagramPacket for sending
        // the data.
        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), portSend);

        // invoke the send call to actually send
        // the data.
        ds.send(DpSend);
    }

    public static void receivePacket(DatagramSocket ds, byte[] receive) throws IOException {

        DatagramPacket DpReceive = null;
        // create a DatgramPacket to receive the data.
        DpReceive = new DatagramPacket(receive, receive.length);

        // revieve the data in byte buffer.
//        ds.setSoTimeout(2000);
//        try {
            ds.receive(DpReceive);
//        } catch (java.net.SocketTimeoutException e) {
//            System.out.println("-receive timed out-");
//        }
        StringBuilder serverMsg = UDP_Server.data(receive);
        String response = serverMsg.toString();


        System.out.println("msg: " + serverMsg);
        if (response.equals("login_ack#successful")) {
            state = STATE_ONLINE;
        } else if (response.equals("logout_ack#successful")) {
            state = STATE_OFFLINE;
            userIndex = -1;
        } else if (response.contains("post_ack#successful")) {
//            receivePacket(ds, receive);
//            for (int i = 0; i < 3; i++) {
//                receive = new byte[65535];
//                receivePacket(ds, receive);
//            }
//            int num = Integer.parseInt(response.substring(response.indexOf("_")+1));
        } else if (response.contains("subscribe_ack#successful")) {

        }




//        else if (response.equals("post_ack#faled")) {
//        }
    }

    public static void loadClientList() {
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

    public static void saveClientList() {

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
        loadClientList();
        System.out.println("\n\ndispUserList!");
        for (UserData userData : userList) {
            System.out.println("-----------------");
            System.out.println("User: " + userData.getUsername());
            System.out.println("Pass: " + userData.getPassword());
            System.out.println("Port: " + userData.getToken());
            System.out.println("Logged in? " + userData.isLoggedin());
//            System.out.println("State: " + userData.getState());
            if (!userData.getSubscriberList().isEmpty()) {
                System.out.println("Subscriber: " + userData.getSubscriberList().get(0));
            } else {
                System.out.println("No subscribers");
            }
        }

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

}
