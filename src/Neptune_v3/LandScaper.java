package Neptune_v3;
import battlecode.common.*;

public class LandScaper extends Unit {

    String currentAction;

    int numRound = 0;

    MapLocation enemyHQLocation = null;

    public LandScaper (RobotController rc) throws GameActionException {
        super(rc);
        readAllBlocks();
        currentAction = "Going To HQ";
        enemyHQLocation = scanForHQ();
        pathing.setTarget(enemyHQLocation);
    }

    public void takeTurn () throws GameActionException {
        if(++numRound >= GameConstants.INITIAL_COOLDOWN_TURNS){
            switch(currentAction){
                case "Going To HQ":
                    GoingToHQ();
                    break;
                case "Digging Dirt":
                    DiggingDirt();
                    break;
            }
        }
    }

    public MapLocation scanForHQ () {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ){
                return robot.getLocation();
            }
        }
        return null;
    }

    public void GoingToHQ () throws GameActionException {
        if(pathing.isAdjacentToTarget()){
            currentAction = "Digging Dirt";
        }else{
            pathing.pathing();
        }
    }

    public void DiggingDirt () throws GameActionException {
        Direction dir = rc.getLocation().directionTo(enemyHQLocation);
        if(rc.isReady() && rc.getDirtCarrying() > 0){
            rc.depositDirt(dir);
        }
        for(Direction adir : Direction.allDirections()){
            if(rc.isReady() && rc.canDigDirt(adir)){
                rc.digDirt(adir);
            }
        }
    }

}
