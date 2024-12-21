package util;

public enum HttpVersion {
    ONE_POINT_ONE( "HTTP/1.1" );

    private final String version;

    HttpVersion( String version ) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
