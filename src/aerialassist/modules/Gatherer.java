package cougartech.aerialassist.modules;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

public class Gatherer
{
    CANJaguar tilter;
    Talon arm1;
    Talon arm2;    
    
    public Gatherer(int arm1Port, int arm2Port, int tilterPort)
    {
        arm1 = new Talon(arm1Port);
        arm2 = new Talon(arm2Port);
        
        try
        {
            tilter = new CANJaguar(tilterPort);
            tilter.enableControl();
        }
        catch(CANTimeoutException ex)
        {
            ex.printStackTrace();
        }        
    }
    
    /*
     * armDirection
     * 
     * @param direction
     *      Direction for arms to spin
     */
    public void armDirection(int direction)
    {
        if(direction == 1)
        {
            arm1.set(0.35);
            arm2.set(-0.35);
        }
        else if(direction == -1)
        {
            arm1.set(-0.35);
            arm2.set(0.35);
        }
        else
        {
            arm1.set(0.0);
            arm2.set(0.0);
        }
    }

    /*
     * tiltArm
     * 
     * @param direction
     *      Direction of the tilter to move
     */
    public void tiltArm(int direction)
    {
        if(direction == 1)
        {   
            try
            {
                tilter.setX(0.5);
            }
            catch(CANTimeoutException ex)
            {
                ex.printStackTrace();
            }
        }
        else if(direction == -1)
        {
            try
            {
                tilter.setX(-0.5);
            }
            catch(CANTimeoutException ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
           try
            {
                tilter.setX(0.0);
            }
            catch(CANTimeoutException ex)
            {
                ex.printStackTrace(); 
            }
        }
    }
}
