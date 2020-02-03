package Neptune_v2;
import battlecode.common.*;
import java.util.HashSet;

public class LandScaper extends Unit {

    String currentAction;

    int numRound = 0;
    HashSet<MapLocation> enemyNetGuns = new HashSet<MapLocation>();
    HashSet<MapLocation> enemyDesiginSchools = new HashSet<MapLocation>();

    public LandScaper (RobotController rc) throws GameActionException {
        super(rc);
        readAllBlocks();
        currentAction = "Going To HQ";
        pathing.setTarget(hqLocation);
    }

    public void takeTurn () throws GameActionException {
        if(++numRound >= GameConstants.INITIAL_COOLDOWN_TURNS){
            scanEnemyBuilding();
            switch(currentAction){
                case "Going To HQ":
                    GoingToHQ();
                    break;
                case "Defense":
                    Defense();
                    break;
                case "Offense":
                    Offense();
                    break;
            }
        }
    }

    public void GoingToHQ () throws GameActionException {
        if(pathing.completed()){
            currentAction = "Defense";
        }else{
            pathing.pathing();
        }
    }

    public void Defense () throws GameActionException {
        MapLocation location = rc.getLocation();
        Direction dir = hqLocation.directionTo(location);
        if(rc.getDirtCarrying() != RobotType.LANDSCAPER.dirtLimit){
            if(rc.canSenseRobot(hqID) && rc.isReady()){
                RobotInfo hq = rc.senseRobot(hqID);
                if(rc.isReady() && hq.getDirtCarrying() > 0){
                    rc.digDirt(dir.opposite());
                }else{
                    for(int i=0; i<4; ++i){
                        RobotInfo robot = null;
                        if(rc.canSenseLocation(location.add(dir))){
                            robot = rc.senseRobotAtLocation(location.add(dir));
                        }
                        boolean temp = robot == null || robot.getType().equals(RobotType.DELIVERY_DRONE);
                        if(rc.isReady() && rc.canDigDirt(dir) && hqLocation.distanceSquaredTo(location.add(dir)) > 2 && temp){
                            rc.digDirt(dir);
                            return;
                        }
                        dir = dir.rotateLeft();
                        dir = dir.rotateLeft();
                    }
                }
            }
        }else{
            if(rc.isReady()){
                MapLocation closestEnemyBuildingLocation = closestEnemyBuilding();
                if(closestEnemyBuildingLocation != null && 
                        rc.getLocation().distanceSquaredTo(closestEnemyBuildingLocation) <= 4){
                    currentAction = "Offense";
                    pathing.setTarget(closestEnemyBuildingLocation);
                    return;
                }
                int temp01 = Integer.MAX_VALUE;
                int temp02 = Integer.MAX_VALUE;
                MapLocation tempL = hqLocation.add(dir.rotateLeft());
                MapLocation tempR = hqLocation.add(dir.rotateRight());
                if(rc.canSenseLocation(tempL) && (rc.senseElevation(tempL) > 10 || rc.getRoundNum() >= 512)){
                    temp01 = rc.senseElevation(tempL) - rc.senseElevation(location);
                }
                if(rc.canSenseLocation(tempR) && (rc.senseElevation(tempR) > 10 || rc.getRoundNum() >= 512)){
                    temp02 = rc.senseElevation(tempR) - rc.senseElevation(location);
                }
                int temp03 = Math.min(Math.min(temp01, temp02), 0);
                if(temp01 == temp03){
                    rc.depositDirt(location.directionTo(tempL));
                }else if(temp02 == temp03){
                    rc.depositDirt(location.directionTo(tempR));
                }else if(temp03 == 0){
                    rc.depositDirt(Direction.CENTER);
                }
            }
        }
    }

    public void Offense () throws GameActionException {
        if(pathing.completed()){
            if(rc.isReady()){
                rc.depositDirt(rc.getLocation().directionTo(pathing.currTarget));
            }
            if(!rc.isLocationOccupied(pathing.currTarget)){
                if(enemyNetGuns.contains(pathing.currTarget)){
                    enemyNetGuns.remove(pathing.currTarget);
                }
                if(enemyDesiginSchools.contains(pathing.currTarget)){
                    enemyDesiginSchools.remove(pathing.currTarget);
                }
                currentAction = "Going To HQ";
                pathing.setTarget(hqLocation);
            }
        }else{
            pathing.pathing();
        }
    }

    public void scanEnemyBuilding () {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo enemy : enemies){
            if(enemy.getType().equals(RobotType.NET_GUN)){
                enemyNetGuns.add(enemy.getLocation());
            }
            if(enemy.getType().equals(RobotType.DESIGN_SCHOOL)){
                enemyDesiginSchools.add(enemy.getLocation());
            }
        }
    }

    public MapLocation closestEnemyBuilding () {
        MapLocation closestEnemyBuildingLocation = null;
        MapLocation location = rc.getLocation();
        int minDistance = Integer.MAX_VALUE;
        for(MapLocation buildingLocation : enemyNetGuns){
            return buildingLocation;
        }
        for(MapLocation buildingLocation : enemyDesiginSchools){
            if(minDistance > location.distanceSquaredTo(buildingLocation)){
                closestEnemyBuildingLocation = buildingLocation;
                minDistance = location.distanceSquaredTo(buildingLocation);
            }
        }
        return closestEnemyBuildingLocation;
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
                    default:
                        break;
                }
            }
        }
    }   

}
