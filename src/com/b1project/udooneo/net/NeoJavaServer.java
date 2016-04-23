package com.b1project.udooneo.net;

/**
 *  Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
 *
 *  This file is part of NeoJava Tools for UDOO Neo
 *
 *  NeoJava Tools for UDOO Neo is free software: you can redistribute it and/or modify
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NeoJavaServer {
    private static final int port = 45045;
    private ServerSocket serverSocket;
    private List<Socket> clientSockets = new ArrayList<>();
    private NeoJavaProtocolListener neoJavaProtocolListener;
    private List<PrintWriter> outPrintWriters = new ArrayList<>();

    public NeoJavaServer(NeoJavaProtocolListener listener){
        super();
        this.neoJavaProtocolListener = listener;
    }

    public static NeoJavaServer getInstance(NeoJavaProtocolListener listener){
        return new NeoJavaServer(listener);
    }

    public void writeOutput(String outputLine) {
        List<PrintWriter> opws = new ArrayList<>();
        opws.addAll(outPrintWriters);
        for (PrintWriter outPrintWriter: opws){
            if (outPrintWriter != null) {
                outPrintWriter.println(outputLine);
            }
        }
    }

    public void startServer(){
        System.out.println("Starting NeoJavaServer");
        try{
            serverSocket = new ServerSocket(port);
            System.out.printf("\nListening on port %d\n#:", port);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                if(clientSocket != null) {
                    clientSockets.add(clientSocket);
                    PrintWriter outPrintWriter =
                            new PrintWriter(clientSocket.getOutputStream(), true);
                    outPrintWriters.add(outPrintWriter);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));

                    (new Thread(new ServerThread(clientSocket, in, outPrintWriter))).start();
                }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

    private class ServerThread implements Runnable{
        PrintWriter out;
        BufferedReader in;
        Socket clientSocket;

        ServerThread(Socket clientSocket, BufferedReader in, PrintWriter out){
            this.in = in;
            this.out = out;
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            String inputLine, outputLine;
            // Initiate conversation with client
            NeoJavaProtocol njp = new NeoJavaProtocol(clientSocket,neoJavaProtocolListener);
            try{
                while (!serverSocket.isClosed()
                        && !clientSocket.isClosed()
                        && (inputLine = in.readLine()) != null) {
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
            writeOutput(NeoJavaProtocol.makeRequest("message/server", "shutdown"));
            for(Socket clientSocket: clientSockets) {
                if(clientSocket != null && !clientSocket.isClosed()){
                    clientSocket.close();
                }
            }
            clientSockets.clear();
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
