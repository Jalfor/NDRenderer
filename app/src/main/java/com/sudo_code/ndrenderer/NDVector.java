package com.sudo_code.ndrenderer;

public class NDVector {

    /**
     * Calculates the cross product between a and b
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
            length += vector[i];
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
}
