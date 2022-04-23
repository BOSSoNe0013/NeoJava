package com.b1project.udooneo.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.b1project.udooneo.NeoJava;
import com.b1project.udooneo.listeners.NeoJavaProtocolListener;
import com.b1project.udooneo.messages.Message;
import com.b1project.udooneo.messages.ResponseMessage;

/**
 *  Copyright (C) 2015 Cyril BOSSELUT <bossone0013@gmail.com>
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
 *  along with this program.  If not, see <<a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>>.
 *
 */
@SuppressWarnings("restriction")
public class NeoJavaSecureServer {
    private static final int SERVER_PORT = 45046;
    private SSLServerSocket serverSocket;
    private final List<SSLSocket> clientSockets = new ArrayList<>();
    private final NeoJavaProtocolListener neoJavaProtocolListener;
    private final List<PrintWriter> outPrintWriters = new ArrayList<>();

    static class SSLProvider extends Provider {
        protected SSLProvider(String name, double version, String info) {
            super(name, version, info);
        }
    }

    static {
        // Registering the JSSE provider
        Security.addProvider(new SSLProvider("SSL Provider", 1.0, ""));

        //Specifying the Keystore details
        System.setProperty("javax.net.ssl.keyStore","NeoJava.ks");
        System.setProperty("javax.net.ssl.keyStorePassword","udooer");

        // Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
        // System.setProperty("javax.net.debug","all");
    }

    private NeoJavaSecureServer(NeoJavaProtocolListener listener){
        super();
        this.neoJavaProtocolListener = listener;
    }

    public static NeoJavaSecureServer getInstance(NeoJavaProtocolListener listener){
        return new NeoJavaSecureServer(listener);
    }

    public void writeOutput(Message msg) {
        List<PrintWriter> opws = new ArrayList<>(outPrintWriters);
        String json = NeoJavaProtocol.toJson(msg);
        for (PrintWriter outPrintWriter: opws){
            if (outPrintWriter != null) {
				outPrintWriter.println(json);
            }
        }
    }

    public void startServer(){
        System.out.println("\rStarting NeoJavaSecureServer");
        System.out.print("#:");
        try{
            System.out.printf("\rListening on port %d\n", SERVER_PORT);
            System.out.print("#:");
            SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(SERVER_PORT);

            serverSocket.setEnabledCipherSuites(sslServerSocketfactory.getSupportedCipherSuites());
            //noinspection InfiniteLoopStatement
            while(true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                if(clientSocket != null) {
                    if(!serverSocket.isClosed() && !clientSocket.isClosed()){
                        if (NeoJava.DEBUG) {
                            System.out.println("\rNew client socket: " + clientSocket.getInetAddress().getHostAddress());
                            System.out.print("#:");
                        }
                        clientSockets.add(clientSocket);
                        PrintWriter outPrintWriter =
                                new PrintWriter(clientSocket.getOutputStream(), true);
                        outPrintWriters.add(outPrintWriter);
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));

                        (new Thread(new ServerThread(clientSocket, in, outPrintWriter))).start();
                    }
                }
            }
        } catch (SocketException e) {
            if (NeoJava.DEBUG) {
                System.out.println("\rSocket closed");
                System.out.print("#:");
            }
        } catch (IOException e) {
            NeoJava.logger.warn("\rException caught when trying to listen on port %d or listening for a connection " + SERVER_PORT + "\n");
            NeoJava.logger.warn("Error: " + e.getMessage());
            System.out.print("#:");
        }
    }

    private class ServerThread implements Runnable{
        final PrintWriter out;
        final BufferedReader in;
        final Socket clientSocket;

        ServerThread(Socket clientSocket, BufferedReader in, PrintWriter out){
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
                System.err.println("\rException caught when  listening for a connection");
                System.err.println("Error: " + e.getMessage());
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
            System.err.println("\rException caught when trying to close socket");
            System.err.println("Error: " + e.getMessage());
            System.out.print("#:");
        }
    }
}