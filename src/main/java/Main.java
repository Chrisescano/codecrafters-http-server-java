import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

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
        try ( ServerSocket serverSocket = new ServerSocket() ) {
            serverSocket.setReuseAddress( true ); // ensures that we don't run into 'Address already in use' errors

            while ( true ) {
                Thread connection = new Thread(() -> {
                    try (
                            Socket socket = serverSocket.accept();
                            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                            BufferedWriter out = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) )
                    ) {
                        System.out.println( "accepted new connection");
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
                        String requestTarget = httpRequest.getRequestTarget();
                        if ( requestTarget.equals( "/" ) ) {
                            out.write( String.format( "HTTP/1.1 200 OK%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
                        } else if ( requestTarget.startsWith( "/echo/" ) ) {
                            String echo = httpRequest.getRequestTarget().substring( 6 );
                            out.write( String.format( "HTTP/1.1 200 OK%sContent-Type: text/plain%sContent-Length: %d%s%s%s",
                                    HttpConstants.CRLF, HttpConstants.CRLF, echo.length(), HttpConstants.CRLF, HttpConstants.CRLF, echo ) );
                        } else if ( requestTarget.equals( "/user-agent" ) ) {
                            Map<String, String> headers = httpRequest.getHeaders();
                            String userAgent = headers.get( "User-Agent" );
                            out.write( String.format(
                                    "HTTP/1.1 200 OK%sContent-Type: text/plain%sContent-Length: %d%s%s%s",
                                    HttpConstants.CRLF, HttpConstants.CRLF, userAgent.length(), HttpConstants.CRLF, HttpConstants.CRLF, userAgent
                            ) );
                        } else if ( requestTarget.equals( "/files/" )) {
                            String fileName = requestTarget.substring( 7 );
                            File file = new File( directory, fileName );
                            if ( file.exists() ) {
                                byte[] fileContents = Files.readAllBytes( file.toPath() );
                                out.write( String.format(
                                        "HTTP/1.1 200 OK%sCotent-Type: application/octet-stream%sCotent-Length: %d%s%s%s",
                                        HttpConstants.CRLF, HttpConstants.CRLF, fileContents.length, HttpConstants.CRLF,
                                        HttpConstants.CRLF, new String( fileContents )
                                ) );
                            } else {
                                out.write( String.format( "HTTP/1.1 404 Not Found%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
                            }
                        } else {
                            out.write( String.format( "HTTP/1.1 404 Not Found%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
                        }
                    } catch ( IOException e ) {
                        throw new RuntimeException( e );
                    }
                });
                connection.start();
            }
        } catch ( IOException e ) {
            System.out.println( "IOException: " + e.getMessage() );
        }
    }
}
