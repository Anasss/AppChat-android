package edu.UTEP.android;
// $Id: ChatServer.java,v 1.3 2012/02/19 06:12:34 cheon Exp $

import java.io.*;
import java.net.*; 
import java.util.*;


public class ChatServer {

    private static final String USAGE = "Usage: java ChatServer";

    /** Default port number on which this server to be run. */
    private static final int PORT_NUMBER = 8008;

    /** List of print writers associated with current clients,
     * one for each. */
    private List<PrintWriter> clients;

    /** Creates a new server. */
    public ChatServer() {
        clients = new LinkedList<PrintWriter>();
    }

    /** Starts the server. */
    public void start() {
        System.out.println("testChat server started on port "
                           + PORT_NUMBER + "!"); 
        try {
            ServerSocket s = new ServerSocket(PORT_NUMBER); 
            for (;;) {
                Socket incoming = s.accept(); 
                new ClientHandler(incoming).start(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("testChat server stopped."); 
    }

    /** Adds a new client identified by the given print writer. */
    private void addClient(PrintWriter out) {
        synchronized(clients) {
            clients.add(out);
        }
    }

    /** Adds the client with given print writer. */
    private void removeClient(PrintWriter out) {
        synchronized(clients) {
            clients.remove(out);
        }
    }

    /** Broadcasts the given text to all clients. */
    private void broadcast(String msg) {
        for (PrintWriter out: clients) {
            out.println(msg);
            out.flush();
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println(USAGE);
            System.exit(-1);
        }
        new ChatServer().start();
    }

    /** A thread to serve a client. This class receive messages from a
     * client and broadcasts them to all clients including the message
     * sender. */
    private class ClientHandler extends Thread {

        /** Socket to read client messages. */
        private Socket incoming; 

        /** Creates a hander to serve the client on the given socket. */
        public ClientHandler(Socket incoming) {
            this.incoming = incoming;
        }

        /** Starts receiving and broadcasting messages. */
        public void run() {
            PrintWriter out = null;
            try {
                out = new PrintWriter(
                        new OutputStreamWriter(incoming.getOutputStream()));
                
                // inform the server of this new client
                ChatServer.this.addClient(out);

                out.print("test de chat ");
                out.println("tape BYE pour exit."); 
                out.flush();

                BufferedReader in 
                    = new BufferedReader(
                        new InputStreamReader(incoming.getInputStream())); 
                for (;;) {
                    String msg = in.readLine(); 
                    if (msg == null) {
                        break; 
                    } else {
                        if (msg.trim().equals("BYE")) 
                            break; 
                        System.out.println("Received: " + msg);
                        // broadcast the receive message
                        ChatServer.this.broadcast(msg);
                    }
                }
                incoming.close(); 
                ChatServer.this.removeClient(out);
            } catch (Exception e) {
                if (out != null) {
                    ChatServer.this.removeClient(out);
                }
                e.printStackTrace(); 
            }
        }
    }
}
