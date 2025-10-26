package javadev.stringcollections.textreplacor.generator;

/**
 * Class that contains method to generate randoms
 * @since 1.0
 * @version 1.0
 * @author nurujjamanpollob
 */
public class RandomGenerator {


    /**
     * Method that generates a random String in a given length with alphabets, numbers and special characters.
     * @param length length of the random string
     * @return random string
     */
    public static String generateRandomString(int length) {

        // create a string builder
        StringBuilder stringBuilder = new StringBuilder();

        // create a random string
        String randomString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz"
                + "1234567890"
                + "!@#$%^&*()_+"
                + "[]{};':\",.<>/?"
                + "\\|`~";

        // create a random object
        java.util.Random random = new java.util.Random();

        // loop through the length
        for (int i = 0; i < length; i++) {

            // append a random character
            stringBuilder.append(randomString.charAt(random.nextInt(randomString.length())));
        }

        // return the random string
        return stringBuilder.toString();
    }

    /**
     * Random string generator which only contains alphabets and numbers
     * @param length length of the random string
     *               @return random string
     */
    public static String generateRandomStringOnlyAlphabetsAndNumbers(int length) {

        // create a string builder
        StringBuilder stringBuilder = new StringBuilder();

        // create a random string
        String randomString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz"
                + "1234567890";

        // create a random object
        java.util.Random random = new java.util.Random();

        // loop through the length
        for (int i = 0; i < length; i++) {

            // append a random character
            stringBuilder.append(randomString.charAt(random.nextInt(randomString.length())));
        }

        // return the random string
        return stringBuilder.toString();
    }
}
