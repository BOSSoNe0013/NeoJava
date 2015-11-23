package com.b1project.udooneo.net;

/**
 *  Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
 *
 *  This file is part of NeoJava examples for UDOO
 *
 *  NeoJava examples for UDOO is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This libraries are distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import com.b1project.udooneo.listeners.NeoJavaProtocolListener;

import java.net.*;
import java.io.*;

public class NeoJavaServer {
    private static final int port = 45045;
    private ServerSocket serverSocket;
    private NeoJavaProtocolListener neoJavaProtocolListener;

    public NeoJavaServer(NeoJavaProtocolListener listener){
        super();
        this.neoJavaProtocolListener = listener;
    }

    public static NeoJavaServer getInstance(NeoJavaProtocolListener listener){
        return new NeoJavaServer(listener);
    }

    public void startServer(){
        System.out.println("Starting NeoJavaServer");
        try{
            serverSocket = new ServerSocket(port);
            System.out.printf("\nListening on port %d\n#:", port);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                if(clientSocket != null) {
                    PrintWriter out =
                            new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));

                    (new Thread(new ServerThread(clientSocket, in, out))).start();
                }

                /*String inputLine, outputLine;

                // Initiate conversation with client
                NeoJavaProtocol njp = new NeoJavaProtocol(neoJavaProtocolListener);
                out.println("Enter a command:");
                while ((inputLine = in.readLine()) != null) {
                    outputLine = njp.processInput(inputLine);
                    if (outputLine != null) {
                        out.println(outputLine);
                    }
                }*/
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    class ServerThread implements Runnable{
        PrintWriter out;
        BufferedReader in;
        Socket clientSocket;

        public ServerThread(Socket clientSocket, BufferedReader in, PrintWriter out){
            this.in = in;
            this.out = out;
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            String inputLine, outputLine;
            // Initiate conversation with client
            NeoJavaProtocol njp = new NeoJavaProtocol(clientSocket,neoJavaProtocolListener);
            out.println("Enter a command:");
            try{
                while ((inputLine = in.readLine()) != null) {
                    outputLine = njp.processInput(inputLine);
                    if (outputLine != null) {
                        out.println(outputLine);
                    }
                }
            } catch (IOException e) {
                System.out.println("Exception caught when  listening for a connection");
                System.out.println(e.getMessage());
            }
        }
    }

    public void stopServer(){
        try{
            /*if(clientSocket != null){
                clientSocket.close();
            }*/
            if(serverSocket != null){
                serverSocket.close();
            }
            System.out.println("\nNeoJavaServer stopped");
        } catch (IOException e) {
            System.out.println("Exception caught when trying to close socket");
            System.out.println(e.getMessage());
        }
    }
}
