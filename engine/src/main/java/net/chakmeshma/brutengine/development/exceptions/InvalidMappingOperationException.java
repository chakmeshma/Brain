package net.chakmeshma.brutengine.development.exceptions;

/**
 * Created by chakmeshma on 10.05.2017.
 */

public class InvalidMappingOperationException extends InvalidOperationException {
    MappingOperationBufferProperty mappingOperationBufferProperty;

    public InvalidMappingOperationException(String s, MappingOperationBufferProperty mappingOperationBufferProperty) {
        super(s);

        this.mappingOperationBufferProperty = mappingOperationBufferProperty;
    }

    @Override
    public String getMessage() {
        String message;
        String operation = "";

        switch (mappingOperationBufferProperty) {
            case ATTRIBUTE_BUFFER_POINTER_OFFSET:
                operation = "no named pointer offset in mappnig";
                break;
            case ATTRIBUTE_BUFFER_POINTER_STRIDE:
                operation = "no named pointer stride in mappnig";
                break;
            case ATTRIBUTE_MESH_BUFFER_INDEX:
                operation = "no named mesh buffer index in mappnig";
                break;
        }

        message = String.format("invalid attribute buffer mapping operation: %s", operation);

        return message;
    }

    /**
     * Created by chakmeshma on 10.05.2017.
     */
    public enum MappingOperationBufferProperty {
        ATTRIBUTE_MESH_BUFFER_INDEX,
        ATTRIBUTE_BUFFER_POINTER_OFFSET,
        ATTRIBUTE_BUFFER_POINTER_STRIDE
    }
}
