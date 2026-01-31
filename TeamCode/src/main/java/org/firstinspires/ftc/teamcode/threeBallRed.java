package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name="threeBallRed")
public class threeBallRed extends LinearOpMode {
    // Hardware Declarations
    public DcMotor leftFront, leftBack, rightFront, rightBack;
    public DcMotor intake, midRoller, flywheel;

    // Movement Control Variables
    double forward = 0;
    double turn = 0;
    double strafe = 0;
    double maxDrivePower = 1.0;

    @Override
    public void runOpMode() throws InterruptedException {
        // 1. HARDWARE MAPPING
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        intake = hardwareMap.get(DcMotor.class, "intake");
        midRoller = hardwareMap.get(DcMotor.class, "midRoller");
        flywheel = hardwareMap.get(DcMotor.class, "flywheel");

        // 2. MOTOR DIRECTIONS & MODES
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);
        
        // Intake/Shooter directions
        intake.setDirection(DcMotor.Direction.FORWARD);
        midRoller.setDirection(DcMotor.Direction.REVERSE);
        flywheel.setDirection(DcMotor.Direction.FORWARD);

        leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        telemetry.addData("Status", "Initialized - Non-Vision Mode");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            // STEP 1: APPROACH GOAL
            moveWithTelemetry("Approaching Goal", 1.0, 0, 0, 2300);

            // STEP 2: SLOW TURN
            moveWithTelemetry("Stabilizing Turn", 0, 0.4, 0, 550);
            sleep(1000);

            // STEP 3: SHOOT ARTIFACTS
            telemetry.addData("Status", "Firing...");
            telemetry.update();
            executeActualShoot();
            sleep(1000);

            // STEP 4: ROTATE 180
            moveWithTelemetry("Rotating 180", 0, -0.5, 0, 2200);
            sleep(1000);

            // STEP 5: RETURN (Move straight down)
            moveWithTelemetry("Returning", 0.5, 0, 0, 1500);
            sleep(1000);

            // STEP 6: STRAFE TO PARK
            moveWithTelemetry("Parking", 0, 0, -0.5, 1000);

            stopMotors();
            telemetry.addData("Status", "Autonomous Complete");
            telemetry.update();
        }
    }

    // --- SHOOTING LOGIC ---

    public void executeActualShoot() {
        flywheel.setPower(0.8);
        sleep(800);            // Rev up time
        
        midRoller.setPower(1.0);
        intake.setPower(1.0);
        sleep(3000);           // Time to clear artifacts
        
        midRoller.setPower(0);
        intake.setPower(0);
        flywheel.setPower(0);
    }

    // --- MOVEMENT ENGINE ---

    public void moveWithTelemetry(String status, double f, double t, double s, long time) {
        telemetry.addData("Action", status);
        telemetry.update();
        forward = f; turn = t; strafe = s;
        applyPowers();
        sleep(time);
        stopMotors();
    }

    public void applyPowers() {
        double pF = forward * maxDrivePower;
        double pT = turn * maxDrivePower;
        double pS = strafe * maxDrivePower;

        leftFront.setPower(pF + pT + pS);
        rightFront.setPower(pF - pT - pS);
        leftBack.setPower(pF + pT - pS);
        rightBack.setPower(pF - pT + pS);
    }

    public void stopMotors() {
        forward = 0; turn = 0; strafe = 0;
        applyPowers();
    }
}