package com.company;

// Java program to illustrate Client side
// Implementation using DatagramSocket
import javax.jws.soap.SOAPBinding;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.util.LinkedList;
import java.util.Scanner;

/*
    Each user needs a unique ID
 */

public class UDP_Client implements Serializable {

    private static final long serialVersionUID = 4088384793623750965L;


    static LinkedList<UserData> userList = new LinkedList<UserData>();

    public static void main(String[] args) throws IOException {
        // write your code here
        System.out.println("Hello this is the UDP Client!");

        Scanner sc = new Scanner(System.in);

        // Step 1:Create the socket object for
        // carrying the data.
        DatagramSocket ds = new DatagramSocket();

        InetAddress ip = InetAddress.getLocalHost();
        byte buf[] = null;

        // loop while user not enters "bye"
        while (true)
        {
            String inp = sc.nextLine();

            // convert the String input into the byte array.
            buf = inp.getBytes();

            // Step 2 : Create the datagramPacket for sending
            // the data.
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 1234);

            // Step 3 : invoke the send call to actually send
            // the data.
            ds.send(DpSend);

            // break the loop if user logs out
            if (inp.equals("logout#")) {
                String username = inp.substring(inp.indexOf("#") + 1, inp.indexOf("&"));
                userList.removeFirstOccurrence(username); // Need to test. If this does not work iterate through w/ for loop
            } else if(inp.contains("login#"))
            {
                save();
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
        UDP_Server.load();
        System.out.println("dispUserList!");
        for (int i = 0; i < userList.size(); i++) {
            System.out.println("User: " + userList.get(i).getUsername());
            System.out.println("Pass: " + userList.get(i).getPassword());
            System.out.println();
        }

    }

}
