package cougartech.aerialassist.modules;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

public class Drivebase
{
    
    CANJaguar mLF;
    CANJaguar mLB;
    CANJaguar mRF;
    CANJaguar mRB;
    public Joystick driveJoy;
    
    public Drivebase(int mLFPort, int mLBPort, int mRFPort, int mRBPort, int joyPort)
    {
        driveJoy = new Joystick(joyPort);
        
        try
        {
            mLF = new CANJaguar(mLFPort, CANJaguar.ControlMode.kPercentVbus);
            mLB = new CANJaguar(mLBPort, CANJaguar.ControlMode.kPercentVbus);
            mRF = new CANJaguar(mRFPort, CANJaguar.ControlMode.kPercentVbus);
            mRB = new CANJaguar(mRBPort, CANJaguar.ControlMode.kPercentVbus);
            mLF.enableControl();
            mLB.enableControl();
            mRF.enableControl();
            mRB.enableControl();
        }
        catch(CANTimeoutException ex)
        {
            System.out.println("<CANTimeoutException>");
            System.out.println("Where: Drivebase Init");
            ex.printStackTrace();
            System.out.println("</CANTimeoutException>");
        }
    }
    
    /*
     * driveForward
     * - Used in autonmous
     * 
     * @param speed
     *      Direction and speed
     */
    public void driveForward(double speed)
    {
        try
        {            
            mLF.setX(speed);
            mLB.setX(speed);
            mRF.setX(speed);
            mRB.setX(speed);
        }
        catch(CANTimeoutException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /*
     * turn
     * - Used in autonomous
     * 
     * @param speed
     *      Direction and speed
     */
    public void turn(double speed)
    {
        try
        {            
            mLF.setX(-speed);
            mLB.setX(-speed);
            mRF.setX(speed);
            mRB.setX(speed);
        }
        catch(CANTimeoutException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /*
     * joyDirection
     * @return 0-9 representing desired direction of travel
     */
    public int joyDirection()
    {
        if(driveJoy.getTwist() > 0.2)
        {
            return 8;
        }
        else if(driveJoy.getTwist() < -0.2)
        {
            return 9;
        }
        else if(driveJoy.getRawButton(6))
        {
            return 1;
        }
        else if(driveJoy.getRawButton(4))
        {
            return 3;
        }
        else if(driveJoy.getRawButton(3))
        {
            return 5;
        }
        else if(driveJoy.getRawButton(5))
        {
            return 7;
        }
        else if(driveJoy.getMagnitude() < 0.3)
        {
            return -1;
        }
        else if(driveJoy.getDirectionDegrees() > -22.5 && driveJoy.getDirectionDegrees() <= 22.5)
        {
            return 0;
        }        
        else if(driveJoy.getDirectionDegrees() > 67.5 && driveJoy.getDirectionDegrees() <= 112.5)
        {
            return 2;
        }        
        else if(driveJoy.getDirectionDegrees() > 157.5 || driveJoy.getDirectionDegrees() <= -157.5)
        {
            return 4;
        }
        else if(driveJoy.getDirectionDegrees() > -112.5 && driveJoy.getDirectionDegrees() <= -67.5)
        {
            return 6;
        }        
        else
        {
            return -1;
        }
    }
    
    /*
     * doDrive
     * - Utilizes joyDirection to set motors to appropriate speed and direction
     */
    public void doDrive()
    {            
        switch(this.joyDirection())
        {
            //F
            case 0:
                try
                {            
                    mLF.setX(driveJoy.getMagnitude());
                    mLB.setX(driveJoy.getMagnitude());
                    mRF.setX(driveJoy.getMagnitude());
                    mRB.setX(driveJoy.getMagnitude());
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //FR
            case 1:
                try
                {            
                    mLF.setX(0.5);
                    mLB.setX(0.0);
                    mRF.setX(0.0);
                    mRB.setX(0.5);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
            
            //R
            case 2:
                try
                {            
                    mLF.setX(driveJoy.getMagnitude());
                    mLB.setX(-driveJoy.getMagnitude());
                    mRF.setX(-driveJoy.getMagnitude());
                    mRB.setX(driveJoy.getMagnitude());
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //BR
            case 3:
                try
                {            
                    mLF.setX(0.0);
                    mLB.setX(-0.5);
                    mRF.setX(-0.5);
                    mRB.setX(0.0);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
            
            //B    
            case 4:
                try
                {            
                    mLF.setX(-driveJoy.getMagnitude());
                    mLB.setX(-driveJoy.getMagnitude());
                    mRF.setX(-driveJoy.getMagnitude());
                    mRB.setX(-driveJoy.getMagnitude());
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //BL
            case 5: 
                try
                {            
                    mLF.setX(-0.5);
                    mLB.setX(0.0);
                    mRF.setX(0.0);
                    mRB.setX(-0.5);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //L
            case 6:
                try
                {            
                    mLF.setX(-driveJoy.getMagnitude());
                    mLB.setX(driveJoy.getMagnitude());
                    mRF.setX(driveJoy.getMagnitude());
                    mRB.setX(-driveJoy.getMagnitude());
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //FL
            case 7:
                try
                {            
                    mLF.setX(0.0);
                    mLB.setX(0.5);
                    mRF.setX(0.5);
                    mRB.setX(0.0);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
            
            //TR    
            case 8:
                try
                {            
                    mLF.setX((Math.abs(driveJoy.getTwist()) - 0.4));
                    mLB.setX((Math.abs(driveJoy.getTwist()) - 0.4));
                    mRF.setX(-(Math.abs(driveJoy.getTwist()) - 0.4));
                    mRB.setX(-(Math.abs(driveJoy.getTwist()) - 0.4));
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //TL
            case 9:
                try
                {            
                    mLF.setX(-(Math.abs(driveJoy.getTwist()) - 0.4));
                    mLB.setX(-(Math.abs(driveJoy.getTwist()) - 0.4));
                    mRF.setX((Math.abs(driveJoy.getTwist()) - 0.4));
                    mRB.setX((Math.abs(driveJoy.getTwist()) - 0.4));
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;            

            default:
                try
                {            
                    mLF.setX(0.0);
                    mLB.setX(0.0);
                    mRF.setX(0.0);
                    mRB.setX(0.0);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;              
        }
    }
    
    /*
     * doReverseDrive
     * - Utilizes joyDirection to set motors to appropriate direction and speed
     * - Is the reverse of doDrive for gathering purposes
     */
    public void doReverseDrive()
    {            
        switch(this.joyDirection())
        {
            //F
            case 0:
                try
                {            
                    mLF.setX(-driveJoy.getMagnitude());
                    mLB.setX(-driveJoy.getMagnitude());
                    mRF.setX(-driveJoy.getMagnitude());
                    mRB.setX(-driveJoy.getMagnitude());
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //FR
            case 1:
                try
                {            
                    mLF.setX(-0.5);
                    mLB.setX(0.0);
                    mRF.setX(0.0);
                    mRB.setX(-0.5);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
            
            //R
            case 2:
                try
                {            
                    mLF.setX(-driveJoy.getMagnitude());
                    mLB.setX(driveJoy.getMagnitude());
                    mRF.setX(driveJoy.getMagnitude());
                    mRB.setX(-driveJoy.getMagnitude());
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //BR
            case 3:
                try
                {            
                    mLF.setX(0.0);
                    mLB.setX(0.5);
                    mRF.setX(0.5);
                    mRB.setX(0.0);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
            
            //B    
            case 4:
                try
                {            
                    mLF.setX(driveJoy.getMagnitude());
                    mLB.setX(driveJoy.getMagnitude());
                    mRF.setX(driveJoy.getMagnitude());
                    mRB.setX(driveJoy.getMagnitude());
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //BL
            case 5: 
                try
                {            
                    mLF.setX(0.5);
                    mLB.setX(0.0);
                    mRF.setX(0.0);
                    mRB.setX(0.5);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //L
            case 6:
                try
                {            
                    mLF.setX(driveJoy.getMagnitude());
                    mLB.setX(-driveJoy.getMagnitude());
                    mRF.setX(-driveJoy.getMagnitude());
                    mRB.setX(driveJoy.getMagnitude());
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //FL
            case 7:
                try
                {            
                    mLF.setX(0.0);
                    mLB.setX(-0.5);
                    mRF.setX(-0.5);
                    mRB.setX(0.0);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
            
            //TR    
            case 8:
                try
                {            
                    mLF.setX((Math.abs(driveJoy.getThrottle()) - 0.4));
                    mLB.setX((Math.abs(driveJoy.getThrottle()) - 0.4));
                    mRF.setX(-(Math.abs(driveJoy.getThrottle()) - 0.4));
                    mRB.setX(-(Math.abs(driveJoy.getThrottle()) - 0.4));
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;
                
            //TL
            case 9:
                try
                {            
                    mLF.setX(-(Math.abs(driveJoy.getThrottle()) - 0.4));
                    mLB.setX(-(Math.abs(driveJoy.getThrottle()) - 0.4));
                    mRF.setX((Math.abs(driveJoy.getThrottle()) - 0.4));
                    mRB.setX((Math.abs(driveJoy.getThrottle()) - 0.4));
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;            

            default:
                try
                {            
                    mLF.setX(0.0);
                    mLB.setX(0.0);
                    mRF.setX(0.0);
                    mRB.setX(0.0);
                }
                catch(CANTimeoutException ex)
                {
                    ex.printStackTrace();
                }
                break;              
        }
    }
        
}
