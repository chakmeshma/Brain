package net.chakmeshma.brain;

public class Brain {
    private static final ThreadType DEFAULT_NEURON_THREAD_TYPE = ThreadType.NEURON_THREAD;
    private static final int DEFAULT_THRESHOLD = 1;
    private static final int DEFAULT_X_LOCATION = 0;
    private static final int DEFAULT_Y_LOCATION = 0;
    private static final int DEFAULT_Z_LOCATION = 0;
    private Object lastCreatedThread_Monitor;
    private boolean lastCreatedThreadRunning;
    private Neuron neurons[];
    private int nextThreshold;
    private int nextZLocation;
    private int nextYLocation;
    private int nextXLocation;
    private Object lastCreatedThreadInput;

    Brain(int countNeurons) {
        this.nextThreshold = DEFAULT_THRESHOLD;
        this.nextXLocation = DEFAULT_X_LOCATION;
        this.nextYLocation = DEFAULT_Y_LOCATION;
        this.nextZLocation = DEFAULT_Z_LOCATION;

        this.neurons = new Neuron[countNeurons];
    }

    public static Brain createBrain(BrainType type, BrainShape shape, int countNeurons) {
        switch (type) {
            case FEED_FORWARD:
                Brain brain = new Brain(countNeurons);
                return brain;
        }

        return null;
    }

    private Thread createThread(ThreadType type, String threadName) {
        switch (type) {
            case NEURON_THREAD:
                Brain.this.lastCreatedThreadRunning = false;

                NeuronThread thread = new NeuronThread(threadName);

                Brain.this.lastCreatedThread_Monitor = thread._monitor;
                Brain.this.lastCreatedThreadInput = thread.threadInput;

                return thread;
        }

        return null;
    }

    boolean isLastCreatedThreadRunning() {
        return Brain.this.lastCreatedThreadRunning;
    }

    Object getLastCreatedThreadMonitor() {
        return Brain.this.lastCreatedThread_Monitor;
    }

    Object getLastCreatedThreadInput() {
        return Brain.this.lastCreatedThreadInput;
    }

    int getNextZLocation() {
        return this.nextZLocation;
    }

    int getNextYLocation() {
        return this.nextYLocation;
    }

    int getNextXLocation() {
        return this.nextXLocation;
    }

    int getNextThreshold() {
        return this.nextThreshold;
    }

    private ThreadType getNextNeuronThreadType() {
        return DEFAULT_NEURON_THREAD_TYPE;
    }

    //region inner classes
    private enum ThreadType {
        NEURON_THREAD
    }

    enum BrainType {
        FEED_FORWARD
    }

    enum BrainShape {
        SPHERE
    }

    class NeuronThread extends Thread {
        private final Object _blocker = new Object();
        private final Object _monitor = new Object();
        private String _threadName;
        private Object threadInput;

        NeuronThread(String s) {
            super(s);

            threadInput = new Object();

            _threadName = s;
        }

        @Override
        public void run() {
            //region release run query lock
            Brain.this.lastCreatedThreadRunning = true;
            this._blocker.notifyAll();
            //endregion

            synchronized (this.threadInput) {
                while (true) {
                    try {
                        this.threadInput.wait(0);


                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    class Connection {

    }

    class Neuron implements Threadable {
        int locationX, locationY, locationZ;
        int threshold;
        Thread neuronThread;
        private ThreadType threadType;
        private Thread thread;

        Neuron(int threshold, int locationX, int locationY, int locationZ) {
            setThreshold(threshold);

            this.locationX = locationX;
            this.locationY = locationY;
            this.locationZ = locationZ;
            this.threadType = Brain.this.getNextNeuronThreadType();

            initThread(true);
        }

        Neuron(int threshold) {
            setThreshold(threshold);

            this.locationX = Brain.this.getNextXLocation();
            this.locationY = Brain.this.getNextYLocation();
            this.locationZ = Brain.this.getNextZLocation();
            this.threadType = Brain.this.getNextNeuronThreadType();

            initThread(true);
        }

        public void initThread(boolean lock) {
            this.thread = createThread(getThreadType(), getThreadName());
            this.thread.start();

            if (lock) {
                synchronized (getLastCreatedThreadMonitor()) {

                    while (true) {
                        try {
                            getLastCreatedThreadMonitor().wait(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (isLastCreatedThreadRunning()) {
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public String getThreadName() {
            return String.format("%d|%d|%d|%d", hashCode(), this.locationX, this.locationY, this.locationZ);
        }

        void setThreshold(int threshold) {
            this.threshold = Brain.this.getNextThreshold();
        }

        ThreadType getThreadType() {
            return this.threadType;
        }
    }
    //endregion
}
