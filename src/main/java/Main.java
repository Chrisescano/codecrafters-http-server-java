import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main( String[] args ) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println( "Logs from your program will appear here!" );

        try {
            ServerSocket serverSocket = new ServerSocket( 4221 );
            serverSocket.setReuseAddress( true ); // ensures that we don't run into 'Address already in use' errors

            while ( true ) {
                Socket socket = serverSocket.accept(); // Wait for connection from client.

                Thread connectionThread = new Thread(() -> {
                    System.out.println( "accepted new connection" );
                    BufferedReader in;
                    BufferedWriter out;

                    try {
                        in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                        out = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
                    } catch ( IOException e ) {
                        throw new RuntimeException( e );
                    }
                    StringBuilder request = new StringBuilder();

                    while ( true ) {
                        String line;
                        try {
                            line = in.readLine();
                        } catch ( IOException e ) {
                            throw new RuntimeException( e );
                        }

                        request.append( line ).append( HttpConstants.CRLF );
                        if ( line == null || line.isEmpty() ) {
                            break;
                        }
                    }

                    System.out.printf( "Received Request: %s\n", request );
                    HttpRequest httpRequest = HttpRequest.parseRequest( request.toString() );
                    if ( httpRequest.getRequestTarget().equals( "/" ) ) {
                        try {
                            out.write( String.format( "HTTP/1.1 200 OK%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
                        } catch ( IOException e ) {
                            throw new RuntimeException( e );
                        }
                    } else if ( httpRequest.getRequestTarget().startsWith( "/echo/" )) {
                        String echo = httpRequest.getRequestTarget().substring( 6 );
                        try {
                            out.write( String.format( "HTTP/1.1 200 OK%sContent-Type: text/plain%sContent-Length: %d%s%s%s",
                                    HttpConstants.CRLF, HttpConstants.CRLF, echo.length(), HttpConstants.CRLF, HttpConstants.CRLF, echo ) );
                        } catch ( IOException e ) {
                            throw new RuntimeException( e );
                        }
                    } else if ( httpRequest.getRequestTarget().equals( "/user-agent" ) ) {
                        Map<String, String> headers = httpRequest.getHeaders();
                        String userAgent = headers.get( "User-Agent" );
                        try {
                            out.write( String.format(
                                    "HTTP/1.1 200 OK%sContent-Type: text/plain%sContent-Length: %d%s%s%s",
                                    HttpConstants.CRLF, HttpConstants.CRLF, userAgent.length(), HttpConstants.CRLF, HttpConstants.CRLF, userAgent
                            ) );
                        } catch ( IOException e ) {
                            throw new RuntimeException( e );
                        }
                    } else {
                        try {
                            out.write( String.format( "HTTP/1.1 404 Not Found%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
                        } catch ( IOException e ) {
                            throw new RuntimeException( e );
                        }
                    }

                    try {
                        out.flush();
                        in.close();
                        out.close();
                        socket.close();
                    } catch ( IOException e ) {
                        throw new RuntimeException( e );
                    }
                });
                connectionThread.start();
            }
        } catch ( IOException e ) {
            System.out.println( "IOException: " + e.getMessage() );
        }
    }
}
