package Neptune_v2;
import battlecode.common.*;

public class FulfillmentCenter extends Building {

    int numDrones = 0;

    public FulfillmentCenter(RobotController rc) throws GameActionException{
        super(rc);
        sendBlock(rc.getLocation(), RobotType.FULFILLMENT_CENTER, 1);
        trySendingMessage();
    }

    public void takeTurn () throws GameActionException {
        super.takeTurn();
        buildDrones();
    }

    public void buildDrones() throws GameActionException {
        if(numDrones < 10000 && rc.getTeamSoup() > RobotType.DESIGN_SCHOOL.cost + RobotType.REFINERY.cost){
            for(Direction dir : Direction.allDirections()){
                if(rc.isReady() && rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)){
                    rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
                    numDrones += 1;
                    break;
                }
            }
        }
    }


}

