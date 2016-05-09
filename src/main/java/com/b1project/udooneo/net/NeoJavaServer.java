package com.b1project.udooneo.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.b1project.udooneo.NeoJava;

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
import com.b1project.udooneo.messages.Message;
import com.b1project.udooneo.messages.ResponseMessage;

public class NeoJavaServer {
    private static final int port = NeoJava.DEFAULT_SERVER_PORT;
    private ServerSocket serverSocket;
    private List<Socket> clientSockets = new ArrayList<>();
    private NeoJavaProtocolListener neoJavaProtocolListener;
    private List<PrintWriter> outPrintWriters = new ArrayList<>();

    public NeoJavaServer(NeoJavaProtocolListener listener){
        this.neoJavaProtocolListener = listener;
    }

    public static NeoJavaServer getInstance(NeoJavaProtocolListener listener){
        return new NeoJavaServer(listener);
    }

    public void writeOutput(Message msg) {
    	String json = NeoJavaProtocol.toJson(msg);
        for (PrintWriter outPrintWriter: outPrintWriters){
            if (outPrintWriter != null) {
				outPrintWriter.println(json);
            }
        }
    }

    public void startServer(){
    	System.out.println("\rStarting NeoJavaServer");
    	System.out.print("#:");
        try{
            System.out.printf("\rListening on port %d\n", port);
            System.out.print("#:");
            serverSocket = new ServerSocket(port);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                if(clientSocket != null) {
                    if(!serverSocket.isClosed() && !clientSocket.isClosed()) {
                        System.out.println("\rNew client socket: " + clientSocket.getInetAddress().getHostAddress());
                        System.out.print("#:");
                        clientSockets.add(clientSocket);
                        PrintWriter outPrintWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                        outPrintWriters.add(outPrintWriter);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        (new Thread(new ServerThread(clientSocket, in, outPrintWriter))).start();
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("\rSocket closed");
            System.out.print("#:");
        } catch (IOException e) {
            System.out.println("\rException caught when trying to listen on port " + port + " or listening for a connection");
            System.out.println(e.getMessage());
            System.out.print("#:");
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
            String inputLine;
            ResponseMessage response;
            // Initiate conversation with client
            NeoJavaProtocol njp = new NeoJavaProtocol(clientSocket,neoJavaProtocolListener);
            try{
                while (!serverSocket.isClosed()
                        && !clientSocket.isClosed()
                        && (inputLine = in.readLine()) != null) {
                	response = njp.processInput(inputLine);
                    if (response != null) {
                        out.println(NeoJavaProtocol.toJson(response));
                    }
                }
            } catch (IOException e) {
                System.out.println("\rException caught when  listening for a connection");
                System.out.println(e.getMessage());
                System.out.print("#:");
            }
        }
    }

    public void stopServer(){
        try{
            writeOutput(NeoJavaProtocol.makeShutdownMessage());
            for(Socket clientSocket: clientSockets) {
                if(clientSocket != null && !clientSocket.isClosed()){
                    clientSocket.close();
                }
            }
            clientSockets.clear();
            if(serverSocket != null){
                serverSocket.close();
            }
            System.out.println("\rNeoJavaServer stopped");
            System.out.print("#:");
        } catch (IOException e) {
            System.out.println("\rException caught when trying to close socket");
            System.out.print("#:");
            System.out.println(e.getMessage());
        }
    }
}
