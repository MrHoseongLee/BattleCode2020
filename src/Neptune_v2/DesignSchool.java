package Neptune_v2;
import battlecode.common.*;

public class DesignSchool extends Building {

    static int numLandScapers = 0;

    MapLocation location;

    boolean isSurrounded = false;

    public DesignSchool(RobotController rc) throws GameActionException{
        super(rc);
        readAllBlocks();
        location = rc.getLocation();
        sendBlock(rc.getLocation(), RobotType.DESIGN_SCHOOL, 1);
        trySendingMessage();
        readAllBlocks();
    }

    public void takeTurn () throws GameActionException {
        super.takeTurn();
        buildLandScaper();
    }

    public void buildLandScaper() throws GameActionException {
        if(numLandScapers < 20 && !isSurrounded){
            for(Direction dir : Direction.allDirections()){
                if(rc.isReady() && rc.canBuildRobot(RobotType.LANDSCAPER, dir)){
                    rc.buildRobot(RobotType.LANDSCAPER, dir);
                    numLandScapers += 1;
                    break;
                }
            }
        }
    }

    public void readBlockat (int round) throws GameActionException {
        Transaction[] transactions = rc.getBlock(round);
        for(Transaction transaction : transactions){
            int[] data = transaction.getMessage();
            if(PassWord == data[0]) {
                switch(dataTypes[data[3]]) {
                    case "Surrounded":
                        if(data[5] == 0){
                            isSurrounded = true;
                        }
                        break;
                }
            }
        }
    }


}

