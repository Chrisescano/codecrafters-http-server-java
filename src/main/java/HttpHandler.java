import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class HttpHandler implements Runnable {

    private final Socket socket;
    private final String directory;

    public HttpHandler( Socket socket, String directory ) {
        this.socket = socket;
        this.directory = directory;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) )
        ) {
            HttpRequest request = new HttpRequest( reader );
            request.parseRequest();

            System.out.printf( "Received Request: %s\n", request );
            String requestTarget = request.getRequestTarget();
            if ( requestTarget.equals( "/" ) ) {
                writer.write( String.format( "HTTP/1.1 200 OK%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
            } else if ( requestTarget.startsWith( "/echo/" ) ) {
                String echo = requestTarget.substring( 6 );
                writer.write( String.format( "HTTP/1.1 200 OK%sContent-Type: text/plain%sContent-Length: %d%s%s%s",
                        HttpConstants.CRLF, HttpConstants.CRLF, echo.length(), HttpConstants.CRLF, HttpConstants.CRLF, echo ) );
            } else if ( requestTarget.equals( "/user-agent" ) ) {
                Map<HttpHeader, String> headers = request.getHeaders();
                String userAgent = headers.get( HttpHeader.USER_AGENT );
                writer.write( String.format(
                        "HTTP/1.1 200 OK%sContent-Type: text/plain%sContent-Length: %d%s%s%s",
                        HttpConstants.CRLF, HttpConstants.CRLF, userAgent.length(), HttpConstants.CRLF, HttpConstants.CRLF, userAgent
                ) );
            } else if ( request.getHttpMethod().equals( "GET" ) && requestTarget.startsWith( "/files/" )) {
                String fileName = requestTarget.substring( 7 );
                File file = Paths.get( directory, fileName ).toFile();
                System.out.printf( "File Path: %s\n", file.getAbsolutePath() );
                if ( file.exists() ) {
                    byte[] fileContents = Files.readAllBytes( file.toPath() );
                    writer.write( String.format(
                            "HTTP/1.1 200 OK%sContent-Type: application/octet-stream%sContent-Length: %d%s%s%s",
                            HttpConstants.CRLF, HttpConstants.CRLF, fileContents.length, HttpConstants.CRLF,
                            HttpConstants.CRLF, new String( fileContents )
                    ) );
                } else {
                    writer.write( String.format( "HTTP/1.1 404 Not Found%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
                }
            } else if ( request.getHttpMethod().equals( "POST" ) && requestTarget.startsWith( "/files/" ) ) {
                String fileName = requestTarget.substring( 7 );
                File file = Paths.get( directory, fileName ).toFile();
                System.out.printf( "File Path: %s\n", file.getAbsolutePath() );
                if ( file.createNewFile() ) {
                    BufferedWriter fileWriter = new BufferedWriter( new FileWriter( file ) );
                    fileWriter.write( request.getBody() );
                    fileWriter.flush();
                    fileWriter.close();
                    writer.write( String.format( "HTTP/1.1 201 Created%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
                }
            } else {
                writer.write( String.format( "HTTP/1.1 404 Not Found%s%s", HttpConstants.CRLF, HttpConstants.CRLF ) );
            }
            writer.flush();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
