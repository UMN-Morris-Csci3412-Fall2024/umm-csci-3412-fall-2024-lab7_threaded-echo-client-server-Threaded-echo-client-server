// package echoserver;

// import java.io.BufferedReader;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
// import java.io.EOFException;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.OutputStream;
// import java.io.PrintWriter;
// import java.net.ServerSocket;
// import java.net.Socket;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;

// public class EchoServer {
//     public static final int PORT_NUMBER = 6013;
//     private static final int THREAD_POOL_SIZE = 10; // Limit number of concurrent clients

//     public static void main(String[] args) throws IOException, InterruptedException {
//         EchoServer server = new EchoServer();
//         server.start();
//     }

//     private void start() throws IOException, InterruptedException {
// try {
//             // Start listening on the specified port
//             ServerSocket serverSock = new ServerSocket(PORT_NUMBER);

//             // Run forever, which is common for server style services
//             while (true) {
//                 // Wait until someone connects
//                 Socket client = serverSock.accept();

//                 try{ 
//                   DataInputStream in = new DataInputStream(client.getInputStream());
//                     DataOutputStream out = new DataOutputStream(client.getOutputStream()); 

//                     // Read and write bytes until the end of the stream
//                     while (true) {
//                         try {
//                             byte inputByte = in.readByte(); // Read a byte
//                             out.writeByte(inputByte); // Echo the byte back
//                             System.out.println(inputByte);
//                         } catch (EOFException e) {
//                             // End of stream reached, break the loop
//                             break;
//                         }
//                 }
//                 } finally {
//                     client.close(); 
//                     serverSock.close(); //why did that fix the error on the large chunk of text?
//                 }
//             }
//         } catch (IOException e) {
//             System.out.println("Exception caught when trying to listen on port "
//                 + PORT_NUMBER + " or listening for a connection");
//             System.out.println(e.getMessage());
//         }
// }
// }

package echoserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {
    public static final int PORT_NUMBER = 6013;
    private static final int THREAD_POOL_SIZE = 20; 

    public static void main(String[] args) throws IOException {
        EchoServer server = new EchoServer();
        server.start();
    }

    private void start() throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        ServerSocket serverSock = new ServerSocket(PORT_NUMBER);

        try {
            System.out.println("Server started, waiting for clients...");

            // Run forever, waiting for client connections
            while (true) {
                // Accept client connection
                Socket clientSocket = serverSock.accept();
                // System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create a new task for each client
                Runnable clientHandler = new ClientHandler(clientSocket);
                threadPool.submit(clientHandler); // Submit the task to the thread pool
            }
        } catch (IOException e) {
            System.out.println("Exception caught when listening for a connection");
            serverSock.close();
        }
        serverSock.close();
        System.out.println("server socket closed");
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
            ) {
                byte inputByte;
                // Read and echo bytes until the end of stream
                while (true) {
                    try {
                        inputByte = in.readByte(); // Read a byte
                        out.writeByte((char)inputByte); // Echo the byte back
                        System.out.println("Echoed byte: " + inputByte);
                        // if (inputByte == -1) {
                        //     break;
                        // }
                    } catch (EOFException e) {
                        // End of stream reached, break the loop
                        System.out.println("end of input");
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client connection: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close(); // Close the client socket
                    System.out.println("client socket closed");
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}
