import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
Create annotations for the different parts of a request (e.g. body, uripath, headers, etc...)
Create annotations for path variables (e.g. files/{readme}
These annotations would be for parameters

resource:
https://www.google.com/search?q=spring+endpoint+methods&rlz=1C1ONGR_enUS1124US1124&oq=spring+endpoint+methods&gs_lcrp=EgZjaHJvbWUyBggAEEUYOTIHCAEQIRigATIHCAIQIRigATIHCAMQIRifBdIBCjE0NjA3ajBqMTWoAgiwAgE&sourceid=chrome&ie=UTF-8
 */
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
