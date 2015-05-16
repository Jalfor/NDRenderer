package com.sudo_code.ndrenderer;

public class NDVector {

    /**
     * Calculates the cross product between two 3D vectors, a and b
     *
     * @param vector1 The first vector
     * @param vector2 The second vector
     * @return The cross product of a and b
     */
    public static float[] cross(float[] vector1, float[] vector2) {
        return new float[] {
                vector1[1] * vector2[2] - vector1[2] * vector2[1],
                vector1[2] * vector2[0] - vector1[0] * vector2[2],
                vector1[0] * vector2[1] - vector1[1] * vector2[0]};
    }

    /**
     * Calculates the vector normalized
     *
     * @param vector The vector
     * @return The input vector normalized
     */
    public static float[] normalize(float[] vector) {
        return mult(1 / getLength(vector), vector);
    }

    /**
     * Calculates the length of the vector
     *
     * @param vector The vector
     * @return The length of the vector
     */
    public static float getLength(float[] vector) {
        float length = 0;

        for (int i = 0; i < vector.length; i++) {
            length += Math.pow(vector[i], 2.f);
        }

        return (float) Math.sqrt(length);
    }

    /**
     * Calculates the scalar multiplied by the vector
     *
     * @param scalar The scalar
     * @param vector The vector
     * @return The scalar multiplied by the vector
     */
    public static float[] mult(float scalar, float[] vector) {
        float[] result = vector.clone();

        for (int i = 0; i < result.length; i++) {
            result[i] *= scalar;
        }

        return result;
    }

    /**
     * Calculates the vector1 + vector2
     *
     * @param vector1 The first vector
     * @param vector2 The second vector
     * @return vector1 + vector2
     */
    public static float[] add(float[] vector1, float[] vector2) {
        float[] result = new float[vector1.length];

        for (int i = 0; i < vector1.length; i++) {
            result[i] = vector1[i] + vector2[i];
        }

        return result;
    }

    /**
     * Calculates the vector1 - vector2
     *
     * @param vector1 The first vector
     * @param vector2 The second vector
     * @return vector1 - vector2
     */
    public static float[] sub(float[] vector1, float[] vector2) {
        float[] result = new float[vector1.length];

        for (int i = 0; i < vector1.length; i++) {
            result[i] = vector1[i] - vector2[i];
        }

        return result;
    }

    /**
     * Calculates matrix * vector
     *
     * Assumes the matrix is indexed column major ([x][y])
     *
     * @param matrix The matrix
     * @param vector The vector
     * @return matrix * vector
     */
    public static float[] multMatrix(float[][] matrix, float[] vector) {
        float[] result = new float[vector.length];

        for (int y = 0; y < vector.length; y++) {
            result[y] = 0;
            for (int x = 0; x < matrix.length; x++) {
                result[y] += matrix[x][y] * vector[x];
            }
        }

        return result;
    }

    /**
     * Rotates a vector along a plane
     *
     * @param vector The vector
     * @param angle The angle the vector is rotated by (in radians)
     * @param rotationPlane The plane of rotation (a length two vector containing the dimensions
     *                      of the rotation plane, e.g. XZ would be {0, 2})
     * @return The rotated vector
     */
    public static float[] rotate(float[] vector, float angle, int[] rotationPlane) {
        float[] result = vector.clone();

        result[rotationPlane[0]] = vector[rotationPlane[0]] * (float) Math.cos(angle) -
                    vector[rotationPlane[1]] * (float) Math.sin(angle);

        result[rotationPlane[1]] = vector[rotationPlane[1]] * (float) Math.cos(angle) +
                    vector[rotationPlane[0]] * (float) Math.sin(angle);

        return result;
    }

    /**
     * Finds the triangle center by averaging each of the components
     *
     * @param vertex1 The first vertex of the triangle
     * @param vertex2 The second vertex of the triangle
     * @param vertex3 The third vertex of the triangle
     * @return The center of the triangle
     */
    public static float[] getTriangleCenter(float[] vertex1, float[] vertex2, float[] vertex3) {
        float[] center = new float[vertex1.length];

        for (int i = 0; i < vertex1.length; i++) {
            center[i] = (vertex1[i] + vertex2[i] + vertex3[i]) / 3;
        }

        return center;
    }
}