package cougartech.aerialassist;

import com.sun.squawk.util.MathUtils;
import cougartech.aerialassist.modules.*;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.image.*;
import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;
import edu.wpi.first.wpilibj.Servo;
import java.util.Date;

public class RobotMain extends IterativeRobot
{

    RobotMap robotMap;
    Drivebase drive;
    Shooter shooter;
    Gatherer gatherer;
    Sensorbase sensor;
    Joystick joyG;
    NetworkTable netTable;
    Date nowTime;
    Eye hotStuff;    
    
    //Teleop
    boolean wantShoot = false;
    boolean once = false;
    boolean reverseDrive = false;
    
    //Auto
    int autoState = 0;
    long sDownTime;
    long sUpTime;
    long autoTime;
    long turnTime;
    boolean autoOnce = false;
    
    //Vision Tracking
    boolean shot;    
    boolean caught = false;

    public void robotInit()
    {
        drive = new Drivebase(10, 11, 12, 13, 1);
        joyG = new Joystick(2);
        shooter = new Shooter(9, 10, 7, 8, 1, 2, 3);
        gatherer = new Gatherer(5, 6, 14);
        sensor = new Sensorbase();
        netTable = NetworkTable.getTable("datatable");
        shot = false;
        hotStuff = new Eye();
    }   

    public void autonomousPeriodic()
    {
        //Construct date wrapper
        nowTime = new Date();
        
        //Autonomous State Machine
        switch (autoState)
        {
            //Drive To Distance Range
            case 0:
                if(!autoOnce)
                {
                    autoTime = nowTime.getTime();
                    autoOnce = true;
                }
                
                if (MathUtils.round(sensor.getDistance(1, 1)) / 12 >= 12 || sensor.getDistance(1, 1) == -2)
                {
                    drive.driveForward(0.33);
                }
                else if (MathUtils.round(sensor.getDistance(1, 1)) / 12 < 10 || sensor.getDistance(1, 1) == -1)
                {
                    drive.driveForward(-0.33);
                }
                else
                {
                    drive.driveForward(0.0);
                    sDownTime = nowTime.getTime();
                    System.out.println("Switching to autoState 1");
                    autoState = 1;
                }
                hotStuff.watch();
                break;

            //Lower Gatherer Arms
            case 1:
                if (nowTime.getTime() <= sDownTime + 850)
                {
                    gatherer.tiltArm(1);
                }
                else
                {
                    gatherer.tiltArm(0);
                    System.out.println("Switching to autoState 2");
                    autoState = 2;
                }
                hotStuff.watch();
                break;

            
            /*
             * Shoot
             * - Will utilize camera target recongition in previous states
             * - If target is found the shooter will activate
             * - After 5 seconds of autonomous the shooter will activate as an override 
             */
            case 2:
                shot = hotStuff.hotMess();
                if(shot)
                {
                    shooter.shoot(225, 1.0, true);

                    if(shooter.state == 0)
                    {
                        System.out.println("Switching autoState to 3");
                        sUpTime = nowTime.getTime();
                        autoState = 3;
                    }
                }
                else if(!caught)
                {
                    hotStuff.watch();
                }
                
                if(nowTime.getTime() >= autoTime + 5000)
                {
                    caught = true;
                    shooter.shoot(225, 1.0, true);
                    
                    if(shooter.state == 0)
                    {
                        System.out.println("OVERRIDE HIT swithincg to 3");
                        sUpTime = nowTime.getTime();
                        autoState = 3;
                    }
                }
                break;

            //Raise Gatherer Arms
            case 3:
                if(nowTime.getTime() < sUpTime + 1000)
                {
                    gatherer.tiltArm(-1);
                }
                else
                {
                    gatherer.tiltArm(0);
                    System.out.println("Switching autoState to 4");
                    turnTime = nowTime.getTime();
                    autoState = 4;
                }
                break;

            //Turn The Robot 180
            case 4:
                if(nowTime.getTime() < turnTime + 2000)
                {
                    drive.turn(0.33);
                }
                else
                {
                    drive.turn(0);
                    System.out.println("Switching autoState to 5");
                    autoState = 5;
                }                
                break;
                
            //Idle
            case 5:
                break;

            default:
                System.out.println("Autonomous has reached the wrong state. If you can read this contact Michael Guglielmo");
                break;
        }
    }  

    public void teleopPeriodic()
    {
        //Drivebase
        if (!reverseDrive)
        {
            drive.doDrive();
            netTable.putBoolean("reverseDrive", false);
        }
        else
        {
            drive.doReverseDrive();
            netTable.putBoolean("reverseDrive", true);
        }

        if (drive.driveJoy.getRawButton(12))
        {
            reverseDrive = false;
        }
        else if (drive.driveJoy.getRawButton(11))
        {
            reverseDrive = true;
        }


        //Network Table
        if (!once)
        {
            netTable.putNumber("timeOn", 0);
            netTable.putNumber("motorPower", 0);
            once = true;
        }
        
        netTable.putBoolean("ball", !shooter.ballDetect.get());
        netTable.putNumber("distance", MathUtils.round(sensor.getDistance(1, 1)));
        netTable.putNumber("shooterState", shooter.state);


        //Sonar Distance Sensor
        if ((MathUtils.round(sensor.getDistance(1, 1)) / 12) >= 7 && (MathUtils.round(sensor.getDistance(1, 1)) / 12) <= 9)
        {
            netTable.putBoolean("near", true);
            netTable.putBoolean("far", false);
        }
        else if (MathUtils.round(sensor.getDistance(1, 1)) / 12 >= 13 && (MathUtils.round(sensor.getDistance(1, 1)) / 12) <= 15)
        {
            netTable.putBoolean("near", false);
            netTable.putBoolean("far", true);
        }
        else
        {
            netTable.putBoolean("near", false);
            netTable.putBoolean("far", false);
        }


        //Shooter
        shooter.shoot(netTable.getNumber("timeOn"), netTable.getNumber("motorPower"), drive.driveJoy.getRawButton(1));
        shooter.cameraAutoTilt();


        //Gatherer
        //Tilt
        if (joyG.getY() >= 0.2)
        {
            //Out
            gatherer.tiltArm(-1);
        }
        else if (joyG.getY() <= -0.2)
        {
            //In
            gatherer.tiltArm(1);
        }
        else
        {
            gatherer.tiltArm(0);
        }

        //Spin Arm
        if (joyG.getRawButton(1))
        {
            //In
            gatherer.armDirection(1);
        }
        else if (joyG.getRawButton(2))
        {
            //Out
            gatherer.armDirection(-1);
        }
        else
        {
            gatherer.armDirection(0);
        }
    }

    public void testPeriodic()
    {
    }
}
