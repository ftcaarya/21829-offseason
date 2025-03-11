package org.firstinspires.ftc.teamcode.vision;

import static org.firstinspires.ftc.teamcode.extraneous.AllMechs.CAMERA_HEIGHT;
import static org.firstinspires.ftc.teamcode.extraneous.AllMechs.CAMERA_WIDTH;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.extraneous.AllMechs;
import org.firstinspires.ftc.vision.VisionPortal;
import org.openftc.easyopencv.OpenCvCameraRotation;

@TeleOp(name = "Testing the Fourth Pipeline")
public class EnhancedColorDetectionOpmode extends OpMode {
    private VisionPortal visionPortal;
    private EnhancedColorDetectionProcessor colourMassDetectionProcessor;
    AllMechs robot;

    @Override
    public void init() {
        robot = new AllMechs(hardwareMap);

//        robot.camera.openCameraDevice();
//        robot.camera.startStreaming(CAMERA_WIDTH, CAMERA_HEIGHT, OpenCvCameraRotation.UPRIGHT);

        colourMassDetectionProcessor = new EnhancedColorDetectionProcessor(
                () -> 100, // these are lambda methods, in case we want to change them while the match is running, for us to tune them or something
                () -> 213, // the left dividing line, in this case the left third of the frame
                () -> 426 // the left dividing line, in this case the right third of the frame
        );
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1")) // the camera on your robot is named "Webcam 1" by default
                .addProcessor(colourMassDetectionProcessor)
                .enableLiveView(true)
                .build();



        FtcDashboard dashboard = FtcDashboard.getInstance();
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
        FtcDashboard.getInstance().startCameraStream(visionPortal, 30);
//        FtcDashboard.getInstance().startCameraStream(robot.camera, 30);
    }

    @Override
    public void loop() {
        telemetry.addData("Camera State", visionPortal.getCameraState());
        telemetry.addData("Currently Detected Mass Center", "x: " + colourMassDetectionProcessor.getMostSaturatedContourX() + ", y: " + colourMassDetectionProcessor.getMostSaturatedContourY());
        telemetry.addData("Currently Detected Mass Area", colourMassDetectionProcessor.getMostSaturatedContourArea());
        telemetry.addData("Currently Detected Mass Saturation", colourMassDetectionProcessor.getMostSaturatedContourSaturation());
    }

    @Override
    public void stop() {
        if (visionPortal.getCameraState() == VisionPortal.CameraState.STREAMING) {
            visionPortal.stopLiveView();
            visionPortal.stopStreaming();
        }
        // this closes down the portal when we stop the code, its good practice!
        colourMassDetectionProcessor.close();
        visionPortal.close();
    }
}
