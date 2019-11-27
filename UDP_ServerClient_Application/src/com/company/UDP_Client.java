package com.company;

// Java program to illustrate Client side
// Implementation using DatagramSocket
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Scanner;

/*
    Each user needs a unique ID
 */

public class UDP_Client implements Serializable {

    private static final long serialVersionUID = 4088384793623750965L;

    static LinkedList<UserData> userList = new LinkedList<UserData>();

    private static final String ip = "127.0.0.1";
    private static final int portSend = 32000;
    private static final int portReceive = 50000;
    String header = "";



    public static void main(String[] args) throws IOException {

        System.out.println("Hello this is the UDP Client!");
        load(); // Load the stored list of Users
        Scanner sc = new Scanner(System.in);

        // create the socket object for
        // carrying the data.
        DatagramSocket dsSend = new DatagramSocket();
        DatagramSocket dsRecieve = new DatagramSocket(portReceive);


        while (true)
        {
            // get input from the user
            String inp = sc.nextLine();

            // convert the String input into the byte array.
            sendPacket(inp, dsSend);

            // if user logs out
            if (inp.contains("logout#")) {
                String username = inp.substring(inp.indexOf("#") + 1, inp.indexOf("&"));
                int rm = getUserIndex(username);
                if (rm == -1) {
                    System.out.println(username + " was not found and was not logged out");
                } else {
                    userList.remove(rm);
                    save();
                    byte[] receive = new byte[65535];
                    receivePacket(dsRecieve, receive);
//                    System.out.println(username + " has been successfully logged out");
                    System.out.println();
                }
            } else if(inp.contains("login#")) {
                // recieve buffer
                byte[] receive = new byte[65535];
                receivePacket(dsRecieve, receive);
//                save();
            } else if (inp.contains("addusr#")) {
                String username = inp.substring(inp.indexOf("#") + 1, inp.indexOf("&"));
                String password = inp.substring(inp.indexOf("&") + 1);
                UserData usr = new UserData(username, password);
                userList.add(usr);
                save();
                System.out.println(username + " has been added");
            }
//            dispUserList();



        }

    }

    public static void sendPacket(String msg, DatagramSocket ds) throws IOException {
        byte buf[];
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
        ds.receive(DpReceive);
        StringBuilder serverMsg = UDP_Server.data(receive);

        System.out.println("Server msg: " + serverMsg);
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

            System.out.println("\nuserList saved to savedUsers.txt");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void dispUserList() {
        load();
        System.out.println("dispUserList!");
        for (UserData userData : userList) {
            System.out.println("User: " + userData.getUsername());
            System.out.println("Pass: " + userData.getPassword());
            System.out.println();
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
