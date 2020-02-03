package Neptune_v2;
import battlecode.common.*;

public class HQ extends Building {

    static int numMiners = 0;
    static int numDesignSchool = 0;
    static int numFulfillmentCenter = 0;

    static int temp1 = -1;
    static int temp2 = -1;

    static MapLocation center;

    int width, height;

    //static int[][] buildingOffset = new int[8][2];

    public HQ(RobotController rc) throws GameActionException{
        super(rc);
        hqLocation = rc.getLocation();
        hqSurrounded = false;
        width = rc.getMapWidth();
        height = rc.getMapHeight();
        sendBlock(rc.getLocation(), RobotType.HQ, 1);
        isSurrounded();
        trySendingMessage();
        center = new MapLocation((int)(rc.getMapWidth() / 2), (int)(rc.getMapHeight() / 2));

        /*
        buildingOffset[0][0] = +2;
        buildingOffset[0][1] = +1;
        buildingOffset[1][0] = +2;
        buildingOffset[1][1] = -1;

        buildingOffset[2][0] = +1;
        buildingOffset[2][1] = +2;
        buildingOffset[3][0] = +1;
        buildingOffset[3][1] = -2;

        buildingOffset[4][0] = -1;
        buildingOffset[4][1] = +2;
        buildingOffset[5][0] = -1;
        buildingOffset[5][1] = -2;

        buildingOffset[6][0] = -2;
        buildingOffset[6][1] = +1;
        buildingOffset[7][0] = -2;
        buildingOffset[7][1] = -1;
        */
    }

    public void takeTurn() throws GameActionException {
        if(rc.getRoundNum() > 1){
            readBlockat(rc.getRoundNum() - 1);
            if(!hqSurrounded && isSurrounded()){
                hqSurrounded = true;
                sendBlock(1);
            }
            if(hqSurrounded && !isSurrounded()){
                hqSurrounded = false;
                sendBlock(1);
            }
            turret();
            buildMiner();
        }
        trySendingMessage();
    }

    public void buildMiner() throws GameActionException {
        if(numMiners < 5){
            for(Direction dir : Direction.allDirections()){
                if(rc.isReady() && rc.canBuildRobot(RobotType.MINER, dir)){
                    rc.buildRobot(RobotType.MINER, dir);
                    numMiners += 1;
                    break;
                }
            }
        }
    }

    /*
    public void buildDesiginSchool () throws GameActionException {
        if(numDesignSchool == 0 && rc.getTeamSoup() > RobotType.DESIGN_SCHOOL.cost){
            MapLocation buildLocation = findBuildingLocation();
            int closestMinerID = closestMiner(buildLocation);
            if(closestMinerID != -1){
                sendBlock(buildLocation, RobotType.DESIGN_SCHOOL, closestMinerID, 1);
                numDesignSchool += 1;
                temp1 = temp2;
            }
        }
    }

    public void buildFulfillmentCenter () throws GameActionException {
        if(numFulfillmentCenter == 0 && buildingLocations.get(indexOf(buildingTypes, RobotType.DESIGN_SCHOOL)).size() == 1
                && rc.getTeamSoup() > RobotType.FULFILLMENT_CENTER.cost){
            MapLocation buildLocation = findBuildingLocation();
            int closestMinerID = closestMiner(buildLocation);
            if(closestMinerID != -1){
                sendBlock(buildLocation, RobotType.FULFILLMENT_CENTER, closestMinerID, 1);
                numFulfillmentCenter += 1;
                temp1 = temp2;
            }
        }
    }

    public MapLocation findBuildingLocation () throws GameActionException {
        MapLocation buildLocation = hqLocation;
        for(int i=temp1+1; i<8; ++i){
            buildLocation = hqLocation.translate(buildingOffset[i][0], buildingOffset[i][1]);
            if(rc.onTheMap(buildLocation) && !rc.isLocationOccupied(buildLocation)){
                temp2 = i;
                return buildLocation;
            }
        }
        return buildLocation;
    }
    */

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

    public boolean isSurrounded () throws GameActionException {
        int elevation = rc.senseElevation(hqLocation);
        for(Direction dir : Direction.allDirections()){
            MapLocation location = hqLocation.add(dir);
            if(rc.onTheMap(location)){
                RobotInfo robotInfo = rc.senseRobotAtLocation(location);
                if((robotInfo == null || robotInfo.getType().equals(RobotType.MINER)) && 
                        Math.abs(rc.senseElevation(location) - elevation) <= GameConstants.MAX_DIRT_DIFFERENCE && !checkEdge(location)){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkEdge (MapLocation location){
        for(Direction checkDir : Direction.allDirections()){
            MapLocation checkLocation = location.add(checkDir);
            if(rc.onTheMap(checkLocation) && hqLocation.distanceSquaredTo(checkLocation) > 2){
                return false;
            }
        }
        return true;
    }

    /*

    public void sendBlock (MapLocation location, RobotType robotType, int robotID, int cost) {
        int[] message = new int[7];
        message[0] = PassWord;
        message[1] = location.x;
        message[2] = location.y;
        message[3] = indexOf(dataTypes, "Construct Building");
        message[4] = indexOf(buildingTypes, robotType);
        message[5] = robotID;
        queue.add(new Pair(message, cost));
    }

    */

    public void sendBlock (int cost) {
        int[] message = new int[7];
        message[0] = PassWord;
        message[3] = indexOf(dataTypes, "Surrounded");
        message[4] = (hqSurrounded) ? 0 : 1;
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
