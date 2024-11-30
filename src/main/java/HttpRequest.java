import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private String httpMethod;
    private String requestTarget;
    private String httpVersion;
    private Map<String, String> headers;

    private HttpRequest() {
        headers = new HashMap<>();
    }

    public static HttpRequest parseRequest( final String request ) {
        HttpRequest httpRequest = new HttpRequest();
        String[] tokens = request.split( HttpConstants.CRLF );
        httpRequest.handleRequestLine( tokens[0] );
        httpRequest.handleHeaders( tokens );
        return httpRequest;
    }

    private void handleRequestLine( String requestLine ) {
        String[] tokens = requestLine.split( HttpConstants.REQUEST_LINE_SEPARATOR );
        httpMethod = tokens[0];
        requestTarget = tokens[1];
        httpVersion = tokens[2];
    }

    private void handleHeaders( String[] headers ) {
        for ( int i = 1; i < headers.length; i++ ) {
            String header = headers[i];
            if ( header.isEmpty() ) {
                return;
            }
            String[] keyValuePair = header.split( HttpConstants.HEADER_SEPARATOR );
            this.headers.put( keyValuePair[0], keyValuePair[1].strip() );
        }
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "httpMethod='" + httpMethod + '\'' +
                ", requestTarget='" + requestTarget + '\'' +
                ", httpVersion='" + httpVersion + '\'' +
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
}
