import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Server {

    private List<Integer> keySequence = new ArrayList<>(); //oczekiwana sekwencja pakietow UDP
    private Map<String,List<Integer>> clientsKnocking; //Mapa wszyskich klientow wraz z pakietami ktore wyslali

    private Server(){
        this.clientsKnocking= new HashMap<>();
    }

    private void setKeySequence(List<Integer> keySequence) {
        this.keySequence = keySequence;
    }

    private synchronized List<Integer> getKeySequence() {
        return keySequence;
    }

    private synchronized Map<String,List<Integer>> getClientsKnocking(){
        return clientsKnocking;
    }

    private synchronized void addNewClientKnocking(String key, int data){
        clientsKnocking.computeIfAbsent(key, k -> new ArrayList<>()).add(data);
    }

    private synchronized void addNewPacketFromClient(String key, int data){
        clientsKnocking.get(key).add(data);
    }

    private synchronized boolean clientKnocked(String key){
        return clientsKnocking.containsKey(key);
    }

    public static class ServerThread extends Thread {
        private final DatagramPacket packet;
        private final DatagramSocket socket;
        Server server;

        ServerThread(DatagramPacket packet, DatagramSocket socket, Server server) {
            super();
            this.packet = packet;
            this.socket = socket;
            this.server = server;

        }

        public void run() {
            //System.out.println(serverApp2.getClientsKnocking());

            String data = new String(packet.getData(),0,packet.getLength());
            System.out.println(data);

            int clientPort = packet.getPort();
            InetAddress clientAddress = packet.getAddress();
            String key = clientAddress+":"+clientPort;

            if(server.clientKnocked(key)) {
                server.addNewPacketFromClient(key, socket.getLocalPort());
               // System.out.println("addNewPacket");

            }
            else {
                server.addNewClientKnocking(key, socket.getLocalPort());
               // System.out.println("addNewClient");
            }

            //System.out.println(serverApp2.getClientsKnocking()); //wypisanie wszystkich klientow wraz z pakietami ktore wysylali

            // jesli dany klient wyslal juz poprawna sekwencje
            if(server.getClientsKnocking().get(key).equals(server.getKeySequence())) {
                server.sendTCPport(socket,clientAddress,clientPort);
            }
        }
    }

     private void sendTCPport(DatagramSocket socket, InetAddress clientAddress, int clientPort){
        //generwoanie numeru portu TCP
        int randomTCPport = (int)(Math.random()*1000 + 1025);
        byte[] respBuff = String.valueOf(randomTCPport).getBytes();
        //wyslanie numeru portu TCP do klienta
        DatagramPacket resp = new DatagramPacket(respBuff, respBuff.length, clientAddress, clientPort);
        try {
            socket.send(resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //otwarcie portu TCP
        TCPServer tcpServer = new TCPServer();
        tcpServer.listenSocket(randomTCPport);

    }

    private void service(DatagramSocket socket) throws IOException {
        byte[] buff = new byte[65535];
        final DatagramPacket datagram = new DatagramPacket(buff, buff.length);

        socket.receive(datagram);

        (new ServerThread(datagram, socket, this)).start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void listenSocket(int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        }
        catch (IOException e) {
            System.out.println("Could not listen");
            System.exit(-1);
        }
        System.out.println("Server listens on port: " + socket.getLocalPort());

        while(true) {
            try {
                service(socket);
            }
            catch (IOException e) {
                System.out.println("Accept failed");
            }
        }
    }

    public static void main(String[] args) {

        if(args.length < 1)
        {
            System.out.println("Too few parameters: got " + args.length + ", expected 1");
            return;
        }

        List<Integer> portSequence = new ArrayList<>();
        List<String> portsList = new ArrayList<>();
        for(String p : args){
            if(!portsList.contains(p))
                portsList.add(p);
            portSequence.add(Integer.parseInt(p));
        }
        Server server = new Server();

        server.setKeySequence(portSequence);
        System.out.println("Expected sequence: " + portSequence);

        for(String port : portsList){
            new Thread(() -> {
                server.listenSocket(Integer.parseInt(port));
            }).start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            System.out.println("Adress IP of server :");
            System.out.println(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
