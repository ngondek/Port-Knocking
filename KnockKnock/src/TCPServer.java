import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    public static class ServerThread extends Thread {
        private final Socket socket;

        public ServerThread(Socket socket) {
            super();
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(),true);

                System.out.println(in.readLine());

                out.println("odpowiedz od TCPServer do TCPClient");

            } catch (IOException e1) {
                System.out.println("IOException ");
            }

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("IOException while closing socket");
            }
        }
    }

    public void listenSocket(int port) {
        ServerSocket server = null;
        Socket client = null;
        try {
            server = new ServerSocket(port);
        }
        catch (IOException e) {
            System.out.println("Could not listen");
            System.exit(-1);
        }
        System.out.println("TCPServer listens on port: " + server.getLocalPort());

        while(true) {
            try {
                client = server.accept();
            }
            catch (IOException e) {
                System.out.println("Accept failed");
                System.exit(-1);
            }
            (new ServerThread(client)).start();
        }
    }
}
