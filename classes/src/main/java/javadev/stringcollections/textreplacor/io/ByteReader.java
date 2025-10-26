package javadev.stringcollections.textreplacor.io;

import java.io.IOException;
import java.io.Reader;

public class ByteReader {

    /**
     * Get a reader instance from a byte array
     */
    public static Reader getReader(byte[] bytes) throws IOException {
        return new java.io.InputStreamReader(new java.io.ByteArrayInputStream(bytes));
    }

}
