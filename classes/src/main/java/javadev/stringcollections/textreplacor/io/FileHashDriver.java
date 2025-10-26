package javadev.stringcollections.textreplacor.io;



import javadev.stringcollections.textreplacor.console.ColoredConsoleOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author nurujjamanpollob
 * @version 1.0
 * @apiNote This class is intended to generate and validate SHA hash.
 * @since 1.0
 */
public class FileHashDriver {

    /**
     * Method to generate SHA hash. The algorithm is used is MD5.
     * May return null if the md5 algorithm is not found in the security provider.
     * @param filePath file path to generate SHA hash
     */
    public static @Nullable String generateMd5SHA(String filePath) {

        // let's generate SHA hash
        try {
            return generateHash(filePath, "MD5");
        } catch (NoSuchAlgorithmException e) {
            ColoredConsoleOutput.printRedText(e.toString());

            return null;
        }
    }

    /**\
     * Method to generate SHA hash. The algorithm is used is SHA-256.
     * May return null if the md5 algorithm is not found in the security provider.
     * @param filePath file path to generate SHA hash
     */
    public static @Nullable String generateSHA256(String filePath) {
        // let's generate SHA hash
        try {
            return generateHash(filePath, "SHA-256");
        } catch (NoSuchAlgorithmException e) {
            ColoredConsoleOutput.printRedText(e.toString());

            return null;
        }
    }

    protected static @NotNull String generateHash(String filePath, String algorithm) throws NoSuchAlgorithmException {
        File f = new File(PathResolver.convertAndCleanPaths(filePath));
        byte[] bytes = new byte[4096];
        MessageDigest md = MessageDigest.getInstance(algorithm);
        try (FileInputStream fis = new FileInputStream(f)) {
            while (true) {
                int len = fis.read(bytes);
                if (len == -1) {
                    break;
                }
                md.update(bytes, 0, len);
            }
        } catch (IOException e) {
            throw new NoSuchAlgorithmException("Error generating MD5 for: " + e.getMessage());
        }
        byte[] hashed_bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte hashedByte : hashed_bytes) {
            sb.append(String.format("%02x", hashedByte));
        }
        return sb.toString();
    }
}
