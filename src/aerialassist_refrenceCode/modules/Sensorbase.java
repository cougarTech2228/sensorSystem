package cougartech.aerialassist.modules;

import edu.wpi.first.wpilibj.AnalogModule;

public class Sensorbase
{

    double d1;
    double d2;
    double d3;
    double dAvg;
    double scaleFactor = 5 / 512;
    double sonarScale = 0.009765625;

    public Sensorbase()
    {
    }

    /*
     * getDistance
     * - Scales the voltage of the sensor to actual distance
     * - Averages three runs of the sensor for a better value
     * 
     * @param module
     *      Module of sonar sensor
     * @param port
     *      Port of sonar sensor
     * 
     * @return
     *      Returns an average of three trials
     *      -1: Too close
     *      -2: Too far
     */
    public double getDistance(int module, int port)
    {
        d1 = AnalogModule.getInstance(module).getVoltage(port) / sonarScale;
        d2 = AnalogModule.getInstance(module).getVoltage(port) / sonarScale;
        d3 = AnalogModule.getInstance(module).getVoltage(port) / sonarScale;
        dAvg = (d1 + d2 + d3) / 3;

        if (dAvg <= 6)
        {
            return -1;
        }
        else if (dAvg >= 253)
        {
            return -2;
        }
        else
        {
            return dAvg;
        }        
    }
}
