# Autonomous Paths Explained (Like You're a 12th Grader)

Hey! So you want to understand how our autonomous code works? Cool, let me break it down in a way that actually makes sense. We've got a bunch of different autonomous programs, and they all do similar things but in slightly different ways. Let me walk you through everything.

## The Big Picture

Think of autonomous like a robot dance routine. The robot has to:
1. Move around the field
2. Pick up balls (or "artifacts" as the game calls them)
3. Shoot them into a goal
4. Park somewhere for bonus points

All our autonomous programs follow this basic pattern, but some are simpler and some are more complex.

---

## How the Robot Moves (Mecanum Drive Math)

Before we get into the specific programs, you need to understand how our robot moves. We have a **mecanum drive**, which means we have 4 wheels that can move in any direction.

### The Four Motors
- `leftFront` - front left wheel
- `leftBack` - back left wheel  
- `rightFront` - front right wheel
- `rightBack` - back right wheel

### Movement Formula
When we want to move, we calculate power for each wheel using this formula:
```
leftFront  = forward + turn + strafe
rightFront = forward - turn - strafe
leftBack   = forward + turn - strafe
rightBack  = forward - turn + strafe
```

**What this means:**
- **Forward/Backward**: All wheels spin the same direction (forward = positive, backward = negative)
- **Turn**: Left wheels go one way, right wheels go the opposite way
- **Strafe**: Front and back wheels spin opposite directions, making the robot slide sideways

**Example:** If we want to move forward at 50% power:
- `forward = 0.5`, `turn = 0`, `strafe = 0`
- All wheels get 0.5 power, robot goes straight

**Example:** If we want to turn right:
- `forward = 0`, `turn = -0.5`, `strafe = 0`
- Left wheels get +0.5, right wheels get -0.5, robot spins clockwise

---

## How We Shoot Balls

All our programs use the same shooting method. We don't use a servo gate - we just use motors:

1. **Flywheel** - Spins up to launch the ball (like a pitching machine)
2. **Mid Roller** - Pushes balls from storage into the flywheel
3. **Intake** - Pulls balls into the robot and feeds them to mid roller

**The Shooting Process:**
```
1. Start flywheel spinning (power = 0.79)
2. Wait 1.5 seconds for it to get up to speed
3. Turn on midRoller and intake (both at full power)
4. Wait 3-8 seconds (depending on how many balls)
5. Turn everything off
```

It's like a conveyor belt system - intake grabs the ball, mid roller moves it to the flywheel, and the flywheel launches it.

---

## The Different Autonomous Programs

We have programs for different scenarios. Let me break them down:

### 1. Two-Ball Autonomous (Simple Version)

**Files:** `updatedTwoBallBlue.java`, `updatedTwoBallRed.java`

**What it does:** This is our simplest autonomous. It shoots the two balls we start with, then parks.

**The Steps:**
1. **Move out of starting position** - Strafe sideways to get clear of the wall
2. **Back up a bit** - Get some distance from the wall
3. **Turn to aim** - Rotate to face the goal
4. **Shoot** - Fire both balls
5. **Turn around** - Face the opposite direction
6. **Drive forward** - Move toward the parking area
7. **Strafe to park** - Slide sideways into the parking zone

**Red vs Blue differences:**
- Red turns **positive** (counter-clockwise) to aim, strafes **left** to park
- Blue turns **negative** (clockwise) to aim, strafes **right** to park

**Why?** Because the field is mirrored - what's left for red is right for blue.

---

### 2. Two-Ball Autonomous with Vision (Fancy Version)

**Files:** `updatedTwoBallRed.java` (has vision), `updatedTwoBallBlue.java` (no vision)

**What it does:** Same as above, but uses **AprilTag vision** to park precisely.

**What's AprilTag?** Think of it like QR codes that the robot can see with its camera. The game puts these tags on the backdrop, and the robot can drive to them automatically.

**The Steps:**
1. **Pre-programmed path** - Same as simple version (move out, shoot, etc.)
2. **AprilTag parking** - Instead of just strafing to park, the robot:
   - Looks for an AprilTag with its camera
   - Calculates how far away it is
   - Calculates what angle it's at
   - Drives toward it automatically
   - Stops when it's close enough (12 inches away, within 5 degrees)

