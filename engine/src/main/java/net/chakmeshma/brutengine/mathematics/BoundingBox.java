package net.chakmeshma.brutengine.mathematics;

import java.text.DecimalFormat;


public class BoundingBox {
    private float _left;
    private float _right;
    private float _top;
    private float _bottom;
    private float _front;
    private float _back;
    private boolean _initialized = false;

    protected BoundingBox() {
        _initialized = true;
    }

    private BoundingBox(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        _left = minX;
        _right = maxX;
        _top = maxY;
        _bottom = minY;
        _front = maxZ;
        _back = minZ;

        _initialized = true;
    }

    @Override
    public String toString() {
        String data = String.format(
                "width: %s\nheight: %s\ndepth: %s" +
                        "\n\nleft: %s\nright: %s\ntop: %s\nbottom: %s\nfront: %s\nback: %s\n\ncenter: (%s, %s, %s)",
                new DecimalFormat("#.##").format(this.getWidth()),
                new DecimalFormat("#.##").format(this.getHeight()),
                new DecimalFormat("#.##").format(this.getDepth()),

                new DecimalFormat("#.#####").format(this.getLeft()),
                new DecimalFormat("#.#####").format(this.getRight()),
                new DecimalFormat("#.#####").format(this.getTop()),
                new DecimalFormat("#.#####").format(this.getBottom()),
                new DecimalFormat("#.#####").format(this.getFront()),
                new DecimalFormat("#.#####").format(this.getBack()),

                new DecimalFormat("#.#####").format(this.getCenterX()),
                new DecimalFormat("#.#####").format(this.getCenterY()),
                new DecimalFormat("#.#####").format(this.getCenterZ())
        );

        return data;
    }

    private void assertInitialized() {
        if (!_initialized)
            throw new IllegalStateException("not initialized");
    }

    public final float getLeft() {
        assertInitialized();

        return _left;
    }

    protected final void setLeft(float value) {
        assertInitialized();

        _left = value;
    }

    public final float getRight() {
        assertInitialized();

        return _right;
    }

    protected final void setRight(float value) {
        assertInitialized();

        _right = value;
    }

    public final float getTop() {
        assertInitialized();

        return _top;
    }

    protected final void setTop(float value) {
        assertInitialized();

        _top = value;
    }

    public final float getBottom() {
        assertInitialized();

        return _bottom;
    }

    protected final void setBottom(float value) {
        assertInitialized();

        _bottom = value;
    }

    public final float getFront() {
        assertInitialized();

        return _front;
    }

    protected final void setFront(float value) {
        assertInitialized();

        _front = value;
    }

    public final float getBack() {
        assertInitialized();

        return _back;
    }

    protected final void setBack(float value) {
        assertInitialized();

        _back = value;
    }

    public final float getWidth() {
        assertInitialized();

        return (_right - _left);
    }

    public final float getHeight() {
        assertInitialized();

        return (_top - _bottom);
    }

    public final float getDepth() {
        assertInitialized();

        return (_front - _back);
    }

    public final float getCenterX() {
        return (_left + _right) / 2.0f;
    }

    public final float getCenterY() {
        return (_top + _bottom) / 2.0f;
    }

    public final float getCenterZ() {
        return (_front + _back) / 2.0f;
    }


}
