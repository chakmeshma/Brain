package net.chakmeshma.brain;

/**
 * Created by chakmeshma on 21.05.2017.
 */

interface Threadable {
    void initThread(boolean lock);

    String getThreadName();
}
