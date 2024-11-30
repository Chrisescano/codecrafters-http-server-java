public class HttpRequest {

    private String httpMethod;
    private String requestTarget;
    private String httpVersion;

    private HttpRequest() {}

    public static HttpRequest parseRequest( final String request ) {
        HttpRequest httpRequest = new HttpRequest();
        String[] tokens = request.split( HttpConstants.REQUEST_LINE_SEPARATOR );

        httpRequest.httpMethod = tokens[0];
        httpRequest.requestTarget = tokens[1];
        httpRequest.httpVersion = tokens[2];

        return httpRequest;
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
}
