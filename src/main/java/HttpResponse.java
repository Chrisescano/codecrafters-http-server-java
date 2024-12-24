import util.HttpConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private final OutputStream outputStream;
    private final String httpVersion;
    private final Map<String, String> headers;
    private String statusCode;

    public HttpResponse( OutputStream outputStream, String httpVersion ) {
        this.outputStream = outputStream;
        this.httpVersion = httpVersion;
        headers = new HashMap<>();
    }

    public void addHeader( String key, String value ) {
        headers.put( key, value );
    }

    public boolean containsHeader( String key ) {
        return headers.containsKey( key );
    }

    public void sendResponse() {
        byte[] responseBytes = buildResponse();
        send( responseBytes );
    }

    public void sendResponse( String body ) {
        byte[] responseBytes = buildResponse();
        byte[] bodyBytes = body.getBytes( StandardCharsets.UTF_8 );
        int totalLength = responseBytes.length + bodyBytes.length;
        byte[] totalBytes = new byte[ totalLength ];
        System.arraycopy( responseBytes, 0, totalBytes, 0, responseBytes.length );
        System.arraycopy( bodyBytes, 0, totalBytes, responseBytes.length, bodyBytes.length );
        send( totalBytes );
    }

    public void sendResponse( byte[] body ) {
        byte[] responseByes = buildResponse();
        int totalLength = responseByes.length + body.length;
        byte[] totalBytes = new byte[ totalLength ];
        System.arraycopy( responseByes, 0, totalBytes, 0, responseByes.length );
        System.arraycopy( body, 0, totalBytes, responseByes.length, body.length );
        send( totalBytes );
    }

    public void terminate() {
        try {
            outputStream.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private byte[] buildResponse() {
        StringBuilder builder = new StringBuilder();
        builder.append( httpVersion )
               .append( HttpConstants.REQUEST_LINE_SEPARATOR )
               .append( statusCode )
               .append( HttpConstants.CRLF );
        if ( !headers.isEmpty() ) {
            for ( String header : headers.keySet() ) {
                builder.append( header )
                       .append( HttpConstants.HEADER_SEPARATOR )
                       .append( HttpConstants.REQUEST_LINE_SEPARATOR )
                       .append( headers.get( header ) )
                       .append( HttpConstants.CRLF );
            }
        }
        builder.append( HttpConstants.CRLF );
        return builder.toString().getBytes( StandardCharsets.UTF_8 );
    }

    private void send( byte[] response ) {
        try {
            outputStream.write( response );
            outputStream.flush();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    /*----- Getters/Setters -----*/

    public void setStatusCode( String statusCode ) {
        this.statusCode = statusCode;
    }
}