**How AprilTag works:**
- The camera sees the tag and gives us three numbers:
  - **Range** - How far away (in inches)
  - **Bearing** - Left/right angle (in degrees)
  - **Yaw** - Rotation of the tag (tilted left or right)

- We calculate "errors" (how far off we are):
  ```
  rangeError = currentDistance - desiredDistance (12 inches)
  headingError = bearing (how much we need to turn)
  yawError = yaw (how much we need to strafe)
  ```

- We convert errors to motor powers using "gains" (multipliers):
  ```
  drive = rangeError × 0.02  (move forward/back)
  turn = headingError × 0.01  (rotate)
  strafe = -yawError × 0.015  (slide sideways)
  ```

- The gains are small numbers so the robot moves smoothly and doesn't overshoot

**Why use vision?** More accurate parking = more bonus points. Plus it's cool.

---

### 3. Three-Ball Autonomous

**Files:** `threeBallRed.java`, `threeBallBlue.java`

**What it does:** Shoots three balls (the two we start with, plus one we pick up).

**The Steps:**
1. **Approach goal** - Drive forward toward the shooting area
2. **Stabilize turn** - Small turn to line up perfectly
3. **Shoot** - Fire all three balls
4. **Rotate 180** - Turn around to face the other direction
5. **Return** - Drive back toward starting area
6. **Park** - Strafe into parking zone

**Note:** This one doesn't actually collect a third ball in the code - it just shoots three. You'd need to add collection logic if you want to pick up a ball during autonomous.

---

### 4. Six-Ball Autonomous (The Big One)

**Files:** `SixBallAutonomous.java`, `SixBallAutonomousRed.java`, `SixBallAutonomousBlue.java`

**What it does:** This is our most complex autonomous. It collects 6 balls total and shoots them all.

**How it works:** Uses a **state machine** - think of it like a flowchart where the robot is always in one "state" and moves to the next when conditions are met.

#### The States (Stages)

1. **INIT** - Set everything up, get ready
2. **LEAVE_START** - Drive out of starting position (1.8 seconds)
3. **COLLECT_1** - First collection phase (drive + intake for 2.5 seconds)
4. **COLLECT_2** - Second collection phase (another 2.5 seconds)
5. **COLLECT_3** - Third collection phase (another 2.5 seconds)
6. **ALIGN_TO_SCORE** - Turn to face the goal (1.2 seconds)
7. **SCORE** - Shoot all 6 balls (1.5 sec spinup + 8 sec feeding = 9.5 seconds total)
8. **PARK** - Drive to parking area (2 seconds)
9. **APRILTAG_PARK** - Use vision to park precisely (only in Red/Blue versions)
10. **DONE** - Stop everything

#### State Machine Logic

The code runs in a loop, and each loop it checks:
- What state am I in?
- How long have I been in this state?
- Should I move to the next state?

**Example:** In COLLECT_1 state:
```java
drive(0.4, 0, 0);  // Drive forward at 40% power
runIntake(true);    // Turn on intake and mid roller
if (elapsed >= 2500ms) {  // If 2.5 seconds have passed
    stopDrive();
    runIntake(false);
    transitionTo(COLLECT_2);  // Move to next state
}
```

**Timeouts:** Each state has a maximum time limit (timeout). If something goes wrong and we're stuck, we move on anyway. Safety feature!

#### Red vs Blue Differences

**SixBallAutonomousRed:**
- Align turn: **+0.5** (counter-clockwise)
- Park strafe: **+0.5** (left)
- Has AprilTag parking

**SixBallAutonomousBlue:**
- Align turn: **-0.5** (clockwise)
- Park strafe: **-0.5** (right)
- Has AprilTag parking

**SixBallAutonomous (generic):**
- Align turn: **-0.5** (adjust for your alliance)
- No AprilTag parking
- Simpler version

#### The Scoring Sequence

