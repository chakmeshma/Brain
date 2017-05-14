package net.chakmeshma.brutengine.utilities;

import net.chakmeshma.brutengine.development.exceptions.InvalidAdaptionOperationException;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;


public class GLUtilities {
    public static int getGLTypeIdentifier(Class typeClass, GLAdaptionRule typeAdaptionRule) throws InvalidAdaptionOperationException {
        switch (typeAdaptionRule) {

            case GL_VERTEX_ATTRIB_POINTER__ATTRIBUTE_REF_SINGLE_TYPE:
                switch (typeClass.toString()) {
                    case "float":
                        return GL_FLOAT;
                }
            case GL_DRAW_ELEMENTS__MESH_INDICES_TYPE:
                switch (typeClass.toString()) {
                    case "short":
                        return GL_UNSIGNED_SHORT;
                }
        }

        throw new InvalidAdaptionOperationException(typeAdaptionRule);
    }


    public enum GLAdaptionRule {
        GL_VERTEX_ATTRIB_POINTER__ATTRIBUTE_REF_SINGLE_TYPE,
        GL_DRAW_ELEMENTS__MESH_INDICES_TYPE
    }
}
