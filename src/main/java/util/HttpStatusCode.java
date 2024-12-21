package util;

public enum HttpStatusCode {
    OK( "200 OK" ),
    CREATED( "201 Created" ),
    NOT_FOUND( "404 Not Found" );

    private final String statusCode;

    HttpStatusCode( String statusCode ) {
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }
}