When we get to SCORE state:
1. **First 1.5 seconds:** Only flywheel spins (getting up to speed)
2. **After 1.5 seconds:** Flywheel + midRoller + intake all run
3. **After 9.5 seconds total:** Everything stops, move to PARK

This gives the flywheel time to spin up before we start feeding balls.

---

## Key Concepts You Should Know

### 1. Hardware Mapping

Before we can use any motor, we have to "map" it - tell the code what it's called in the robot configuration:

```java
leftFront = hardwareMap.get(DcMotor.class, "leftFront");
```

This says "get me the motor named 'leftFront' from the robot config."

### 2. Motor Directions

Motors can spin forward or backward. We set directions so positive power = forward movement:

```java
leftFront.setDirection(DcMotor.Direction.REVERSE);
```

This means when we give leftFront positive power, it spins backward (which makes the robot go forward because of how it's mounted).

### 3. Telemetry

Telemetry is like a dashboard - it shows info on the driver station screen:

```java
telemetry.addData("Stage", currentStage);
telemetry.update();
```

This helps us debug and see what the robot is doing.

### 4. Sleep vs Time-Based Movement

**Sleep:** Pauses the entire program
```java
sleep(1000);  // Wait 1 second, do nothing
```

**Time-based:** Check elapsed time while doing other things
```java
long startTime = System.currentTimeMillis();
while (elapsed < 2000) {
    drive(0.5, 0, 0);  // Keep driving
    elapsed = System.currentTimeMillis() - startTime;
}
```

Time-based is better because the robot can still react to things while moving.

### 5. Power Clipping

Sometimes our math gives us motor powers greater than 1.0 (which is max). We "clip" them:

```java
if (max > 1.0) {
    lf /= max;  // Scale everything down proportionally
    rf /= max;
    // etc.
}
```

This keeps all motors balanced and prevents damage.

---

## Common Patterns

### Pattern 1: Move and Wait

```java
moveWithTelemetry("Moving forward", 0.5, 0, 0, 2000);
// This moves forward at 50% for 2 seconds, then stops
```

### Pattern 2: Shoot Sequence

```java
flywheel.setPower(0.79);  // Start flywheel
sleep(1500);               // Wait for spinup
midRoller.setPower(1.0);   // Start feeding
intake.setPower(1.0);
sleep(3000);               // Feed for 3 seconds
// Turn everything off
```

### Pattern 3: State Machine Transition

```java
if (elapsed >= TIME_LIMIT || elapsed >= TIMEOUT) {
    stopCurrentAction();
    transitionTo(NEXT_STATE);
}
```

---

## Tips for Understanding the Code

1. **Start with the simple programs** - `updatedTwoBallBlue` is easiest to follow
2. **Follow the flow** - Read `runOpMode()` from top to bottom
3. **Look at the constants** - Times and powers are defined at the top
4. **Check the state machine** - In six-ball, the `runStateMachine()` method is the heart
5. **Read the comments** - They explain what each section does

---

## Troubleshooting Tips

**Robot not moving?**
- Check hardware mapping names match robot config
- Check motor directions are set correctly
- Make sure motors aren't disabled

**Robot moving wrong direction?**
- Flip the sign on forward/turn/strafe values
- Check motor directions in initHardware()

**Shooting not working?**
- Check flywheel power (should be around 0.79)
- Make sure you're waiting for spinup time
- Verify intake and midRoller directions

**AprilTag not working?**
- Check camera is connected and named "Webcam 1"
- Make sure exposure settings are correct
- Verify tags are in the camera's view

---

## Summary

All our autonomous programs follow the same basic pattern:
1. Initialize hardware
2. Move to shooting position
3. Shoot balls
4. Park

The differences are:
- **How many balls** we shoot (2, 3, or 6)
- **Whether we collect** balls or just shoot pre-loaded ones
- **Whether we use vision** for precise parking
- **How complex** the movement path is

The six-ball autonomous is the most advanced because it uses a state machine to handle multiple collection phases and has vision-based parking. The two-ball programs are simpler and easier to understand.

Hope this helps! If you're confused about something specific, look at the code and trace through it step by step. The robot does exactly what the code tells it to do, in order.
