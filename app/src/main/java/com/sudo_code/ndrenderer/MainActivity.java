package com.sudo_code.ndrenderer;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.content.Context;
import android.opengl.GLES30;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;


public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {

    private static final String TAG = "MainActivity";

    private Vibrator mVibrator;
    private CardboardOverlayView mOverlayView;

    private int program;

    private Hypercube hypercube;

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
        GLES30.glViewport(0, 0, width, height);
    }

    /**
     * Initialization code
     *
     * @param config The EGL configuration used when creating the surface.
     */

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        int[] shaders = new int[2];

        shaders[0] = Utils.genShader(GLES30.GL_VERTEX_SHADER, R.raw.vert, this);
        shaders[1] = Utils.genShader(GLES30.GL_FRAGMENT_SHADER, R.raw.frag, this);

        program = Utils.genProgram(shaders);
        Utils.delShaders(shaders);

        hypercube = new Hypercube(4, 3, 0, 1, 2);

        Utils.checkGLError("onSurfaceCreated");
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        GLES30.glUseProgram(program);
        hypercube.draw();
        GLES30.glUseProgram(0);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");

        // Always give user feedback.
        mVibrator.vibrate(50);
    }
}