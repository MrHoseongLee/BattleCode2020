package Neptune_v3;
import battlecode.common.*;

public class DesignSchool extends Building {

    static int numLandScapers = 0;

    public DesignSchool(RobotController rc) throws GameActionException{
        super(rc);
        sendBlock(rc.getLocation(), RobotType.DESIGN_SCHOOL, 1);
        trySendingMessage();
    }

    public void takeTurn () throws GameActionException {
        super.takeTurn();
        buildLandScaper();
    }

    public void buildLandScaper() throws GameActionException {
        if(numLandScapers < 8){
            for(Direction dir : Direction.allDirections()){
                if(rc.isReady() && rc.canBuildRobot(RobotType.LANDSCAPER, dir)){
                    //rc.buildRobot(RobotType.LANDSCAPER, dir);
                    //numLandScapers += 1;
                    break;
                }
            }
        }
    }


}

