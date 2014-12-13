/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cougartech.aerialassist.modules;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVision;
import edu.wpi.first.wpilibj.image.NIVisionException;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;



public class Eye{
    
        public class Scores
    {

        double rectangularity;
        double aspectRatioVertical;
        double aspectRatioHorizontal;
    }

    public class TargetReport
    {

        int verticalIndex;
        int horizontalIndex;
        boolean Hot;
        double totalScore;
        double leftScore;
        double rightScore;
        double tapeWidthScore;
        double verticalScore;
    }
    
      //Camera constants used for distance calculation
    final int Y_IMAGE_RES = 480;		//X Image resolution in pixels, should be 120, 240 or 480
    final double VIEW_ANGLE = 47;		//Axis M1013 after rough calibration
    final double PI = 3.141592653;
    //Score limits used for target identification
    final int RECTANGULARITY_LIMIT = 40;
    final int ASPECT_RATIO_LIMIT = 55;
    //Score limits used for hot target determination
    final int TAPE_WIDTH_LIMIT = 50;
    final int VERTICAL_SCORE_LIMIT = 50;
    final int LR_SCORE_LIMIT = 50;
    //Minimum area of particles to be considered
    final int AREA_MINIMUM = 130;
    //Maximum number of particles to process
    final int MAX_PARTICLES = 8;
    
    
    AxisCamera camera;    // the axis camera object (connected to the switch)
    CriteriaCollection cc;      // the criteria for doing the particle filter operation
    Servo x;
    Servo y;
    TargetReport target = new TargetReport();
    int verticalTargets[] = new int[MAX_PARTICLES];
    int horizontalTargets[] = new int[MAX_PARTICLES];
    int verticalTargetCount, horizontalTargetCount;
    boolean tracking = false;
    int iterCount = 0; //keep track of how many iterations completed
    double[] distances = new double[2]; //array of distances over time
    int distanceTrack = 0;
    int trackWait = 0; //make sure the camera doesn't immediatly scan when target only lost for a frame.
    double camDistance = 0;
    boolean visionOverride = false;
    private boolean hotStuff;

