public class HttpRequest {

    private String httpMethod;
    private String requestTarget;
    private String httpVersion;

    private HttpRequest() {}

    public static HttpRequest parseRequest( final String request ) {
        HttpRequest httpRequest = new HttpRequest();
        //int back = 0;
        //int front = request.indexOf( HttpConstants.CRLF );
        //httpRequest.handleRequestLine( request.substring( back, front ) );
        httpRequest.handleRequestLine( request );
        return httpRequest;
    }

    private void handleRequestLine( String requestLine ) {
        System.out.printf( "Handling Request Line: %s", requestLine );
        String[] tokens = requestLine.split( HttpConstants.REQUEST_LINE_SEPARATOR );
        httpMethod = tokens[0];
        requestTarget = tokens[1];
        httpVersion = tokens[2];
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
}
