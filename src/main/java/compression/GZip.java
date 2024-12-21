package compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class GZip {

    public static byte[] compress( String data ) {
        if ( data == null || data.isEmpty() ) {
            return new byte[] {};
        }

        try (
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipStream = new GZIPOutputStream( byteStream )
        ) {
            gzipStream.write( data.getBytes( StandardCharsets.UTF_8 ) );
            gzipStream.flush();
            return byteStream.toByteArray();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    // might need decompress in future
}
