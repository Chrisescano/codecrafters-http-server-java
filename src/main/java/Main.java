import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    @SuppressWarnings( "InfiniteLoopStatement" )
    public static void main( String[] args ) {
        final String directory;
        if ( args.length > 1 && args[0].equals( "--directory" ) ) {
            directory = args[1];
        } else {
            directory = "";
        }

        System.out.println( "Logs from your program will appear here!" );
        try (
                ServerSocket serverSocket = new ServerSocket( 4221 )
        ) {
            serverSocket.setReuseAddress( true );

            while ( true ) {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch ( IOException e ) {
                    throw new RuntimeException( e );
                }
                System.out.println( "accepted new connection" );
                Thread requestThread = new Thread(() -> {
                    HttpHandler handler = new HttpHandler( socket, directory );
                    handler.run();
                    try {
                        socket.close();
                    } catch ( IOException e ) {
                        throw new RuntimeException( e );
                    }
                });
                requestThread.start();
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
