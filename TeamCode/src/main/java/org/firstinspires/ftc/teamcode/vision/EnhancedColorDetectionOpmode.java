package org.firstinspires.ftc.teamcode.vision;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.extraneous.AllMechs;
import org.firstinspires.ftc.vision.VisionPortal;
import org.opencv.core.Scalar;

@Autonomous
public class EnhancedColorDetectionOpmode extends OpMode {
    private VisionPortal visionPortal;
    private EnhancedColorDetectionProcessor colourMassDetectionProcessor;
    AllMechs robot;
    public Action dropPreloaded;
    public Action rightSideBarnacle;

    @Override
    public void init() {
        Pose2d initialPose = new Pose2d(-35, -61, Math.toRadians(90));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        // HSV takes the form: (HUE, SATURATION, VALUE)
        // the domains are: ([0, 180], [0, 255], [0, 255])
        double lowerH = 150; // the lower hsv threshold for your detection
        double upperH = 180; // the upper hsv threshold for your detection
        double minArea = 100; // the minimum area for the detection to consider for your prop

        colourMassDetectionProcessor = new EnhancedColorDetectionProcessor(
                lowerH,
                upperH,
                () -> minArea,
                () -> 213, // the left dividing line, in this case the left third of the frame
                () -> 426 // the left dividing line, in this case the right third of the frame
        );

        // Start the camera
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(colourMassDetectionProcessor)
                .build();


        robot = new AllMechs(hardwareMap, 213, 426);

         Action dropPreloaded = drive.actionBuilder(initialPose)
                .setReversed(false)
                .splineToLinearHeading(new Pose2d(-52, -52, Math.toRadians(45)), -Math.PI)
//                 .stopAndAdd(),
                        .strafeToLinearHeading(new Vector2d(-54, -45), Math.toRadians(90))

                .build();

         Action rightSideBarnacle = drive.actionBuilder(new Pose2d(-54, -45, Math.toRadians(90)))
//                 .stopAndAdd(),
                 .strafeToLinearHeading(new Vector2d(-52, -52), Math.toRadians(45))
//                 .stopAndAdd()
                 .strafeToLinearHeading(new Vector2d(-54, -44), (Math.PI - Math.atan((18/14.5))))
//                 .stopAndAdd()
                 .strafeToLinearHeading(new Vector2d(-52, -52), Math.toRadians(45))
//                 .stopAndAdd()
                 .setReversed(false)
                 .setTangent(Math.toRadians(270))
                 .splineToLinearHeading(new Pose2d(-60, -50, Math.toRadians(90)), -Math.toRadians(270))
                 .setTangent(-Math.toRadians(270))
                 .splineToConstantHeading(new Vector2d(-60, -6), -Math.toRadians(270))
                 .splineToLinearHeading(new Pose2d(-30, -6, Math.toRadians(0)), Math.toRadians(0))
//                 .stopAndAdd()
                 .setReversed(true)
                 .splineToSplineHeading(new Pose2d(-60, -6, Math.toRadians(90)), Math.toRadians(180))
                 .setReversed(true)
                 .setTangent(Math.toRadians(270))
                 .splineToLinearHeading(new Pose2d(-60, -50, Math.toRadians(90)), Math.toRadians(-90))

                 .splineToLinearHeading(new Pose2d(-52, -52, Math.toRadians(45)), -Math.toRadians(270))
//                 .stopAndAdd()
                 .setTangent(Math.toRadians(0))
                 .splineToLinearHeading(new Pose2d(-30, -55, Math.toRadians(0)), Math.toRadians(0))
                 .build();



    }

    @Override
    public void start() {


        // Move to be in position for the vision and drop preloaded
        Actions.runBlocking(
                dropPreloaded
        );






        // Feed us data for the values and what the camera is seeing.
        telemetry.addData("Currently Recorded Position", colourMassDetectionProcessor.getRecordedPropPosition());
        telemetry.addData("Camera State", visionPortal.getCameraState());
        telemetry.addData("Currently Detected Mass Center", "x: " + colourMassDetectionProcessor.getMostSaturatedContourX() + ", y: " + colourMassDetectionProcessor.getMostSaturatedContourY());
        telemetry.addData("Currently Detected Mass Area", colourMassDetectionProcessor.getMostSaturatedContourArea());
        telemetry.addData("Currently Detected Mass Saturation", colourMassDetectionProcessor.getMostSaturatedContourSaturation());

        // Shut down the camera, after detected.
        if (visionPortal.getCameraState() == VisionPortal.CameraState.STREAMING) {
            visionPortal.stopLiveView();
            visionPortal.stopStreaming();
        }

        // gets the recorded prop position
        EnhancedColorDetectionProcessor.PropPositions recordedBarnaclePosition = colourMassDetectionProcessor.getRecordedPropPosition();



        // switch through the detected cases and run separate trajectories for each of them.
        switch (recordedBarnaclePosition) {
            case LEFT:
                // code to do if barnacle in the left
                break;

            case MIDDLE:
                // code to do if barnacle in middle
                break;
            case RIGHT:
                Actions.runBlocking(
                    rightSideBarnacle
                );
                break;

            case UNFOUND:
                //code if barnacle wasn't found.


        }
    }

    @Override
    public void loop() {

    }
    @Override
    public void stop() {
        // this closes down the portal when we stop the code, its good practice!
        colourMassDetectionProcessor.close();
        visionPortal.close();
    }
}