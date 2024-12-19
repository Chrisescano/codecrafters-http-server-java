import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main( String[] args ) {
        final String directory;
        if ( args.length > 1 && args[0].equals( "--directory" ) ) {
            directory = args[1];
        } else {
            directory = "";
        }

        System.out.println( "Logs from your program will appear here!" );
        try (
                ServerSocket serverSocket = new ServerSocket( 4221 );
        ) {
            serverSocket.setReuseAddress( true );

            while ( true ) {
                Thread socketAcceptThread = new Thread(() -> {
                    while ( true ) {
                        try ( Socket socket = serverSocket.accept() ) {
                            System.out.println( "accepted new connection" );
                            Runnable handler = new HttpHandler( socket, directory );
                            handler.run();
                        } catch ( IOException e ) {
                            throw new RuntimeException( e );
                        }
                    }
                });
                socketAcceptThread.start();
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
