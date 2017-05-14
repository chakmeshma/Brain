package net.chakmeshma.brutengine.rendering;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import net.chakmeshma.brutengine.development.exceptions.InitializationException;
import net.chakmeshma.brutengine.development.exceptions.InvalidOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_DYNAMIC_DRAW;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_STREAM_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;


public final class Mesh {
    private boolean hasNormals = false;
    private int _indicesCount;
    private IndexArrayBuffer _indexArrayBuffer;
    private VertexArrayBuffer[] _vertexArrayBuffers;
    private StepLoadListener stepLoadListener;

    public Mesh(ObjFile objFile, StepLoadListener stepLoadListener) throws InitializationException {
        this.stepLoadListener = stepLoadListener;

        makeMesh(objFile);
    }

    public Mesh(ObjFile objFile) throws InitializationException {
        makeMesh(objFile);
    }

    private void makeMesh(ObjFile objFile) throws InitializationException {
        try {
            if (!objFile.arrayLoaded(ObjFile.ArrayType.VERTEX_GEOMETRY_ARRAY) || !objFile.arrayLoaded(ObjFile.ArrayType.FACE_VERTEX_GEOMETRY_INDEX_ARRAY))
                throw new InitializationException(String.format("ObjFile (%s) not loaded enough (missing geometry data)", objFile.getFileName()));

            if (objFile.arrayLoaded(ObjFile.ArrayType.FACE_VERTEX_NORMAL_INDEX_ARRAY) && objFile.arrayLoaded(ObjFile.ArrayType.VERTEX_NORMAL_ARRAY))
                hasNormals = true;

            short[] geometryIndices;
            float[] vertices;

            short[] normalIndices;
            float[] normals;

            if (!hasNormals) {
                int indicesCount = objFile.getArraySize(ObjFile.ArrayType.FACE_VERTEX_GEOMETRY_INDEX_ARRAY);

                if (indicesCount > Short.MAX_VALUE)
                    throw new InitializationException("too much indices (exceeding short value range)!");

                geometryIndices = objFile.getShortArray(ObjFile.ArrayType.FACE_VERTEX_GEOMETRY_INDEX_ARRAY);

                vertices = objFile.getFloatArray(ObjFile.ArrayType.VERTEX_GEOMETRY_ARRAY);

                float[] resortedVertices = new float[indicesCount * 3 + 3];
                short[] resortedIndices = new short[indicesCount];

                if (stepLoadListener != null) {
                    stepLoadListener.setExtraPartCount(indicesCount);
                }

                for (int i = 0; i < indicesCount; i++) {
                    int geometryIndex = geometryIndices[i];

                    for (int j = 0; j < 3; j++) {
                        resortedVertices[i * 3 + j] = vertices[geometryIndex * 3 + j];
                    }

                    resortedIndices[i] = (short) i;

                    if (stepLoadListener != null) {
                        stepLoadListener.partLoaded();
                    }
                }

                IndexArrayBuffer indexArrayBuffer = new IndexArrayBuffer(BufferUsageHint.STATIC_DRAW);
                VertexArrayBuffer vertexArrayBuffer = new VertexArrayBuffer(BufferUsageHint.STATIC_DRAW);

                indexArrayBuffer.uploadData(resortedIndices);
                vertexArrayBuffer.uploadData(resortedVertices);

                this._indicesCount = indicesCount;
                this._indexArrayBuffer = indexArrayBuffer;
                this._vertexArrayBuffers = new VertexArrayBuffer[1];
                this._vertexArrayBuffers[0] = vertexArrayBuffer;
            } else {
                int indicesCount = objFile.getArraySize(ObjFile.ArrayType.FACE_VERTEX_GEOMETRY_INDEX_ARRAY);
                int normalIndicesCount = objFile.getArraySize(ObjFile.ArrayType.FACE_VERTEX_NORMAL_INDEX_ARRAY);

                if (indicesCount > Short.MAX_VALUE || normalIndicesCount > Short.MAX_VALUE)
                    throw new InitializationException("too much indices (exceeding short value range)!");

                if (indicesCount != normalIndicesCount)
                    throw new InitializationException(String.format("invalid ObjFile (%s): number of vertex geometry indices and vertex normal indices differ!", objFile.getFileName()));

                geometryIndices = objFile.getShortArray(ObjFile.ArrayType.FACE_VERTEX_GEOMETRY_INDEX_ARRAY);
                normalIndices = objFile.getShortArray(ObjFile.ArrayType.FACE_VERTEX_NORMAL_INDEX_ARRAY);

                vertices = objFile.getFloatArray(ObjFile.ArrayType.VERTEX_GEOMETRY_ARRAY);
                normals = objFile.getFloatArray(ObjFile.ArrayType.VERTEX_NORMAL_ARRAY);

                float[] resortedVertices = new float[indicesCount * 3 + 3];
                float[] resortedNormals = new float[indicesCount * 3 + 3];
                short[] resortedIndices = new short[indicesCount];

                if (stepLoadListener != null) {
                    stepLoadListener.setExtraPartCount(indicesCount);
                }

                for (int i = 0; i < indicesCount; i++) {
                    int geometryIndex = geometryIndices[i];
                    int normalIndex = normalIndices[i];

                    for (int j = 0; j < 3; j++) {
                        resortedVertices[i * 3 + j] = vertices[geometryIndex * 3 + j];
                        resortedNormals[i * 3 + j] = normals[normalIndex * 3 + j];
                    }

                    resortedIndices[i] = (short) i;

                    if (stepLoadListener != null) {
                        stepLoadListener.partLoaded();
                    }
                }


                IndexArrayBuffer indexArrayBuffer = new IndexArrayBuffer(BufferUsageHint.STATIC_DRAW);
                VertexArrayBuffer vertexArrayBuffer = new VertexArrayBuffer(BufferUsageHint.STATIC_DRAW);
                VertexArrayBuffer normalArrayBuffer = new VertexArrayBuffer(BufferUsageHint.STATIC_DRAW);

                indexArrayBuffer.uploadData(resortedIndices);
                vertexArrayBuffer.uploadData(resortedVertices);
                normalArrayBuffer.uploadData(resortedNormals);

                this._indicesCount = indicesCount;
                this._indexArrayBuffer = indexArrayBuffer;
                this._vertexArrayBuffers = new VertexArrayBuffer[2];
                this._vertexArrayBuffers[0] = vertexArrayBuffer;
                this._vertexArrayBuffers[1] = normalArrayBuffer;
            }

        } catch (InvalidOperationException e) {
            throw new InitializationException(e.getMessage());
        }

    }

