package com.company;

// Implementation using DatagramSocket
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;

public class UDP_Server {

    static LinkedList<UserData> userList = new LinkedList<UserData>();

    public static void main(String[] args) throws IOException {
        // write your code here
        System.out.println("Hello this is the UDP Server!");

        // Step 1 : Create a socket to listen at port 1234
        DatagramSocket ds = new DatagramSocket(1234);
        byte[] receive = new byte[65535];

        DatagramPacket DpReceive = null;


        while (true)
        {

            // Step 2 : create a DatgramPacket to receive the data.
            DpReceive = new DatagramPacket(receive, receive.length);

            // Step 3 : revieve the data in byte buffer.
            ds.receive(DpReceive);
            StringBuilder clientMsg = data(receive);

            if (clientMsg.toString().equals("clr")) {
                userList.clear();
                UDP_Client.save();
            } else if (clientMsg.toString().equals("disp")) {
                UDP_Client.dispUserList();
            }


//            System.out.println("Client:- " + clientMsg);



            // Exit the server if the client sends "bye"
            if (clientMsg.toString().contains("logout#"))
            {
                System.out.println("Client sent bye.....EXITING");
                userList.remove(0);
                System.out.println(userList.get(0).getUsername() + " removed");
                break;
            } else if (clientMsg.toString().contains("login#")) {
                login(clientMsg);
            }

            // Clear the buffer after every message.
            receive = new byte[65535];
        }

    }

    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a)
    {
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

    public static boolean login(StringBuilder msg)
    {
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
                UserData usr = new UserData(username, pass);
                userList.add(usr);
                loginSuccesful = true;
                UDP_Client.save();
                System.out.println("\n" + username + " has been logged in!\n");
                break;userList
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

            // return loadedList;

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return;
    }

}
