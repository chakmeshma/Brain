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

    //region forceSleep
    public static void forceSleep(long nWaitTime) {
        long nRemainingTime = nWaitTime;

        if (nWaitTime <= 0)
            return;

        do {
            long lastSleepStartTimestamp = System.nanoTime();

            try {
                long millis = Math.round(((double) nRemainingTime) / 1000_000.0);
                int nanos = (int) (nRemainingTime - (millis * 1000_000));

                Thread.sleep(millis, nanos);
            } catch (InterruptedException e) {
            }

            nRemainingTime -= System.nanoTime() - lastSleepStartTimestamp;
        } while (nRemainingTime > 0);
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
        private static final Object threadRunning_Monitor = new Object();
        private static final Object captureThreadBlocker_Monitor = new Object();
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

                        synchronized (threadRunning_Monitor) {
                            threadRunning = true;
                            threadRunning_Monitor.notifyAll();
                        }

                        while (true) {
                            boolean interrupted = false;

                            synchronized (captureThreadBlocker_Monitor) {

                                try {
                                    captureThreadBlocker_Monitor.wait();
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

                while (true) {
                    boolean running;

                    synchronized (threadRunning_Monitor) {
                        running = threadRunning;
                    }

                    if (running) {
                        break;
                    } else {
                        synchronized (threadRunning_Monitor) {
                            try {
                                threadRunning_Monitor.wait();
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

            synchronized (captureThreadBlocker_Monitor) {
                captureThreadBlocker_Monitor.notifyAll();
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

    public static class Occupy {
        public static int occupyCPU(Long msOccupyTime) {
            long startTime = System.nanoTime();

            long sum = 0;

            while (true) {
                if (System.nanoTime() - startTime > (msOccupyTime * 1000_000L))
                    break;

                for (int i = 0; i < 10000; i++) {
                    sum++;
                }
            }

            return (int) (sum % Integer.MAX_VALUE + 1);
        }
    }

    //region SinStack
    public static class SinStack {
        public static void drawCurve(float radius, int resolution, float sTotalWaitTime, int cycles) {
            float cycleWaitTime = sTotalWaitTime / cycles;

            float partAngle = (float) (Math.PI / resolution);
            float partWidth = cycleWaitTime / resolution;

            for (int i = 0; i < resolution * cycles; i++) {
                float angleLow = partAngle * i;
                float angleHigh = partAngle * (i + 1);
                float heightLow = (float) Math.sin(angleLow) * radius;
                float heightHigh = (float) Math.sin(angleHigh) * radius;
                float height = (heightLow + heightHigh) / 2.0f;

                drawSegment(radius - height, partWidth);
            }
        }

        private static void drawSegment(float height, float width) {
            if (width <= 0.0f || height < 0.0f)
                return;

            int depth = Math.round(height);
            long nWaitTime = (long) (1000_000_000L * width);

            if (depth == 0) {
                forceSleep(nWaitTime);
            } else {
                recursiveStackTreeFunction(1, depth, nWaitTime);
            }
        }

        private static int recursiveStackTreeFunction(int depth, int maxDepth, long waitTime) {
            if (depth == maxDepth) {
                forceSleep(waitTime);

                return 1;
            } else {
                return recursiveStackTreeFunction(depth + 1, maxDepth, waitTime);
            }
        }
    }
    //endregion
}
