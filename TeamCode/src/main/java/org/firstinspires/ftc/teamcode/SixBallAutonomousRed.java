package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
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

/**
 * 6-ball autonomous for RED alliance. State machine with gate and AprilTag parking.
 * Gate closed at start; opens before scoring. Final stage drives to AprilTag for parking.
 */
@Autonomous(name = "6-Ball Red (State Machine)")
public class SixBallAutonomousRed extends LinearOpMode {

    private DcMotor leftFront, leftBack, rightFront, rightBack;
    private DcMotor intake, midRoller, flywheel;
    private Servo gateServo;

    private static final double GATE_CLOSED = 0.0;
    private static final double GATE_OPEN   = 1.0;

    private static final long TIMEOUT_LEAVE_START_MS   = 2_500;
    private static final long TIMEOUT_COLLECT_MS       = 3_000;
    private static final long TIMEOUT_ALIGN_MS         = 2_000;
    private static final long TIMEOUT_GATE_MS          = 800;
    private static final long TIMEOUT_SCORE_MS         = 12_000;
    private static final long TIMEOUT_PARK_MS          = 4_000;
    private static final long TIMEOUT_APRILTAG_MS     = 8_000;

    // AprilTag (webcam)
    private static final boolean USE_WEBCAM = true;
    private static final int DESIRED_TAG_ID = -1;
    private static final double APRILTAG_DESIRED_DISTANCE_INCHES = 12.0;
    private static final double APRILTAG_SPEED_GAIN  = 0.02;
    private static final double APRILTAG_STRAFE_GAIN  = 0.015;
    private static final double APRILTAG_TURN_GAIN   = 0.01;
    private static final double APRILTAG_MAX_SPEED   = 0.5;
    private static final double APRILTAG_MAX_STRAFE   = 0.5;
    private static final double APRILTAG_MAX_TURN     = 0.3;
    private static final double APRILTAG_AT_TARGET_RANGE_INCHES = 2.0;
    private static final double APRILTAG_AT_TARGET_ANGLE_DEG    = 5.0;

    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;

    private static final long TIME_LEAVE_START_MS      = 1_800;
    private static final long TIME_COLLECT_PHASE_MS   = 2_500;
    private static final long TIME_ALIGN_MS           = 1_200;
    private static final long TIME_GATE_OPEN_MS       = 400;
    private static final long TIME_FLYWHEEL_SPINUP_MS = 1_500;
    private static final long TIME_FEED_BALLS_MS      = 8_000;
    private static final long TIME_GATE_CLOSE_MS      = 400;
    private static final long TIME_PARK_MS            = 2_000;

    // Red: leave start forward; aim turn positive (ccw); park strafe left (positive strafe)
    private static final double LEAVE_POWER   = 0.5;
    private static final double ALIGN_TURN   = 0.5;    // Red aim (match twoBallRed)
    private static final double PARK_STRAFE  = 0.5;     // Red strafe left

    public enum AutoStage {
        INIT, LEAVE_START, COLLECT_1, COLLECT_2, COLLECT_3,
        ALIGN_TO_SCORE, OPEN_GATE, SCORE, CLOSE_GATE, PARK, APRILTAG_PARK, DONE
    }

    private AutoStage currentStage = AutoStage.INIT;
    private long stageStartTimeMs = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        initHardware();
        initAprilTag();
        if (USE_WEBCAM) setManualExposure(6, 250);
        setGateClosed();

        telemetry.addData("Alliance", "RED");
        telemetry.addData("Stage", currentStage);
        telemetry.addData("Gate", "CLOSED at start");
        telemetry.addData("AprilTag", "Webcam ready");
        telemetry.update();

        waitForStart();
        if (!opModeIsActive()) return;

        stageStartTimeMs = System.currentTimeMillis();

        while (opModeIsActive()) {
            runStateMachine();
            telemetry.addData("Alliance", "RED");
            telemetry.addData("Stage", currentStage);
            telemetry.addData("Elapsed (ms)", System.currentTimeMillis() - stageStartTimeMs);
            telemetry.update();
            sleep(20);
        }

