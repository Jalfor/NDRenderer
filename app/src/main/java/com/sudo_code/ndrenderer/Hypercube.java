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
    private int[]   mIndices;   //These are obviously the same in 3D

    private float[] mVertices3d;    //All this is in 3D
    private float[] mNormals;

    private FloatBuffer mNativeVertBuffer;
    private IntBuffer   mNativeIndexBuffer;

    private int mVAO;
    private int mVertVBO;
    private int mIndexVBO;

    private int   mDimensions;
    private float mProjectionConstant;
    private float mViewDist;
    private int   mVertexHandle;
    private int   mNormalHandle;

    private int mFaceCount;

    /**
     * Initializes the hypercube
     *
     * @param dimensions The number of dimensions the hypercube should have
     * @param projectionConstant The camera's distance to the hypervolume of projection (MUST BE GREATER THAN 1)
     * @param viewDist The distance from the 2D camera to the center of projection
     * @param vertexHandle The attribute index of the vertex position
     * @param normalHandle The attribute index of the vertex normal
     */
    public Hypercube(int dimensions, float projectionConstant, float viewDist, int vertexHandle, int normalHandle) {
        mDimensions         = dimensions;
        mProjectionConstant = projectionConstant;
        mViewDist           = viewDist;
        mVertexHandle       = vertexHandle;
        mNormalHandle       = normalHandle;

        mFaceCount          = (int) (CombinatoricsUtils.binomialCoefficient(mDimensions, 2) *
                                     Utils.powI(2, mDimensions - 2));

        //4 * number of faces vertices, each with mDimensions components
        mVertices   = new float[mFaceCount * 4 * mDimensions];

        //4 * number of faces * 3 components
        mVertices3d = new float[mFaceCount * 4 * 3];

        //4 * number of faces vertices, each with 3 components
        mNormals    = new float[mFaceCount * 4 * 3];

        //6 * number of faces vertices (2 triangles per face * 3 points per triangle)
        mIndices    = new int[mFaceCount * 6];

        genVertexData();
        updateProjection();
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
                (mVertices3d.length + mNormals.length) * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mNativeIndexBuffer = ByteBuffer.allocateDirect(
                (mIndices.length) * BYTES_PER_INT)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        mNativeVertBuffer.put(mVertices3d);
        mNativeVertBuffer.put(mNormals);
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
                GLES30.GL_STREAM_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mIndexVBO);

        GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                mNativeIndexBuffer.capacity() * BYTES_PER_INT,
                mNativeIndexBuffer,
                GLES30.GL_STREAM_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Update vertex native buffer
     */
    private void updateNativeBuffers() {
        mNativeVertBuffer.put(mVertices3d);
        mNativeVertBuffer.put(mNormals);
        mNativeIndexBuffer.put(mIndices);

        mNativeVertBuffer.position(0);
        mNativeIndexBuffer.position(0);
    }

    /**
     * Updates the vertex buffer object containing the a vertex positions and normals
     */
    private void updateVBOs() {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertVBO);

        GLES30.glBufferSubData(
                GLES30.GL_ARRAY_BUFFER,
                0,
                mNativeVertBuffer.capacity() * BYTES_PER_FLOAT,
                mNativeVertBuffer);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mIndexVBO);

        GLES30.glBufferSubData(
                GLES30.GL_ARRAY_BUFFER,
                0,
                mNativeIndexBuffer.capacity() * BYTES_PER_INT,
                mNativeIndexBuffer);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Generates and initializes the vertex array object that will be used to draw the hypercube
     */
    private void genVAO() {
        int[] VAOArray = new int[1];
        GLES30.glGenVertexArrays(1, VAOArray, 0);
        mVAO = VAOArray[0];

        GLES30.glBindVertexArray(mVAO);

        GLES30.glEnableVertexAttribArray(mVertexHandle);
        GLES30.glEnableVertexAttribArray(mNormalHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertVBO);

        GLES30.glVertexAttribPointer(
                mVertexHandle,
                3,
                GLES30.GL_FLOAT,
                false,
                0,
                0);
        GLES30.glVertexAttribPointer(
                mNormalHandle,
                3,
                GLES30.GL_FLOAT,
                false,
                0,
                mVertices3d.length * BYTES_PER_FLOAT);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mIndexVBO);

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

                vertFaceStartI += 4;
                indexFaceStartI += 6;

            } while (nextLockedAxesValues(lockedAxesValues));
        } while (nextLockedAxes(lockedAxes));
    }

    /**
     * Updates the mNormals array based on mVertices3d (note that inwards and outwards are
     * meaningless concepts when with a projection
     */
    private void updateNormals() {
        for (int indexFaceStartI = 0; indexFaceStartI < mIndices.length; indexFaceStartI += 6) {   //index triangle start index
            float[] vertex1 = new float[3];
            float[] vertex2 = new float[3];
            float[] vertex3 = new float[3];

            System.arraycopy(mVertices3d, mIndices[indexFaceStartI + 0] * 3, vertex1, 0, 3);
            System.arraycopy(mVertices3d, mIndices[indexFaceStartI + 1] * 3, vertex2, 0, 3);
            System.arraycopy(mVertices3d, mIndices[indexFaceStartI + 2] * 3, vertex3, 0, 3);

            float[] triSide1 = NDVector.sub(vertex2, vertex1);
            float[] triSide2 = NDVector.sub(vertex3, vertex1);

            float[] normal = NDVector.normalize(NDVector.cross(triSide1, triSide2));

            System.arraycopy(normal, 0, mNormals, mIndices[indexFaceStartI + 0] * 3, 3);
            System.arraycopy(normal, 0, mNormals, mIndices[indexFaceStartI + 1] * 3, 3);
            System.arraycopy(normal, 0, mNormals, mIndices[indexFaceStartI + 2] * 3, 3);

            System.arraycopy(normal, 0, mNormals, mIndices[indexFaceStartI + 3] * 3, 3);
            System.arraycopy(normal, 0, mNormals, mIndices[indexFaceStartI + 4] * 3, 3);
            System.arraycopy(normal, 0, mNormals, mIndices[indexFaceStartI + 5] * 3, 3);
        }
    }

    /**
     * Updates the mVertices3d array as a projection of mVertices
     */
    private void updateProjection() {
        for (int vertI = 0; vertI < mFaceCount * 4; vertI++) {  //Vertices
            float[] vertex = new float[mDimensions];
            System.arraycopy(mVertices, vertI * mDimensions, vertex, 0, mDimensions);

            for (int dim = mDimensions - 1; dim > 2; dim--) {    //Dimension we're projecting from
                for (int comp = 0; comp < dim - 1; comp++) {    //Component we're updating
                    vertex[comp] *= mProjectionConstant / (mProjectionConstant + vertex[dim]);
                }
            }

            mVertices3d[vertI * 3 + 0] = vertex[0];
            mVertices3d[vertI * 3 + 1] = vertex[1];
            mVertices3d[vertI * 3 + 2] = vertex[2];// - mViewDist;

            mNormals[vertI * 3 + 0] = 0;
            mNormals[vertI * 3 + 1] = 0;
            mNormals[vertI * 3 + 2] = 0;
        }

        updateNormals();
        sortFaces();
    }

    /**
     * Rearranges mIncices so that it's in order by z coordinates of the center of the faces
     */
    private void sortFaces() {
        float[] faceDists = new float[mFaceCount];  //So we don't recalculate these multiple times

        for (int faceI = 0; faceI < faceDists.length; faceI++) {
            faceDists[faceI] += (float) Math.pow((mVertices3d[mIndices[faceI * 6 + 1] * 3 + 0] +
                                                  mVertices3d[mIndices[faceI * 6 + 3] * 3 + 0]) / 2.f, 2.f);
            faceDists[faceI] += (float) Math.pow((mVertices3d[mIndices[faceI * 6 + 1] * 3 + 1] +
                                                  mVertices3d[mIndices[faceI * 6 + 3] * 3 + 1]) / 2.f, 2.f);
            faceDists[faceI] += (float) Math.pow((mVertices3d[mIndices[faceI * 6 + 1] * 3 + 2] - mViewDist +
                                                  mVertices3d[mIndices[faceI * 6 + 3] * 3 + 2] - mViewDist) / 2.f, 2.f);

            faceDists[faceI] = (float) Math.sqrt(faceDists[faceI]);
        }

        for (int faceI = 0; faceI < mFaceCount - 1; faceI++) {
            int farthestFaceI = faceI;
            float farthestFaceDist = faceDists[faceI];

            for (int faceI2 = faceI + 1; faceI2 < mFaceCount; faceI2++) {   //Find the minimum
                float faceI2Dist = faceDists[faceI2];

                if (faceI2Dist > farthestFaceDist) {
                    farthestFaceI = faceI2;
                    farthestFaceDist = faceI2Dist;
                }
            }

            int[] temp = new int[6];    //Swap the faces
            System.arraycopy(mIndices, faceI * 6, temp, 0, 6);
            System.arraycopy(mIndices, farthestFaceI * 6, mIndices, faceI * 6, 6);
            System.arraycopy(temp, 0, mIndices, farthestFaceI * 6, 6);

            float temp2 = faceDists[faceI];
            faceDists[faceI] = faceDists[farthestFaceI];
            faceDists[farthestFaceI] = temp2;
        }
    }

    /**
     * Rotates the hypercube on the rotationPlane plane by an angle of angle radians
     * @param angle The angle the vector is rotated by (in radians)
     * @param rotationPlane The plane of rotation (a length two vector containing the dimensions
                            of the rotation plane, e.g. XZ would be {0, 2})
     */
    public void rotate(float angle, int[] rotationPlane) {
        for (int vertI = 0; vertI < mVertices.length; vertI += mDimensions) {
            float[] vertex = new float[mDimensions];
            System.arraycopy(mVertices, vertI, vertex, 0, mDimensions);
            vertex = NDVector.rotate(vertex, angle, rotationPlane);
            System.arraycopy(vertex, 0, mVertices, vertI, mDimensions);
        }
    }

    /**
     * Draws the hypercube to the screen
     */
    public void draw() {
        updateProjection();
        updateNativeBuffers();
        updateVBOs();

        GLES30.glBindVertexArray(mVAO);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndices.length, GLES30.GL_UNSIGNED_INT, 0);
        GLES30.glBindVertexArray(0);
    }
}