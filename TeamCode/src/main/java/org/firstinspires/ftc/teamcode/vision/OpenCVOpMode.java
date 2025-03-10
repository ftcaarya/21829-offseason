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

import org.firstinspires.ftc.teamcode.extraneous.AllMechs;
import org.openftc.easyopencv.OpenCvCameraRotation;

@TeleOp(name = "OpenCV Testing")
public class OpenCVOpMode extends LinearOpMode {
    AllMechs robot;

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
        FtcDashboard dashboard = FtcDashboard.getInstance();
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
        FtcDashboard.getInstance().startCameraStream(robot.camera, 30);

        waitForStart();

        while (opModeIsActive()) {
            FtcDashboard.getInstance().startCameraStream(robot.camera, 30);

            if (gamepad1.cross) {
                robot.camera.setPipeline(YellowPipeline);
                gamepad1.setLedColor(250, 250, 0, 5000);
            } else if (gamepad1.circle) {
                robot.camera.setPipeline(TestingPipeline);
                gamepad1.setLedColor(0, 250, 0, 5000);
            } else if (gamepad1.square) {
                robot.camera.setPipeline(TejasPipeline);
                gamepad1.setLedColor(0, 0, 250, 5000);
            }

            telemetry.addData("Coordinate", "(" + (int) cX + ", " + (int) cY + ")");
            telemetry.addData("Distance in Inch", (getDistance(width)));
            telemetry.addData("Width: ", width);

            telemetry.addData("Wtv the telemtry is: ", TestingPipeline.getGreenSampleCoordinates());
            telemetry.update();

        }

    }
}
