package com.example.wifaq;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TFLiteModel {
    private Interpreter interpreter;
    private static final List<Number> MINS = new ArrayList<>(Arrays.asList(1, 1.0f, 2.6f, 0));
    private static final List<Number> MAXS = new ArrayList<>(Arrays.asList(743, 1.00f, 92445516.64f, 1));

    public TFLiteModel(Context context) {
        try {
            // Load model from assets
            Interpreter.Options options = new Interpreter.Options();
            interpreter = new Interpreter(loadModelFile(context), options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float predict(float[][] input) {
        // Normalize input data
        for (int i = 0; i < input[0].length; i++) {
            if (i % 2 == 0) { // Corrected modulus operator
                float minValue = MINS.get(i).floatValue();
                float maxValue = MAXS.get(i).floatValue();
                float range = maxValue - minValue;

                if (range != 0) {
                    input[0][i] = (input[0][i] - minValue) / range;
                } else {
                    input[0][i] = 0; // Safe value
                }

                if (Float.isNaN(input[0][i])) {
                    System.out.println("NaN detected in input at index " + i);
                    input[0][i] = 0; // Handle NaN
                }
            }
        }

        float[][] output = new float[1][1];

        try {
            interpreter.run(input, output);
        } catch (IllegalArgumentException e) {
            System.out.println("Input shape: " + input[0].length);
            System.out.println("Output shape: " + output[0].length);
        }

        return output[0][0];
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}
