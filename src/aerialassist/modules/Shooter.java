package cougartech.aerialassist.modules;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import java.util.Date;

public class Shooter
{
    
    Talon mL1;
    Talon mL2;
    Talon mR1;
    Talon mR2;
    Servo cameraTilt;
    public DigitalInput stopSwitch;
    public DigitalInput ballDetect;
    Date nowTime;
    public boolean shooting = false;
    boolean reset = false;
    long sTime;
    long timeOn;
    public int state = 0;
    
    public Shooter(int mL1Port, int mL2Port, int mR1Port, int mR2Port,int stopPort, int ballDetectPort, int cameraTiltPort)
    {
        stopSwitch = new DigitalInput(stopPort);
        ballDetect = new DigitalInput(ballDetectPort);
        cameraTilt = new Servo(cameraTiltPort);
  
        mL1 = new Talon(mL1Port);
        mL2 = new Talon(mL2Port);
        mR1 = new Talon(mR1Port);
        mR2 = new Talon(mR2Port);
    }
    
    /*
     * cameraAutoTilt
     * - Adjusts the camera to appropriate posistion based on whether there is a ball in the robot
     */
    public void cameraAutoTilt()
    {
        if(!ballDetect.get())
        {
            cameraTilt.set(0.75);
        }
        else
        {
            cameraTilt.set(1.0);
        }
    }      
    
    /*
     * shoot
     * - Shooter state machine
     *      0: Idle
     *      1: Shoot
     *      2: Reset
     * 
     * @param timePer
     *      Time that the shooter motors are activated for
     * @param motorPower
     *      Power at which the shooter motors are run
     * @param button
     *      Operated interface of whether to shoot
     */
    public void shoot(double timePer, double motorPower, boolean button)
    {
        nowTime = new Date();
        
        switch(state)
        {
            case 0:
                mL1.set(0.0);
                mL2.set(0.0);
                mR1.set(0.0);
                mR2.set(0.0);
                
                if(button)
                {
                    if(!ballDetect.get())
                    {
                        System.out.println("State being set to 1");
                        timeOn = nowTime.getTime();
                        sTime = nowTime.getTime();
                        state = 1;
                    }                    
                }
                break;
                
            case 1:
                if(nowTime.getTime() < sTime + timePer)
                {
                    mL1.set(motorPower);
                    mL2.set(motorPower);
                    mR1.set(-motorPower);
                    mR2.set(-motorPower);
                }
                else
                {
                    System.out.println("Changing to rev");
                    System.out.println("Motor was on time: " + (nowTime.getTime() - timeOn));
                    state = 2;
                }
                break;
                
            case 2:
                if(stopSwitch.get())
                {
                    mL1.set(-0.0625);
                    mL2.set(0.0);
                    mR1.set(0.0625);
                    mR2.set(0.0);
                }
                else
                {
                    System.out.println("Changing to 0");
                    state = 0;
                }
                break;
            
            default:
                System.out.println("State reached unacceptable place!");
                break;     
        }
    }    
}