        stopAll();
        if (visionPortal != null) visionPortal.close();
    }

    private void runStateMachine() {
        long elapsed = System.currentTimeMillis() - stageStartTimeMs;

        switch (currentStage) {
            case INIT:
                setGateClosed();
                transitionTo(AutoStage.LEAVE_START);
                break;

            case LEAVE_START:
                drive(LEAVE_POWER, 0, 0);
                if (elapsed >= TIME_LEAVE_START_MS || elapsed >= TIMEOUT_LEAVE_START_MS) {
                    stopDrive();
                    transitionTo(AutoStage.COLLECT_1);
                }
                break;

            case COLLECT_1:
                drive(0.4, 0, 0);
                runIntake(true);
                if (elapsed >= TIME_COLLECT_PHASE_MS || elapsed >= TIMEOUT_COLLECT_MS) {
                    stopDrive();
                    runIntake(false);
                    transitionTo(AutoStage.COLLECT_2);
                }
                break;

            case COLLECT_2:
                drive(0.4, 0, 0);
                runIntake(true);
                if (elapsed >= TIME_COLLECT_PHASE_MS || elapsed >= TIMEOUT_COLLECT_MS) {
                    stopDrive();
                    runIntake(false);
                    transitionTo(AutoStage.COLLECT_3);
                }
                break;

            case COLLECT_3:
                drive(0.4, 0, 0);
                runIntake(true);
                if (elapsed >= TIME_COLLECT_PHASE_MS || elapsed >= TIMEOUT_COLLECT_MS) {
                    stopDrive();
                    runIntake(false);
                    transitionTo(AutoStage.ALIGN_TO_SCORE);
                }
                break;

            case ALIGN_TO_SCORE:
                drive(0, ALIGN_TURN, 0);  // Red: positive turn
                if (elapsed >= TIME_ALIGN_MS || elapsed >= TIMEOUT_ALIGN_MS) {
                    stopDrive();
                    transitionTo(AutoStage.OPEN_GATE);
                }
                break;

            case OPEN_GATE:
                setGateOpen();
                if (elapsed >= TIME_GATE_OPEN_MS || elapsed >= TIMEOUT_GATE_MS) {
                    transitionTo(AutoStage.SCORE);
                }
                break;

            case SCORE:
                if (elapsed <= TIME_FLYWHEEL_SPINUP_MS) {
                    flywheel.setPower(0.79);
                } else {
                    flywheel.setPower(0.79);
                    midRoller.setPower(1.0);
                    intake.setPower(1.0);
                }
                if (elapsed >= (TIME_FLYWHEEL_SPINUP_MS + TIME_FEED_BALLS_MS) || elapsed >= TIMEOUT_SCORE_MS) {
                    flywheel.setPower(0);
                    midRoller.setPower(0);
                    intake.setPower(0);
                    transitionTo(AutoStage.CLOSE_GATE);
                }
                break;

            case CLOSE_GATE:
                setGateClosed();
                if (elapsed >= TIME_GATE_CLOSE_MS || elapsed >= TIMEOUT_GATE_MS) {
                    transitionTo(AutoStage.PARK);
                }
                break;

            case PARK:
                drive(0, 0, PARK_STRAFE);  // Red: strafe left (positive)
                if (elapsed >= TIME_PARK_MS || elapsed >= TIMEOUT_PARK_MS) {
                    stopDrive();
                    transitionTo(AutoStage.APRILTAG_PARK);
                }
                break;

            case APRILTAG_PARK:
                runAprilTagPark();
                break;

            case DONE:
                stopAll();
                break;
        }
    }

    private void transitionTo(AutoStage next) {
        currentStage = next;
        stageStartTimeMs = System.currentTimeMillis();
    }

    private void initHardware() {
        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack   = hardwareMap.get(DcMotor.class, "leftBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        rightBack  = hardwareMap.get(DcMotor.class, "rightBack");
        intake     = hardwareMap.get(DcMotor.class, "intake");
        midRoller  = hardwareMap.get(DcMotor.class, "midRoller");
        flywheel   = hardwareMap.get(DcMotor.class, "flywheel");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        midRoller.setDirection(DcMotor.Direction.REVERSE);
        flywheel.setDirection(DcMotor.Direction.FORWARD);

        try {
            gateServo = hardwareMap.get(Servo.class, "gate");
        } catch (Exception e) {
            telemetry.addData("Warning", "Gate servo 'gate' not found");
        }
    }

    private void initAprilTag() {
        aprilTagProcessor = new AprilTagProcessor.Builder().build();
        aprilTagProcessor.setDecimation(2);
        if (USE_WEBCAM) {
            visionPortal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                    .addProcessor(aprilTagProcessor)
                    .build();
        } else {
            visionPortal = new VisionPortal.Builder()
                    .setCamera(BuiltinCameraDirection.BACK)
                    .addProcessor(aprilTagProcessor)
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
            ExposureControl ec = visionPortal.getCameraControl(ExposureControl.class);
            if (ec.getMode() != ExposureControl.Mode.Manual) {
                ec.setMode(ExposureControl.Mode.Manual);
                sleep(50);
            }
            ec.setExposure((long) exposureMS, TimeUnit.MILLISECONDS);
            sleep(20);
            GainControl gc = visionPortal.getCameraControl(GainControl.class);
            gc.setGain(gain);
            sleep(20);
        }
    }

    private void runAprilTagPark() {
        long elapsed = System.currentTimeMillis() - stageStartTimeMs;
        if (elapsed >= TIMEOUT_APRILTAG_MS) {
            stopDrive();
            transitionTo(AutoStage.DONE);
            return;
        }

        AprilTagDetection tag = null;
        List<AprilTagDetection> detections = aprilTagProcessor.getDetections();
        for (AprilTagDetection d : detections) {
            if (d.metadata != null && (DESIRED_TAG_ID < 0 || d.id == DESIRED_TAG_ID)) {
                tag = d;
                break;
            }
        }

        if (tag != null) {
            double rangeError = tag.ftcPose.range - APRILTAG_DESIRED_DISTANCE_INCHES;
            double headingError = tag.ftcPose.bearing;
            double yawError = tag.ftcPose.yaw;
            double drive = Range.clip(rangeError * APRILTAG_SPEED_GAIN, -APRILTAG_MAX_SPEED, APRILTAG_MAX_SPEED);
            double turn = Range.clip(headingError * APRILTAG_TURN_GAIN, -APRILTAG_MAX_TURN, APRILTAG_MAX_TURN);
            double strafe = Range.clip(-yawError * APRILTAG_STRAFE_GAIN, -APRILTAG_MAX_STRAFE, APRILTAG_MAX_STRAFE);

            if (Math.abs(rangeError) < APRILTAG_AT_TARGET_RANGE_INCHES
                    && Math.abs(headingError) < APRILTAG_AT_TARGET_ANGLE_DEG
                    && Math.abs(yawError) < APRILTAG_AT_TARGET_ANGLE_DEG) {
                stopDrive();
                transitionTo(AutoStage.DONE);
                return;
            }
            moveRobotAprilTag(drive, strafe, turn);
        } else {
            stopDrive();
        }
    }

    private void moveRobotAprilTag(double x, double y, double yaw) {
        double lf = x - y - yaw;
        double rf = x + y + yaw;
        double lb = x + y - yaw;
        double rb = x - y + yaw;
        double max = Math.max(Math.max(Math.abs(lf), Math.abs(rf)), Math.max(Math.abs(lb), Math.abs(rb)));
        if (max > 1.0) {
            lf /= max; rf /= max; lb /= max; rb /= max;
        }
        leftFront.setPower(lf);
        rightFront.setPower(rf);
        leftBack.setPower(lb);
        rightBack.setPower(rb);
    }

    private void setGateClosed() {
        if (gateServo != null) gateServo.setPosition(GATE_CLOSED);
    }

    private void setGateOpen() {
        if (gateServo != null) gateServo.setPosition(GATE_OPEN);
    }

    private void drive(double forward, double turn, double strafe) {
        double lf = forward + turn + strafe;
        double rf = forward - turn - strafe;
        double lb = forward + turn - strafe;
        double rb = forward - turn + strafe;
        double max = Math.max(Math.max(Math.abs(lf), Math.abs(rf)), Math.max(Math.abs(lb), Math.abs(rb)));
        if (max > 1.0) {
            lf /= max; rf /= max; lb /= max; rb /= max;
        }
        leftFront.setPower(lf);
        rightFront.setPower(rf);
        leftBack.setPower(lb);
        rightBack.setPower(rb);
    }

    private void stopDrive() {
        drive(0, 0, 0);
    }

    private void runIntake(boolean on) {
        intake.setPower(on ? 1.0 : 0.0);
        midRoller.setPower(on ? 1.0 : 0.0);
    }

    private void stopAll() {
        stopDrive();
        runIntake(false);
        flywheel.setPower(0);
        setGateClosed();
    }
}
