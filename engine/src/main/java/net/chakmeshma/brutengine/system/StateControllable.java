package net.chakmeshma.brutengine.system;

import android.opengl.Matrix;

import net.chakmeshma.brutengine.development.exceptions.InitializationException;
import net.chakmeshma.brutengine.development.exceptions.InvalidOperationException;
import net.chakmeshma.brutengine.mathematics.Viewable;
import net.chakmeshma.brutengine.system.StateVariable.StateVariableMatcher;

import java.util.HashSet;

public interface StateControllable {
    boolean isEnabled();

    void attachStateVariable(StateVariable stateVariable) throws InvalidOperationException;

    void bindAttachments();

    void unbindAttachments();

    void envalueBindings();

    //region inner classes
    class SimpleViewableStateController implements StateControllable, Viewable {
        private HashSet<StateVariable> stateVariables;
        private HashSet<StateVariable> modelviewMatrixBindings;
        private HashSet<StateVariable> normalMatrixBindings;
        private HashSet<StateVariable> projectionMatrixBindings;
        private float viewportRatio;
        private boolean _enabled;
        private float fov;
        private float near;
        private float far;
        private float[] cameraRotationMatrix;
        private float[] cameraTranslationMatrix;
        private StateVariableMatcher projectionMatrixMatcher;
        private StateVariableMatcher modelviewMatrixMatcher;
        private StateVariableMatcher normalMatrixMatcher;


        public SimpleViewableStateController(StateVariableMatcher projectionMatrixMatcher,
                                             StateVariableMatcher modelviewMatrixMatcher,
                                             StateVariableMatcher normalMatrixMatcher,
                                             float fov,
                                             float viewportRatio,
                                             float near,
                                             float far) throws InitializationException {
            this.fov = fov;
            this.viewportRatio = viewportRatio;
            this.near = near;
            this.far = far;

            this.projectionMatrixMatcher = projectionMatrixMatcher;
            this.modelviewMatrixMatcher = modelviewMatrixMatcher;
            this.normalMatrixMatcher = normalMatrixMatcher;

            this.cameraRotationMatrix = new float[16];
            this.cameraTranslationMatrix = new float[16];
            Matrix.setIdentityM(this.cameraRotationMatrix, 0);
            Matrix.setIdentityM(this.cameraTranslationMatrix, 0);

            this.stateVariables = new HashSet<StateVariable>();

            this._enabled = true;

            startAnimation();
        }

        @Override
        public boolean isEnabled() {
            return this._enabled;
        }

        @Override
        public void attachStateVariable(StateVariable stateVariable) throws InvalidOperationException {
            if (stateVariable == null)
                throw new InvalidOperationException("state variable null");

            this.stateVariables.add(stateVariable);
        }

        @Override
        public void bindAttachments() {
            if (this.stateVariables != null) {
                this.projectionMatrixBindings = new HashSet<StateVariable>();
                this.modelviewMatrixBindings = new HashSet<StateVariable>();
                this.normalMatrixBindings = new HashSet<StateVariable>();

                for (StateVariable stateVariable : this.stateVariables) {
                    if(this.projectionMatrixMatcher != null && this.projectionMatrixMatcher.matches(stateVariable)) {
                        this.projectionMatrixBindings.add(stateVariable);
                    }

                    if(this.modelviewMatrixMatcher != null && this.modelviewMatrixMatcher.matches(stateVariable)) {
                        this.modelviewMatrixBindings.add(stateVariable);
                    }

                    if(this.normalMatrixMatcher != null && this.normalMatrixMatcher.matches(stateVariable)) {
                        this.normalMatrixBindings.add(stateVariable);
                    }
                }
            }
        }

        @Override
        public void unbindAttachments() {
            this.projectionMatrixBindings = null;
            this.modelviewMatrixBindings = null;
            this.normalMatrixBindings = null;
        }

        @Override
        public void envalueBindings() {
            float[] tempMatrix = new float[16];

            if (this.projectionMatrixBindings != null) {
                updateProjection();
            }

            if (this.modelviewMatrixBindings != null) {
                updateModelview();
            }

            if (this.normalMatrixBindings != null) {
                updateModelview();
            }
        }

        @Override
        public void rotateCamera(float x, float y, float[] center) throws InvalidOperationException {
            float[] tempMatrix = new float[16];
            float[] resultMatrix = new float[16];

            Matrix.setIdentityM(tempMatrix, 0);

            Matrix.translateM(tempMatrix, 0, -center[0], -center[1], -center[2]);

            float sum = (float) Math.sqrt(x * x + y * y);

            if (!(sum > 0))
                throw new InvalidOperationException("rotation vector zero");

            float normalizedX = x / sum;
            float normalizedY = y / sum;

            Matrix.rotateM(tempMatrix, 0, sum, normalizedY, normalizedX, 0.0f);

            Matrix.translateM(tempMatrix, 0, center[0], center[1], center[2]);

            synchronized (this) {
                Matrix.multiplyMM(resultMatrix, 0, tempMatrix, 0, this.cameraRotationMatrix, 0);

                this.cameraRotationMatrix = resultMatrix;

                updateModelview();
            }
        }

