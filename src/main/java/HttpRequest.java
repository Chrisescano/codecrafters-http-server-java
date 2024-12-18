import java.io.BufferedReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private String httpMethod;
    private String requestTarget;
    private String httpVersion;
    private String body;
    private final BufferedReader reader;
    private final Map<HttpHeader, String> headers;

    private final static int END_OF_STREAM = -1;

    public HttpRequest( BufferedReader reader ) {
        this.reader = reader;
        headers = new HashMap<>();
    }

    public void parseRequest() {
        try {
            httpMethod = readUntil( reader, HttpConstants.REQUEST_LINE_SEPARATOR );
            requestTarget = readUntil( reader, HttpConstants.REQUEST_LINE_SEPARATOR );
            httpVersion = readUntil( reader, HttpConstants.CRLF );

            while ( true ) {
                Map.Entry<HttpHeader, String> header = readHeader( reader );
                if ( header == null ) {
                    break; //end of header reached
                } else {
                    headers.put( header.getKey(), header.getValue() );
                }
            }

            if ( headers.containsKey( HttpHeader.CONTENT_LENGTH ) ) {
                int bodyLength = Integer.parseInt( headers.get( HttpHeader.CONTENT_LENGTH ) );
                char[] charBuff = new char[ bodyLength ];
                int result = reader.read( charBuff, 0, bodyLength );
                if ( result == END_OF_STREAM ) {
                    throw new RuntimeException( "End of Stream Reached" );
                }
                body = new String( charBuff );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private String readUntil( BufferedReader reader, String delimiter ) throws IOException {
        StringBuilder builder = new StringBuilder();
        StringBuilder stack = new StringBuilder();
        char[] delimiterTokens = delimiter.toCharArray();
        for ( int i = 0; i < delimiterTokens.length; i++ ) {
            int charRead = reader.read();
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

    private Map.Entry<HttpHeader, String> readHeader( BufferedReader reader ) throws IOException {
        String header = readUntil( reader, HttpConstants.CRLF );
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
        return new AbstractMap.SimpleImmutableEntry<>( HttpHeader.getHeader( field ), value.trim() );
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

    public Map<HttpHeader, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
