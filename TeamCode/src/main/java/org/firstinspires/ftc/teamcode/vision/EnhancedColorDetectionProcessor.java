package org.firstinspires.ftc.teamcode.vision;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.function.Continuation;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamSource;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleSupplier;

public class EnhancedColorDetectionProcessor implements VisionProcessor, CameraStreamSource {
    private final DoubleSupplier minArea, left, right;
    private final Scalar upper = new Scalar(120, 255, 255); // lower bounds for masking
    private final Scalar lower = new Scalar(80, 100, 100); // upper bounds for masking
    private final TextPaint textPaint = new TextPaint();
    private final Paint linePaint = new Paint();
    private final ArrayList<MatOfPoint> contours;
    private final Mat hierarchy = new Mat();
    private final Mat sel1 = new Mat(); // these facilitate capturing through 0
    private final Mat sel2 = new Mat();
    private final Mat mask = new Mat();
    private final AtomicReference<Bitmap> lastFrame = new AtomicReference<>(Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565));
    private double mostSaturatedContourX;
    private double mostSaturatedContourY;
    private double mostSaturatedContourArea;
    private double mostSaturatedContourSaturation;
    private MatOfPoint mostSaturatedContour;
//    private PropPositions previousPropPosition;
//    private PropPositions recordedPropPosition = PropPositions.UNFOUND;


    public EnhancedColorDetectionProcessor(DoubleSupplier minArea, DoubleSupplier left, DoubleSupplier right) {
        this.contours = new ArrayList<>();
        this.minArea = minArea;
        this.left = left;
        this.right = right;

        textPaint.setColor(Color.GREEN); // you may want to change this
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(40); // or this
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // setting up the paint for the lines that comprise the box
        linePaint.setColor(Color.GREEN); // you may want to change this
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(10); // or this
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

    }


    @Override
    public void init(int width, int height, CameraCalibration calibration) {
        lastFrame.set(Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565));
    }

    /**
     * @return the x position of the currently found most saturated contour in the range [0, camera width], or -1 if no largest contour has been determined
     */
    public double getMostSaturatedContourX() {
        return mostSaturatedContourX;
    }

    /**
     * @return the y position of the currently found most saturated contour in the range [0, camera height], or -1 if no largest contour has been determined
     */
    public double getMostSaturatedContourY() {
        return mostSaturatedContourY;
    }

    /**
     * @return the area of the currently found most saturated contour, or -1 if no largest contour has been determined
     */
    public double getMostSaturatedContourArea() {
        return mostSaturatedContourArea;
    }

    /**
     * @return the area of the currently found most saturated contour, or -1 if no largest contour has been determined
     */
    public double getMostSaturatedContourSaturation() {
        return mostSaturatedContourSaturation;
    }

    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2HSV);

        if (upper.val[0] < lower.val[0]) {
            // makes new scalars for the upper [upper, 0] detection, places the result in sel1
            Core.inRange(frame, new Scalar(upper.val[0], lower.val[1], lower.val[2]), new Scalar(0, upper.val[1], upper.val[2]), sel1);
            // makes new scalars for the lower [0, lower] detection, places the result in sel2
            Core.inRange(frame, new Scalar(0, lower.val[1], lower.val[2]), new Scalar(lower.val[0], upper.val[1], upper.val[2]), sel2);

            // combines the selections
            Core.bitwise_or(sel1, sel2, mask);
        } else {
            // this process is simpler if we are not trying to wrap through 0
            // this method makes the colour image black and white, with everything between your upper and lower bound values as white, and everything else black
            Core.inRange(frame, lower, upper, mask);
        }

        Core.bitwise_and(frame, mask, frame);

        contours.clear();

        Imgproc.findContours(frame, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        mostSaturatedContourArea = -1;
        mostSaturatedContourSaturation = -1;

        mostSaturatedContour = null;

        double minArea = this.minArea.getAsDouble();

        // finds the largest contour!
        for (MatOfPoint contour : contours) {
            double meanSaturation = Core.mean(contour).val[1];
            double area = Imgproc.contourArea(contour);
            if (meanSaturation > mostSaturatedContourSaturation && area > minArea) {
                mostSaturatedContour = contour;
                mostSaturatedContourSaturation = meanSaturation;
                mostSaturatedContourArea = area;
            }
        }

        mostSaturatedContourX = mostSaturatedContourY  = -1;

        if (mostSaturatedContour != null) {
            Moments moment = Imgproc.moments(mostSaturatedContour);
            mostSaturatedContourX = (moment.m10 / moment.m00);
            mostSaturatedContourY = (moment.m01 / moment.m00);
        }

        Bitmap b = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(frame, b);
        lastFrame.set(b);
        return frame;
    }

    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
        if (mostSaturatedContour != null) {
            Rect rect = Imgproc.boundingRect(mostSaturatedContour);

            float[] points = {rect.x * scaleBmpPxToCanvasPx, rect.y * scaleBmpPxToCanvasPx, (rect.x + rect.width) * scaleBmpPxToCanvasPx, (rect.y + rect.height) * scaleBmpPxToCanvasPx};

            canvas.drawLine(points[0], points[1], points[0], points[3], linePaint);
            canvas.drawLine(points[0], points[1], points[2], points[1], linePaint);

            canvas.drawLine(points[0], points[3], points[2], points[3], linePaint);
            canvas.drawLine(points[2], points[1], points[2], points[3], linePaint);

            String text = String.format(Locale.ENGLISH, "Position: (%.2f, %.2f)", mostSaturatedContourX, mostSaturatedContourY);

            canvas.drawText(text, (float) mostSaturatedContourX * scaleBmpPxToCanvasPx, (float) mostSaturatedContourY * scaleBmpPxToCanvasPx, textPaint);
        }
    }

    // returns the largest contour if you want to get information about it
    public MatOfPoint getMostSaturatedContour() {
        return mostSaturatedContour;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() {
        hierarchy.release();
        sel1.release();
        sel2.release();
        mask.release();
    }

    @Override
    public void getFrameBitmap(Continuation<? extends Consumer<Bitmap>> continuation) {
        continuation.dispatch(bitmapConsumer -> bitmapConsumer.accept(lastFrame.get()));
    }
}
