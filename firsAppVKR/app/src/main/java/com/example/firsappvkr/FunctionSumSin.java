package com.example.firsappvkr;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class FunctionSumSin {
    final int SAMPLE_RATE = 8000;
    final int SOME_FUNC_SIZE = 1024;

    private static final int AMPLITUDE_OF_FIRST_HARMONIC = 15;
    private static final int AMPLITUDE_OF_SECOND_HARMONIC = 1;
    private static final int FREQUENCY_OF_FIRST_HARMONIC = 100;
    private static final int FREQUENCY_OF_SECOND_HARMONIC = 880;
    double[] someFunc = new double[SOME_FUNC_SIZE];

    private double cos(int index, int frequency, int sampleRate) {
        return Math.cos((2 * Math.PI * frequency * index) / sampleRate);
    }

    private double sin(int index, int frequency, int sampleRate) {
        return Math.sin((2 * Math.PI * frequency * index) / sampleRate);
    }

    static private double someFun(int index, int sampleRate) {
        return AMPLITUDE_OF_FIRST_HARMONIC * Math.sin((FREQUENCY_OF_FIRST_HARMONIC * 2 * Math.PI * index) / sampleRate)
                + AMPLITUDE_OF_SECOND_HARMONIC * Math.sin((FREQUENCY_OF_SECOND_HARMONIC * 2 * Math.PI * index) / sampleRate);
    }

    static private double someFun2(int index, int sampleRate) {
        int newAmplitudeOfFirstHarmonic = 23;
        int newAmplitudeOfSecondHarmonic = 3;
        int new2AmplitudeOfFirstHarmonic = 17;
        return newAmplitudeOfFirstHarmonic * Math.sin((FREQUENCY_OF_FIRST_HARMONIC * 2 * Math.PI * index) / sampleRate)
                + newAmplitudeOfSecondHarmonic * Math.sin((FREQUENCY_OF_SECOND_HARMONIC * 2 * Math.PI * index) / sampleRate) +
                new2AmplitudeOfFirstHarmonic * Math.cos(((FREQUENCY_OF_FIRST_HARMONIC + 300) * 2 * Math.PI * index) / sampleRate);
    }

    private double[] generSignal(int first) {
        if (first == 1) {
            for (int i = 0; i < someFunc.length; i++) {
                someFunc[i] = someFun(i, SAMPLE_RATE);
            }
        } else {
            for (int i = 0; i < someFunc.length; i++) {
                someFunc[i] = someFun2(i, SAMPLE_RATE);
            }
        }
        return someFunc;
    }

    public double[] createSpectr(int second) {

        Complex[] dd = new Complex[1024];
        double[] result = new double[dd.length];
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        if (second == 1) {
            dd = fft.transform(generSignal(1), TransformType.FORWARD);
            for (int i = 0; i < dd.length; i++) {
                result[i] = 2 * dd[i].abs() / dd.length;
            }
            result[0] = result[0] / 2;
        } else {
            dd = fft.transform(generSignal(2), TransformType.FORWARD);
            for (int i = 0; i < dd.length; i++) {
                result[i] = 2 * dd[i].abs() / dd.length;
            }
            result[0] = result[0] / 2;
        }
        return result;
    }

}
