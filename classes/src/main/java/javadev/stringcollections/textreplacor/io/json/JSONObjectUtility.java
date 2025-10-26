package javadev.stringcollections.textreplacor.io.json;

import org.jetbrains.annotations.Nullable;

/**
 * @author nurujjamanpollob
 * @version 1.0
 * @apiNote This class is intended to provide utility methods for working with JSON objects. Such as, escaping special characters, to that can be added to a JSON object, etc.
 */
public class JSONObjectUtility {

    /**
     * Escapes special characters in a string to make it safe for JSON. Support full Unicode characters.
     * @param input the string to escape
     * @return the escaped string
     */
    public static @Nullable String escapeSpecialCharacters(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Formats a JSON string to be more readable, by adding indentation and new lines.
     * @param json the JSON string to format
     * @return formatted JSON string
     */
    public static String formatJson(String json) {
        if (json == null || json.isEmpty()) return json;
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inQuotes = false;
        boolean escape = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && !escape) {
                escape = true;
                sb.append(c);
                continue;
            }
            if (c == '"' && !escape) {
                inQuotes = !inQuotes;
            }
            escape = false;
            if (!inQuotes) {
                switch (c) {
                    case '{':
                    case '[':
                        sb.append(c).append('\n');
                        indent++;
                        appendIndent(sb, indent);
                        break;
                    case '}':
                    case ']':
                        sb.append('\n');
                        indent--;
                        appendIndent(sb, indent);
                        sb.append(c);
                        break;
                    case ',':
                        sb.append(c).append('\n');
                        appendIndent(sb, indent);
                        break;
                    case ':':
                        sb.append(c).append(' ');
                        break;
                    default:
                        if (!Character.isWhitespace(c)) {
                            sb.append(c);
                        }
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("    "); // 4 spaces per indent
        }
    }
}
