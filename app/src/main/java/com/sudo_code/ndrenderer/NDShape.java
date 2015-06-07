package com.sudo_code.ndrenderer;

import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class NDShape {
    private static final int BYTES_PER_INT = 4;
    private static final int BYTES_PER_FLOAT = 4;

    protected float[] mVertices;  //In the form x,y,z,w...x,y etc.
    protected int[]   mIndices;   //These are obviously the same in 3D

    protected float[] mVertices3d;    //All this is in 3D
    protected float[] mSecondaryData;

    private FloatBuffer mNativeVertBuffer;
    private IntBuffer   mNativeIndexBuffer;

    private int mVAO;
    private int mVertVBO;
    private int mIndexVBO;

    protected int   mDimensions;
    protected float mProjectionConstant;
    private   float mViewDist;
    private   int   mPosHandle;     //Vertex attribute indices
    private   int   mSecondaryHandle;

    protected int mFaceCount;

    /**
     * Initializes the NDShape
     *
     * @param dimensions The number of dimensions the NDShape should have
     * @param projectionConstant The camera's distance to the NDShape of projection (MUST BE GREATER THAN 1)
     * @param viewDist The distance from the 2D camera to the center of projection
     * @param posHandle The attribute index of the vertex position
     * @param secondayHandle The attribute index of the vertex normal
     */
    public NDShape(int dimensions, float projectionConstant, float viewDist, int posHandle, int secondayHandle) {
        mDimensions         = dimensions;
        mProjectionConstant = projectionConstant;
        mViewDist           = viewDist;
        mPosHandle          = posHandle;
        mSecondaryHandle    = secondayHandle;
    }

    /**
     * Allocates and generates the primary and secondary vertex data as well as the indices
     */
    protected abstract void genVertexData();

    /**
     * Updates the secondary vertex data (this is probably going to be normals or colors which
     * could need to be regenerated while the primary data is position data in model space so
     * doesn't change)
     */
    protected abstract void updateSecondaryData();

    /**
     * MUST be called from subclass constructors. You might ask why this isn't in the constructor,
     * but Java, in it's infinite wisdom doesn't let you modify variables before calling the
     * super constructor, making it pretty much impossible to modify superclass initialization behaviour
     */
    protected void init() {
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
                (mVertices3d.length + mSecondaryData.length) * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mNativeIndexBuffer = ByteBuffer.allocateDirect(
                (mIndices.length) * BYTES_PER_INT)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        mNativeVertBuffer.put(mVertices3d);
        mNativeVertBuffer.put(mSecondaryData);
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
        mNativeVertBuffer.put(mSecondaryData);
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
     * Generates and initializes the vertex array object that will be used to draw the NDShape
     */
    private void genVAO() {
        int[] VAOArray = new int[1];
        GLES30.glGenVertexArrays(1, VAOArray, 0);
        mVAO = VAOArray[0];

        GLES30.glBindVertexArray(mVAO);

        GLES30.glEnableVertexAttribArray(mPosHandle);
        GLES30.glEnableVertexAttribArray(mSecondaryHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertVBO);

        GLES30.glVertexAttribPointer(
                mPosHandle,
                3,
                GLES30.GL_FLOAT,
                false,
                0,
                0);
        GLES30.glVertexAttribPointer(
                mSecondaryHandle,
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
     * Updates the mVertices3d array as a projection of mVertices
     */
    private void updateProjection() {
        for (int vertI = 0; vertI < mVertices.length / mDimensions; vertI++) {  //Vertices
            float[] vertex = new float[mDimensions];
            System.arraycopy(mVertices, vertI * mDimensions, vertex, 0, mDimensions);

            for (int dim = mDimensions - 1; dim > 2; dim--) {    //Dimension we're projecting from
                for (int comp = 0; comp < dim - 1; comp++) {    //Component we're updating
                    vertex[comp] *= mProjectionConstant / (mProjectionConstant + vertex[dim]);
                }
            }

            mVertices3d[vertI * 3 + 0] = vertex[0];
            mVertices3d[vertI * 3 + 1] = vertex[1];
            mVertices3d[vertI * 3 + 2] = vertex[2];
        }

        updateSecondaryData();
        //sortFaces();
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

        for (int faceI = 0; faceI < mFaceCount - 1; faceI++) {  //Selection sort
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
     * Rotates the NDShape on the rotationPlane plane by an angle of angle radians
     * @param angle The angle the vector is rotated by (in radians)
     * @param rotationPlane The plane of rotation (a length two vector containing the dimensions
     *                      of the rotation plane, e.g. XZ would be {0, 2})
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
     * Draws the NDShape to the screen
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
