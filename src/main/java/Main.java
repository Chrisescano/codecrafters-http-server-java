import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main( String[] args ) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println( "Logs from your program will appear here!" );

        // Uncomment this block to pass the first stage

        try {
            ServerSocket serverSocket = new ServerSocket( 4221 );
            serverSocket.setReuseAddress( true ); // ensures that we don't run into 'Address already in use' errors
            Socket socket = serverSocket.accept(); // Wait for connection from client.
            System.out.println( "accepted new connection" );

            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            BufferedWriter out = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );

            String message;
            while ( ( message = in.readLine() ) != null ) {
                out.write( "HTTP/1.1 200 OK\r\n\r\n" );
                out.flush();
            }

            in.close();
            out.close();
        } catch ( IOException e ) {
            System.out.println( "IOException: " + e.getMessage() );
        }
    }
}
