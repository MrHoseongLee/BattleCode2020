package Neptune_v2;
import battlecode.common.*;
import java.util.HashSet;

public class DeliveryDrone extends Unit {

    String currentAction;

    int numRound = 0;
    boolean isSurrounded = false;
    boolean atHQ = false;

    HashSet<MapLocation> waterLocations;

    public DeliveryDrone (RobotController rc) throws GameActionException {
        super(rc);
        waterLocations = new HashSet<MapLocation>();
        readAllBlocks();
        currentAction = "Scouting";
        pathing.setTarget(new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight())));
    }

    public void takeTurn () throws GameActionException {
        if(++numRound >= GameConstants.INITIAL_COOLDOWN_TURNS){
            scanForWater();
            switch(currentAction){
                case "Scouting":
                    Scouting();
                    break;
                case "Kill":
                    Kill();
                    break;
                case "Going To HQ":
                    GoingToHQ();
                    break;
            }
        }
    }

    public void Scouting () throws GameActionException {
        scanForWater();
        if(waterLocations.size() > 0 && isSurrounded){
            pathing.setTarget(hqLocation);
            currentAction = "Going To HQ";
        }
        int enemyID = scanForEnemy();
        if(enemyID != -1){
            rc.pickUpUnit(enemyID);
        }
        if(rc.isCurrentlyHoldingUnit()){
            if(waterLocations.size() > 0){
                currentAction = "Kill";
                pathing.setTarget(closestWater());
                return;
            }
        }
        if(pathing.completed()){
            if(waterLocations.size() > 0 && isSurrounded){
                pathing.setTarget(hqLocation);
                currentAction = "Going To HQ";
            }else{
                pathing.setTarget(new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight())));
            }
        }else{
            pathing.dronePathing();
        }
    }

    public void Kill () throws GameActionException {
        scanForWater();
        if(pathing.isAdjacentToTarget()){
            if(rc.isReady()){
                rc.dropUnit(rc.getLocation().directionTo(pathing.currTarget));
                pathing.setTarget(hqLocation);
                currentAction = "Going To HQ";
            }
        }else if(pathing.completed()){
            for(Direction dir : Direction.allDirections()){
                if(rc.isReady() && rc.canMove(dir)){
                    rc.move(dir);
                }
            }
        }else{
            pathing.pathing();
        }
    }

    public void GoingToHQ () throws GameActionException {
        scanForWater();
        int enemyID = scanForEnemy();
        if(enemyID != -1){
            rc.pickUpUnit(enemyID);
        }
        if(rc.isCurrentlyHoldingUnit()){
            if(waterLocations.size() > 0){
                currentAction = "Kill";
                pathing.setTarget(closestWater());
                return;
            }
        }
        if(rc.getLocation().distanceSquaredTo(hqLocation) > 8){
            pathing.dronePathing();
        }else{
            if(!atHQ){
                pathing.freeMode = true;
                atHQ = true;
            }
            pathing.droneCircle();
        }
    }

    public void scanForWater () throws GameActionException {
        MapLocation location = rc.getLocation();
        for(int dx=-2; dx<=2; ++dx){
            for(int dy=-2; dy<=2; ++dy){
                MapLocation target = location.translate(dx, dy);
                if(rc.canSenseLocation(target) && rc.senseFlooding(target)){
                    waterLocations.add(target);
                }
            }
        }
    }

    public int scanForEnemy () throws GameActionException {
        RobotInfo[] robotInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo robotInfo : robotInfos){
            if(rc.canPickUpUnit(robotInfo.getID())){
                return robotInfo.getID();
            }
        }
        robotInfos = rc.senseNearbyRobots(-1, Team.NEUTRAL);
        for(RobotInfo robotInfo : robotInfos){
            if(rc.canPickUpUnit(robotInfo.getID())){
                return robotInfo.getID();
            }
        }
        return -1;
    }

    public MapLocation closestWater () {
        int minDistance = Integer.MAX_VALUE;
        MapLocation closestWaterLocation = null;
        MapLocation location = rc.getLocation();
        for(MapLocation waterLocation : waterLocations){
            if(minDistance > location.distanceSquaredTo(waterLocation)){
                closestWaterLocation = waterLocation;
                minDistance = location.distanceSquaredTo(waterLocation);
            }
        }
        return closestWaterLocation;
    }



    public void readBlockat (int round) throws GameActionException {
        Transaction[] transactions = rc.getBlock(round);
        for(Transaction transaction : transactions){
            int[] data = transaction.getMessage();
            if(PassWord == data[0]) {
                switch(dataTypes[data[3]]) {
                    case "Finish Construction":
                        if(buildingTypes[data[4]] == RobotType.HQ){
                            hqLocation = new MapLocation(data[1], data[2]);
                            hqID = data[5];
                        }
                        buildingLocations.get(data[4]).add(new MapLocation(data[1], data[2]));
                        break;
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
