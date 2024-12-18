import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

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
                ExecutorService executorService = newFixedThreadPool( 100 )
        ) {
            serverSocket.setReuseAddress( true );

//            Thread socketAcceptThread = new Thread(() -> {
//                while ( true ) {
//                    try ( Socket socket = serverSocket.accept() ) {
//                        System.out.println( "accepted new connection" );
//                        executorService.execute( new HttpHandler( socket, directory ) );
//                    } catch ( IOException e ) {
//                        throw new RuntimeException( e );
//                    }
//                }
//            });
//            socketAcceptThread.start();

            try {
                Socket socket = serverSocket.accept();
                System.out.println( "accepted new connection" );
                Runnable handler = new HttpHandler( socket, directory );
                executorService.execute( handler );
            } catch ( IOException e ) {
                throw new IOException( e );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
