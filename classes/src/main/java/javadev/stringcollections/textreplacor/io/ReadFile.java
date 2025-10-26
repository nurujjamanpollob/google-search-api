package javadev.stringcollections.textreplacor.io;



import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Class that read file from a path
 */
public class ReadFile {

    /**
     * Read file from a path
     * @param path path to read file
     * @return byte array of file
     * @throws IOException if file isn't found
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static byte[] read(String path) throws IOException {

        if (path == null) {
            throw new IOException("Path is null");
        }

        java.io.File file = new java.io.File(path);

        if (!file.exists()) {
            throw new IOException(String.format("File not found at: %s", path ));
        }

        java.io.FileInputStream fileInputStream = new java.io.FileInputStream(file);

        byte[] bytes = new byte[(int) file.length()];

        fileInputStream.read(bytes);

        fileInputStream.close();

        return bytes;

    }

    /**
     * Read a file from a path and return the bytes as a string
     */
    public static String readAsString(String path) throws IOException {
        return new String(read(path));
    }

    /**
     * This method reads multiple files and return a two-dimensional byte array
     * @param paths paths to read files from
     * @return two-dimensional byte arrays, containing all files
     */
    public static byte[][] read(String[] paths) throws IOException {
        byte[][] bytes = new byte[paths.length][];

        for (int i = 0; i < paths.length; i++) {
                bytes[i] = read(paths[i]);

        }

        return bytes;
    }

    /**
     * Reads a file and return an input stream
     * @param filePath file path
     * @return input stream
     */
    public static InputStream getInputStream(String filePath) {

        if (filePath == null) {
            return null;
        }

        java.io.File file = new java.io.File(filePath);

        if (!file.exists()) {
            return null;
        }

        try {
            return new java.io.FileInputStream(file);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * read file from a jar resource path, and return byte array of file
     * @param path path to read file, the path must be in jar resource.
     *             for example, if a file named <b>{jar}/resources/win/lib/file.txt</b>,
     *             you should pass <b>/win/lib/file.txt</b> as parameter.
     * @return byte array of file
     */
    public static byte[] readFromJarResource(String path) throws IOException {

        if (path == null) {
            throw new IOException("Path is null");
        }

        String jarPath = getAbsoluteResourcePath(path);

        // test if the path is exists
        if(PathResolver.isPathExists(jarPath)) {
            return read(jarPath);
        } else {
            // if the path does not exist, try to read the file from the resources directory
            String filename = "BOOT-INF/classes/" + path;
            // Get the input stream from the classloader
            InputStream inputStream = ReadFile.class.getResourceAsStream("/" + filename);

            if (inputStream == null) {
                throw new IOException("File not found: " + filename);
            }

            // Read the file contents and store it in a byte array
            byte[] bytes = new byte[inputStream.available()];


            // Read the file contents and store it in a byte array
            int bytesRead = inputStream.read(bytes);

            // if the read is successful, return the byte array
            inputStream.close();
            if (bytesRead > 0) {
                return bytes;
            } else {
                throw new IOException("Cannot read file from jar resource: " + path);
            }
        }


    }

    /**
     * This method returns base resource path of a jar file
     * @return base resource path of a jar file
     * @throws IOException if it fails to get base resource path
     */
    public static String getBaseResourcePath() throws IOException {

        URL absoluteURL = Thread.currentThread().getContextClassLoader().getResource("");

        if (absoluteURL == null) {
            throw new IOException("Cannot get base resource path");
        }

        return absoluteURL.getPath();
    }

    /**
     * This method returns a absolute path of resources directory, following the path of a file in resources directory
     * @param path path of a file in resources directory, for example, if a file named <b>{jar}/resources/win/lib/file.txt</b>,
     *             you should pass <b>/win/lib/file.txt</b> as parameter.
     * @return absolute path of resources directory
     */
    public static String getAbsoluteResourcePath(String path) throws IOException {

        if (path == null) {
            throw new IOException("Path is null");
        }

        // Get current running file absolute path, suppose, I am running the jar file from, C:\Users\Nuru\Desktop\myapp.jar, so I expect the absolute path to be C:\Users\Nuru\Desktop\myapp.jar
        URL absoluteURL = Thread.currentThread().getContextClassLoader().getResource(path);

        if (absoluteURL == null) {
            throw new IOException("File not found, at this location: " + path);
        }

        return absoluteURL.getPath();
    }
}
