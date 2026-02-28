package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Autonomous(name = "updatedTwoBallBlue_Vision")
public class updatedTwoBallBlue extends LinearOpMode {
    // Hardware Declarations
    public DcMotor leftFront, leftBack, rightFront, rightBack;
    public DcMotor intake, midRoller, flywheel;

    // Movement Variables
    double forward = 0;
    double turn = 0;
    double strafe = 0;

    // AprilTag / Vision
    private static final boolean USE_WEBCAM = true;
    private static final int DESIRED_TAG_ID = -1;  // -1 = any tag; set to specific ID for backdrop parking
    private static final double DESIRED_DISTANCE_INCHES = 12.0;
    private static final double SPEED_GAIN = 0.02;
    private static final double STRAFE_GAIN = 0.015;
    private static final double TURN_GAIN = 0.01;
    private static final double MAX_AUTO_SPEED = 0.5;
    private static final double MAX_AUTO_STRAFE = 0.5;
    private static final double MAX_AUTO_TURN = 0.3;
    private static final long APRILTAG_TIMEOUT_MS = 8000;

    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;

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

        // 3. INIT APRILTAG (webcam)
        initAprilTag();
        if (USE_WEBCAM) {
            setManualExposure(6, 250);
        }

        telemetry.addData("Status", "Initialized - Vision (AprilTag + Webcam)");
        telemetry.addData("Stage", "Waiting for START");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            // ---------- STAGE 1: Pre-programmed path ----------
            telemetry.addData("Stage", "1 - Move out & shoot");
            telemetry.update();

            moveWithTelemetry("Strafing right", 0.6, 0, 0, 2000);
            moveWithTelemetry("Backing up", -0.5, 0, 0, 1500);
            moveWithTelemetry("Aiming", 0, -0.8, 0, 450);
            executeActualShoot();
            moveWithTelemetry("Rotating 180", 0, 0.6, 0, 1800);
            moveWithTelemetry("Returning", 0.6, 0, 0, 1500);

            // ---------- STAGE 2: AprilTag parking ----------
            telemetry.addData("Stage", "2 - Drive to AprilTag");
            telemetry.update();
            driveToAprilTag();

            stopMotors();
        }

        if (visionPortal != null) {
            visionPortal.close();
        }
    }

    private void initAprilTag() {
        aprilTag = new AprilTagProcessor.Builder().build();
        aprilTag.setDecimation(2);

        if (USE_WEBCAM) {
            visionPortal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                    .addProcessor(aprilTag)
                    .build();
        } else {
            visionPortal = new VisionPortal.Builder()
                    .setCamera(BuiltinCameraDirection.BACK)
                    .addProcessor(aprilTag)
                    .build();
        }
    }

    private void setManualExposure(int exposureMS, int gain) {
        if (visionPortal == null) return;
        if (visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            while (!isStopRequested() && visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
                sleep(20);
            }
        }
        if (!isStopRequested()) {
            ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
            if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
                exposureControl.setMode(ExposureControl.Mode.Manual);
                sleep(50);
            }
            exposureControl.setExposure((long) exposureMS, TimeUnit.MILLISECONDS);
            sleep(20);
            GainControl gainControl = visionPortal.getCameraControl(GainControl.class);
            gainControl.setGain(gain);
            sleep(20);
        }
    }

    private void driveToAprilTag() {
        long startTime = System.currentTimeMillis();
        double drive, str, turn;

        while (opModeIsActive() && (System.currentTimeMillis() - startTime) < APRILTAG_TIMEOUT_MS) {
            AprilTagDetection desiredTag = null;
            List<AprilTagDetection> detections = aprilTag.getDetections();

            for (AprilTagDetection detection : detections) {
                if (detection.metadata != null && (DESIRED_TAG_ID < 0 || detection.id == DESIRED_TAG_ID)) {
                    desiredTag = detection;
                    break;
                }
            }

            if (desiredTag != null) {
                double rangeError = desiredTag.ftcPose.range - DESIRED_DISTANCE_INCHES;
                double headingError = desiredTag.ftcPose.bearing;
                double yawError = desiredTag.ftcPose.yaw;

                drive = Range.clip(rangeError * SPEED_GAIN, -MAX_AUTO_SPEED, MAX_AUTO_SPEED);
                turn = Range.clip(headingError * TURN_GAIN, -MAX_AUTO_TURN, MAX_AUTO_TURN);
                str = Range.clip(-yawError * STRAFE_GAIN, -MAX_AUTO_STRAFE, MAX_AUTO_STRAFE);

                if (Math.abs(rangeError) < 2.0 && Math.abs(headingError) < 5 && Math.abs(yawError) < 5) {
                    stopMotors();
                    telemetry.addData("AprilTag", "At target");
                    telemetry.update();
                    sleep(300);
                    return;
                }

                telemetry.addData("AprilTag", "ID %d Range %.1f\" Bearing %.1f°", desiredTag.id, desiredTag.ftcPose.range, desiredTag.ftcPose.bearing);
            } else {
                drive = 0;
                str = 0;
                turn = 0;
                telemetry.addData("AprilTag", "No tag - holding");
            }

            telemetry.update();
            moveRobot(drive, str, turn);
            sleep(20);
        }

        stopMotors();
        telemetry.addData("AprilTag", "Done or timeout");
        telemetry.update();
    }

    private void moveRobot(double x, double y, double yaw) {
        double lf = x - y - yaw;
        double rf = x + y + yaw;
        double lb = x + y - yaw;
        double rb = x - y + yaw;
        double max = Math.max(Math.max(Math.abs(lf), Math.abs(rf)), Math.max(Math.abs(lb), Math.abs(rb)));
        if (max > 1.0) {
            lf /= max;
            rf /= max;
            lb /= max;
            rb /= max;
        }
        leftFront.setPower(lf);
        rightFront.setPower(rf);
        leftBack.setPower(lb);
        rightBack.setPower(rb);
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
        forward = f;
        turn = t;
        strafe = s;
        applyPowers();
        sleep(time);
        stopMotors();
        sleep(500);
    }

    public void applyPowers() {
        leftFront.setPower(forward + turn + strafe);
        rightFront.setPower(forward - turn - strafe);
        leftBack.setPower(forward + turn - strafe);
        rightBack.setPower(forward - turn + strafe);
    }

    public void stopMotors() {
        forward = 0;
        turn = 0;
        strafe = 0;
        applyPowers();
    }
}
