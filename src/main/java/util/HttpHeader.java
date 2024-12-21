package util;

public enum HttpHeader {
    HOST( "Host" ),
    USER_AGENT( "User-Agent" ),
    ACCEPT( "Accept" ),
    CONTENT_TYPE( "Content-Type" ),
    CONTENT_LENGTH( "Content-Length" ),
    CONTENT_ENCODING( "Content-Encoding" ),
    ACCEPT_ENCODING( "Accept_Encoding" );

    private final String field;

    HttpHeader( String field ) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public static HttpHeader getHeader( String header ) {
        HttpHeader httpHeader = null;
        switch ( header ) {
            case "Host" -> httpHeader = HttpHeader.HOST;
            case "User-Agent" -> httpHeader = HttpHeader.USER_AGENT;
            case "Accept" -> httpHeader = HttpHeader.ACCEPT;
            case "Content-Type" -> httpHeader = HttpHeader.CONTENT_TYPE;
            case "Content-Length" -> httpHeader = HttpHeader.CONTENT_LENGTH;
            case "Content-Encoding" -> httpHeader = HttpHeader.CONTENT_ENCODING;
            case "Accept-Encoding" -> httpHeader = HttpHeader.ACCEPT_ENCODING;
        }
        return httpHeader;
    }
}
