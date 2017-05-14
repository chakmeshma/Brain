package net.chakmeshma.brutengine.development;

/**
 * Created by chakmeshma on 03.05.2017.
 */

final class _CPUTester extends TesterModule {
    private static final int _timeSpan = 100000;

    static void occupyProcessor() {
        try {
            Thread.sleep(1000 * _timeSpan);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //TODO implement
    @Override
    protected void doInifinity() {

    }

    //TODO implement
    @Override
    TesterResult test(TesterCommandString command) {
        return null;
    }
}
