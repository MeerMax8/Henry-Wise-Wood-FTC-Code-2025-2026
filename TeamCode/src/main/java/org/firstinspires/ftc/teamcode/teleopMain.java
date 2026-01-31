package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="HWW_v2")
public class teleopMain extends OpMode {

    public DcMotor leftFront;
    public DcMotor leftBack;
    public DcMotor rightFront;
    public DcMotor rightBack;
    public DcMotor intake;
    public DcMotor midRoller;
    public DcMotor index;
    public DcMotor flywheel;

    @Override
    public void init() {
        //Control hub declarations
        leftFront = hardwareMap.get(DcMotor.class,"leftFront");
        leftBack = hardwareMap.get(DcMotor.class,"leftBack");
        rightFront = hardwareMap.get(DcMotor.class,"rightFront");
        rightBack = hardwareMap.get(DcMotor.class,"rightBack");
        intake = hardwareMap.get(DcMotor.class,"intake");
        midRoller = hardwareMap.get(DcMotor.class,"midRoller");
        index = hardwareMap.get(DcMotor.class,"index");
        flywheel = hardwareMap.get(DcMotor.class,"flywheel");

        //run without encoders temporarily (DT might use later)
        leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        midRoller.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        index.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //Motor directions
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        midRoller.setDirection(DcMotor.Direction.REVERSE);
        index.setDirection(DcMotor.Direction.REVERSE);
        flywheel.setDirection(DcMotor.Direction.FORWARD);

        //Zero behaviour
//        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        midRoller.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        index.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT); //not enough inertia to keep spinning anyways
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT); //change to brake?

        //Init behaviour
        leftFront.setPower(0);
        leftBack.setPower(0);
        rightFront.setPower(0);
        rightBack.setPower(0);
        intake.setPower(0);
        midRoller.setPower(0);
        index.setPower(0);
        flywheel.setPower(0);

        telemetry.addData("Initialize","completed");
        telemetry.update();
    }

    final double driveSpeed = 1;
    final double turnSpeed = 1;
    final double intakeSpeed = 1;
    final double flywheelSpeed = 0.77; //tune this to ideal shooting distance/height

    @Override
    public void loop() {
        //controller data
        float drive = gamepad1.left_stick_y;
        float strafe = gamepad1.left_stick_x;
        float turn = gamepad1.right_stick_x;


        //dt motor powers
        double frontLeftPower = (drive - strafe)*driveSpeed - turn*turnSpeed;
        double backLeftPower = (drive + strafe)*driveSpeed - turn*turnSpeed;
        double frontRightPower = (drive + strafe)*driveSpeed + turn*turnSpeed;
        double backRightPower = (drive - strafe)*driveSpeed + turn*turnSpeed;
        leftFront.setPower(frontLeftPower);
        leftBack.setPower(backLeftPower);
        rightFront.setPower(frontRightPower);
        rightBack.setPower(backRightPower);

        //index balls (stored before the index roller)
        if (gamepad1.right_bumper){
            intake.setPower(intakeSpeed);
            midRoller.setPower(intakeSpeed*0.9);
        }
        //second stage and score -> add distance detect to auto-stop
        if (gamepad1.left_bumper) {
            index.setPower(1); //1:1 with midRoller
        }
        //outtake index roller only (micro adjustment)
        if (gamepad1.right_trigger>0 && gamepad1.right_trigger<0.5){ //trigger measures depth of press as a float
            index.setPower(-0.4); //tune to adjust ball back slightly when tapped
        }
        //outtake all rollers
        if (gamepad1.right_trigger>0.5){
            index.setPower(-1);
            midRoller.setPower(-1);
            intake.setPower(-1);
        }

        //flywheel
        double flywheelPower = gamepad1.left_trigger;
        flywheel.setPower(flywheelPower*flywheelSpeed); //limiting max speed (tuned to goal height)

        //alternate flywheel -> safeguard
        /*
        if (gamepad1.left_trigger*flywheelSpeed>0.1){
            flywheel.setPower(gamepad1.left_trigger*flywheelSpeed);
        }
        flywheel.setPower(0);
        */

        if (gamepad1.y){ //dead spot reverse -> kinda sucks for now
            flywheel.setPower(-1);
        }

        if (gamepad1.a){ //dead spot reverse -> kinda sucks for now
            flywheel.setPower(1);
        }

        intake.setPower(0);
        midRoller.setPower(0);
        index.setPower(0);

        telemetry.addData("Drive", drive*100);
        telemetry.addData("Strafe", strafe*100);
        telemetry.addData("Turn", turn*100);
        telemetry.addLine();
        telemetry.addData("Flywheel",flywheelPower*100);
        telemetry.update();
    }
}

/* Note to other coders or teams:
I'm not sure why but the code only compiles and pushes to the driver hub when directly plugged into it (on the robot).
Please take note of this.

Things to remember at a comp:
 - License plate colors
 - Connected and activated controller (start + A/B)
 - Correct config
 - Correct auton/teleop running
 - Same wifi connection
 - Robot starting position
 - Robot battery and DS charge

    This year:
 - Push down the flywheel to ensure it's adjusted into motor gear
 - Check to ensure midRoller motor is plugged in (broken clip)
 - Check for loose screws, misaligned gears, increased friction, broken parts

Good luck!

 - Maxwell 2025
 - Team 24226
 */