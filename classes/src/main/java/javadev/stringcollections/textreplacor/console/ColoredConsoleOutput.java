/*
 * Copyright (c) 2023 Nurujjaman Pollob, All Right Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javadev.stringcollections.textreplacor.console;

/**
 * @apiNote Class to print colored output in console, for example, green, red, yellow, etc.
 * @author nurujjamanpollob
 * @since 0.0.1
 * @version 0.0.1
 */
@SuppressWarnings("unused")
public class ColoredConsoleOutput {

    /**
     * Suppress default constructor for non-instantiability
     */
    private ColoredConsoleOutput() {
        throw new AssertionError("No instance creation is allowed");
    }

    /**
     * Print green-colored text in console
     * @param text Text to print
     */
    public static void printGreenText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[32m", text);
    }

    /**
     * Print red-colored text in console
     * @param text Text to print
     */
    public static void printRedText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[31m", text);
    }

    /**
     * Print yellow-colored text in console
     * @param text Text to print
     */
    public static void printYellowText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[33m", text);
    }

    /**
     * Print blue-colored text in console
     * @param text Text to print
     */
    public static void printBlueText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[34m", text);
    }

    /**
     * Print cyan-colored text in console
     * @param text Text to print
     */
    public static void printCyanText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[36m", text);

    }

    /**
     * Print purple-colored text in console
     * @param text Text to print
     */
    public static void printPurpleText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[35m", text);
    }

    /**
     * Print white-colored text in console
     * @param text Text to print
     */
    public static void printWhiteText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[37m", text);
    }

    /**
     * Print black-colored text in console
     * @param text Text to print
     */
    public static void printBlackText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[30m", text);
    }

    /**
     * Print bold-colored text in console
     * @param text Text to print
     */
    public static void printBoldText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[1m", text);
    }

    /**
     * Print underlined-colored text in console
     * @param text Text to print
     */
    public static void printUnderlinedText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[4m", text);
    }

    /**
     * Print reversed-colored text in console
     * @param text Text to print
     */
    public static void printReversedText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[7m", text);
    }

    /**
     * Print invisible-colored text in console
     * @param text Text to print
     */
    public static void printInvisibleText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[8m", text);
    }

    /**
     * Print strikethrough-colored text in console
     * @param text Text to print
     */
    public static void printStrikethroughText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[9m", text);
    }

    /**
     * Print framed-colored text in console
     * @param text Text to print
     */
    public static void printFramedText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[51m", text);
    }

    /**
     * Print encircled-colored text in console
     * @param text Text to print
     */
    public static void printEncircledText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[52m", text);
    }

    /**
     * Print overlined-colored text in console
     * @param text Text to print
     */
    public static void printOverlinedText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[53m", text);
    }

    /**
     * Print bold-colored text in console
     * @param text Text to print
     */
    public static void printBoldColoredText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[1m", text);
    }

    /**
     * Print underlined-colored text in console
     * @param text Text to print
     */
    public static void printUnderlinedColoredText(String text) {

        // Pass to print-colored text method
        printColoredText("\u001B[4m", text);
    }


    /**
     * Method to print ANSI colored text in console
     * @param color Color code
     * @param text Text to print
     * @see <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#Colors">ANSI escape code</a>
     */
    public static void printColoredText(String color, String text) {

        // split text by new line
        String[] lines = text.split("\n");

        for (String line : lines) {
            System.out.println(color + line + "\u001B[0m");
        }
    }

}