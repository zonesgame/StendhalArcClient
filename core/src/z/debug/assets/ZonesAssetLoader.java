package z.debug.assets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import arc.util.io.Streams;

/**
 *
 */

public abstract class ZonesAssetLoader {


    public static byte[] getZonesAssets (Coding encoder, byte[] bytes) {
        String encodeName =  encoder.name();
        for (int i = 0, len = encodeName.length(); i < len; i++) {
            char c = encodeName.charAt(i);
            if (c == 'Z')
                bytes = getGZip(bytes);
            else if (c == 'B')
                bytes = getDecode(bytes);
        }

        return bytes;
    }

    private static byte[] getDecode (byte[] bytes) {
        return Base128.getDecoder().decode(bytes);
    }

    private static byte[] getGZip (byte[] bytes) {
        InputStream inputStream = null;
        try {
            inputStream = new GZIPInputStream(new ByteArrayInputStream(bytes), bytes.length);
            bytes = Streams.copyBytes(inputStream, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Streams.close(inputStream);
        }
        return bytes;
    }

}
