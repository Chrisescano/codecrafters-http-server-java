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

        // Uncomment this block to pass the first stage

        try {
            ServerSocket serverSocket = new ServerSocket( 4221 );
            serverSocket.setReuseAddress( true ); // ensures that we don't run into 'Address already in use' errors
            Socket socket = serverSocket.accept(); // Wait for connection from client.
            System.out.println( "accepted new connection" );
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            BufferedWriter out = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
            StringBuilder request = new StringBuilder();

            while ( true ) {
                String line = in.readLine();
                request.append( line ).append( HttpConstants.CRLF );
                if ( line == null || line.isEmpty() ) {
                    break;
                }
            }

            System.out.printf( "Received Request: %s\n", request );
            HttpRequest httpRequest = HttpRequest.parseRequest( request.toString() );
            if ( httpRequest.getRequestTarget().equals( "/" ) ) {
                out.write( String.format( "HTTP/1.1 200 OK%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
            } else if ( httpRequest.getRequestTarget().startsWith( "/echo/" )) {
                String echo = httpRequest.getRequestTarget().substring( 6 );
                out.write( String.format( "HTTP/1.1 200 OK%sContent-Type: text/plain%sContent-Length: %d%s%s%s",
                        HttpConstants.CRLF, HttpConstants.CRLF, echo.length(), HttpConstants.CRLF, HttpConstants.CRLF, echo ) );
            } else if ( httpRequest.getRequestTarget().equals( "/user-agent" ) ) {
                Map<String, String> headers = httpRequest.getHeaders();
                String userAgent = headers.get( "User-Agent" );
                out.write( String.format(
                        "HTTP/1.1 200 OK%sContent-Type: text/plain%sContent-Length: %d%s%s%s",
                        HttpConstants.CRLF, HttpConstants.CRLF, userAgent.length(), HttpConstants.CRLF, HttpConstants.CRLF, userAgent
                ) );
            } else {
                out.write( String.format( "HTTP/1.1 404 Not Found%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
            }

            out.flush();
            in.close();
            out.close();
            socket.close();
            serverSocket.close();
        } catch ( IOException e ) {
            System.out.println( "IOException: " + e.getMessage() );
        }
    }
}
