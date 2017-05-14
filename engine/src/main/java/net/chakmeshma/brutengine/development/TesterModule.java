package net.chakmeshma.brutengine.development;

/**
 * Created by chakmeshma on 03.05.2017.
 */

abstract class TesterModule {
    abstract void doInifinity();

    //TODO implement
    @Override
    public boolean equals(Object obj) {
        return false;
    }

    abstract TesterResult test(TesterCommandString command);
}
