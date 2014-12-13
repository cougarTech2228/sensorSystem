
public class PointScanner {

	public static void main(String[] args) {
	
		int data[][];
		
		double x = -1;
		double y = -1;
		int tDir = -1;
		int pDir = 1;
		double accur = 0.5;
		
		int xLoc = 0;
		int yLoc = 0;
		
		while(true){
			
			if(y == tDir && x != pDir){
				
				x = x + (pDir * accur);
				xLoc = xLoc + pDir;
				tDir = tDir * -1;
		
			}
			else if(y == tDir && x == pDir){
				
				tDir *= -1;
				pDir *= -1;
				y = y + (tDir * accur);
				if(y == 1.0){
					yLoc = 0;
				}
				if(y == 0.5){
					yLoc = 1;
				}
				if(y == 0){
					yLoc = 2;
				}
				if(y == -0.5){
					yLoc = 3;
				}
				if(y == -1){
					yLoc = 4;
				}
				
			}else{
				
				y = y + (tDir * accur);
				if(y == 1.0){
					yLoc = 0;
				}
				if(y == 0.5){
					yLoc = 1;
				}
				if(y == 0){
					yLoc = 2;
				}
				if(y == -0.5){
					yLoc = 3;
				}
				if(y == -1){
					yLoc = 4;
				}
				
			}
				
			System.out.println(x + ", " + y + " | " + xLoc + ", " + yLoc);
			
		}
	}		
}
