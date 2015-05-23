package com.sudo_code.ndrenderer;

public class ComplexNumber {
    private float mReal;
    private float mImag;

    ComplexNumber(float real, float imaginary) {
        mReal = real;
        mImag = imaginary;
    }

    public float getReal() {
        return mReal;
    }

    public float getImaginary() {
        return mImag;
    }

    public void mult(ComplexNumber complexNumber) {
        mReal += mReal * complexNumber.getReal();
        mReal -= mImag * complexNumber.getImaginary();
        mImag += mReal * complexNumber.getImaginary();
        mImag += mImag * complexNumber.getReal();
    }

    public void mult(float scalar) {
        mReal *= scalar;
        mImag *= scalar;
    }

    public void add(ComplexNumber complexNumber) {
        mReal += complexNumber.getReal();
        mImag += complexNumber.getImaginary();
    }

    public void add(float real, float imaginary) {
        mReal += real;
        mImag += imaginary;
    }

    public float getMod() {
        return (float) Math.pow(mReal, 2) + (float) Math.pow(mImag, 2);
    }
}