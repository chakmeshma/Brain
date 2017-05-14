package net.chakmeshma.brutengine.development.exceptions;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.opengl.GLES20.glGetError;

/**
 * Created by chakmeshma on 24.04.2017.
 */

public final class GLCustomShaderException extends GLCustomException {
    private static final short leftPadding = 7;
    private static final short topPadding = 2;
    private static final short rightPadding = 1;
    private static final short bottomPadding = 2;
    private static Pattern shaderErrorLocationPattern;

    static {
        shaderErrorLocationPattern = Pattern.compile(".*ERROR:\\s*(\\d+):(\\d+).*", Pattern.DOTALL);
    }

    private String _shaderCode = null;
    private int shaderCodeErrorColumn = -1;
    private int shaderCodeErrorLine = -1;

    public GLCustomShaderException(String string) {
        super(glGetError(), string);
    }

    public GLCustomShaderException(String string, String shaderCode) {
        super(glGetError(), string);

        this._shaderCode = shaderCode;

        Matcher shaderErrorLocationMatcher = shaderErrorLocationPattern.matcher(string);
        if (shaderErrorLocationMatcher.matches()) {
            shaderCodeErrorColumn = Integer.valueOf(shaderErrorLocationMatcher.group(1), 10);
            shaderCodeErrorLine = Integer.valueOf(shaderErrorLocationMatcher.group(2), 10);
        }
    }

    private static String fillChar(int n, char c) {
        char[] chars = new char[n];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    //format to embed shader code inside exception message (making exception message more readable via slash-indention)
    private String getFormattedMessage() {
        String message = super.getMessage();
        String formatted = message;


        if (_shaderCode != null) {
            int longestLineLength = 0;
            int longestMessageLineLength = 0;

            Scanner messageLengthScanner = new Scanner(message);
            Scanner lengthScanner = new Scanner(_shaderCode);
            Scanner scanner = new Scanner(_shaderCode);

            while (messageLengthScanner.hasNextLine()) {
                String line = messageLengthScanner.nextLine();

                if (line.length() > longestMessageLineLength) {
                    longestMessageLineLength = line.length();
                }
            }
            messageLengthScanner.close();

            while (lengthScanner.hasNextLine()) {
                String line = lengthScanner.nextLine();

                if (line.length() > longestLineLength) {
                    longestLineLength = line.length();
                }
            }
            lengthScanner.close();

            short lineCounter = 0;

            String leftSlashes = fillChar(leftPadding, '/');
            String leftXs = fillChar(leftPadding, 'X');

//        String fullSlashLine = fillChar(longestMessageLineLength, 'G');
            String completeSlashLine = fillChar(leftPadding + longestLineLength + rightPadding + 6, '/');


            for (short __zx = 0; __zx < topPadding; __zx++)
                formatted += completeSlashLine + "\n";
            formatted += leftSlashes + "\n";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (lineCounter + 1 == shaderCodeErrorLine) {
                    formatted += leftXs + ">  " + line + "\n";
                } else
                    formatted += leftSlashes + "   " + line + "\n";

                lineCounter++;
            }
            scanner.close();
            formatted += leftSlashes + "\n";
            for (short __zx = 0; __zx < bottomPadding; __zx++)
                formatted += completeSlashLine + "\n";
        }

        return formatted;
    }

    @Override
    public String getMessage() {
        return getFormattedMessage();
    }
}
