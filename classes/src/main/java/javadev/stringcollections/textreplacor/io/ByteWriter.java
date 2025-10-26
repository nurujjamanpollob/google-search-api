package javadev.stringcollections.textreplacor.io;



import java.io.*;
import java.nio.file.FileSystems;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * Class to write a list of bytes to a file or output stream
 * <p>
 * @author nurujjamanpollob
 * @since 0.0.1
 */
public class ByteWriter {

    /**
     * Suppress default constructor for noninstantiability
     */
    private ByteWriter() {
        throw new AssertionError("Cannot instantiate ByteWriter class with default constructor");
    }

    /**
     * Write a list of bytes to a file
     * @param bytes list of bytes to write
     * @param path path to write the bytes
     * @throws IOException if an I/O error occurs
     */
    public static void writeToPath(byte[] bytes, String path) throws IOException {

        String updatedPath = convertAndCleanPaths(path);

        // Resolve a path if not exists
        PathResolver.resolvePathIfNotExists(updatedPath);

            FileOutputStream fileOutputStream = new FileOutputStream(updatedPath);
            fileOutputStream.write(bytes);
            fileOutputStream.close();


            // checks if the file is created
        if (!PathResolver.isPathExists(path)) {

            throw new IOException("It seems, strange things are happening, file not available at: " + updatedPath);
        }


    }

    /**
     * Write a list of {@link X509Certificate} to a file
     */
    public static void writeToPath(Certificate[] certificates, String path) throws IOException, CertificateEncodingException {
        // Resolve path if not exists
        PathResolver.resolvePathIfNotExists(convertAndCleanPaths(path));

        FileOutputStream fileOutputStream = new FileOutputStream(convertAndCleanPaths(path));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

        for (Certificate certificate : certificates) {
            bufferedOutputStream.write(certificate.getEncoded());
        }

        bufferedOutputStream.close();
        fileOutputStream.close();

        // final check if the file is created
        if (!PathResolver.isPathExists(path)) {
            throw new IOException("It seems, strange things are happening, file not available at: " + path);
        }
    }

    /**
     * This method used to write certificates to a output stream
     * @param certificates certificates to write
     */
    public static void writeToOutputStream(Certificate[] certificates, OutputStream bufferedOutputStream) throws IOException, CertificateEncodingException {

        for (Certificate certificate : certificates) {
            bufferedOutputStream.write(certificate.getEncoded());
        }
    }

    /**
     * Used to write a list of bytes as String to a file
     * @param content content to write
     * @param path path to write the bytes
     * @throws IOException if an I/O error occurs
     */
    public static void writeToPath(String content, String path) throws IOException {

        File f = new File(convertAndCleanPaths(path));
        // Resolve path if not exists
        PathResolver.resolvePathIfNotExists(f.getAbsolutePath());
        // Write as String
        PrintWriter printWriter = new PrintWriter(f);
        printWriter.write(content);

        printWriter.close();
    }

    /**
     * Converts path separators to the operating system's path separator, like in windows, it will convert / to \
     * Also, it will clean the path, to avoid any unwanted path separator at the end of the path
     * @param path path to convert
     */
    public static String convertAndCleanPaths(String path) {

        // As some path may mix both / and \, so we need to convert them to the operating system's path separator,
        // We need to filter both / and \, and replace them with the operating system's path separator
        String osPathSeparator = FileSystems.getDefault().getSeparator();

        return PathResolver.cleanPath(path.replaceAll("[\\/\\\\]", "\\" + osPathSeparator));
    }



}
