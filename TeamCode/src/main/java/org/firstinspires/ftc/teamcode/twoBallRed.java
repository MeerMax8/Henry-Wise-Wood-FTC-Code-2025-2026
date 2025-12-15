package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name="twoBallAuto")
public class twoBallAuto extends LinearOpMode {
    public DcMotor leftFront;
    public DcMotor leftBack;
    public DcMotor rightFront;
    public DcMotor rightBack;
    public DcMotor intake;
    public DcMotor midRoller;
    public DcMotor flywheel;

    @Override
    public void runOpMode() throws InterruptedException {
        //Control hub declarations
        leftFront = hardwareMap.get(DcMotor.class,"leftFront");
        leftBack = hardwareMap.get(DcMotor.class,"leftBack");
        rightFront = hardwareMap.get(DcMotor.class,"rightFront");
        rightBack = hardwareMap.get(DcMotor.class,"rightBack");
        intake = hardwareMap.get(DcMotor.class,"intake");
        midRoller = hardwareMap.get(DcMotor.class,"midRoller");
        flywheel = hardwareMap.get(DcMotor.class,"flywheel");

        //run without encoders temporarily (DT might use later)
        leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        midRoller.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //Motor directions
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        midRoller.setDirection(DcMotor.Direction.REVERSE);
        flywheel.setDirection(DcMotor.Direction.FORWARD);

        //Zero behaviour
//        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        midRoller.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT); //change to brake?

        //Init behaviour
        leftFront.setPower(0);
        leftBack.setPower(0);
        rightFront.setPower(0);
        rightBack.setPower(0);
        intake.setPower(0);
        midRoller.setPower(0);
        flywheel.setPower(0);

        telemetry.addData("Initialize","completed");
        telemetry.update();

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////AUTON CODE STARTS HERE////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        flywheel.setPower(0.2);
        leftFront.setPower(-0.4);
        leftBack.setPower(-0.4);
        rightFront.setPower(-0.4);
        rightBack.setPower(-0.4);

        sleep(1100);

        flywheel.setPower(0.4);

        leftFront.setPower(0);
        leftBack.setPower(0);
        rightFront.setPower(0);
        rightBack.setPower(0);

        flywheel.setPower(0.6);
        sleep(500);
        flywheel.setPower(0.74); //final speed
        sleep(1000);

        midRoller.setPower(1);
        sleep(2000);
        midRoller.setPower(0);

        intake.setPower(1);
        midRoller.setPower(0);

        sleep(2000);
        intake.setPower(0);
        midRoller.setPower(0);
        flywheel.setPower(0);

        leftFront.setPower(-0.4); //move off line
        leftBack.setPower(0.4);
        rightFront.setPower(0.4);
        rightBack.setPower(-0.4);

        sleep(1000);

        leftFront.setPower(0);
        leftBack.setPower(0);
        rightFront.setPower(0);
        rightBack.setPower(0);

    }

}
