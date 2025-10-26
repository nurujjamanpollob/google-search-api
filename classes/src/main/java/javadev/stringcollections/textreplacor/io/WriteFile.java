package javadev.stringcollections.textreplacor.io;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A utility class for writing data to files.
 */
public class WriteFile {

    /**
     * Writes a byte array to a file. If the file does not exist, it will be created.
     * If the file exists, its content will be overwritten.
     *
     * @param path The path of the file to write to.
     * @param data The byte array to write to the file.
     * @throws IOException If the path or data is null, or if an I/O error occurs during writing.
     */
    public static void write(String path, byte[] data) throws IOException {
        if (path == null) {
            throw new IOException("Path cannot be null.");
        }
        if (data == null) {
            // To be consistent with FileOutputStream, which allows writing a 0-length byte array.
            // Or we could throw new IOException("Data cannot be null.");
            data = new byte[0];
        }

        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(data);
        }
    }

    /**
     * Writes a String to a file using the platform's default charset.
     * If the file does not exist, it will be created. If the file exists, it will be overwritten.
     *
     * @param path    The path of the file to write to.
     * @param content The String content to write to the file.
     * @throws IOException If the path or content is null, or if an I/O error occurs.
     */
    public static void write(String path, String content) throws IOException {
        if (content == null) {
            throw new IOException("Content cannot be null.");
        }
        write(path, content.getBytes());
    }

    /**
     * Writes multiple byte arrays to their corresponding files.
     *
     * @param paths The array of file paths to write to.
     * @param data  The two-dimensional byte array containing the data for each file.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException if the lengths of the paths and data arrays do not match.
     */
    public static void write(String[] paths, byte[][] data) throws IOException {
        if (paths == null || data == null) {
            throw new IllegalArgumentException("Paths or data cannot be null.");
        }

        if (paths.length != data.length) {
            throw new IllegalArgumentException("The number of paths must match the number of data arrays.");
        }

        for (int i = 0; i < paths.length; i++) {
            write(paths[i], data[i]);
        }
    }

    /**
     * Copy a file from source path to destination path, its usages buffered stream for better performance.
     * It also automatically creates the destination path if not exists.
     * @param sourcePath source path
     * @param destinationPath destination path
     * @throws IOException if an I/O error occurs
     */
    public static void copyFile(String sourcePath, String destinationPath) throws IOException {
        // read with buffered stream and write with buffered stream
        java.io.FileInputStream fileInputStream = new java.io.FileInputStream(sourcePath);
        java.io.FileOutputStream fileOutputStream = new java.io.FileOutputStream(destinationPath);
        byte[] buffer = new byte[1024];
        int length;
        // resolve the path if not exists
        PathResolver.resolvePathIfNotExists(destinationPath);
        while ((length = fileInputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, length);
        }

        fileInputStream.close();
        fileOutputStream.close();
    }
}
