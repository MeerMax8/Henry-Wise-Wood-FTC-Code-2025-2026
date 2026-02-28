# 6-Ball Autonomous – Stage Design

## Overview

The autonomous is implemented as a **state machine** in alliance-specific op modes. The robot starts with the **gate closed**. The gate is **opened only in the OPEN_GATE stage**, immediately before scoring, so that when the flywheel and feed run, the six balls can pass through. After scoring, the gate is closed again.

---

## Stage Summary

| Stage             | Goal                         | Movement                    | Mechanisms              | Advance condition / safeguard      |
|-------------------|------------------------------|-----------------------------|-------------------------|------------------------------------|
| **INIT**          | Ensure gate closed, init HW  | None                        | Gate → CLOSED           | Immediate → LEAVE_START            |
| **LEAVE_START**   | Clear starting position      | Forward                     | None                    | Time ≥ `TIME_LEAVE_START_MS` or timeout |
| **COLLECT_1**     | First ball collection       | Forward                     | Intake + mid roller ON  | Time ≥ `TIME_COLLECT_PHASE_MS` or timeout |
| **COLLECT_2**     | Second collection           | Forward                     | Intake + mid roller ON  | Same                               |
| **COLLECT_3**     | Third collection            | Forward                     | Intake + mid roller ON  | Same                               |
| **ALIGN_TO_SCORE**| Aim at goal                 | Turn in place               | None                    | Time ≥ `TIME_ALIGN_MS` or timeout   |
| **OPEN_GATE**     | Allow balls to exit          | None                        | Gate → OPEN             | Time ≥ `TIME_GATE_OPEN_MS` or timeout |
| **SCORE**         | Shoot 6 balls                | None                        | Flywheel spinup, then feed (mid + intake) | Time ≥ spinup + feed or timeout |
| **CLOSE_GATE**    | Secure mechanism             | None                        | Gate → CLOSED           | Time ≥ `TIME_GATE_CLOSE_MS` or timeout |
| **PARK**          | Brief strafe toward backdrop | Strafe (Blue: right; Red: left) | None | Time ≥ `TIME_PARK_MS` or timeout    |
| **APRILTAG_PARK** | Drive to AprilTag for parking| Vision-based drive (range, bearing, yaw) | None | At target (range/angle) or `TIMEOUT_APRILTAG_MS` |
| **DONE**          | Stop all motors              | Stop                        | All off, gate closed, vision closed | —                                   |

---

## When and How the Gate Is Used

- **Start of autonomous:** Gate is set to **CLOSED** in INIT (and in `initHardware` before start).
- **Opening:** In stage **OPEN_GATE**, the gate servo is set to `GATE_OPEN`. This happens **after** alignment and **before** SCORE. The robot then runs flywheel + feed in SCORE so all six balls can pass through the open gate.
- **Closing:** In stage **CLOSE_GATE**, the gate is set back to **CLOSED** after scoring. The robot then parks in PARK and stops in DONE.

Constants (in code):

- `GATE_CLOSED` – servo position when gate is closed (e.g. `0.0`).
- `GATE_OPEN` – servo position when gate is open (e.g. `1.0`).

Adjust these and the servo name (`"gate"`) to match your mechanism.

---

## Safeguards

- **Timeouts:** Every stage has a maximum duration (e.g. `TIMEOUT_LEAVE_START_MS`, `TIMEOUT_COLLECT_MS`, ...). If the stage doesn’t finish in time, the state machine still advances so the routine doesn’t get stuck.
- **Fail-safe:** In DONE, `stopAll()` is called so drive, intake, flywheel, and gate are all turned off / returned to a safe state.

---

## Extending with Encoders or Sensors

To advance stages on **encoder** or **sensor** conditions instead of (or in addition to) time:

1. **Encoder example (e.g. LEAVE_START):**  
   - In `initHardware()`, set drive motors to `RUN_TO_POSITION` or `RUN_USING_ENCODER` and optionally reset encoders when entering the stage.  
   - In the stage’s `switch` case, compute `currentPosition` (e.g. average of left/right).  
   - Advance when `currentPosition >= targetTicks` **or** `elapsed >= TIMEOUT_...`.

2. **Sensor example (e.g. ball count):**  
   - If you have a sensor (e.g. color/distance) that indicates “ball present” or “ball count”:  
   - In COLLECT_1/2/3, advance when `ballCount >= 2` (or desired count) **or** `elapsed >= TIMEOUT_COLLECT_MS`.

Pseudocode for encoder-based LEAVE_START:

```text
case LEAVE_START:
  if (stageJustEntered) { resetEncoders(); setTargetPosition(targetTicks); }
  drive(0.5, 0, 0);
  int pos = getAverageDrivePosition();
  if (pos >= targetTicks || elapsed >= TIMEOUT_LEAVE_START_MS) {
    stopDrive();
    transitionTo(COLLECT_1);
  }
  break;
```

---

## Alliance-specific op modes (with AprilTag)

- **Blue:** `SixBallAutonomousBlue.java` — OpMode name: **"6-Ball Blue (State Machine)"**. Align turn negative (cw); park strafe right; then **AprilTag parking** via webcam (drive to tag).
- **Red:** `SixBallAutonomousRed.java` — OpMode name: **"6-Ball Red (State Machine)"**. Align turn positive (ccw); park strafe left; then **AprilTag parking** via webcam.
- **Generic:** `SixBallAutonomous.java` — single op mode (no AprilTag, no alliance-specific turn/strafe).

Both Blue and Red use **Webcam 1** and the same AprilTag gains; set `DESIRED_TAG_ID` to a specific backdrop tag or `-1` for any tag.

## File and Configuration

- **OpModes:** `SixBallAutonomousBlue.java`, `SixBallAutonomousRed.java`, `SixBallAutonomous.java`  
- **Robot Configuration:** Ensure a servo is named **`gate`** (or change the name in `initHardware()`).  
- Tune times and timeouts at the top of each file. Tune `ALIGN_TURN` and `PARK_STRAFE` in Blue/Red for your field.