    boolean hasNormals() {
        return hasNormals;
    }

    int getIndicesCount() {
        return _indicesCount;
    }

    IndexArrayBuffer getIndicesBuffer() {
        return this._indexArrayBuffer;
    }

    VertexArrayBuffer[] getVertexArrayBuffers() {
        return this._vertexArrayBuffers;
    }

    int getIndicesOffset() {
        return 0;
    }

    Class getIndicesClass() {
        return short.class;
    }

    int getPrimitiveAssemblyMode() {
        return GL_TRIANGLES;
    }

    private enum BufferUsageHint {
        STATIC_DRAW,
        DYNAMIC_DRAW,
        STREAM_DRAW
    }

    //region inner classes
    public static final class ObjFile {
        private static final Pattern _v_Pattern;
        private static final Pattern _f_Pattern;
        private static final Pattern _n_Pattern;
        private static final Pattern face3ComponentsPattern;
        private static final Pattern face6ComponentsPattern;
        private static final Pattern face9ComponentsPattern;

        static {
            _v_Pattern = Pattern.compile("\\A\\s*v\\s+((-?\\s*\\d+)|(-?\\s*\\d*\\.\\d+))\\s+((-?\\s*\\d+)|(-?\\s*\\d*\\.\\d+))\\s+((-?\\s*\\d+)|(-?\\s*\\d*\\.\\d+))\\s*\\z");
            _f_Pattern = Pattern.compile("\\A\\s*f\\s+([0-9/]+)\\s+([0-9/]+)\\s+([0-9/]+)\\s*\\z");
            _n_Pattern = Pattern.compile("\\A\\s*vn\\s+((-?\\s*\\d+)|(-?\\s*\\d*\\.\\d+))\\s+((-?\\s*\\d+)|(-?\\s*\\d*\\.\\d+))\\s+((-?\\s*\\d+)|(-?\\s*\\d*\\.\\d+))\\s*\\z");
            face3ComponentsPattern = Pattern.compile("\\A\\s*(\\d+)\\s*\\z");
            face6ComponentsPattern = Pattern.compile("\\A\\s*(\\d+)/(\\d*)\\s*\\z");
            face9ComponentsPattern = Pattern.compile("\\A\\s*(\\d+)/(\\d*)/(\\d*)\\s*\\z");
        }

