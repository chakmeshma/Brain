package net.chakmeshma.brutengine.rendering;

import net.chakmeshma.brutengine.development.DebugUtilities;
import net.chakmeshma.brutengine.development.exceptions.InitializationException;
import net.chakmeshma.brutengine.development.exceptions.InvalidAdaptionOperationException;
import net.chakmeshma.brutengine.development.exceptions.InvalidMappingOperationException;
import net.chakmeshma.brutengine.development.exceptions.InvalidOperationException;
import net.chakmeshma.brutengine.development.exceptions.RenderException;
import net.chakmeshma.brutengine.system.StateControllable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;
import static net.chakmeshma.brutengine.utilities.GLUtilities.GLAdaptionRule.GL_DRAW_ELEMENTS__MESH_INDICES_TYPE;
import static net.chakmeshma.brutengine.utilities.GLUtilities.GLAdaptionRule.GL_VERTEX_ATTRIB_POINTER__ATTRIBUTE_REF_SINGLE_TYPE;
import static net.chakmeshma.brutengine.utilities.GLUtilities.getGLTypeIdentifier;


public interface Drawable {
    void render() throws RenderException, InitializationException;

    void setMesh(Mesh mesh) throws InitializationException;

    void setMeshes(Mesh[] meshes) throws InitializationException;

    void setStateController(StateControllable stateController) throws InitializationException;

    void setProgram(Program program) throws InitializationException;

    void setPrograms(Program[] programs) throws InitializationException;

    void setAttributeBufferMapping(AttributeBufferMapping attributeBufferMapping) throws InitializationException;

    void setAttributeBufferMappings(AttributeBufferMapping[] attributeBufferMappings) throws InitializationException;

    //region inner classes
    class AttributeBufferMapping {
        HashMap<String, Integer> bufferNameMapping;
        HashMap<String, Integer> bufferOffsetMapping;
        HashMap<String, Integer> bufferStrideMapping;

        public AttributeBufferMapping(HashMap<String, Integer> bufferNameMapping) throws InitializationException {
            if (bufferNameMapping == null && bufferNameMapping.size() < 1)
                throw new InitializationException("not a valid attribute buffer name mapping");

            this.bufferNameMapping = bufferNameMapping;
            bufferOffsetMapping = new HashMap<>();
            bufferStrideMapping = new HashMap<>();

            Iterator it = this.bufferNameMapping.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
                bufferOffsetMapping.put(entry.getKey(), 0);
                bufferStrideMapping.put(entry.getKey(), 0);
            }
        }

        boolean attributeNameExist(Program.AttributeReference attributeReference) {
            String attributeName = attributeReference.getName();

            Iterator it = bufferNameMapping.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();

                if (entry.getKey().equals(attributeName))
                    return true;
            }

