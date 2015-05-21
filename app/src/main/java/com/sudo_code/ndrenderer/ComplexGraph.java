package com.sudo_code.ndrenderer;

import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ComplexGraph {
    private static final int BYTES_PER_FLOAT = 4;

    private int   mPosHandle;
    private float mProjectionConstant;

    private int mDensity;   //The number of points along each axis
    private int mViewSize;

    private int mVAO;
    private int mVBO;

    private float[] mPoints;
    private float[] mPoints3d;
    private FloatBuffer mPointsNativeBuffer;

    public ComplexGraph(int density, int viewSize, float projectionConstant, int posHandle) {
        mDensity = density;
        mViewSize = viewSize;
        mProjectionConstant = projectionConstant;
        mPosHandle = posHandle;

        mPoints   = new float[mDensity * mDensity * 4];
        mPoints3d = new float[mDensity * mDensity * 3];

        genPoints();
        updateProjection();
        genNativeBuffer();
        genVBO();
        genVAO();
    }

    private void genVBO() {
        int[] VBOs = new int[1];
        GLES30.glGenBuffers(1, VBOs, 0);

        mVBO = VBOs[0];

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBO);

        GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                mPointsNativeBuffer.capacity() * BYTES_PER_FLOAT,
                mPointsNativeBuffer,
                GLES30.GL_STREAM_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    private void genVAO() {
        int[] VAOArray = new int[1];
        GLES30.glGenVertexArrays(1, VAOArray, 0);
        mVAO = VAOArray[0];

        GLES30.glBindVertexArray(mVAO);

        GLES30.glEnableVertexAttribArray(mPosHandle);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBO);
        GLES30.glVertexAttribPointer(
                mPosHandle,
                3,
                GLES30.GL_FLOAT,
                false,
                0,
                0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        GLES30.glBindVertexArray(0);
    }

    private void genNativeBuffer() {
        mPointsNativeBuffer = ByteBuffer.allocateDirect(
                mPoints3d.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mPointsNativeBuffer.put(mPoints3d);
        mPointsNativeBuffer.position(0);
    }

    private void updateProjection() {
        for (int pointI = 0; pointI < mPoints.length / 4; pointI++) {  //Point iterator
            float[] point = new float[4];
            System.arraycopy(mPoints, pointI * 4, point, 0, 4);

            for (int dim = 3; dim > 2; dim--) {    //Dimension we're projecting from (only project once)
                for (int comp = 0; comp < dim - 1; comp++) {    //Component we're updating
                    point[comp] *= mProjectionConstant / (mProjectionConstant + point[dim]);
                }
            }

            mPoints3d[pointI * 3 + 0] = point[0];
            mPoints3d[pointI * 3 + 1] = point[1];
            mPoints3d[pointI * 3 + 2] = point[2];
        }
    }

    private void genPoints() {
        for (int realIter = 0; realIter < mDensity; realIter++) {
            for (int imagIter = 0; imagIter < mDensity; imagIter++) {
                float real = (realIter - (float) mDensity / 2.f) / (float) mDensity * (float) mViewSize;
                float imag = (imagIter - (float) mDensity / 2.f) / (float) mDensity * (float) mViewSize;

                ComplexNumber input = new ComplexNumber(real, imag);

                //input.add()
                input.mult(input);


                mPoints[(realIter * mDensity + imagIter) * 4 + 0] = real;
                mPoints[(realIter * mDensity + imagIter) * 4 + 1] = imag;
                mPoints[(realIter * mDensity + imagIter) * 4 + 2] = input.getReal();
                mPoints[(realIter * mDensity + imagIter) * 4 + 3] = input.getImaginary();
            }
        }
    }

    private void updateNativeBuffer() {
        mPointsNativeBuffer.put(mPoints3d);
        mPointsNativeBuffer.position(0);
    }

    private void updateVBO() {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBO);

        GLES30.glBufferSubData(
                GLES30.GL_ARRAY_BUFFER,
                0,
                mPointsNativeBuffer.capacity() * BYTES_PER_FLOAT,
                mPointsNativeBuffer);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    public void rotate(float angle, int[] rotationPlane) {
        for (int vertI = 0; vertI < mPoints.length; vertI += 4) {
            float[] point = new float[4];
            System.arraycopy(mPoints, vertI, point, 0, 4);
            point = NDVector.rotate(point, angle, rotationPlane);
            System.arraycopy(point, 0, mPoints, vertI, 4);
        }
    }

    public void draw() {
        updateProjection();
        updateNativeBuffer();
        updateVBO();

        GLES30.glBindVertexArray(mVAO);
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, mDensity * mDensity);
        GLES30.glBindVertexArray(0);
    }
}