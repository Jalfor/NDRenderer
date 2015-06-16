package com.sudo_code.ndrenderer;

public class ComplexGraph extends NDShape {

    private int   mDensity;   //The number of points along each axis
    private float mViewSize;

    public ComplexGraph(int density, float viewSize, float projectionConstant, float viewDist, int posHandle, int colorHandle) {
        super(4, projectionConstant, viewDist, posHandle, colorHandle);

        mDensity = density;
        mViewSize = viewSize;

        init();
    }

    /**
     * Generates the position and derivative values of the points on the complex graph
     */
    protected void genVertexData() {
        mVertices      = new float[mDensity * mDensity * 4];
        mVertices3d    = new float[mDensity * mDensity * 3];
        mSecondaryData = new float[mDensity * mDensity * 3];
        mIndices       = new int[(mDensity - 1) * (mDensity - 1) * 6];

        for (int realIter = 0; realIter < mDensity; realIter++) {
            for (int imagIter = 0; imagIter < mDensity; imagIter++) {

                float real = ((float) realIter - (float) (mDensity - 1) / 2.f) / (float) (mDensity - 1) * mViewSize;
                float imag = ((float) imagIter - (float) (mDensity - 1) / 2.f) / (float) (mDensity - 1) * mViewSize;

                int currentIndex = realIter * mDensity + imagIter;

                ComplexNumber input  = new ComplexNumber(real, imag);
                ComplexNumber result = new ComplexNumber(real, imag);
                ComplexNumber deriv  = new ComplexNumber(real, imag);

                deriv.mult(2.f);    //derivative of x^2 = 2x

                result.mult(input);

                mVertices[currentIndex * 4 + 0] = real;
                mVertices[currentIndex * 4 + 1] = imag;
                mVertices[currentIndex * 4 + 2] = result.getReal();
                mVertices[currentIndex * 4 + 3] = result.getImaginary();

                mSecondaryData[currentIndex * 3 + 0] = (float) Math.pow(Math.sin(deriv.getReal()), 2.f);
                mSecondaryData[currentIndex * 3 + 1] = (float) Math.pow(Math.sin(deriv.getImaginary()), 2.f);
                mSecondaryData[currentIndex * 3 + 2] = (float) Math.pow(Math.sin(deriv.getMod()), 2.f);
            }
        }

        for (int realIter = 0; realIter < mDensity - 1; realIter++) {
            for (int imagIter = 0; imagIter < mDensity - 1; imagIter++) {

                //First triangle
                mIndices[(realIter * (mDensity - 1) + imagIter) * 6 + 0] = (realIter + 0) * mDensity + imagIter + 0;
                mIndices[(realIter * (mDensity - 1) + imagIter) * 6 + 1] = (realIter + 1) * mDensity + imagIter + 0;
                mIndices[(realIter * (mDensity - 1) + imagIter) * 6 + 2] = (realIter + 1) * mDensity + imagIter + 1;

                //Second triangle
                mIndices[(realIter * (mDensity - 1) + imagIter) * 6 + 3] = (realIter + 0) * mDensity + imagIter + 0;
                mIndices[(realIter * (mDensity - 1) + imagIter) * 6 + 4] = (realIter + 0) * mDensity + imagIter + 1;
                mIndices[(realIter * (mDensity - 1) + imagIter) * 6 + 5] = (realIter + 1) * mDensity + imagIter + 1;
            }
        }
    }

    @Override
    protected void updateSecondaryData() {
        /*for (int indexFaceStartI = 0; indexFaceStartI < mIndices.length; indexFaceStartI += 6) {   //index triangle start index
            float[] vertex1 = new float[3];
            float[] vertex2 = new float[3];
            float[] vertex3 = new float[3];

            System.arraycopy(mVertices3d, mIndices[indexFaceStartI + 0] * 3, vertex1, 0, 3);
            System.arraycopy(mVertices3d, mIndices[indexFaceStartI + 1] * 3, vertex2, 0, 3);
            System.arraycopy(mVertices3d, mIndices[indexFaceStartI + 2] * 3, vertex3, 0, 3);

            float[] triSide1 = NDVector.sub(vertex2, vertex1);
            float[] triSide2 = NDVector.sub(vertex3, vertex1);

            float[] normal = NDVector.normalize(NDVector.cross(triSide1, triSide2));

            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 0] * 3, 3);
            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 1] * 3, 3);
            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 2] * 3, 3);

            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 3] * 3, 3);
            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 4] * 3, 3);
            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 5] * 3, 3);
        }*/
    }
}