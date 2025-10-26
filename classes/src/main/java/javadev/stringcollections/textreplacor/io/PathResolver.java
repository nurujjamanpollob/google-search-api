package javadev.stringcollections.textreplacor.io;

import java.io.File;
import java.io.IOException;

/**
 * @author nurujjamanpollob
 * Class that resolve path and support operations on a path
 */
public class PathResolver {


    /**
     * This method resolves a path if not exists. If a path does not exist, then it will create the path.
     * @param path path to resolve
     * @throws IOException if unable to create directory
     */
    public static void resolvePathIfNotExists(String path) throws IOException {


        if (path == null) {
            throw new IOException(
                    """
                            Unable to resolve path. the path is null.\s
                            Suggestion: pass a fully qualified absolute path to resolve this issue.\s
                            If you want to create the path under current directory, from where you are running your application,\s
                            use System.getProperty("user.dir") + "your path here\""""
            );
        }
        String pathToResolve = getPathFromFilePath(path);

        File file = new File(pathToResolve);

        if (!file.exists()) {
           boolean isSuccess = file.mkdirs();


           // test if the directory is created successfully

           if (!isSuccess && !isPathExists(pathToResolve)) {
               throw new IOException("Unable to create directory. Here is the path: " + pathToResolve);
           }
        }

    }


    private static String getPathFromFilePath(String filePath) throws IOException {

        // convert and clean the path
        filePath = convertAndCleanPaths(filePath);


        if (filePath == null) {
            throw new IOException("File path is null");
        }

        // parse until we meet the last file separator either / or \
        int lastSeparatorIndex = findLastSeparatorIndex(filePath);

        return filePath.substring(0, lastSeparatorIndex);

    }

    private static int findLastSeparatorIndex(String filePath) {

        // is fil extension contains then process
        if (filePath.contains(".")) {
            // read reverse and when we meet either / or \, return the index
            for (int i = filePath.length() - 1; i >= 0; i--) {
                if (filePath.charAt(i) == '/' || filePath.charAt(i) == '\\') {
                    return i;
                }
            }
        }



        return filePath.length();
    }

    /**
     * This method extracts the file name from a file path. Please note that, this method return only file name, excluding file extension.
     *
     * @param filePath file path to extract file name
     *
     * @return file name. May return if file is not found or the file path is null
     */
    public static String getFileNameFromFilePathWithoutExtension(String filePath) {

        if (filePath == null) {
            return null;
        }

        File file = new File(filePath);

        if (file.isDirectory()) {
            return null;
        }

        return file.getName().split("\\.")[0];
    }

    /**
     * This method extracts the file name from a file path including a file extension.
     *
     * @param filePath file path to an extract file extension
     *
     * @return file extension. May return if file is not found or the file path is null
     */
    public static String getFileNameFromFilePathWithExtension(String filePath) {

        if (filePath == null) {
            return null;
        }

        File file = new File(filePath);

        if (file.isDirectory()) {
            return null;
        }

        return file.getName();
    }

    /**
     * This method extracts the file extension from a file path. Please note that, this method returns only a file extension, excluding file name.
     * @param filePath file path to an extract file extension
     */
    public static String getFileExtensionFromFilePathWithoutFileName(String filePath) {

        if (filePath == null) {
            return null;
        }

        File file = new File(filePath);

        if (file.isDirectory()) {
            return null;
        }

        return file.getName().split("\\.")[1];
    }

    /**
     * This method extracts a file path from a file path. Please note that, this method returns only a file path, excluding file name and file extension.
     */
    public static String getFilePathFromFilePathWithoutFileNameAndExtension(String filePath) {

        if (filePath == null) {
            return null;
        }

        File file = new File(filePath);

        if (file.isDirectory()) {
            return null;
        }

        return file.getParent();
    }

    /**
     * This method returns true if a path or file exists
     */
    public static boolean isPathExists(String path) {

        if (path == null) {
            return false;
        }

        File file = new File(path);

        return file.exists();
    }

    /**
     * This method tests if a path is writable.
     * If the file directory is passed, if then extracts the file directory and tests if the file directory is writable.
     * @param path path to test
     */
    public static boolean isPathWritable(String path) {

        if (!isPathExists(path)) {
            return false;
        }

        File file = new File(path);

        if (file.isDirectory()) {
            return file.canWrite();
        }

        return file.getParentFile().canWrite();
    }

    public static String getTemporaryDirectoryPath() {

        return System.getProperty("java.io.tmpdir");
    }

    /**
     * This method tests if a path is a file
     * @param inputFilePath path to test
     * @return true if the path is a file
     */
    public static boolean isFile(String inputFilePath) {

        return new File(inputFilePath).isFile();
    }

    /**
     * Method that clean an full absolute path, and removes all illegal characters from the path.
     * Allowed characters are: a-z, A-Z, 0-9, -, _, ., :, and / \ and space. Other characters will be removed.
     * @param path path to clean
     */
    public static String cleanPath(String path) {

        if (path == null) {
            return null;
        }

        char[] allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:-_.\\/ ".toCharArray();

        StringBuilder stringBuilder = new StringBuilder();

        for (char c : path.toCharArray()) {
            for (char a : allowed) {
                if (c == a) {
                    stringBuilder.append(c);
                    break;
                }
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Cleans and converts path separators to the operating system's path separator, like in windows, it will convert / to \
     */
    public static String convertAndCleanPaths(String path) {

        if (path == null) {
            return null;
        }

        return ByteWriter.convertAndCleanPaths(path);
    }

    /**
     * Get the absolute path of a file
     * @param filePath file path
     */
    public static String getAbsolutePath(String filePath) {

        if (filePath == null) {
            return null;
        }

        return new File(filePath).getAbsolutePath();
    }
}
