package net.chakmeshma.brutengine.development.exceptions;

import android.opengl.GLException;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by chakmeshma on 25.04.2017.
 */

public class GLCustomException extends GLException {
    private static final int leftPadding = 5;
    private String _stringMessage = null;
    private int _errorCode = -1;

    public GLCustomException(int error) {
        super(error);

        _errorCode = error;
    }

    public GLCustomException(int error, String string) {
        super(error);

        _errorCode = error;

        _stringMessage = string;
    }

    @Override
    public String getMessage() {
        String _superMessage = super.getMessage();

        int maxSuperMessageLineLength = 0;
        int maxStringMessageLineLength = 0;
        Scanner scanner = new Scanner(_superMessage);
        int lineCounter = 0;
        while (scanner.hasNextLine()) {
            String line;
            if (lineCounter == 0)
                line = scanner.nextLine() + String.format(" (%d)", _errorCode);
            else
                line = scanner.nextLine();

            int currentSuperMessageLineLength = line.length();

            if (currentSuperMessageLineLength > maxSuperMessageLineLength)
                maxSuperMessageLineLength = currentSuperMessageLineLength;

            lineCounter++;
        }
        scanner.close();

        if (_stringMessage != null) {
            scanner = new Scanner(_stringMessage);
            while (scanner.hasNextLine()) {
                String line;
                line = scanner.nextLine();

                if (line.length() > maxStringMessageLineLength)
                    maxStringMessageLineLength = line.length();
            }
        }

        int n = maxSuperMessageLineLength + leftPadding + 6;
        char[] chars = new char[n];
        Arrays.fill(chars, '/');
        String fullSlashesLine = new String(chars);

        n = leftPadding;
        chars = new char[n];
        Arrays.fill(chars, '/');
        String slashesLine = new String(chars);

        String formatted = "\n";
        formatted += fullSlashesLine + "\n";
        formatted += fullSlashesLine + "\n";
//        formatted += slashesLine + "\n";
        formatted += slashesLine + "\n";
        scanner = new Scanner(_superMessage);
        lineCounter = 0;
        while (scanner.hasNextLine()) {
            String line;
            if (lineCounter == 0)
                line = scanner.nextLine() + String.format(" (%d)", _errorCode);
            else
                line = scanner.nextLine();

            formatted += slashesLine + "   " + line + "\n";

            lineCounter++;
        }
        scanner.close();
//        formatted += slashesLine + "\n";
        formatted += slashesLine + "\n";
        formatted += fullSlashesLine + "\n";
        if (_stringMessage == null)
            formatted += fullSlashesLine + "\n";

        if (_stringMessage != null) {
            n = maxStringMessageLineLength + leftPadding + 6;
            chars = new char[n];
            Arrays.fill(chars, '/');
            fullSlashesLine = new String(chars);


//            formatted += fullSlashesLine + "\n";
            formatted += fullSlashesLine + "\n";
            formatted += slashesLine + "\n";
            scanner = new Scanner(_stringMessage);
            while (scanner.hasNextLine()) {
                String line;
                line = scanner.nextLine();

                formatted += slashesLine + "   " + line + "\n";

                lineCounter++;
            }
            scanner.close();
            formatted += slashesLine + "\n";
            formatted += fullSlashesLine + "\n";
            formatted += fullSlashesLine + "\n\n";
        }


        return formatted;
    }
}