        @Override
        public synchronized void zoomCamera(float dz) {
            float[] tempMatrix = new float[16];
            float[] resultMatrix = new float[16];

            Matrix.setIdentityM(tempMatrix, 0);

            Matrix.translateM(tempMatrix, 0, 0.0f, 0.0f, dz);

            Matrix.multiplyMM(resultMatrix, 0, tempMatrix, 0, this.cameraTranslationMatrix, 0);

            this.cameraTranslationMatrix = resultMatrix;

            updateModelview();
        }

        private synchronized void startAnimation() {
            Thread animationThread = new Thread(new Runnable() {
                private long lastTimestamp = 0;
                private float rotationSpeed = 10.0f;

                @Override
                public void run() {

                    while (true) {
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        try {
                            long timestamp = System.nanoTime();

                            if (lastTimestamp != 0) {
                                float rotation = ((float) (((double) (timestamp - lastTimestamp)) / 1000_000_000.0)) * rotationSpeed;


                                rotateCamera(rotation, 0.0f);
                            }
                            lastTimestamp = timestamp;
                        } catch (InvalidOperationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, "animation thread");

            animationThread.start();
        }

        private synchronized void updateModelview() {
            float[] resultMatrix = new float[16];

            if (this.modelviewMatrixBindings != null) {
                Matrix.multiplyMM(resultMatrix, 0, this.cameraTranslationMatrix, 0, this.cameraRotationMatrix, 0);

                for (StateVariable modelViewMatrixBinding : this.modelviewMatrixBindings)
                    modelViewMatrixBinding.setValues(resultMatrix);

                if (this.normalMatrixBindings != null) {
                    float[] normalMatrix = new float[16];

                    Matrix.setIdentityM(normalMatrix, 0);

                    double sx = Math.sqrt(resultMatrix[0] * resultMatrix[0] + resultMatrix[1] * resultMatrix[1] + resultMatrix[2] * resultMatrix[2]);
                    double sy = Math.sqrt(resultMatrix[4] * resultMatrix[4] + resultMatrix[5] * resultMatrix[5] + resultMatrix[6] * resultMatrix[6]);
                    double sz = Math.sqrt(resultMatrix[8] * resultMatrix[8] + resultMatrix[9] * resultMatrix[9] + resultMatrix[10] * resultMatrix[10]);

                    normalMatrix[0] = (float) (resultMatrix[0] / sx);
                    normalMatrix[1] = (float) (resultMatrix[1] / sx);
                    normalMatrix[2] = (float) (resultMatrix[2] / sx);

                    normalMatrix[4] = (float) (resultMatrix[4] / sy);
                    normalMatrix[5] = (float) (resultMatrix[5] / sy);
                    normalMatrix[6] = (float) (resultMatrix[6] / sy);

                    normalMatrix[8] = (float) (resultMatrix[8] / sz);
                    normalMatrix[9] = (float) (resultMatrix[9] / sz);
                    normalMatrix[10] = (float) (resultMatrix[10] / sz);

                    for (StateVariable normalMatrixBinding : this.normalMatrixBindings)
                        normalMatrixBinding.setValues(normalMatrix);
                }
            }
        }

        private synchronized void updateProjection() {
            if (this.projectionMatrixBindings != null) {
                float[] resultMatrix = new float[16];


                Matrix.perspectiveM(resultMatrix, 0, getFOV(), getViewportRatio(), getNear(), getFar());

                for (StateVariable projectionMatrixBinding : this.projectionMatrixBindings)
                    projectionMatrixBinding.setValues(resultMatrix);
            }
        }

        @Override
        public synchronized void rotateCamera(float x, float y) throws InvalidOperationException {
            rotateCamera(x, y, new float[]{0.0f, 0.0f, 0.0f});
        }


        @Override
        public float getViewportRatio() {
            return this.viewportRatio;
        }

        @Override
        public void setViewportRatio(float viewportRatio) {
            this.viewportRatio = viewportRatio;

            updateProjection();
        }

        @Override
        public float getFar() {
            return this.far;
        }

        @Override
        public void setFar(float far) {
            this.far = far;

            updateProjection();
        }

        @Override
        public float getNear() {
            return this.near;
        }

        @Override
        public void setNear(float near) {
            this.near = near;

            updateProjection();
        }

        @Override
        public float getFOV() {
            return this.fov;
        }

        @Override
        public void setFOV(float fov) {
            this.fov = fov;

            updateProjection();
        }

    }
    //endregion
}
