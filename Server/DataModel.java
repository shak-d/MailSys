package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class DataModel {


    private ServerListner serverListner;
    private final int MAILPORT = 5522;
    private ServerSocket serverSocket;
    private boolean serverRunning = false;
    private final Object fileWriting = new Object();
    private ArrayList<ClientHandler> clients;
    private int clientIndex;
    private ArrayList<Thread> userThreads;
    private Thread acceptingThread;

    void startServer(){

        if(serverSocket !=null)
            throw new RuntimeException("Server Already running");
        try {
            serverSocket = new ServerSocket(MAILPORT);
        }catch (IOException e){
            e.printStackTrace();
        }

        serverRunning = true;
        if(serverListner != null)
            serverListner.onServerEvent(ServerLog.SERVER_STARTED);

        clients = new ArrayList<>();
        userThreads = new ArrayList<>();
        clientIndex = 0;

        acceptingThread = new Thread(() -> {
            while (serverRunning){
                try {
                    Socket socket = serverSocket.accept();
                    if(serverListner != null)
                        serverListner.onServerEvent(ServerLog.ACCEPTED_CONNECTION);
                    ClientHandler clientHandler = new ClientHandler(socket, this, clientIndex);
                    synchronized (fileWriting) {
                        clients.add(clientHandler);
                    }
                    clientIndex++;
                    Thread handler = new Thread(clientHandler);
                    userThreads.add(handler);
                    handler.start();
                } catch (IOException e) {
                    if(!serverRunning) {
                        if(serverListner!= null)
                            serverListner.onServerError(ServerError.ACCEPT_CONNECTION);
                        else
                        e.printStackTrace();
                    }
                }
            }
        });
        acceptingThread.start();
    }

    int getMailPort(){
        return MAILPORT;
    }

    Object getFileWriting() {
        return fileWriting;
    }


    ArrayList<ClientHandler> getClients() {
        return clients;
    }

    ServerListner getServerListner() {
        return serverListner;
    }

    void setServerListner(ServerListner serverListner) {
        this.serverListner = serverListner;
    }

    boolean isServerRunning(){return serverRunning;}

    void removeClient(ClientHandler handler){
        clients.remove(handler);
    }

    void closeServer() throws InterruptedException, IOException {

        serverRunning = false;
        acceptingThread.interrupt();
        for(ClientHandler h : clients){
            h.close();
        }
        for (Thread t : userThreads)
            t.join();
        serverSocket.close();
    }

}
