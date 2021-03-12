import java.io.IOException;
import java.net.*;

public class Client {

    private static int port;
    private static String ip;
    private DatagramSocket socket;
    private InetAddress address;

    Client(){
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(2000);
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            System.out.println("Unknown Host");
            System.exit(-1);
        } catch (SocketException e) {
            System.out.println("Socket Error");
            System.exit(-1);
        }
    }

    private void sendMsg(String msg){
        byte[] buf= msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("No IO");
            System.exit(-1);
        }
     // System.out.println("Packet sent to " + port);
    }

    private String getResponse(){
        byte[] buf = new byte[65535];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {
            socket.receive(packet);
            System.out.println("Connection successful");
        }
        catch (IOException e){
            System.out.println("Error during communication");
            System.exit(-1);
        }

        return new String(packet.getData(),0, packet.getLength());
    }

    private void close() {
        socket.close();
    }

    private void connectToTCP(int port){
        TCPClient tcpClient = new TCPClient();
        tcpClient.connectToTCPServer(port, ip);
    }

    public static void main(String[] args){
        if(args.length < 1)
        {
            System.out.println("Too few parameters: got " + args.length + ", expected 1");
            return;
        }

        ip = args[0];

        Client client = new Client();
        for(int i = 1; i <args.length; i++) {

            port = Integer.parseInt(args[i]);
            client.sendMsg("Knock knock! to " +  port);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        String odp = client.getResponse();
        client.connectToTCP(Integer.parseInt(odp));
        client.close();

    }
}