            return false;
        }

        int getBufferIndex(Program.AttributeReference attributeReference) throws InvalidOperationException {
            String attributeName = attributeReference.getName();

            Iterator it = bufferNameMapping.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();

                if (entry.getKey().equals(attributeName))
                    return entry.getValue();
            }

            throw new InvalidMappingOperationException("attribute name doesn't exist in mapping", InvalidMappingOperationException.MappingOperationBufferProperty.ATTRIBUTE_MESH_BUFFER_INDEX);
        }

        int getBufferStride(Program.AttributeReference attributeReference) throws InvalidOperationException {
            String attributeName = attributeReference.getName();

            Iterator it = bufferStrideMapping.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();

                if (entry.getKey().equals(attributeName))
                    return entry.getValue();
            }

            throw new InvalidMappingOperationException("attribute name doesn't exist in mapping", InvalidMappingOperationException.MappingOperationBufferProperty.ATTRIBUTE_BUFFER_POINTER_STRIDE);
        }

        int getBufferOffset(Program.AttributeReference attributeReference) throws InvalidOperationException {
            String attributeName = attributeReference.getName();

            Iterator it = bufferOffsetMapping.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();

                if (entry.getKey().equals(attributeName))
                    return entry.getValue();
            }

            throw new InvalidMappingOperationException("attribute name doesn't exist in mapping", InvalidMappingOperationException.MappingOperationBufferProperty.ATTRIBUTE_BUFFER_POINTER_OFFSET);
        }
    }

    class SimpleDrawable implements Drawable {
        private boolean _meshSet = false;
        private boolean _stateControllerSet = false;
        private boolean _programSet = false;
        private boolean _attributeBufferMappingSet = false;
        private boolean _linked = false;
        private AttributeBufferMapping attributeBufferMapping;
        private Mesh mesh;
        private Program program;
        private StateControllable stateController;

        public SimpleDrawable(Program program, Mesh mesh, StateControllable stateController, AttributeBufferMapping attributeBufferMapping) throws InitializationException {
            setProgram(program);
            setMesh(mesh);
            setStateController(stateController);
            setAttributeBufferMapping(attributeBufferMapping);
            link();
        }

        private boolean isSet() {
            return _meshSet && _programSet && _stateControllerSet && _attributeBufferMappingSet;
        }

        private void link() throws InitializationException {
            stateController.unbindAttachments();

            for (Program.Uniform uniform : program.getUniforms())
                try {
                    stateController.attachStateVariable(uniform);
                } catch (InvalidOperationException e) {
                    throw new InitializationException(e.getMessage());
                }

            stateController.bindAttachments();
            stateController.envalueBindings();

            setLinked();
        }

        public void render() throws RenderException, InitializationException {
            if (!isSet())
                throw new RenderException("not drawable (probably not fully initialized)");

            if (!isLinked())
                link();

            program.bind();

            for (Program.AttributeReference attributeReference : program.getAttributeReferences()) {
                if (attributeBufferMapping.attributeNameExist(attributeReference)) {
                    int bufferIndex;

                    try {
                        bufferIndex = attributeBufferMapping.getBufferIndex(attributeReference);
                    } catch (InvalidOperationException e) {
                        throw new RenderException(e.getMessage());
                    }

                    glEnableVertexAttribArray(attributeReference.getIndex());

                    try {
                        mesh.getVertexArrayBuffers()[bufferIndex].bind();

                        try {
                            glVertexAttribPointer(
                                    attributeReference.getIndex(),
                                    attributeReference.getValuesCount(),
                                    getGLTypeIdentifier(attributeReference.getValueType(), GL_VERTEX_ATTRIB_POINTER__ATTRIBUTE_REF_SINGLE_TYPE),
                                    false,
                                    attributeBufferMapping.getBufferStride(attributeReference),
                                    attributeBufferMapping.getBufferOffset(attributeReference));
                        } catch (InvalidOperationException e) {
                            throw new RenderException(e.getMessage());
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new RenderException(e.getMessage());
                    }
                } else
                    DebugUtilities.logWarning(String.format("unused/[unbound to buffer] attribute \"%s\" defined in shader(s)", attributeReference.getName()));
            }

            for (Program.Uniform uniform : program.getUniforms()) {
                if (uniform.hasChanged()) {
                    uniform.commitChange();
                }
            }

            mesh.getIndicesBuffer().bind();

            try {
                glDrawElements(mesh.getPrimitiveAssemblyMode(), mesh.getIndicesCount(), getGLTypeIdentifier(mesh.getIndicesClass(), GL_DRAW_ELEMENTS__MESH_INDICES_TYPE), mesh.getIndicesOffset());
            } catch (InvalidAdaptionOperationException e) {
                throw new RenderException(e.getMessage());
            }

            mesh.getIndicesBuffer().unbind();

            for (Program.AttributeReference attributeReference : program.getAttributeReferences()) {
                if (attributeBufferMapping.attributeNameExist(attributeReference)) {
                    int bufferIndex;

                    try {
                        bufferIndex = attributeBufferMapping.getBufferIndex(attributeReference);
                    } catch (InvalidOperationException e) {
                        throw new RenderException(e.getMessage());
                    }

                    glDisableVertexAttribArray(attributeReference.getIndex());

                    try {
                        mesh.getVertexArrayBuffers()[bufferIndex].unbind();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new RenderException(e.getMessage());
                    }
                }
            }

            program.unbind();

        }

        public void setMesh(Mesh mesh) throws InitializationException {
            this.mesh = mesh;

            _meshSet = true;

            clearLinked();
        }

        public void setMeshes(Mesh[] meshes) throws InitializationException {
            setMesh(meshes[0]);
        }

        public void setStateController(StateControllable stateController) throws InitializationException {
            this.stateController = stateController;

            _stateControllerSet = true;

            clearLinked();
        }

        public void setProgram(Program program) throws InitializationException {
            this.program = program;

            _programSet = true;

            clearLinked();
        }

        public void setPrograms(Program[] programs) throws InitializationException {
            setProgram(programs[0]);
        }

        public void setAttributeBufferMapping(AttributeBufferMapping attributeBufferMapping) {
            this.attributeBufferMapping = attributeBufferMapping;

            _attributeBufferMappingSet = true;

            clearLinked();
        }

        public void setAttributeBufferMappings(AttributeBufferMapping[] attributeBufferMappings) {
            setAttributeBufferMapping(attributeBufferMappings[0]);
        }

        private boolean isLinked() {
            return _linked;
        }

        private void setLinked() {
            this._linked = true;
        }

        private void clearLinked() {
            this._linked = false;
        }
    }
    //endregion
}
