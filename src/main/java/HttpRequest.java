import util.HttpConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private String httpMethod;
    private String requestTarget;
    private String httpVersion;
    private String body;
    private final InputStream inputStream;
    private final Map<String, String> headers;

    private final static int END_OF_STREAM = -1;

    public HttpRequest( InputStream reader ) {
        this.inputStream = reader;
        headers = new HashMap<>();
    }

    public void parseRequest() {
        try {
            httpMethod = readUntil( HttpConstants.REQUEST_LINE_SEPARATOR );
            requestTarget = readUntil( HttpConstants.REQUEST_LINE_SEPARATOR );
            httpVersion = readUntil( HttpConstants.CRLF );

            while ( true ) {
                String[] header = readHeader();
                if ( header == null ) {
                    break; //end of header reached
                } else {
                    headers.put( header[0], header[1] );
                }
            }

            if ( headers.containsKey( HttpConstants.CONTENT_LENGTH ) ) {
                int bodyLength = Integer.parseInt( headers.get( HttpConstants.CONTENT_LENGTH ) );
                byte[] charBuff = new byte[ bodyLength ];
                int result = inputStream.read( charBuff, 0, bodyLength );
                if ( result == END_OF_STREAM ) {
                    throw new RuntimeException( "End of Stream Reached" );
                }
                body = new String( charBuff );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    /*----- Helper Methods -----*/

    private String readUntil( String delimiter ) throws IOException {
        StringBuilder builder = new StringBuilder();
        StringBuilder stack = new StringBuilder();
        char[] delimiterTokens = delimiter.toCharArray();
        for ( int i = 0; i < delimiterTokens.length; i++ ) {
            int charRead = inputStream.read();
            if ( charRead == END_OF_STREAM ) {
                return builder.toString();
            }

            if ( charRead == delimiterTokens[i] ) {
                stack.append( delimiterTokens[i] );
                if ( stack.toString().equals( delimiter ) ) {
                    return builder.toString();
                }
            } else {
                builder.append( stack ).append( (char) charRead );
                stack.delete( 0, stack.length() );
                i = -1;
            }
        }

        return builder.toString();
    }

    public void terminate() {
        try {
            inputStream.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private String[] readHeader() throws IOException {
        String header = readUntil( HttpConstants.CRLF );
        if ( header.isEmpty() ) {
            return null;
        }
        int colonIndex = header.indexOf( HttpConstants.HEADER_SEPARATOR );
        if ( colonIndex == END_OF_STREAM ) {
            return null;
        }
        String field = header.substring( 0, colonIndex );
        String value = header.substring( colonIndex + 1 );
        if ( field.isEmpty() || value.isEmpty() ) {
            return null;
        }
        return new String[] { field, value.trim() };
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "httpMethod='" + httpMethod + '\'' +
                ", requestTarget='" + requestTarget + '\'' +
                ", httpVersion='" + httpVersion + '\'' +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                '}';
    }

    /*----- Getters/Setters -----*/

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestTarget() {
        return requestTarget;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
