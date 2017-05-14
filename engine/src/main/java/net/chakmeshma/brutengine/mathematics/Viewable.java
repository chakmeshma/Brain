package net.chakmeshma.brutengine.mathematics;

import net.chakmeshma.brutengine.development.exceptions.InvalidOperationException;


public interface Viewable {
    void rotateCamera(float x, float y, float[] center) throws InvalidOperationException;

    void zoomCamera(float dz);

    void rotateCamera(float x, float y) throws InvalidOperationException;

    float getViewportRatio();

    void setViewportRatio(float viewportRatio);

    float getFar();

    void setFar(float far);

    float getNear();

    void setNear(float near);

    float getFOV();

    void setFOV(float fov);
}