        private float[] vertexComponents;
        private float[] normalComponents;
        private short[] indicesOfVertices;
        private short[] indicesOfNormals;

        private String _fileName;

        public ObjFile(Context context, String fileName) throws InitializationException {
            this(context, fileName, null);
        }

        public ObjFile(Context context, String fileName, StepLoadListener stepLoadListener) throws InitializationException {
            InputStream fileIn = null;
            int totalFileSize = 0;
            try {
                fileIn = context.getAssets().open(fileName);
            } catch (IOException e) {
                throw new InitializationException(String.format("Couldn't load model file (%s)", fileName));
            }

            InputStreamReader fileInR = new InputStreamReader(fileIn);

            try {
                totalFileSize = fileIn.available();
            } catch (IOException e) {
                throw new InitializationException(String.format("Couldn't load model file (%s)", fileName));
            }

            BufferedReader reader = null;
            String line = null;

            BufferedReader br = new BufferedReader(fileInR);

            Matcher _v_PatternMatcher = _v_Pattern.matcher("");
            Matcher _f_PatternMatcher = _f_Pattern.matcher("");
            Matcher _n_PatternMatcher = _n_Pattern.matcher("");
            Matcher face3ComponentsPatternMatcher = face3ComponentsPattern.matcher("");
            Matcher face6ComponentsPatternMatcher = face6ComponentsPattern.matcher("");
            Matcher face9ComponentsPatternMatcher = face9ComponentsPattern.matcher("");

            Vector<Short> _vIndicesOfVertices;
            Vector<Float> _vVertexComponents;
            Vector<Short> _vIndicesOfNormals;
            Vector<Float> _vNormalComponents;

            _vVertexComponents = new Vector<>();
            _vIndicesOfVertices = new Vector<>();
            _vNormalComponents = new Vector<>();
            _vIndicesOfNormals = new Vector<>();


            int faceRichness = -1;

            try {
                while ((line = br.readLine()) != null) {
                    _v_PatternMatcher.reset(line);
                    _f_PatternMatcher.reset(line);
                    _n_PatternMatcher.reset(line);


                    if (_v_PatternMatcher.matches()) {
                        _vVertexComponents.add(Float.valueOf(_v_PatternMatcher.group(1)));
                        _vVertexComponents.add(Float.valueOf(_v_PatternMatcher.group(4)));
                        _vVertexComponents.add(Float.valueOf(_v_PatternMatcher.group(7)));
                    }


                    if (_n_PatternMatcher.matches()) {
                        _vNormalComponents.add(Float.valueOf(_n_PatternMatcher.group(1)));
                        _vNormalComponents.add(Float.valueOf(_n_PatternMatcher.group(4)));
                        _vNormalComponents.add(Float.valueOf(_n_PatternMatcher.group(7)));
                    }


                    if (_f_PatternMatcher.matches()) {
                        String gp1 = _f_PatternMatcher.group(1);
                        String gp2 = _f_PatternMatcher.group(2);
                        String gp3 = _f_PatternMatcher.group(3);

                        if (faceRichness == -1) {
                            for (short i = 0; i < gp1.length(); i++) {
                                if (gp1.charAt(i) == '/')
                                    faceRichness++;
                            }
                            faceRichness++;
                        }

                        short[] face;

                        switch (faceRichness) {
                            case 0:
                                face = new short[3];

                                face3ComponentsPatternMatcher.reset(gp1);

                                if (face3ComponentsPatternMatcher.matches()) {
                                    face[0] = Short.valueOf(face3ComponentsPatternMatcher.group(1));
                                }

                                face3ComponentsPatternMatcher.reset(gp2);

                                if (face3ComponentsPatternMatcher.matches()) {
                                    face[1] = Short.valueOf(face3ComponentsPatternMatcher.group(1));
                                }

                                face3ComponentsPatternMatcher.reset(gp3);

                                if (face3ComponentsPatternMatcher.matches()) {
                                    face[2] = Short.valueOf(face3ComponentsPatternMatcher.group(1));
                                }

                                _vIndicesOfVertices.add((short) (face[0] - 1));
                                _vIndicesOfVertices.add((short) (face[1] - 1));
                                _vIndicesOfVertices.add((short) (face[2] - 1));
                                break;
                            case 2:
                                face = new short[9];

                                face9ComponentsPatternMatcher.reset(gp1);

                                if (face9ComponentsPatternMatcher.matches()) {
                                    try {
                                        face[0] = Short.valueOf(face9ComponentsPatternMatcher.group(1));
                                    } catch (NumberFormatException e) {
                                        face[0] = 0;
                                    }
                                    try {
                                        face[1] = Short.valueOf(face9ComponentsPatternMatcher.group(2));
                                    } catch (NumberFormatException e) {
                                        face[1] = 0;
                                    }
                                    try {
                                        face[2] = Short.valueOf(face9ComponentsPatternMatcher.group(3));
                                    } catch (NumberFormatException e) {
                                        face[2] = 0;
                                    }
                                }

                                face9ComponentsPatternMatcher.reset(gp2);

                                if (face9ComponentsPatternMatcher.matches()) {
                                    try {
                                        face[3] = Short.valueOf(face9ComponentsPatternMatcher.group(1));
                                    } catch (NumberFormatException e) {
                                        face[3] = 0;
                                    }
                                    try {
                                        face[4] = Short.valueOf(face9ComponentsPatternMatcher.group(2));
                                    } catch (NumberFormatException e) {
                                        face[4] = 0;
                                    }
                                    try {
                                        face[5] = Short.valueOf(face9ComponentsPatternMatcher.group(3));
                                    } catch (NumberFormatException e) {
                                        face[5] = 0;
                                    }
                                }

                                face9ComponentsPatternMatcher.reset(gp3);

                                if (face9ComponentsPatternMatcher.matches()) {
                                    try {
                                        face[6] = Short.valueOf(face9ComponentsPatternMatcher.group(1));
                                    } catch (NumberFormatException e) {
                                        face[6] = 0;
                                    }
                                    try {
                                        face[7] = Short.valueOf(face9ComponentsPatternMatcher.group(2));
                                    } catch (NumberFormatException e) {
                                        face[7] = 0;
                                    }
                                    try {
                                        face[8] = Short.valueOf(face9ComponentsPatternMatcher.group(3));
                                    } catch (NumberFormatException e) {
                                        face[8] = 0;
                                    }
                                }

                                if (face[0] > 0 && face[3] > 0 && face[6] > 0) {
                                    _vIndicesOfVertices.add((short) (face[0] - 1));
                                    _vIndicesOfVertices.add((short) (face[3] - 1));
                                    _vIndicesOfVertices.add((short) (face[6] - 1));
                                }

                                if (face[2] > 0 && face[5] > 0 && face[8] > 0) {
                                    _vIndicesOfNormals.add((short) (face[2] - 1));
                                    _vIndicesOfNormals.add((short) (face[5] - 1));
                                    _vIndicesOfNormals.add((short) (face[8] - 1));
                                }

                                break;
                        }
                    }

                }
            } catch (IOException e) {
                throw new InitializationException(String.format("Couldn't load model file (%s)", fileName));
            }

            try {
                br.close();
            } catch (IOException e) {
            }
            try {
                fileInR.close();
            } catch (IOException e) {
            }
            try {
                fileIn.close();
            } catch (IOException e) {
            }

            _fileName = fileName;

            if (_vVertexComponents.size() > 0) {
                vertexComponents = new float[_vVertexComponents.size()];

                for (int i = 0; i < _vVertexComponents.size(); i++)
                    vertexComponents[i] = _vVertexComponents.get(i);
            }

            if (_vNormalComponents.size() > 0) {
                normalComponents = new float[_vNormalComponents.size()];

                for (int i = 0; i < _vNormalComponents.size(); i++)
                    normalComponents[i] = _vNormalComponents.get(i);
            }

            if (_vIndicesOfVertices.size() > 0) {
                indicesOfVertices = new short[_vIndicesOfVertices.size()];

                for (int i = 0; i < _vIndicesOfVertices.size(); i++)
                    indicesOfVertices[i] = _vIndicesOfVertices.get(i);
            }

            if (_vIndicesOfNormals.size() > 0) {
                indicesOfNormals = new short[_vIndicesOfNormals.size()];

                for (int i = 0; i < _vIndicesOfNormals.size(); i++) {
                    indicesOfNormals[i] = _vIndicesOfNormals.get(i);
                }
            }
        }

