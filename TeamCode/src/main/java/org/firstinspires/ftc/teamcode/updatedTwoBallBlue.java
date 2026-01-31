package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name="updatedTwoBallBlue_NoVision")
public class updatedTwoBallBlue extends LinearOpMode {
    // Hardware Declarations
    public DcMotor leftFront, leftBack, rightFront, rightBack;
    public DcMotor intake, midRoller, flywheel;

    // Movement Variables
    double forward = 0;
    double turn = 0;
    double strafe = 0;

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

        // 2. MOTOR DIRECTIONS
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        midRoller.setDirection(DcMotor.Direction.REVERSE);
        flywheel.setDirection(DcMotor.Direction.FORWARD);

        telemetry.addData("Status", "Initialized - No Vision Mode");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            // STEP 1: MOVE OUT PART 1 - STRAFE RIGHT
            moveWithTelemetry("Strafing right", 0.6, 0, 0, 2000);

            // STEP 2: MOVE OUT PART 2 - BACK UP
            moveWithTelemetry("Backing up", -0.5, 0, 0, 1500);

            // STEP 3: TURN TO AIM
            moveWithTelemetry("Aiming", 0, -0.8, 0, 450);

            // STEP 4: SHOOT
            executeActualShoot();

            // STEP 5: TURN AROUND 180
            moveWithTelemetry("Rotating 180", 0, 0.6, 0, 1800);

            // STEP 6: MOVE STRAIGHT DOWN
            moveWithTelemetry("Returning", 0.6, 0, 0, 1500);

            // STEP 7: STRAFE LEFT TO PARK
            moveWithTelemetry("Parking", 0, 0, -0.6, 1000);

            stopMotors();
        }
    }

    public void executeActualShoot() {
        flywheel.setPower(0.79);
        sleep(1500);
        midRoller.setPower(1.0);
        intake.setPower(1.0);
        sleep(3000);
        midRoller.setPower(0);
        intake.setPower(0);
        flywheel.setPower(0);
    }

    public void moveWithTelemetry(String status, double f, double t, double s, long time) {
        telemetry.addData("Action", status);
        telemetry.update();
        forward = f; turn = t; strafe = s;
        applyPowers();
        sleep(time);
        stopMotors();
        sleep(500); // Short pause for stability
    }

    public void applyPowers() {
        leftFront.setPower(forward + turn + strafe);
        rightFront.setPower(forward - turn - strafe);
        leftBack.setPower(forward + turn - strafe);
        rightBack.setPower(forward - turn + strafe);
    }

    public void stopMotors() {
        forward = 0; turn = 0; strafe = 0;
        applyPowers();
    }
}