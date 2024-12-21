package compression;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZip {

    public static void main( String[] args ) {
        byte[] bArray = compress( "abc" );
        String s = decompress( bArray );
        System.out.println(s);
    }

    public static byte[] compress( final String data ) {
        if ( data == null || data.isEmpty() ) {
            return new byte[] {};
        }

        try (
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipStream = new GZIPOutputStream( byteStream )
        ) {
            gzipStream.write( data.getBytes( StandardCharsets.UTF_8 ) );
            gzipStream.finish();
            return byteStream.toByteArray();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public static String decompress( final byte[] compressed ) {
        StringBuilder decompressed = new StringBuilder();
        if ( compressed == null || compressed.length == 0 ) {
            return "";
        }

        if ( isCompressed( compressed ) ) {
            try (
                    GZIPInputStream gzipStream = new GZIPInputStream( new ByteArrayInputStream( compressed ) );
                    BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( gzipStream, StandardCharsets.UTF_8 ) )
            ) {
                String line;
                while ( ( line = bufferedReader.readLine() ) != null ) {
                    decompressed.append( line );
                }
            } catch ( IOException e ) {
                throw new RuntimeException( e );
            }
        } else {
            decompressed.append( Arrays.toString( compressed ) );
        }
        return decompressed.toString();
    }

    public static boolean isCompressed( final byte[] compressed ) {
        return ( compressed[0] == (byte) GZIPInputStream.GZIP_MAGIC ) && ( compressed[1] == (byte) ( GZIPInputStream.GZIP_MAGIC >> 8 ) );
    }
}