        boolean arrayLoaded(ArrayType arrayType) throws InvalidOperationException {
            switch (arrayType) {
                case FACE_VERTEX_GEOMETRY_INDEX_ARRAY:
                    return (indicesOfVertices != null && indicesOfVertices.length > 0);
                case FACE_VERTEX_NORMAL_INDEX_ARRAY:
                    return (indicesOfNormals != null && indicesOfNormals.length > 0);
                case VERTEX_GEOMETRY_ARRAY:
                    return (vertexComponents != null && vertexComponents.length > 0);
                case VERTEX_NORMAL_ARRAY:
                    return (normalComponents != null && normalComponents.length > 0);
            }

            throw new InvalidOperationException(String.format("not a valid array type (%s)", arrayType.toString()));
        }

        int getArraySize(ArrayType arrayType) throws InvalidOperationException {
            if (!arrayLoaded(arrayType)) {
                throw new InvalidOperationException(String.format("array not loaded (%s)", arrayType.toString()));
            }

            switch (arrayType) {
                case FACE_VERTEX_GEOMETRY_INDEX_ARRAY:
                    return indicesOfVertices.length;
                case FACE_VERTEX_NORMAL_INDEX_ARRAY:
                    return indicesOfNormals.length;
                case VERTEX_GEOMETRY_ARRAY:
                    return vertexComponents.length;
                case VERTEX_NORMAL_ARRAY:
                    return normalComponents.length;
            }

            return 0;
        }

