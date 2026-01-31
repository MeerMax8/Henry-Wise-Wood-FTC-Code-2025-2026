package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import java.util.List;

@Autonomous(name="threeBallRed")
public class threeBallRed extends LinearOpMode {
    // Hardware Declarations
    public DcMotor leftFront, leftBack, rightFront, rightBack;
    public DcMotor intake, midRoller, flywheel;

    // Vision Declarations
    public AprilTagProcessor aprilTag;
    public VisionPortal visionPortal;

    // Movement Control Variables
    double forward = 0;
    double turn = 0;
    double strafe = 0;
    double maxDrivePower = 1.0;

    @Override
    public void runOpMode() throws InterruptedException {
        // 1. HARDWARE MAPPING (Moulded to your real robot names)
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        intake = hardwareMap.get(DcMotor.class, "intake");
        midRoller = hardwareMap.get(DcMotor.class, "midRoller");
        flywheel = hardwareMap.get(DcMotor.class, "flywheel");

        // 2. VISION INITIALIZATION
        initAprilTag();

        // 3. MOTOR DIRECTIONS & MODES
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

        telemetry.addData("Status", "Initialized - Hardware Vision Mode");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            // STEP 1: APPROACH GOAL (Slower approach per your logic)
            moveWithTelemetry("Approaching Goal", 1.0, 0, 0, 2300);

            // STEP 2: SLOW TURN
            moveWithTelemetry("Stabilizing Turn", 0, 0.4, 0, 550);
            sleep(1000);

            // STEP 3: VISION SCAN (Displays tags while preparing to fire)
            checkAprilTags();

            // STEP 4: SHOOT ARTIFACTS (Replaces shoot() with actual motor logic)
            telemetry.addData("Status", "Firing...");
            telemetry.update();
            executeActualShoot();
            sleep(1000);

            // STEP 5: ROTATE 180
            moveWithTelemetry("Rotating 180", 0, -0.5, 0, 2200);
            sleep(1000);

            // STEP 6: RETURN (Move straight down)
            moveWithTelemetry("Returning", 0.5, 0, 0, 1500);
            sleep(1000);

            // STEP 7: STRAFE TO PARK
            moveWithTelemetry("Parking", 0, 0, -0.5, 1000);

            stopMotors();
            telemetry.addData("Status", "Autonomous Complete");
            telemetry.update();
        }
    }

    // --- VISION LOGIC ---

    private void initAprilTag() {
        aprilTag = new AprilTagProcessor.Builder().build();
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "webcam")) // Ensure your webcam is named "webcam"
                .addProcessor(aprilTag)
                .build();
    }

    private void checkAprilTags() {
        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                telemetry.addData("Detected Tag", "ID %d (%s)", detection.id, detection.metadata.name);
                telemetry.addData("Range", "%5.1f inches", detection.ftcPose.range);
                telemetry.addData("Yaw", "%5.1f deg", detection.ftcPose.yaw);
            }
        }
        telemetry.update();
    }

    // --- SHOOTING LOGIC (Moulded from your real hardware motors) ---

    public void executeActualShoot() {
        flywheel.setPower(0.8); // Your shootPower
        sleep(800);            // Rev up time
        
        // Feed artifacts using your intake and midRoller
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