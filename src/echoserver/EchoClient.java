package echoserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class EchoClient {
    public static final int PORT_NUMBER = 6013;
    private static final int TIMEOUT_MS = 5000;

    public static void main(String[] args) throws IOException {
        EchoClient client = new EchoClient();
        client.start();
    }

    private void start() throws IOException {
        String server = "127.0.0.1";
        // Use "127.0.0.1", i.e., localhost

        try {
            Socket socket = new Socket(server, PORT_NUMBER);

            InputStream userInput = System.in;
            // OutputStream userOutput = System.out;
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Thread for reading user input and sending to server
            Thread inputThread = new Thread(new InputHandler(userInput, out));
            inputThread.start();

            // System.out.println("input thread done starting");

            // Thread for reading from server and printing to standard output
            Thread outputThread = new Thread(new OutputHandler(in));
            outputThread.start();

            // System.out.println("output thread done starting");

            // Wait for threads to finish
            inputThread.join();
            // System.out.println("input thread joined");
            socket.shutdownOutput(); //very imporatnt
            outputThread.join();

            // System.out.println("threads joined");

            socket.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("We caught an unexpected exception");
            System.err.println(e);
        }
    }

    // Runnable to handle reading from System.in (keyboard) and writing to socket
    private static class InputHandler implements Runnable {
        private InputStream userInput;
        private OutputStream out;

        public InputHandler(InputStream userInput, OutputStream out) {
            this.userInput = userInput;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                int data;
                while ((data = userInput.read()) != -1) {
                    out.write(data);
                    out.flush();
                }
                // System.out.println("done with input handler");
            } catch (IOException e) {
                System.err.println("Error reading from user input or sending to server.");
                e.printStackTrace();
            }
        }
    }

    // Runnable to handle reading from socket and writing to System.out (console)
    private static class OutputHandler implements Runnable {
        private InputStream in;

        public OutputHandler(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                int data;
                long lastReadTime = System.currentTimeMillis(); // Track the last time we read data

                while (true) {
                    data = in.read();
                    if (data == -1) {
                        // End of stream, break the loop
                        // System.out.println("End of stream reached. Closing output thread.");
                        break;
                    }

                    // Reset the last read time upon successful data read
                    lastReadTime = System.currentTimeMillis();

                    // Output the received byte
                    System.out.write(data);
                    System.out.flush();

                    // System.out.println("after flush in output handler");
                    //System.out.println(System.currentTimeMillis() - lastReadTime);

                    // Check if we've timed out (if no data has been received for a period)
                    if (System.currentTimeMillis() - lastReadTime > TIMEOUT_MS) {
                        System.out.println("Timeout occurred, no data received for " + TIMEOUT_MS + "ms.");
                        break;  // Exit the loop after timeout
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading from server or writing to output.");
                e.printStackTrace();
            }
        }
}
}