        short[] getShortArray(ArrayType arrayType) throws InvalidOperationException {
            if (!arrayLoaded(arrayType))
                throw new InvalidOperationException(String.format("array not loaded (%s)", arrayType.toString()));

            switch (arrayType) {
                case FACE_VERTEX_GEOMETRY_INDEX_ARRAY:
                    return indicesOfVertices;
                case FACE_VERTEX_NORMAL_INDEX_ARRAY:
                    return indicesOfNormals;
            }

            throw new InvalidOperationException(String.format("not a valid short array type (%s)", arrayType.toString()));
        }

        float[] getFloatArray(ArrayType arrayType) throws InvalidOperationException {
            if (!arrayLoaded(arrayType))
                throw new InvalidOperationException(String.format("array not loaded (%s)", arrayType.toString()));

            switch (arrayType) {
                case VERTEX_GEOMETRY_ARRAY:
                    return vertexComponents;
                case VERTEX_NORMAL_ARRAY:
                    return normalComponents;
            }

            throw new InvalidOperationException(String.format("not a valid float array type (%s)", arrayType.toString()));
        }

        String getFileName() {
            return _fileName;
        }

        enum ArrayType {
            VERTEX_GEOMETRY_ARRAY,
            VERTEX_NORMAL_ARRAY,
            FACE_VERTEX_GEOMETRY_INDEX_ARRAY,
            FACE_VERTEX_NORMAL_INDEX_ARRAY
        }
    }

