package com.sudo_code.ndrenderer;

import android.opengl.GLES30;

import org.apache.commons.math3.util.CombinatoricsUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Hypercube {

    private static final int BYTES_PER_INT = 4;
    private static final int BYTES_PER_FLOAT = 4;

    private float[] mVertices;  //In the form x,y,z,w...x,y etc.
    private float[] mPrevVertices;
    private float[] mNextVertices;
    private int[]   mIndices;

    private FloatBuffer mNativeVertBuffer;
    private IntBuffer   mNativeIndexBuffer;

    private int VAO;
    private int mVertVBO;
    private int mIndexVBO;

    private int mDimensions;
    private int mVertexHandle;
    private int mPrevVertexHandle;
    private int mNextVertexHandle;

    /**
     * Initializes the hypercube
     *
     * @param dimensions The number of dimensions the hypercube should have
     * @param projectionConstant The camera's distance to the hypervolume of projection (MUST BE GREATER THAN 1)
     * @param vertexHandle The attribute index of the vertex position
     * @param prevVertexHandle The attribute index of the previous vertex's position
     * @param nextVertexHandle The attribute index of the next vertex's position
     */
    public Hypercube(int dimensions, float projectionConstant, int vertexHandle, int prevVertexHandle, int nextVertexHandle) {
        mDimensions       = dimensions;
        mVertexHandle     = vertexHandle;
        mPrevVertexHandle = prevVertexHandle;
        mNextVertexHandle = nextVertexHandle;

        genVertexData();
        genNativeBuffers();
        genVBOs();
        genVAO();
    }

    /**
     * Generates two native buffers from the Java arrays, one containing the vertex data and the
     * other containing the draw indices
     */
    private void genNativeBuffers() {
        mNativeVertBuffer = ByteBuffer.allocateDirect(
                (mVertices.length + mPrevVertices.length + mNextVertices.length) * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mNativeIndexBuffer = ByteBuffer.allocateDirect(
                (mIndices.length) * BYTES_PER_INT)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        mNativeVertBuffer.put(mVertices);
        mNativeVertBuffer.put(mPrevVertices);
        mNativeVertBuffer.put(mNextVertices);
        mNativeIndexBuffer.put(mIndices);

        mNativeVertBuffer.position(0);
        mNativeIndexBuffer.position(0);
    }

    /**
     * Generates the vertex buffer objects containing the vertex data and the draw indices
     */
    private void genVBOs() {
        int[] VBOs = new int[2];
        GLES30.glGenBuffers(2, VBOs, 0);

        mVertVBO = VBOs[0];
        mIndexVBO = VBOs[1];

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertVBO);
        GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                mNativeVertBuffer.capacity() * BYTES_PER_FLOAT,
                mNativeVertBuffer,
                GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mIndexVBO);
        GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                mNativeIndexBuffer.capacity() * BYTES_PER_INT,
                mNativeIndexBuffer,
                GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Generates and initializes the vertex array object that will be used to draw the hypercube
     */
    private void genVAO() {
        int[] VAOArray = new int[1];
        GLES30.glGenVertexArrays(1, VAOArray, 0);
        VAO = VAOArray[0];

        GLES30.glBindVertexArray(VAO);

        GLES30.glEnableVertexAttribArray(mVertexHandle);
        GLES30.glEnableVertexAttribArray(mPrevVertexHandle);
        GLES30.glEnableVertexAttribArray(mNextVertexHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertVBO);

        GLES30.glVertexAttribPointer(
                mVertexHandle,
                BYTES_PER_FLOAT,
                GLES30.GL_FLOAT,
                false,
                0,
                0);
        GLES30.glVertexAttribPointer(
                mPrevVertexHandle,
                BYTES_PER_FLOAT,
                GLES30.GL_FLOAT,
                false,
                0,
                mVertices.length * BYTES_PER_FLOAT);
        GLES30.glVertexAttribPointer(
                mNextVertexHandle,
                BYTES_PER_FLOAT,
                GLES30.GL_FLOAT,
                false,
                0,
                (mVertices.length + mPrevVertices.length) * BYTES_PER_FLOAT);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mIndexVBO);

        GLES30.glBindVertexArray(0);
    }

    /**
     * Draws the hypercube to the screen
     */
    public void draw() {
        GLES30.glBindVertexArray(VAO);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndices.length, GLES30.GL_UNSIGNED_INT, 0);
        GLES30.glBindVertexArray(0);
    }

    /**
     * Iterates currentLockedAxes to the next combination of locked axes.
     * The locked axes are the axes that will remain fixed in drawing a square
     *
     * @param currentLockedAxes An array with the index of each axis that is currently locked
     * @return Whether or not there is a next combination
     */
    private boolean nextLockedAxes(int[] currentLockedAxes) {
        for (int i = currentLockedAxes.length - 1; i >= 0; i--) {
            if (currentLockedAxes[i] < i + 2) {
                currentLockedAxes[i]++;

                for (int resetI = i + 1; resetI < currentLockedAxes.length; resetI++) {
                    currentLockedAxes[resetI] = currentLockedAxes[i] + resetI - i;
                }

                return true;
            }
        }

        return false;   //We're at the last one
    }

    /**
     * Iterates to the next combination of values of 1.f and -1.f in binary counting
     *
     * @param currentLockedAxesValues An array with the values of each axis that is currently locked
     * @return Whether or not there is a next combination
     */
    private boolean nextLockedAxesValues(float[] currentLockedAxesValues) {
        for (int i = 0; i < currentLockedAxesValues.length; i++) {
            if (currentLockedAxesValues[i] == -1.f) {
                currentLockedAxesValues[i] = 1.f;

                for (int wipeI = i - 1; wipeI >= 0; wipeI--) {
                    currentLockedAxesValues[wipeI] = -1.f;
                }

                return true;
            }
        }

        return false;   //We're at the last one
    }

    /**
     * Generates the unlocked axes (the ones the square will lie on) from the locked axes
     *
     * @param currentLockedAxes An array with the index of each axis that is currently locked
     * @return The unlocked axes (the axes the square will lie on)
     */
    private int[] getUnlockedAxes(int[] currentLockedAxes) {
        int[] unlockedAxes = new int[2];    //A square lies on two axes
        int unlockedAxesIndex = 0;

        for (int checkLocked = 0; checkLocked < currentLockedAxes.length + 2; checkLocked++) {
            boolean found = false;

            for (int i = 0; i < currentLockedAxes.length; i++) {
                if (currentLockedAxes[i] == checkLocked) {
                    found = true;
                }
            }

            if (!found) {
                unlockedAxes[unlockedAxesIndex] = checkLocked;
                unlockedAxesIndex++;
            }
        }

        return unlockedAxes;
    }

    /**
     * Generates the vertex data for the hypercube (arrays of vertices, indices and normals)
     */
    private void genVertexData() {
        //4 * number of faces vertices, each with mDimensions components
        mVertices = new float[(int) (CombinatoricsUtils.binomialCoefficient(mDimensions, 2) *
                                     Utils.powI(2, mDimensions - 2) * 4) * mDimensions];

        //4 * number of faces vertices, each with mDimensions components
        mPrevVertices = new float[(int) (CombinatoricsUtils.binomialCoefficient(mDimensions, 2) *
                                         Utils.powI(2, mDimensions - 2) * 4) * mDimensions];

        //4 * number of faces vertices, each with mDimensions components
        mNextVertices = new float[(int) (CombinatoricsUtils.binomialCoefficient(mDimensions, 2) *
                                         Utils.powI(2, mDimensions - 2) * 4) * mDimensions];

        //6 * number of faces vertices (2 triangles per face * 3 points per triangle)
        mIndices = new int[(int) CombinatoricsUtils.binomialCoefficient(mDimensions, 2) *
                                 Utils.powI(2, mDimensions - 2) * 6];

        int[] lockedAxes = new int[mDimensions - 2];
        float[] lockedAxesValues = new float[mDimensions - 2];

        for (int i = 0; i < mDimensions - 2; i++) {
            lockedAxes[i] = i;
        }

        int vertFaceStartI = 0;
        int indexFaceStartI = 0;

        //loop through all the combinations of locked axes
        do {
            int[] unlockedAxes = getUnlockedAxes(lockedAxes);

            for (int i = 0; i < mDimensions - 2; i++) {
                lockedAxesValues[i] = -1.f;
            }

            //loop through all the combinations of values of the locked axes
            do {
                for (int vertexI = 0; vertexI < 4; vertexI++) { //Fill in all the locked axes
                    for (int lockedAxesI = 0; lockedAxesI < lockedAxes.length; lockedAxesI++) {
                        mVertices[(vertFaceStartI + vertexI) * mDimensions + lockedAxes[lockedAxesI]] = lockedAxesValues[lockedAxesI];
                    }
                }

                mVertices[(vertFaceStartI + 0) * mDimensions + unlockedAxes[0]] = -1.f;
                mVertices[(vertFaceStartI + 0) * mDimensions + unlockedAxes[1]] = -1.f;

                mVertices[(vertFaceStartI + 1) * mDimensions + unlockedAxes[0]] = -1.f;
                mVertices[(vertFaceStartI + 1) * mDimensions + unlockedAxes[1]] = 1.f;

                mVertices[(vertFaceStartI + 2) * mDimensions + unlockedAxes[0]] = 1.f;
                mVertices[(vertFaceStartI + 2) * mDimensions + unlockedAxes[1]] = 1.f;

                mVertices[(vertFaceStartI + 3) * mDimensions + unlockedAxes[0]] = 1.f;
                mVertices[(vertFaceStartI + 3) * mDimensions + unlockedAxes[1]] = -1.f;

                mIndices[indexFaceStartI + 0] = vertFaceStartI + 0;
                mIndices[indexFaceStartI + 1] = vertFaceStartI + 1;
                mIndices[indexFaceStartI + 2] = vertFaceStartI + 3;

                mIndices[indexFaceStartI + 3] = vertFaceStartI + 1;
                mIndices[indexFaceStartI + 4] = vertFaceStartI + 2;
                mIndices[indexFaceStartI + 5] = vertFaceStartI + 3;

                for (int i = 0; i < 4; i++) {
                    System.arraycopy(   //Previous vertex
                            mVertices,
                            (vertFaceStartI + ((i + 3) % 4)) * mDimensions, //It's really (i - 1)
                            mPrevVertices,                                    //but Java's mod is a remainder
                            (vertFaceStartI + i) * mDimensions,
                            mDimensions);

                    System.arraycopy(   //Next vertex
                            mVertices,
                            (vertFaceStartI + ((i + 1) % 4)) * mDimensions,
                            mNextVertices,
                            (vertFaceStartI + i) * mDimensions,
                            mDimensions);
                }

                vertFaceStartI += 4;
                indexFaceStartI += 6;

            } while (nextLockedAxesValues(lockedAxesValues));
        } while (nextLockedAxes(lockedAxes));
    }
}