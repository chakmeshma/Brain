package net.chakmeshma.brutengine.development;

import java.util.regex.Pattern;

/**
 * Created by chakmeshma on 03.05.2017.
 */
//TODO implement
public class TesterCommandString{
    private static Pattern testConstructionLinePattern;

    static {
        testConstructionLinePattern = Pattern.compile("\\A\\s*[a-zA-Z]+\\s+[a-zA-Z0-9]+\\s[a-zA-Z0-9]*\\s*\\z");
    }

    //TODO implement
    public TesterCommandString(String s) {

    }

    //TODO maybe implement
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
