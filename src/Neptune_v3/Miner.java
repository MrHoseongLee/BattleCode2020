package Neptune_v3;
import battlecode.common.*;
import java.util.HashSet;

public class Miner extends Unit {

    HashSet<MapLocation> soupLocations;

    MapLocation assignedSoupLocation;
    
    MapLocation center;

    String currentAction;
    String prevAction;

    int numRound = 0;

    int numTries = 0;

    public Miner(RobotController rc) throws GameActionException {
        super(rc);
        soupLocations = new HashSet<MapLocation>();
        readAllBlocks();
        center = new MapLocation((int)(rc.getMapWidth() / 2), (int)(rc.getMapHeight() / 2));
        if(rc.getRoundNum() == 2){
            pathing.setTarget(symmetry(center));
            currentAction = "Rush";
        }else{
            pathing.setTarget(new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight())));
            currentAction = "Scouting";

        }
    }

    public MapLocation symmetry (MapLocation center) {
        switch(numTries++){
            case 0:
                return new MapLocation(rc.getMapWidth() - hqLocation.x - 1, hqLocation.y);
            case 1:
                return new MapLocation(rc.getMapWidth() - hqLocation.x - 1, rc.getMapHeight() - hqLocation.y - 1);
            case 2:
                return new MapLocation(hqLocation.x, rc.getMapHeight() - hqLocation.y - 1);
        }
        return null;
    }

    public void takeTurn () throws GameActionException {
        super.takeTurn();
        if(++numRound >= GameConstants.INITIAL_COOLDOWN_TURNS){
            switch(currentAction){
                case "Scouting":
                    Scouting();
                    break;
                case "Going To Soup":
                    GoingToSoup();
                    break;
                case "Mining":
                    Mining();
                    break;
                case "Return Soup":
                    ReturnSoup();
                    break;
                case "Rush":
                    Rush();
                    break;
                case "Build Design School":
                    BuildDesignSchool();
                    break;
            }
            trySendingMessage();
        }
        Debug();
    }

    public void Debug() {
        switch(currentAction){
            case "Scouting":
                rc.setIndicatorLine(rc.getLocation(), pathing.currTarget, 255, 0, 0);
                break;
            case "Going To Soup":
                rc.setIndicatorLine(rc.getLocation(), pathing.currTarget, 0, 255, 0);
                break;
            case "Return Soup":
                rc.setIndicatorLine(rc.getLocation(), pathing.currTarget, 0, 0, 255);
                break;
        }
    }

    public void Scouting () throws GameActionException {
        scanForSoup();
        MapLocation closestSoupLocation = closestSoup();
        if(closestSoupLocation != null){
            currentAction = "Going To Soup";
            assignedSoupLocation = closestSoupLocation;
            pathing.setTarget(closestSoupLocation);
            pathing.pathing();
            return;
        }
        pathing.pathing();
        if(pathing.completed()){
            pathing.setTarget(new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight())));
        }
    }

    public void GoingToSoup () throws GameActionException {
        scanForSoup();
        if(rc.canSenseLocation(pathing.currTarget) && rc.senseFlooding(pathing.currTarget)){
            soupLocations.remove(pathing.currTarget);
            currentAction = "Scouting";
        }
        if(pathing.completed()){
            currentAction = "Mining";
            return;
        }
        pathing.pathing();
    }

    public void Mining () throws GameActionException {
        MapLocation location = rc.getLocation();
        Direction dir = location.directionTo(pathing.currTarget);
        if(rc.canMineSoup(dir)){
            rc.mineSoup(dir);
        }
        if(rc.senseSoup(pathing.currTarget) == 0){
            scanForSoup();
            soupLocations.remove(pathing.currTarget);
            MapLocation closestSoupLocation = closestSoup();
            if(closestSoupLocation != null){
                pathing.setTarget(closestSoupLocation);
                assignedSoupLocation = closestSoupLocation;
                currentAction = "Going To Soup";
            }else{
                currentAction = "Scouting";
                pathing.setTarget(new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight())));
            }
        }
        if(rc.getSoupCarrying() >= 49){
            currentAction = "Return Soup";
            pathing.setTarget(hqLocation);
        }
    }

    public void ReturnSoup () throws GameActionException {
        if(pathing.completed()){
            if(rc.isReady()){
                Direction dir = rc.getLocation().directionTo(pathing.currTarget);
                rc.depositSoup(dir, rc.getSoupCarrying());
                currentAction = "Going To Soup";
                pathing.setTarget(assignedSoupLocation);
            }
        }else{
            pathing.pathing();
        }
    }

    public void BuildDesignSchool () throws GameActionException {
        Direction dir = rc.getLocation().directionTo(pathing.currTarget);
        if(rc.canBuildRobot(RobotType.DESIGN_SCHOOL, dir)){
            rc.buildRobot(RobotType.DESIGN_SCHOOL, dir);
        }
    }

    public void Rush () throws GameActionException {
        rc.setIndicatorDot(pathing.currTarget, 255, 0, 0);
        pathing.pathing();
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo robot : robots){
            if(robot.getType() == RobotType.HQ){
                currentAction = "Build Design School";
                return;
            }
        }
        if(rc.canSenseLocation(pathing.currTarget)){
            pathing.setTarget(symmetry(center));
        }
    }

    public void scanForSoup () throws GameActionException {
        MapLocation[] nearbySoups = rc.senseNearbySoup();
        for(MapLocation soupLocation : nearbySoups){
            if(rc.canSenseLocation(soupLocation) && 
                    !rc.senseFlooding(soupLocation)){
                if(!soupLocations.contains(soupLocation)){
                    soupLocations.add(soupLocation);
                }
            }
        }
    }

    public MapLocation closestSoup () {
        int minDistance = Integer.MAX_VALUE;
        MapLocation closestSoupLocation = null;
        MapLocation location = rc.getLocation();
        for(MapLocation soupLocation : soupLocations){
            if(minDistance > location.distanceSquaredTo(soupLocation)){
                minDistance = location.distanceSquaredTo(soupLocation);
                closestSoupLocation = soupLocation;
            }
        }
        return closestSoupLocation;
    }

}