    abstract class Buffer {
        private final int USAGE_HINT_STATIC_DRAW = GL_STATIC_DRAW;
        private final int USAGE_HINT_DYNAMIC_DRAW = GL_DYNAMIC_DRAW;
        private final int USAGE_HINT_STREAM_DRAW = GL_STREAM_DRAW;
        private final int USAGE_HINT_DEFAULT = USAGE_HINT_STATIC_DRAW;
        int designatedBufferBinding;
        private BufferUsageHint usageHint;
        private int bufferName;

        Buffer(BufferUsageHint usageHint) {
            int[] bufferNames = new int[1];

            glGenBuffers(1, bufferNames, 0);

            bufferName = bufferNames[0];

            this.usageHint = usageHint;
        }

        int getBufferName() {
            return bufferName;
        }

        int getUsageHint() {
            switch (usageHint) {
                case STATIC_DRAW:
                    return USAGE_HINT_STATIC_DRAW;
                case DYNAMIC_DRAW:
                    return USAGE_HINT_DYNAMIC_DRAW;
                case STREAM_DRAW:
                    return USAGE_HINT_STREAM_DRAW;
            }

            return USAGE_HINT_DEFAULT;
        }

        void bind() {
            glBindBuffer(designatedBufferBinding, getBufferName());
        }

        void unbind() {
            glBindBuffer(designatedBufferBinding, 0);
        }

    }

    final class VertexArrayBuffer extends Buffer {
        {
            this.designatedBufferBinding = GL_ARRAY_BUFFER;
        }

        VertexArrayBuffer(BufferUsageHint usageHint) {
            super(usageHint);
        }

        void uploadData(float[] data) {
            int dataLength = data.length * (Float.SIZE / 8);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(dataLength);
            byteBuffer.order(ByteOrder.nativeOrder());
            FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
            floatBuffer.put(data);
            floatBuffer.position(0);

            glBindBuffer(designatedBufferBinding, getBufferName());

            glBufferData(designatedBufferBinding, dataLength, floatBuffer, getUsageHint());

            glBindBuffer(designatedBufferBinding, 0);
        }
    }

    final class IndexArrayBuffer extends Buffer {
        {
            this.designatedBufferBinding = GL_ELEMENT_ARRAY_BUFFER;
        }

        IndexArrayBuffer(BufferUsageHint usageHint) {
            super(usageHint);
        }

        void uploadData(short[] data) {
            int dataLength = data.length * (Short.SIZE / 8);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(dataLength);
            byteBuffer.order(ByteOrder.nativeOrder());
            ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
            shortBuffer.put(data);
            shortBuffer.position(0);

            bind();

            glBufferData(designatedBufferBinding, dataLength, shortBuffer, getUsageHint());

            unbind();
        }

        private void uploadData(byte[] data) {
            int dataLength = data.length * (Byte.SIZE / 8);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(dataLength);
            byteBuffer.order(ByteOrder.nativeOrder());
            byteBuffer.put(data);
            byteBuffer.position(0);

            bind();

            glBufferData(designatedBufferBinding, dataLength, byteBuffer, getUsageHint());

            unbind();
        }
    }
    //endregion
}
