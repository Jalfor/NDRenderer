package com.sudo_code.ndrenderer;

import android.app.Activity;
import android.opengl.GLES30;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Utils {

    private static final String TAG = "Utils";

    /**
     * Projects a vertex down a dimension
     *
     * @param vertex The vertex to be projected
     * @param projectionConstant The camera's distance to the hypervolume of projection (MUST BE GREATER THAN GREATEST EXTENT OF SHAPE)
     */
    public static float[] projectDown(float[] vertex, float projectionConstant) {
        float[] projVertex = new float[vertex.length - 1];

        for (int i = 0; i < vertex.length - 1; i++) {
            projVertex[i] = vertex[i] * (projectionConstant / (projectionConstant +  vertex[vertex.length - 1]));
        }

        return projVertex;
    }

    /**
     * Projects a vertex down to 3D
     *
     * @param vertex The vertex to be projected
     * @param projectionConstant The camera's distance to the hypervolume of projection (MUST BE GREATER THAN GREATEST EXTENT OF SHAPE)
     */
    public static float[] projectTo3D(float[] vertex, float projectionConstant) {
        float[] projVertex = vertex.clone();

        while (projVertex.length > 3) {
            projVertex = projectDown(projVertex, projectionConstant);
        }

        return projVertex;
    }

    /**
     * Integer exponentiation by squaring
     *
     * @param base Base of exponentiation
     * @param exponent of exponentiation (must be >= 0)
     */
    public static int powI(int base, int exponent) {
        int result = 1;

        while (exponent != 0) {
            if ((exponent & 1) != 0) {
                result *= base;
            }

            exponent >>= 1;
            base *= base;
        }

        return result;
    }

    /**
    * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
    *
    * @param label Label to report in case of error.
    */
    public static void checkGLError(String label) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Converts a raw text file into a string.
     *
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    public static String readRawTextFile(int resId, Activity activity) {
        InputStream inputStream = activity.getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int genShader(int type, int resId, Activity activity) {
        String code = readRawTextFile(resId, activity);
        int shader = GLES30.glCreateShader(type);

        GLES30.glShaderSource(shader, code);
        GLES30.glCompileShader(shader);

        //Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);

        //If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    public static void delShaders(int[] shaders) {
        for (int i = 0; i < shaders.length; i++) {
            GLES30.glDeleteShader(shaders[i]);
        }
    }

    public static int genProgram(int[] shaders) {
        int program = GLES30.glCreateProgram();

        for (int i = 0; i < shaders.length; i++) {
            GLES30.glAttachShader(program, shaders[i]);
        }

        GLES30.glLinkProgram(program);

        int[] isLinked = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, isLinked, 0);
        if(isLinked[0] == 0)
        {
            Log.i(TAG, "Error linking program: " + GLES30.glGetProgramInfoLog(program));
            GLES30.glDeleteProgram(program);

            throw new RuntimeException("Error linking program.");
        }

        for (int i = 0; i < shaders.length; i++) {
            GLES30.glDetachShader(program, shaders[i]);
        }

        Utils.checkGLError("Program");

        return program;
    }
}