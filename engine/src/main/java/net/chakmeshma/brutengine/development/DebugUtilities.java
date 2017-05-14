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
    private static final Object timestampsStackLock = new Object();
    private static long[] timestampsStack = new long[10240];
    private static int timestampsStackPointer = 0;
    private static Thread timestampCaptureThread;

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

    public static void logWarning(String warningMessage) {
        Log.w(warningTag, warningMessage);
    }

    public static void logWarning(Exception warningException) {
        Log.w(warningTag, warningException);
    }

    public static long popTimestamp() throws InvalidStackOperationException {
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

    public static void pushTimestamp() {
        long timestamp = System.nanoTime();

        if (timestampCaptureThread == null) {
            timestampCaptureThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(Long.MAX_VALUE);
                        } catch (InterruptedException e) {

                        }

                        Long timestamp = System.nanoTime();

                        synchronized (timestampsStackLock) {
                            timestampsStack[timestampsStackPointer] = timestamp;
                            timestampsStackPointer++;
                        }
                    }
                }
            }, "framerate refresh thread");

            timestampCaptureThread.start();
        }

        timestampCaptureThread.interrupt();
    }

    public static int getCurrentStackSize() {
        int currentSize;
        synchronized (timestampsStackLock) {
            currentSize = timestampsStackPointer;
        }
        return currentSize;
    }

//    public static int getTimestampStackSize() {
//        int size;
//
//        synchronized (timestampsStackLock) {
//            size = timestampsStackPointer;
//        }
//
//        return size;
//    }
}
