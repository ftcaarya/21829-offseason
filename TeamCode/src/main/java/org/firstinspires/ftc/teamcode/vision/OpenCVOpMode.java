package org.firstinspires.ftc.teamcode.vision;

import static org.firstinspires.ftc.teamcode.extraneous.AllMechs.CAMERA_HEIGHT;
import static org.firstinspires.ftc.teamcode.extraneous.AllMechs.CAMERA_WIDTH;
import static org.firstinspires.ftc.teamcode.vision.OpenCVPipeline.width;
import static org.firstinspires.ftc.teamcode.vision.OpenCVPipeline.cX;
import static org.firstinspires.ftc.teamcode.vision.OpenCVPipeline.cY;
import static org.firstinspires.ftc.teamcode.vision.OpenCVPipeline.getDistance;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.extraneous.AllMechs;
import org.firstinspires.ftc.vision.VisionPortal;
import org.openftc.easyopencv.OpenCvCameraRotation;

@TeleOp(name = "OpenCV Testing")
public class OpenCVOpMode extends LinearOpMode {
    AllMechs robot;
    private VisionPortal visionPortal;
    private EnhancedColorDetectionProcessor colourMassDetectionProcessor;

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new AllMechs(hardwareMap);
        OpenCVPipeline YellowPipeline = new OpenCVPipeline();
        ErodedOpenCVPipeline TestingPipeline = new ErodedOpenCVPipeline();
        TejasGivenPipeline TejasPipeline = new TejasGivenPipeline();

        robot.camera.setPipeline(YellowPipeline);
//        robot.camera.setPipeline(TestingPipeline);
//        robot.camera.setPipeline(TejasPipeline);


        robot.camera.openCameraDevice();
        robot.camera.startStreaming(CAMERA_WIDTH, CAMERA_HEIGHT, OpenCvCameraRotation.UPRIGHT);

        colourMassDetectionProcessor = new EnhancedColorDetectionProcessor(
                () -> 100, // these are lambda methods, in case we want to change them while the match is running, for us to tune them or something
                () -> 213, // the left dividing line, in this case the left third of the frame
                () -> 426 // the left dividing line, in this case the right third of the frame
        );

        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1")) // the camera on your robot is named "Webcam 1" by default
                .addProcessor(colourMassDetectionProcessor)
                .build();

        FtcDashboard dashboard = FtcDashboard.getInstance();
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
        FtcDashboard.getInstance().startCameraStream(robot.camera, 30);

        waitForStart();

        while (opModeIsActive()) {
            FtcDashboard.getInstance().startCameraStream(robot.camera, 30);

            if (gamepad1.cross) {
                visionPortal.stopStreaming();
                visionPortal.stopLiveView();
                robot.camera.setPipeline(YellowPipeline);
                gamepad1.setLedColor(250, 250, 0, 5000);
            } else if (gamepad1.circle) {
                visionPortal.stopStreaming();
                visionPortal.stopLiveView();
                robot.camera.setPipeline(TestingPipeline);
                gamepad1.setLedColor(0, 250, 0, 5000);
            } else if (gamepad1.square) {
                visionPortal.stopStreaming();
                visionPortal.stopLiveView();
                robot.camera.setPipeline(TejasPipeline);
                gamepad1.setLedColor(0, 0, 250, 5000);
            } else if (gamepad1.triangle) {
                visionPortal = new VisionPortal.Builder()
                        .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1")) // the camera on your robot is named "Webcam 1" by default
                        .addProcessor(colourMassDetectionProcessor)
                        .build();
            }

            telemetry.addData("Coordinate", "(" + (int) cX + ", " + (int) cY + ")");
            telemetry.addData("Distance in Inch", (getDistance(width)));
            telemetry.addData("Width: ", width);

            telemetry.addData("Wtv the telemtry is: ", TestingPipeline.getGreenSampleCoordinates());

            telemetry.addData("Camera State", visionPortal.getCameraState());
            telemetry.addData("Currently Detected Mass Center", "x: " + colourMassDetectionProcessor.getMostSaturatedContourX() + ", y: " + colourMassDetectionProcessor.getMostSaturatedContourY());
            telemetry.addData("Currently Detected Mass Area", colourMassDetectionProcessor.getMostSaturatedContourArea());
            telemetry.addData("Currently Detected Mass Saturation", colourMassDetectionProcessor.getMostSaturatedContourSaturation());
            telemetry.update();

        }

    }
}
