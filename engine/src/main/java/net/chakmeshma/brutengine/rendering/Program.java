package net.chakmeshma.brutengine.rendering;

import android.content.Context;

import net.chakmeshma.brutengine.development.exceptions.GLCustomException;
import net.chakmeshma.brutengine.development.exceptions.GLCustomShaderException;
import net.chakmeshma.brutengine.development.exceptions.InitializationException;
import net.chakmeshma.brutengine.system.StateVariable;
import net.chakmeshma.brutengine.utilities.AssetFileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FALSE;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_MAX_VERTEX_ATTRIBS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindAttribLocation;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDetachShader;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetIntegerv;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;


public final class Program {
    private static Pattern vertexShaderAttributePattern;
    private static Pattern shaderUniformPattern;
    private static Pattern shaderUniformGroupPattern;

    private static int _maxGenericAttributes = -1;


    private static int _nextGenericAttributeIndex = 0;

    static {
        vertexShaderAttributePattern = Pattern.compile("\\A\\s*attribute\\s+([a-zA-Z0-9]+)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;.*\\z");
        shaderUniformPattern = Pattern.compile("\\A\\s*uniform\\s+([a-zA-Z0-9]+)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;.*\\z");
        shaderUniformGroupPattern = Pattern.compile("\\A\\s*uniform\\s+([a-zA-Z0-9]+)\\s+([a-zA-Z_][a-zA-Z0-9_]*\\s*(\\s*,\\s*([a-zA-Z_][a-zA-Z0-9_]*))+)\\s*;.*\\z");
    }

    private int id;
    private ArrayList<AttributeReference> attributes;
    private ArrayList<Uniform> uniforms;

    public Program(Context context, String vertexShaderFileName, String fragmentShaderFileName) throws InitializationException {
        int[] shaderCompileStatusIntegers = new int[2];
        int[] shaderLinkStatusIntegers = new int[1];
        int vertexShader;
        int fragmentShader;
        String vertexShaderSource;
        String fragmentShaderSource;

        attributes = new ArrayList<AttributeReference>();
        uniforms = new ArrayList<Uniform>();

        shaderCompileStatusIntegers[0] = -1;
        shaderCompileStatusIntegers[1] = -1;
        shaderLinkStatusIntegers[0] = -1;

        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

        try {
            vertexShaderSource = AssetFileReader.getAssetFileAsString(context, vertexShaderFileName);
        } catch (IOException e) {
            throw new InitializationException(e.getMessage());
        }

        glShaderSource(vertexShader, vertexShaderSource);

        try {
            fragmentShaderSource = AssetFileReader.getAssetFileAsString(context, fragmentShaderFileName);
        } catch (IOException e) {
            throw new InitializationException(e.getMessage());
        }

        glShaderSource(fragmentShader, fragmentShaderSource);

        glCompileShader(vertexShader);

        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, shaderCompileStatusIntegers, 0);

        if (shaderCompileStatusIntegers[0] == GL_FALSE) {
            String vertexShaderInfoLog = glGetShaderInfoLog(vertexShader);

            glDeleteShader(vertexShader);

            throw new GLCustomShaderException(vertexShaderInfoLog, vertexShaderSource);
        }

