package net.chakmeshma.brutengine.development;

import android.util.Log;

import net.chakmeshma.brutengine.development.exceptions.GLCustomException;
import net.chakmeshma.brutengine.development.exceptions.InvalidStackOperationException;

import static android.opengl.GLES20.glGetError;

/**
 * Created by chakmeshma on 05.05.2017.
 */

public final class DebugUtilities {
    private static final String warningTag = "MY_WARNING_TAG";

    //region checkAssertGLError
    public static void checkAssertGLError() {
        _checkAssertGLError(null);
    }

    public static void checkAssertGLError(String message) {
        _checkAssertGLError(message);
    }

    private static void _checkAssertGLError(String _message) {
        boolean messageProvided = !(_message == null || _message.length() == 0);

        int errorCode = glGetError();

        if (errorCode != 0) {
            if (messageProvided)
                throw new GLCustomException(errorCode, _message);
            else
                throw new GLCustomException(errorCode);
        } else {
            GLCustomException dummyException;

            if (messageProvided)
                dummyException = new GLCustomException(errorCode, _message);
            else
                dummyException = new GLCustomException(errorCode);

            dummyException.getMessage();

            logWarning(dummyException);
        }
    }
    //endregion

    //region logWarning
    public static void logWarning(String warningMessage) {
        Log.w(warningTag, warningMessage);
    }

    public static void logWarning(Exception warningException) {
        Log.w(warningTag, warningException);
    }
    //endregion

    public final static class FramerateCapture {
        private static final Object timestampsStackLock = new Object();
        private static final Object threadRunningMonitor = new Object();
        private static final Object captureThreadRunMonitor = new Object();
        private static final int stackCapacity = 10240;
        private static long[] timestampsStack;
        private static int timestampsStackPointer = 0;
        private static Thread timestampCaptureThread;
        private static boolean threadRunning = false;
        private static long lastReportedTimestamp = 0L;

        private static void assertStackAllocated() {
            if (timestampsStack == null) {
                timestampsStack = new long[stackCapacity];
            }
        }

        public static long popTimestamp() throws InvalidStackOperationException {
            assertStackAllocated();

            long value;

            synchronized (timestampsStackLock) {
                if (timestampsStackPointer == 0)
                    throw new InvalidStackOperationException("empty stack!");

                value = timestampsStack[--timestampsStackPointer];
            }

            return value;
        }

        public static void clearTimestamps() {
            synchronized (timestampsStackLock) {
                timestampsStackPointer = 0;
            }
        }

        public static long[] popTimestampsAll(boolean timeRightOrder) throws InvalidStackOperationException {
            assertStackAllocated();

            long[] values;

            synchronized (timestampsStackLock) {
                if (timestampsStackPointer == 0)
                    throw new InvalidStackOperationException("empty stack!");

                values = new long[timestampsStackPointer];

                if (timeRightOrder) {
                    for (int i = values.length - 1; i >= 0; i--) {
                        values[i] = popTimestamp();
                    }
                } else {
                    for (int i = 0; i < values.length; i++) {
                        values[i] = popTimestamp();
                    }
                }
            }

            return values;
        }

        private static void assertCaptureThreadRunning() {
            if (timestampCaptureThread == null) {
                timestampCaptureThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        assertStackAllocated();

                        synchronized (threadRunningMonitor) {
                            threadRunning = true;
                            threadRunningMonitor.notifyAll();
                        }

                        while (true) {
                            boolean interrupted = false;

                            synchronized (captureThreadRunMonitor) {

                                try {
                                    captureThreadRunMonitor.wait();
                                } catch (InterruptedException e) {
                                    interrupted = true;
                                }
                            }

                            if (!interrupted) {
                                Long timestamp = lastReportedTimestamp;

                                synchronized (timestampsStackLock) {
                                    timestampsStack[timestampsStackPointer] = timestamp;
                                    timestampsStackPointer++;
                                }
                            }
                        }
                    }
                }, "framerate refresh thread");

                timestampCaptureThread.start();

                while (!threadRunning) {
                    boolean running;

                    synchronized (threadRunningMonitor) {
                        running = threadRunning;
                    }

                    if (running) {
                        break;
                    } else {
                        synchronized (threadRunningMonitor) {
                            try {
                                threadRunningMonitor.wait();
                            } catch (InterruptedException e) {

                            }
                        }
                    }
                }
            }
        }

        public static void pushTimestamp() {
            assertCaptureThreadRunning();

            lastReportedTimestamp = System.nanoTime();

            synchronized (captureThreadRunMonitor) {
                captureThreadRunMonitor.notifyAll();
            }
        }

        public static int getCurrentStackSize() {
            int currentSize;
            synchronized (timestampsStackLock) {
                currentSize = timestampsStackPointer;
            }
            return currentSize;
        }

    }
}
