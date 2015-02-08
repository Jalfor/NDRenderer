package com.sudo_code.ndrenderer;

import android.opengl.*;
import org.apache.commons.math3.util.CombinatoricsUtils;

public class Hypercube {

    private int     mDimensions;
    private float[] mVertices;  //In the form x,y,z,w...x,y etc.
    private float[] mNormals;
    private int[]   mIndices;

    /**
     * Initializes the hypercube, generating the vertices
     */
    public Hypercube(int dimensions) {
        mDimensions = dimensions;
        genVertices();
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

    private void genVertices() {
        //4 * number of faces vertices, each with mDimensions components
        mVertices = new float[(int) (CombinatoricsUtils.binomialCoefficient(mDimensions, 2) *
                                     Utils.powI(2, mDimensions - 2) * 4) * mDimensions];

        //4 * number of faces vertices, each with 3 components  (normals of the 3D projection)
        mNormals = new float[(int) (CombinatoricsUtils.binomialCoefficient(mDimensions, 2) *
                                    Utils.powI(2, mDimensions - 2) * 4) * 3];

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

                //System.arraycopy(mVertices, vertFaceStartI * 3);

                vertFaceStartI += 4;
                indexFaceStartI += 6;

            } while (nextLockedAxesValues(lockedAxesValues));
        } while (nextLockedAxes(lockedAxes));
    }
}