    public Eye(){
        camera = AxisCamera.getInstance();  // get an instance of the camera
        camera.writeRotation(AxisCamera.RotationT.k180);
        cc = new CriteriaCollection();      // create the criteria for the particle filter
        cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_AREA, AREA_MINIMUM, 65535, false);
        x = new Servo(2);
        x.setPosition(.22);
        y = new Servo(1);
        y.setPosition(.9);
        hotStuff = false;
    }
    
    public void watch(){
        
        boolean localHot = false;
        try
        {
            ColorImage image = camera.getImage();     // comment if using stored images
            //ColorImage image;                           // next 2 lines read image from flash on cRIO
            //image = new RGBImage("/testImage.jpg");		// get the sample image from the cRIO flash
            BinaryImage thresholdImage = image.thresholdHSV(114, 145, 80, 255, 175, 254);   // keep only green objects
            //thresholdImage.write("/threshold.bmp");
            BinaryImage convexHullImg = thresholdImage.convexHull(true);        //fill in the bounding boxes
            BinaryImage filteredImage = convexHullImg.particleFilter(cc);           // filter out small particles
            //filteredImage.write("/filteredImage.bmp");

            //iterate through each particle in the phantom array of particles in filteredImage and score to see if it is a target
            Scores scores[] = new Scores[filteredImage.getNumberParticles()];
            horizontalTargetCount = verticalTargetCount = 0;

            if (filteredImage.getNumberParticles() > 0)
            {
                for (int i = 0; i < MAX_PARTICLES && i < filteredImage.getNumberParticles(); i++)
                {
                    ParticleAnalysisReport report = filteredImage.getParticleAnalysisReport(i);
                    scores[i] = new Scores();

                    //Score each particle on rectangularity and aspect ratio
                    scores[i].rectangularity = scoreRectangularity(report);
                    scores[i].aspectRatioVertical = scoreAspectRatio(filteredImage, report, i, true);
                    scores[i].aspectRatioHorizontal = scoreAspectRatio(filteredImage, report, i, false);

                    //Check if the particle is a horizontal target, if not, check if it's a vertical target
                    if (scoreCompare(scores[i], false))
                    {
                        System.out.println("particle: " + i + " is a Horizontal Target centerX: " + report.center_mass_x + "centerY: " + report.center_mass_y);
                        horizontalTargets[horizontalTargetCount++] = i; //Add particle index to target array and increment count
                        centerTrack(report.center_mass_x, report.center_mass_y);
                        tracking = true;
                        localHot = true;
                    }
                    else if (scoreCompare(scores[i], true))
                    {
                        distances[distanceTrack] = computeDistance(filteredImage, report, i);
                        System.out.println("particle: " + i + " is a Vertical Target centerX: " + report.center_mass_x + "centerY: " + report.center_mass_y);
                        //System.out.println("Distance: " + (distances[0]+distances[1])/2);
                        camDistance = (distances[0] + distances[1]) / 2;
                        verticalTargets[verticalTargetCount++] = i;  //Add particle index to target array and increment count
                        trackWait = 1;
                    }
                    else
                    {
                        //System.out.println("particle: " + i + " is not a Target centerX: " + report.center_mass_x + "centerY: " + report.center_mass_y);
                    }
                    //System.out.println("rect: " + scores[i].rectangularity + "ARHoriz: " + scores[i].aspectRatioHorizontal);
                    //System.out.println("ARVert: " + scores[i].aspectRatioVertical);
                }

                //Zero out scores and set verticalIndex to first target in case there are no horizontal targets
                target.totalScore = target.leftScore = target.rightScore = target.tapeWidthScore = target.verticalScore = 0;
                target.verticalIndex = verticalTargets[0];
                for (int i = 0; i < verticalTargetCount; i++)
                {
                    ParticleAnalysisReport verticalReport = filteredImage.getParticleAnalysisReport(verticalTargets[i]);
                    for (int j = 0; j < horizontalTargetCount; j++)
                    {
                        ParticleAnalysisReport horizontalReport = filteredImage.getParticleAnalysisReport(horizontalTargets[j]);
                        double horizWidth, horizHeight, vertWidth, leftScore, rightScore, tapeWidthScore, verticalScore, total;

                        //Measure equivalent rectangle sides for use in score calculation
                        horizWidth = NIVision.MeasureParticle(filteredImage.image, horizontalTargets[j], false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_LONG_SIDE);
                        vertWidth = NIVision.MeasureParticle(filteredImage.image, verticalTargets[i], false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);
                        horizHeight = NIVision.MeasureParticle(filteredImage.image, horizontalTargets[j], false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);

                        //Determine if the horizontal target is in the expected location to the left of the vertical target
                        leftScore = ratioToScore(1.2 * (verticalReport.boundingRectLeft - horizontalReport.center_mass_x) / horizWidth);
                        //Determine if the horizontal target is in the expected location to the right of the  vertical target
                        rightScore = ratioToScore(1.2 * (horizontalReport.center_mass_x - verticalReport.boundingRectLeft - verticalReport.boundingRectWidth) / horizWidth);
                        //Determine if the width of the tape on the two targets appears to be the same
                        tapeWidthScore = ratioToScore(vertWidth / horizHeight);
                        //Determine if the vertical location of the horizontal target appears to be correct
                        verticalScore = ratioToScore(1 - (verticalReport.boundingRectTop - horizontalReport.center_mass_y) / (4 * horizHeight));
                        total = leftScore > rightScore ? leftScore : rightScore;
                        total += tapeWidthScore + verticalScore;

                        //If the target is the best detected so far store the information about it
                        if (total > target.totalScore)
                        {
                            target.horizontalIndex = horizontalTargets[j];
                            target.verticalIndex = verticalTargets[i];
                            target.totalScore = total;
                            target.leftScore = leftScore;
                            target.rightScore = rightScore;
                            target.tapeWidthScore = tapeWidthScore;
                            target.verticalScore = verticalScore;
                        }
                    }
                    //Determine if the best target is a Hot target
                    target.Hot = hotOrNot(target);
                }

                if (verticalTargetCount > 0)
                {
                    //Information about the target is contained in the "target" structure
                    //To get measurement information such as sizes or locations use the
                    //horizontal or vertical index to get the particle report as shown below
                    ParticleAnalysisReport distanceReport = filteredImage.getParticleAnalysisReport(target.verticalIndex);
                    double distance = computeDistance(filteredImage, distanceReport, target.verticalIndex);
                    if (target.Hot)
                    {
                        System.out.println("Hot target located");
//                            System.out.println("Distance: " + distance);
                    }
                    else
                    {
                        //System.out.println("No hot target present");
//                            System.out.println("Distance: " + distance);
                    }
                }
            }

            filteredImage.free();
            thresholdImage.free();
            convexHullImg.free();
            image.free();
            if (distanceTrack == 1)
            {
                distanceTrack = 0;
            }
            distanceTrack++;
            if (!tracking)
            {
                if (trackWait > 0)
                {
                    trackWait--;
                }
                else
                {
                    scan();
                }
            }
            iterCount++;//add 1 to the iteration counter. Overflow is fine.
        
            if(target.Hot)
                hotStuff = true;
        }
        catch (Exception ex){        // this is needed if the camera.getImage() is called
            ex.printStackTrace();
        }
    }
    
    public boolean hotMess(){
        return hotStuff;
    }
    
     /**
     * Computes the estimated distance to a target using the height of the
     * particle in the image. For more information and graphics showing the math
     * behind this approach see the Vision Processing section of the
     * ScreenStepsLive documentation.
     *
     * @param image The image to use for measuring the particle estimated
     * rectangle
     * @param report The Particle Analysis Report for the particle
     * @param outer True if the particle should be treated as an outer target,
     * false to treat it as a center target
     * @return The estimated distance to the target in Inches.
     */
    double computeDistance(BinaryImage image, ParticleAnalysisReport report, int particleNumber) throws NIVisionException
    {
        double rectLong, height;
        double targetHeight;

        rectLong = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_LONG_SIDE);
        //using the smaller of the estimated rectangle long side and the bounding rectangle height results in better performance
        //on skewed rectangles
        height = Math.min(report.boundingRectHeight, rectLong);
        targetHeight = 32;

        return Y_IMAGE_RES * targetHeight / (height * 12 * 2 * Math.tan(VIEW_ANGLE * Math.PI / (180 * 2))) * 12;
    }

    /**
     * Computes a score (0-100) comparing the aspect ratio to the ideal aspect
     * ratio for the target. This method uses the equivalent rectangle sides to
     * determine aspect ratio as it performs better as the target gets skewed by
     * moving to the left or right. The equivalent rectangle is the rectangle
     * with sides x and y where particle area= x*y and particle perimeter= 2x+2y
     *
     * @param image The image containing the particle to score, needed to
     * perform additional measurements
     * @param report The Particle Analysis Report for the particle, used for the
     * width, height, and particle number
     * @param outer	Indicates whether the particle aspect ratio should be
     * compared to the ratio for the inner target or the outer
     * @return The aspect ratio score (0-100)
     */
    public double scoreAspectRatio(BinaryImage image, ParticleAnalysisReport report, int particleNumber, boolean vertical) throws NIVisionException
    {
        double rectLong, rectShort, aspectRatio, idealAspectRatio;

        rectLong = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_LONG_SIDE);
        rectShort = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);
        idealAspectRatio = vertical ? (4.0 / 32) : (23.5 / 4);	//Vertical reflector 4" wide x 32" tall, horizontal 23.5" wide x 4" tall

        //Divide width by height to measure aspect ratio
        if (report.boundingRectWidth > report.boundingRectHeight)
        {
            //particle is wider than it is tall, divide long by short
            aspectRatio = ratioToScore((rectLong / rectShort) / idealAspectRatio);
        }
        else
        {
            //particle is taller than it is wide, divide short by long
            aspectRatio = ratioToScore((rectShort / rectLong) / idealAspectRatio);
        }
        return aspectRatio;
    }

    /**
     * Compares scores to defined limits and returns true if the particle
     * appears to be a target
     *
     * @param scores The structure containing the scores to compare
     * @param outer True if the particle should be treated as an outer target,
     * false to treat it as a center target
     *
     * @return True if the particle meets all limits, false otherwise
     */
    boolean scoreCompare(Scores scores, boolean vertical)
    {
        boolean isTarget = true;

        isTarget &= scores.rectangularity > RECTANGULARITY_LIMIT;
        if (vertical)
        {
            isTarget &= scores.aspectRatioVertical > ASPECT_RATIO_LIMIT;
        }
        else
        {
            isTarget &= scores.aspectRatioHorizontal > ASPECT_RATIO_LIMIT;
        }
        //System.out.println("Vertical Target");
        return isTarget;
    }

    /**
     * Computes a score (0-100) estimating how rectangular the particle is by
     * comparing the area of the particle to the area of the bounding box
     * surrounding it. A perfect rectangle would cover the entire bounding box.
     *
     * @param report The Particle Analysis Report for the particle to score
     * @return The rectangularity score (0-100)
     */
    double scoreRectangularity(ParticleAnalysisReport report)
    {
        if (report.boundingRectWidth * report.boundingRectHeight != 0)
        {
            return 100 * report.particleArea / (report.boundingRectWidth * report.boundingRectHeight);
        }
        else
        {
            return 0;
        }
    }

    /**
     * Converts a ratio with ideal value of 1 to a score. The resulting function
     * is piecewise linear going from (0,0) to (1,100) to (2,0) and is 0 for all
     * inputs outside the range 0-2
     */
    double ratioToScore(double ratio)
    {
        return (Math.max(0, Math.min(100 * (1 - Math.abs(1 - ratio)), 100)));
    }

    /**
     * Takes in a report on a target and compares the scores to the defined
     * score limits to evaluate if the target is a hot target or not.
     *
     * Returns True if the target is hot. False if it is not.
     */
    boolean hotOrNot(TargetReport target)
    {
        boolean isHot = true;

        isHot &= target.tapeWidthScore >= TAPE_WIDTH_LIMIT;
        isHot &= target.verticalScore >= VERTICAL_SCORE_LIMIT;
        isHot &= (target.leftScore > LR_SCORE_LIMIT) | (target.rightScore > LR_SCORE_LIMIT);

        return isHot;
    }

    void centerTrack(double pixX, double pixY)
    { //tracking for the center of the target
        double posX = x.getPosition() - (.07323 / 201.83) * (320 - pixX); //the multipied constant is calibrated and then multiplied by the distance to the center to get the necessary delta position
        double posY = y.getPosition() - (.10979 / 238.92) * (240 - pixY); //same thing as above but for Y
        if (posX < 0) //just a boundary check to make sure servo doesn't get negative numbers
        {
            posX = 0;
        }
        //       System.out.println(posX);
        //       System.out.println(posY);
        x.setPosition(posX);
        y.setPosition(posY);
    }

    void scan()
    {//basic scanning function that is as resource effecient as possible while still allowing some redundancy
        y.setPosition(.8); //Y position shoudl be constant enough to leave as a constant during scanning
        double posX = x.getPosition() + (.08745 / 201.83) * (500);//change the x-position while leaving some redundancy, change as necessary to accomadate movement
        if (posX > .85)//check to make sure camera doesn't go beyond servo range
        {
            posX = 0;//start x scanning position
        }
        System.out.println(posX);
        x.setPosition(posX);
    }
}
