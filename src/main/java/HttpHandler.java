import util.HttpConstants;
import util.HttpHeader;
import util.HttpStatusCode;
import util.HttpVersion;

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
import java.util.HashMap;
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
            StringBuilder response = null;

            if ( requestTarget.equals( "/" ) ) {
                response = buildResponse( HttpStatusCode.OK, true );
            } else if ( requestTarget.startsWith( "/echo/" ) ) {
                String echo = requestTarget.substring( 6 );
                Map<HttpHeader, String> responseHeaders = new HashMap<>();
                responseHeaders.put( HttpHeader.CONTENT_TYPE, "text/plain" );
                responseHeaders.put( HttpHeader.CONTENT_LENGTH, String.valueOf( echo.length() ) );

                if ( request.getHeaders().get( HttpHeader.ACCEPT_ENCODING ).equals( "gzip" ) ) {
                    responseHeaders.put( HttpHeader.CONTENT_ENCODING, "gzip" );
                }

                response = buildResponse( HttpStatusCode.OK, responseHeaders );
            } else if ( requestTarget.equals( "/user-agent" ) ) {
                String userAgent = request.getHeaders().get( HttpHeader.USER_AGENT );
                Map<HttpHeader, String> responseHeaders = Map.of(
                        HttpHeader.CONTENT_TYPE, "text/plain",
                        HttpHeader.CONTENT_LENGTH, String.valueOf( userAgent.length() )
                );
                response = buildResponse( HttpStatusCode.OK, responseHeaders ).append( userAgent );
            } else if ( request.getHttpMethod().equals( "GET" ) && requestTarget.startsWith( "/files/" )) {
                String fileName = requestTarget.substring( 7 );
                File file = Paths.get( directory, fileName ).toFile();
                if ( file.exists() ) {
                    byte[] fileContents = Files.readAllBytes( file.toPath() );
                    Map<HttpHeader, String> responseHeaders = Map.of(
                            HttpHeader.CONTENT_TYPE, "application/octet-stream",
                            HttpHeader.CONTENT_LENGTH, String.valueOf( fileContents.length )
                    );
                    response = buildResponse( HttpStatusCode.OK, responseHeaders ).append( new String( fileContents ) );
                } else {
                    response = buildResponse( HttpStatusCode.NOT_FOUND, true );
                }
            } else if ( request.getHttpMethod().equals( "POST" ) && requestTarget.startsWith( "/files/" ) ) {
                String fileName = requestTarget.substring( 7 );
                File file = Paths.get( directory, fileName ).toFile();
                if ( file.createNewFile() ) {
                    BufferedWriter fileWriter = new BufferedWriter( new FileWriter( file ) );
                    fileWriter.write( request.getBody() );
                    fileWriter.flush();
                    fileWriter.close();
                    response = buildResponse( HttpStatusCode.CREATED, true );
                }
            } else {
                response = buildResponse( HttpStatusCode.NOT_FOUND, true );
            }

            if ( response != null ) {
                writer.write( response.toString() );
            }
            writer.flush();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private StringBuilder buildResponse( HttpStatusCode statusCode, boolean hasCRLF ) {
        StringBuilder response = new StringBuilder();
        response.append( HttpVersion.ONE_POINT_ONE.getVersion() )
                .append( HttpConstants.REQUEST_LINE_SEPARATOR )
                .append( statusCode.getStatusCode() )
                .append( HttpConstants.CRLF );
        return hasCRLF ? response.append( HttpConstants.CRLF ) : response;
    }

    private StringBuilder buildResponse( HttpStatusCode statusCode, Map<HttpHeader, String> responseHeaders ) {
        StringBuilder response = buildResponse( statusCode, false );
        for ( HttpHeader header : responseHeaders.keySet() ) {
            response.append( header.getField() )
                    .append( HttpConstants.HEADER_SEPARATOR )
                    .append( HttpConstants.REQUEST_LINE_SEPARATOR )
                    .append( responseHeaders.get( header ) );
        }
        return response.append( HttpConstants.CRLF );
    }
}
