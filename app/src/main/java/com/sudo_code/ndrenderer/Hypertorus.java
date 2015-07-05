package com.sudo_code.ndrenderer;

public class Hypertorus extends NDShape {

    private int mSmoothness;    //The number of vertices on the circles the hypertorus is based off
    private int mVertI; //A counter for what vertex we're up to

    /**
     * Initializes the hypertorus
     *
     * @param dimensions The number of dimensions the hypertorus should have
     * @param projectionConstant The camera's distance to the hypervolume of projection (MUST BE GREATER THAN 1)
     * @param viewDist The distance from the 2D camera to the center of projection
     * @param vertexHandle The attribute index of the vertex position
     * @param normalHandle The attribute index of the vertex normal
     */
    public Hypertorus(int dimensions, float projectionConstant, float viewDist, int vertexHandle, int normalHandle, int smoothness) {
        super(dimensions, projectionConstant, viewDist, vertexHandle, normalHandle);

        mSmoothness = smoothness;

        init();
    }

    /**
     * Generates the rotation matrix to transform an XY circle to the plane of the added offset
     * of the dimension being added to the hypertorus by setting the X basis vector tp the previous
     * torus vector and Y to the new dimension
     *
     * @param torusVecDimension The dimension being added (0 indexeda)
     * @param prevTorusVec The offset added by the previous dimension
     */
    float[][] genNDTorusVecRotMatrix(int torusVecDimension, float[] prevTorusVec) {
        float[][] rotMatrix = new float[mDimensions][mDimensions];

        for (int x = 0; x < mDimensions; x++) {
            for (int y = 0; y < mDimensions; y++) {
                if (x == 0) {
                    for (int prevTVecI = 0; prevTVecI < prevTorusVec.length; prevTVecI++) {
                        rotMatrix[x][prevTVecI] = prevTorusVec[prevTVecI];
                    }
                }

                else if (x == 1 && y == torusVecDimension) {
                    rotMatrix[x][y] = 1.f;
                }

                else {
                    rotMatrix[x][y] = 0;
                }
            }
        }

        return rotMatrix;
    }

    /**
     * Recursive function called initially from genVertexData to generate all points that are in
     * a rotation around that point.
     *
     * @param torusVecDimension The dimension we're generating (0 indexed)
     * @param currentVector The current position of the point
     * @param prevTorusVec The offset added by the previous dimension
     */
    void genTorusVertices(int torusVecDimension, float[] currentVector, float[] prevTorusVec) {
        float[][] rotMatrix = genNDTorusVecRotMatrix(torusVecDimension, prevTorusVec);

        if (torusVecDimension < mDimensions - 1) {  //This isn't going to be the final position
            for (int i = 0; i < mSmoothness; i++) {
                float[] circleVec = new float[mDimensions];
                circleVec[0] = (float) (Math.cos(i * 2.f * Math.PI / mSmoothness) / Math.pow(2.f, torusVecDimension - 1));
                circleVec[1] = (float) (Math.sin(i * 2.f * Math.PI / mSmoothness) / Math.pow(2.f, torusVecDimension - 1));

                for (int fillI = 2; fillI < mDimensions; fillI++) {
                    circleVec[fillI] = 0;
                }

                genTorusVertices(
                        torusVecDimension + 1,
                        NDVector.add(
                                currentVector,
                                NDVector.multMatrix(rotMatrix, circleVec)),
                        NDVector.multMatrix(rotMatrix, circleVec));
            }
        }

        else {  //We're at the final dimension
            for (int i = 0; i < mSmoothness; i++) {
                float[] circleVec = new float[mDimensions];
                circleVec[0] = (float) (Math.cos(i * 2.f * Math.PI / mSmoothness) / Math.pow(2.f, torusVecDimension - 1));
                circleVec[1] = (float) (Math.sin(i * 2.f * Math.PI / mSmoothness) / Math.pow(2.f, torusVecDimension - 1));

                for (int fillI = 2; fillI < mDimensions; fillI++) {
                    circleVec[fillI] = 0;
                }

                System.arraycopy(
                        NDVector.add(currentVector, NDVector.multMatrix(rotMatrix, circleVec)),
                        0,
                        mVertices,
                        mVertI * mDimensions,
                        mDimensions);

                mVertI++;
            }
        }
    }

