package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.List;

@Autonomous(name="updatedTwoBallRed")
public class updatedTwoBallRed extends LinearOpMode {
    // Hardware Declarations
    public DcMotor leftFront, leftBack, rightFront, rightBack;
    public DcMotor intake, midRoller, flywheel;

    // Vision Declarations
    public AprilTagProcessor aprilTag;
    public VisionPortal visionPortal;

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

        // 2. VISION INITIALIZATION
        initAprilTag();

        // 3. MOTOR DIRECTIONS
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        midRoller.setDirection(DcMotor.Direction.REVERSE);
        flywheel.setDirection(DcMotor.Direction.FORWARD);

        telemetry.addData("Status", "Initialized with Vision");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            // STEP 1: MOVE OUT PART 1 - STRAFE LEFT
            moveWithTelemetry("Strafing left",-0.6, 0, 0, 2000);

            // STEP 2: MOVE OUT PART 2 - BACK UP (Back up to Shooting Zone)
            moveWithTelemetry("Backing up", -0.5, 0, 0, 1500);

            // STEP 3: TURN TO AIM (Manual Turn)
            moveWithTelemetry("Aiming", 0, 0.8, 0, 450);

            // OPTIONAL: VISION CHECK (Telemetry only, doesn't stop movement)
            checkAprilTags();

            // STEP 4: SHOOT
            executeActualShoot();

            // STEP 5: TURN AROUND 180
            moveWithTelemetry("Rotating 180", 0, 0.3, 0, 1800);

            // STEP 6: MOVE STRAIGHT DOWN
            moveWithTelemetry("Returning", 0.6, 0, 0, 1500);

            // STEP 7: STRAFE RIGHT TO PARK
            moveWithTelemetry("Parking", 0, 0, -0.6, 1000);

            stopMotors();
        }
    }

    /**
     * Initialize the AprilTag processor.
     */
    private void initAprilTag() {
        aprilTag = new AprilTagProcessor.Builder().build();

        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTag)
                .build();
    }

    /**
     * Logic to display detected tags during the run.
     */
    private void checkAprilTags() {
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                telemetry.addData("Found Tag", "ID %d (%s)", detection.id, detection.metadata.name);
                telemetry.addData("Range", "%5.1f inches", detection.ftcPose.range);
            }
        }
        telemetry.update();
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