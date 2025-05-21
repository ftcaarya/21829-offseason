package org.firstinspires.ftc.teamcode.extraneous;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.ArrayList;
import java.util.List;

@TeleOp(name = "intake testing")
public class testing extends OpMode {
    private FtcDashboard dash = FtcDashboard.getInstance();
    private List<Action> runningActions = new ArrayList<>();

    private ColorSensor colorSensor;
    private DcMotor intake;
    private Servo pooper;

    @Override
    public void init() {
        colorSensor = hardwareMap.get(ColorSensor.class, "color sensor");

        intake = hardwareMap.get(DcMotor.class, "intake motor");
        intake.setDirection(DcMotorSimple.Direction.FORWARD);

        pooper = hardwareMap.get(Servo.class, "pooper");
    }

    @Override
    public void loop() {
        TelemetryPacket packet = new TelemetryPacket();

        telemetry.addData("Red: ", colorSensor.red());
        telemetry.addData("Green: ", colorSensor.green());
        telemetry.addData("Blue: ", colorSensor.blue());
        telemetry.update();

        runningActions.add(
                checkColorRed()
        );


        List<Action> newActions = new ArrayList<>();
        for (Action action : runningActions) {
            action.preview(packet.fieldOverlay());
            if (action.run(packet)) {
                newActions.add(action);
            }
        }
        runningActions = newActions;

        dash.sendTelemetryPacket(packet);
    }


    public class CheckColorRed implements Action {


        double redColor = (double) colorSensor.red() / 2;
        double greenColor = (double) colorSensor.green() / 2;
        double blueColor = (double) colorSensor.blue() / 2;

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            if (colorSensor.red() > colorSensor.green() + 50 && colorSensor.red() > colorSensor.blue() + 50) {
                pooper.setPosition(.9);
                intake.setPower(0);
                return false;
            } else if (greenColor > blueColor && redColor > blueColor) {
                pooper.setPosition(.9);
                intake.setPower(.5);
                return false;
            } else {
                pooper.setPosition(.1);
                intake.setPower(.7);
                return true;
            }

        }
    }

    public Action checkColorRed() {
        return new CheckColorRed();
    }
}