    /**
     * Generates the vertex data for the hypertorus (arrays of vertices, indices and normals)
     */
    @Override
    protected void genVertexData() {
        mFaceCount = Utils.powI(mSmoothness, mDimensions - 1) * (mDimensions - 2);

        //Number of vertices * mDimensions components
        mVertices   = new float[Utils.powI(mSmoothness, mDimensions - 1) * mDimensions];

        //Number of vertices * 3 components
        mVertices3d = new float[Utils.powI(mSmoothness, mDimensions - 1) * 3];

        //Number of vertices * 3 components
        mSecondaryData    = new float[Utils.powI(mSmoothness, mDimensions - 1) * 3];

        //6 * number of faces vertices (2 triangles per face * 3 points per triangle)
        mIndices    = new int[mFaceCount * 6];

        mVertI = 0;

        //Generate the vertex positions
        for (int i = 0; i < mSmoothness; i++) {
            float[] torusVec = new float[mDimensions];
            torusVec[0] = (float) Math.cos(i * 2.f * Math.PI / mSmoothness);
            torusVec[1] = (float) Math.sin(i * 2.f * Math.PI / mSmoothness);

            genTorusVertices(2, torusVec, torusVec);
        }

        //Generate the faces
        mVertI = 0;
        for (int dim = 3; dim <= mDimensions; dim++) {
            for (int i = 0; i < mVertices.length / mDimensions; i++) {

                /*
                 * The different directions from a point are mSmoothness to different powers.
                 * This is complicated however by the fact that we have to deal with wrap around,
                 * hence all the modulus and floor stuff.
                 */

                //First triangle

                mIndices[mVertI + 0] = i;

                mIndices[mVertI + 1] = (int) (
                        Math.floor(i / Utils.powI(mSmoothness, dim - 1))
                        * Utils.powI(mSmoothness, dim - 1)
                        + (i + Utils.powI(mSmoothness, dim - 3))
                        % Utils.powI(mSmoothness, dim - 1));

                mIndices[mVertI + 2] = (int) (
                        Math.floor(i / Utils.powI(mSmoothness, dim - 1))
                        * Utils.powI(mSmoothness, dim - 1)
                        + (i + Utils.powI(mSmoothness, dim - 2))
                        % Utils.powI(mSmoothness, dim - 1));

                //Second triangle

                mIndices[mVertI + 3] = (int) (
                        Math.floor(i / Utils.powI(mSmoothness, dim - 1))
                        * Utils.powI(mSmoothness, dim - 1)
                        + (i + Utils.powI(mSmoothness, dim - 3))
                        % Utils.powI(mSmoothness, dim - 1));

                mIndices[mVertI + 4] = (int) (
                        Math.floor(i / Utils.powI(mSmoothness, dim - 1))
                        * Utils.powI(mSmoothness, dim - 1)
                        + (i + Utils.powI(mSmoothness, dim - 2) + Utils.powI(mSmoothness, dim - 3))
                        % Utils.powI(mSmoothness, dim - 1));

                mIndices[mVertI + 5] = (int) (
                        Math.floor(i / Utils.powI(mSmoothness, dim - 1))
                        * Utils.powI(mSmoothness, dim - 1)
                        +  (i + Utils.powI(mSmoothness, dim - 2))
                        % Utils.powI(mSmoothness, dim - 1));

                mVertI += 6;
            }
        }
    }

    /**
     * Updates the mSecondaryData array based on mVertices3d (note that inwards and outwards are
     * meaningless concepts when with a projection)
     */
    @Override
    protected void updateSecondaryData() {
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

            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 0] * 3, 3);
            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 1] * 3, 3);
            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 2] * 3, 3);

            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 3] * 3, 3);
            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 4] * 3, 3);
            System.arraycopy(normal, 0, mSecondaryData, mIndices[indexFaceStartI + 5] * 3, 3);
        }
    }
}