package net.chakmeshma.brutengine.development;

/**
 * Created by chakmeshma on 03.05.2017.
 */

final class _RAMTester extends TesterModule {
    private static final int _fillSpan = 1024 * 1024 * 1000;

    void fillMemory() {
        byte[] filler = new byte[_fillSpan];
    }


    //TODO implement
    @Override
    void doInifinity() {

    }

    //TODO implement
    @Override
    TesterResult test(TesterCommandString command) {
        return null;
    }
}
