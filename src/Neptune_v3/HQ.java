package Neptune_v3;
import battlecode.common.*;

public class HQ extends Building {

    static int numMiners = 0;
    static int numDesignSchool = 0;

    static MapLocation center;
    static MapLocation location;

    public HQ(RobotController rc) throws GameActionException{
        super(rc);
        sendBlock(rc.getLocation(), RobotType.HQ, 1);
        center = new MapLocation((int)(rc.getMapWidth() / 2), (int)(rc.getMapHeight() / 2));
        trySendingMessage();
        location = rc.getLocation();
    }

    public void takeTurn() throws GameActionException {
        turret();
        buildDesiginSchool();
        buildMiner();
        trySendingMessage();
    }

    public void buildMiner() throws GameActionException {
        if(numMiners < 4){
            for(Direction dir : Direction.allDirections()){
                if(rc.isReady() && rc.canBuildRobot(RobotType.MINER, dir)){
                    rc.buildRobot(RobotType.MINER, dir);
                    numMiners += 1;
                    break;
                }
            }
        }
    }

    public void buildDesiginSchool () {
        if(numDesignSchool == 0 && rc.getTeamSoup() > RobotType.DESIGN_SCHOOL.cost){
            MapLocation buildLocation = findBuildingLocation();
            int closestMinerID = closestMiner(buildLocation);
            if(closestMinerID != -1){
                sendBlock(buildLocation, RobotType.DESIGN_SCHOOL, closestMinerID, 1);
                numDesignSchool += 1;
            }
        }
    }

    public MapLocation findBuildingLocation () {
        Direction dir = location.directionTo(center);
        MapLocation buildLocation = location.add(dir);
        buildLocation = buildLocation.add(dir);
        dir = dir.opposite();
        dir = dir.rotateLeft();
        buildLocation = buildLocation.add(dir);
        return buildLocation;
    }

    public int closestMiner (MapLocation target) {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        int minDistance = Integer.MAX_VALUE;
        int closestMinerID = -1;
        for(RobotInfo nearbyRobot : nearbyRobots){
            if(nearbyRobot.getType() == RobotType.MINER){
                if(minDistance > target.distanceSquaredTo(nearbyRobot.getLocation())){
                    closestMinerID = nearbyRobot.getID();
                }
            }
        }
        return closestMinerID;
    }

    public void sendBlock (MapLocation location, RobotType robotType, int ID, int cost) {
        int[] message = new int[7];
        message[0] = PassWord;
        message[1] = location.x;
        message[2] = location.y;
        message[3] = indexOf(dataTypes, "Construct Building");
        message[4] = indexOf(buildingTypes, robotType);
        message[5] = ID;
        queue.add(new Pair(message, cost));
    }

    public void turret() throws GameActionException {
        RobotInfo[] robotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo robotInfo : robotInfos){
            if(rc.canShootUnit(robotInfo.getID())){
                rc.shootUnit(robotInfo.getID());
            }
        }
    }

}
