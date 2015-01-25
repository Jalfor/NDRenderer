package com.sudo_code.ndrenderer;

import android.app.Activity;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

public class GLUtils {

    private static final String TAG = "GLUtils";

    static final int BYTES_PER_FLOAT = 4;

    /**
    * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
    *
    * @param label Label to report in case of error.
    */
    public static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
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
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        //Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        //If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    public static int genProgram(int[] shaders) {
        int program = GLES20.glCreateProgram();

        for (int i = 0; i < shaders.length; i++) {
            GLES20.glAttachShader(program, shaders[i]);
        }

        GLES20.glLinkProgram(program);

        for (int i = 0; i < shaders.length; i++) {
            GLES20.glDetachShader(program, shaders[i]);
            GLES20.glDeleteShader(shaders[i]);
        }

        GLUtils.checkGLError("Program");

        return program;
    }

    public static int[] genVBO(float[] data) {
        final int[] VBO = new int[1];

        GLES20.glGenBuffers(1, VBO, 0);

        FloatBuffer nativeBuffer = ByteBuffer.allocateDirect(data.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        nativeBuffer.put(data);
        nativeBuffer.position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBO[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, nativeBuffer.capacity() * BYTES_PER_FLOAT, nativeBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        nativeBuffer.limit(0);  //These two lines I think will make it...more likely that
        nativeBuffer = null;        //it'll be garbage collected

        return VBO;
    }
}