        glCompileShader(fragmentShader);

        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, shaderCompileStatusIntegers, 1);

        if (shaderCompileStatusIntegers[1] == GL_FALSE) {
            String fragmentShaderInfoLog = glGetShaderInfoLog(fragmentShader);

            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);


            throw new GLCustomShaderException(fragmentShaderInfoLog, fragmentShaderSource);
        }

        id = glCreateProgram();

        glAttachShader(id, vertexShader);
        glAttachShader(id, fragmentShader);

        inflateAttributes(vertexShaderSource);

        for (AttributeReference attributeReference : attributes) {
            glBindAttribLocation(id, attributeReference.getIndex(), attributeReference.getName());
        }

        glLinkProgram(id);

        glGetProgramiv(id, GL_LINK_STATUS, shaderLinkStatusIntegers, 0);

        if (shaderLinkStatusIntegers[0] == GL_FALSE) {
            String programLinkInfoLog = glGetProgramInfoLog(id);

            glDeleteProgram(id);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);

            throw new GLCustomException(glGetError(), programLinkInfoLog);
        }

        glDetachShader(id, vertexShader);
        glDetachShader(id, fragmentShader);

        inflateUniforms(vertexShaderSource);
        inflateUniforms(fragmentShaderSource);

        for (Uniform uniform : uniforms) {
            uniform.setUniformLocation(glGetUniformLocation(id, uniform.getName()));
        }

    }

    private static int _getMaxGenericAttributes() {
        if (_maxGenericAttributes == -1) {
            int[] maxAttribs = new int[1];
            glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, maxAttribs, 0);
            _maxGenericAttributes = maxAttribs[0];
        }

        return _maxGenericAttributes;
    }

    void bind() {
        glUseProgram(id);
    }

    int getId() {
        return this.id;
    }

    private void inflateUniforms(String shaderSource) throws InitializationException {
        Matcher uniformMatcher = null;

        Scanner scanner = new Scanner(shaderSource);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (uniformMatcher == null)
                uniformMatcher = shaderUniformPattern.matcher(line);
            else
                uniformMatcher.reset(line);

            if (uniformMatcher.matches()) {
                boolean duplicate = false;

                String uniformTypeName = uniformMatcher.group(1);
                String uniformName = uniformMatcher.group(2);

                for (Uniform uniform : uniforms) {
                    if (uniform.getTypeName().equals(uniformTypeName) && uniform.getName().equals(uniformName)) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    uniforms.add(new Uniform(uniformTypeName, uniformName));
                }
            } else {
                Matcher uniformGroupMatcher = shaderUniformGroupPattern.matcher(line);

                if (uniformGroupMatcher.matches()) {
                    String variableNamesTogether = uniformGroupMatcher.group(2);

                    String[] parts = variableNamesTogether.split("\\s*,\\s*");

                    for (String part : parts) {
                        boolean duplicate = false;

                        String uniformTypeName = uniformGroupMatcher.group(1);

                        for (Uniform uniform : uniforms) {
                            if (uniform.getTypeName().equals(uniformTypeName) && uniform.getName().equals(part)) {
                                duplicate = true;
                                break;
                            }
                        }

                        if (!duplicate) {
                            uniforms.add(new Uniform(uniformTypeName, part));
                        }
                    }
                }
            }
        }
        scanner.close();
    }

    private void inflateAttributes(String vertexShaderSource) {
        Matcher attributePatternMatcher = null;

        Scanner scanner = new Scanner(vertexShaderSource);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (attributePatternMatcher == null)
                attributePatternMatcher = vertexShaderAttributePattern.matcher(line);
            else
                attributePatternMatcher.reset(line);

            if (attributePatternMatcher.matches()) {
                attributes.add(new AttributeReference(attributePatternMatcher.group(1), attributePatternMatcher.group(2)));
            }
        }
        scanner.close();
    }

    ArrayList<AttributeReference> getAttributeReferences() {
        return attributes;
    }

    ArrayList<Uniform> getUniforms() {
        return uniforms;
    }

    void unbind() {
        glUseProgram(0);
    }

    //region inner classes
    abstract class ShaderInputReference {
        private String _typeName;
        private String _name;

        ShaderInputReference(String typeName, String name) {
            this._typeName = typeName;
            this._name = name;
        }

        String getTypeName() {
            return _typeName;
        }

        String getName() {
            return _name;
        }

        public Class getValueType() {
            switch (getTypeName()) {
                case "float":
                case "vec2":
                case "vec3":
                case "vec4":
                case "mat2":
                case "mat3":
                case "mat4":
                    return float.class;
                case "bool":
                case "bvec2":
                case "bvec3":
                case "bvec4":
                    return boolean.class;
                case "int":
                case "ivec2":
                case "ivec3":
                case "ivec4":
                case "sampler2D":
                case "samplerCube":
                    return int.class;
                case "void":
                    return void.class;
            }

            return null;
        }

        public int getValuesCount() {
            switch (getTypeName()) {
                case "float":
                case "bool":
                case "int":
                case "sampler2D":
                case "samplerCube":
                    return 1;
                case "ivec2":
                case "vec2":
                case "bvec2":
                    return 2;
                case "ivec3":
                case "vec3":
                case "bvec3":
                    return 3;
                case "ivec4":
                case "vec4":
                case "bvec4":
                case "mat2":
                    return 4;
                case "mat3":
                    return 9;
                case "mat4":
                    return 16;
                case "void":
                    return 0;
            }

            return -1;
        }
    }

    final class Uniform extends ShaderInputReference implements StateVariable {
        private final Object valuesLock = new Object();
        private int _uniformLocation = -1;
        private float[] floatValues;
        private int[] intValues;
        private boolean[] boolValues;
        private boolean _hasChanged = true;

        Uniform(String typeName, String name) {
            super(typeName, name);
        }

        int getUniformLocation() {
            return _uniformLocation;
        }

        void setUniformLocation(int id) {
            this._uniformLocation = id;
        }

        public void setValues(float[] values) {
            synchronized (this.valuesLock) {
                this.floatValues = Arrays.copyOf(values, getValuesCount());

                setChanged();
            }
        }

        public void setValues(int[] values) {
            synchronized (this.valuesLock) {
                this.intValues = Arrays.copyOf(values, getValuesCount());

                setChanged();
            }
        }

        public void setValues(boolean[] values) {
            synchronized (this.valuesLock) {
                this.boolValues = Arrays.copyOf(values, getValuesCount());

                setChanged();
            }
        }

        public void setValue(float value) {
            synchronized (this.valuesLock) {
                this.floatValues = new float[1];
                this.floatValues[0] = value;

                setChanged();
            }
        }

        public void setValue(int value) {
            synchronized (this.valuesLock) {
                this.intValues = new int[1];
                this.intValues[0] = value;

                setChanged();
            }
        }

        public void setValue(boolean value) {
            synchronized (this.valuesLock) {
                this.boolValues = new boolean[1];
                this.boolValues[0] = value;

                setChanged();
            }
        }

        public String getDefinedName() {
            return getName();
        }

        public String getDefinedTypeName() {
            return getTypeName();
        }

        public void commitChange() {
            boolean committed = false;

            switch (getTypeName()) {
                case "mat4":
                    synchronized (this.valuesLock) {
                        glUniformMatrix4fv(getUniformLocation(), 1, false, floatValues, 0);
                        committed = true;
                    }
                    break;
            }

            if (committed)
                clearChanged();
        }

        public boolean hasChanged() {
            boolean returnValue;

            synchronized (this.valuesLock) {
                returnValue = _hasChanged;
            }

            return returnValue;
        }

        private void setChanged() {
            synchronized (this.valuesLock) {
                _hasChanged = true;
            }
        }

        private void clearChanged() {
            synchronized (this.valuesLock) {
                _hasChanged = false;
            }
        }
    }

    final class AttributeReference extends ShaderInputReference {

        private int _genericVertexAttributeIndex = -1;

        AttributeReference(String typeName, String name) {
            super(typeName, name);

            if (_nextGenericAttributeIndex >= _getMaxGenericAttributes())
                throw new GLCustomException(glGetError(), "Maximum allowed generic attriubtes allocated!");

            _genericVertexAttributeIndex = _nextGenericAttributeIndex;

            _nextGenericAttributeIndex++;
        }


        int getIndex() {
            return _genericVertexAttributeIndex;
        }
    }
    //endregion
}
