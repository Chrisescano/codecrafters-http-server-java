import compression.GZip;
import util.HttpConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        try {
            HttpRequest request = new HttpRequest( socket.getInputStream() );
            HttpResponse response = new HttpResponse( socket.getOutputStream(), HttpConstants.HTTP_VERSION_1_1 );
            request.parseRequest();
            Map<String, String> requestHeaders = request.getHeaders();
            System.out.printf( "Received Request: %s\n", request );
            String requestTarget = request.getRequestTarget();

            if ( requestTarget.equals( "/" ) ) {
                response.setStatusCode( HttpConstants.OK_RESPONSE );
                response.sendResponse();
            } else if ( requestTarget.startsWith( "/echo/" ) ) {
                String echo = requestTarget.substring( 6 );
                response.addHeader( HttpConstants.CONTENT_TYPE, "text/plain" );
                if ( requestHeaders.containsKey( HttpConstants.ACCEPT_ENCODING ) ) {
                    String[] clientEncodings = requestHeaders.get( HttpConstants.ACCEPT_ENCODING ).split( "," );
                    for ( String encoding : clientEncodings ) {
                        if ( encoding.trim().equals( "gzip" ) ) {
                            response.addHeader( HttpConstants.CONTENT_ENCODING, "gzip" );
                        }
                    }

                    if ( response.containsHeader( HttpConstants.CONTENT_ENCODING ) ) {
                        byte[] compressedData = GZip.compress( echo );
                        response.addHeader( HttpConstants.CONTENT_LENGTH, String.valueOf( compressedData.length ) );
                        response.setStatusCode( HttpConstants.OK_RESPONSE );
                        response.sendResponse( compressedData );
                    }
                }
                response.addHeader( HttpConstants.CONTENT_LENGTH, String.valueOf( echo.length() ) );
                response.setStatusCode( HttpConstants.OK_RESPONSE );
                response.sendResponse( echo );
            } else if ( requestTarget.equals( "/user-agent" ) ) {
                String userAgent = requestHeaders.get( HttpConstants.USER_AGENT );
                response.addHeader( HttpConstants.CONTENT_TYPE, "text/plain" );
                response.addHeader( HttpConstants.CONTENT_LENGTH, String.valueOf( userAgent.length() ) );
                response.setStatusCode( HttpConstants.OK_RESPONSE );
                response.sendResponse( userAgent );
            } else if ( request.getHttpMethod().equals( "GET" ) && requestTarget.startsWith( "/files/" )) {
                String fileName = requestTarget.substring( 7 );
                File file = Paths.get( directory, fileName ).toFile();
                if ( file.exists() ) {
                    byte[] fileContents = Files.readAllBytes( file.toPath() );
                    response.addHeader( HttpConstants.CONTENT_TYPE, "application/octet-stream" );
                    response.addHeader( HttpConstants.CONTENT_LENGTH, String.valueOf( fileContents.length ) );
                    response.setStatusCode( HttpConstants.OK_RESPONSE );
                    response.sendResponse( fileContents );
                } else {
                    response.setStatusCode( HttpConstants.NOT_FOUND_RESPONSE );
                    response.sendResponse();
                }
            } else if ( request.getHttpMethod().equals( "POST" ) && requestTarget.startsWith( "/files/" ) ) {
                String fileName = requestTarget.substring( 7 );
                File file = Paths.get( directory, fileName ).toFile();
                if ( file.createNewFile() ) {
                    BufferedWriter fileWriter = new BufferedWriter( new FileWriter( file ) );
                    fileWriter.write( request.getBody() );
                    fileWriter.flush();
                    fileWriter.close();
                    response.setStatusCode( HttpConstants.CREATED_RESPONSE );
                    response.sendResponse();
                }
            } else {
                response.setStatusCode( HttpConstants.NOT_FOUND_RESPONSE );
                response.sendResponse();
            }
            request.terminate();
            response.terminate();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
