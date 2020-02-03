package Neptune_v2;
import battlecode.common.*;
import java.util.HashSet;

public class Miner extends Unit {

    HashSet<MapLocation> soupLocations;
    HashSet<MapLocation> assignedSoupLocations;

    MapLocation assignedSoupLocation;

    String currentAction;
    String prevAction;

    int numRound = 0;
    int roundAtMoveStart = -1;

    public Miner(RobotController rc) throws GameActionException {
        super(rc);
        soupLocations = new HashSet<MapLocation>();
        assignedSoupLocations = new HashSet<MapLocation>();
        readAllBlocks();
        pathing.setTarget(new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight())));
        currentAction = "Scouting";
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
                case "Build Refinery":
                    BuildRefinery();
                    break;
                case "Build Design School":
                    BuildDesignSchool();
                    break;
                case "Build Fulfillment Center":
                    BuildFulfillmentCenter();
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
            case "Build Refinery":
                rc.setIndicatorLine(rc.getLocation(), pathing.currTarget, 255, 0, 255);
                break;
            case "Build Design School":
                rc.setIndicatorLine(rc.getLocation(), pathing.currTarget, 0, 255, 255);
                break;
            case "Build Fulfillment Center":
                rc.setIndicatorLine(rc.getLocation(), pathing.currTarget, 255, 255, 0);
                break;
        }
    }

    public void Scouting () throws GameActionException {
        scanForSoup();
        MapLocation closestSoupLocation = closestSoup();
        if(roundAtMoveStart == -1){
            roundAtMoveStart = numRound;
        }
        if(numRound - roundAtMoveStart > 20){
            pathing.setTarget(new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight())));
            roundAtMoveStart = numRound;
        }
        if(closestSoupLocation != null){
            currentAction = "Going To Soup";
            assignedSoupLocation = closestSoupLocation;
            pathing.setTarget(closestSoupLocation);
            pathing.pathing();
            roundAtMoveStart = -1;
            return;
        }
        pathing.pathing();
        if(pathing.completed()){
            pathing.setTarget(new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight())));
        }
    }

    public void GoingToSoup () throws GameActionException {
        scanForSoup();
        MapLocation closestSoupLocation = closestSoup();
        if(closestSoupLocation != null && rc.canMineSoup(rc.getLocation().directionTo(closestSoupLocation))){
            currentAction = "Mining";
            assignedSoupLocation = closestSoupLocation;
            pathing.setTarget(closestSoupLocation);
            return;
        }
        if(rc.canSenseLocation(pathing.currTarget)){
            if(rc.senseSoup(pathing.currTarget) == 0 || !accessibleSoup(pathing.currTarget)){
                sendBlock(pathing.currTarget, "Soup Depleted", 1);
                currentAction = "Scouting";
                return;
            }
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
            soupLocations.remove(pathing.currTarget);
            scanForSoup();
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
        int temp = (rc.getRoundNum() > 70) ? 70 : 49;
        if(rc.getSoupCarrying() >= temp){
            currentAction = "Return Soup";
            MapLocation closestRefineryLocation = closestRefinery();
            if(location.distanceSquaredTo(closestRefineryLocation) > 100 && rc.getTeamSoup() > 100){
                currentAction = "Build Refinery";
            }else{
                pathing.setTarget(closestRefineryLocation);
            }
        }
    }

    public void ReturnSoup () throws GameActionException {
        if(roundAtMoveStart == -1){
            roundAtMoveStart = numRound;
        }
        if(numRound - roundAtMoveStart > 50){
            buildingLocations.get(indexOf(buildingTypes, RobotType.REFINERY)).remove(pathing.currTarget);
            MapLocation closestRefineryLocation = closestRefinery();
            if(closestRefineryLocation != null && !closestRefineryLocation.equals(pathing.currTarget)){
                pathing.setTarget(closestRefineryLocation);
                roundAtMoveStart = numRound;
            }else{
                pathing.setTarget(assignedSoupLocation);
                currentAction = "Build Refinery";
                roundAtMoveStart = -1;
            }
            return;
        }
        if(pathing.completed()){
            if(rc.isReady()){
                Direction dir = rc.getLocation().directionTo(pathing.currTarget);
                rc.depositSoup(dir, rc.getSoupCarrying());
                int numDesignSchool = buildingLocations.get(indexOf(buildingTypes, RobotType.DESIGN_SCHOOL)).size();
                int numFulfillmentCenter = buildingLocations.get(indexOf(buildingTypes, RobotType.FULFILLMENT_CENTER)).size();
                if(numDesignSchool == 0 && rc.getTeamSoup() >= RobotType.DESIGN_SCHOOL.cost && assignedSoupLocation.distanceSquaredTo(hqLocation) > 8){
                    currentAction = "Build Design School";
                    pathing.setTarget(assignedSoupLocation);
                }else if(numFulfillmentCenter == 0 && numDesignSchool > 0
                        && rc.getTeamSoup() >= RobotType.FULFILLMENT_CENTER.cost && assignedSoupLocation.distanceSquaredTo(hqLocation) > 8){
                    currentAction = "Build Fulfillment Center";
                    pathing.setTarget(assignedSoupLocation);
                }else{
                    currentAction = "Going To Soup";
                    pathing.setTarget(assignedSoupLocation);
                }
                roundAtMoveStart = -1;
            }
        }else{
            pathing.pathing();
        }
    }

    public void BuildRefinery () throws GameActionException {
        if(pathing.isAdjacentToTarget()){
            MapLocation location = rc.getLocation();
            for(Direction dir : Direction.allDirections()){
                if(hqLocation.distanceSquaredTo(location.add(dir)) > 5 && 
                rc.canBuildRobot(RobotType.REFINERY, dir)){
                    rc.buildRobot(RobotType.REFINERY, dir);
                    MapLocation refineryLocation = rc.getLocation().add(dir);
                    pathing.setTarget(refineryLocation);
                    currentAction = "Return Soup";
                    return;
                }
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

    public void BuildDesignSchool () throws GameActionException {
        int numDesignSchool = buildingLocations.get(indexOf(buildingTypes, RobotType.DESIGN_SCHOOL)).size();
        if(numDesignSchool > 0){
            pathing.setTarget(assignedSoupLocation);
            currentAction = "Going To Soup";
            return;
        }
        if(pathing.isAdjacentToTarget()){
            MapLocation location = rc.getLocation();
            for(Direction dir : Direction.allDirections()){
                if(hqLocation.distanceSquaredTo(location.add(dir)) > 5 && 
                        rc.canBuildRobot(RobotType.DESIGN_SCHOOL, dir)){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, dir);
                    pathing.setTarget(assignedSoupLocation);
                    currentAction = "Going To Soup";
                    return;
                }
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

    public void BuildFulfillmentCenter () throws GameActionException {
        if(pathing.isAdjacentToTarget()){
            MapLocation location =  rc.getLocation();
            Direction dir = whereToBuild(location, RobotType.FULFILLMENT_CENTER);
            if(dir != null){
                rc.buildRobot(RobotType.FULFILLMENT_CENTER, dir);
                pathing.setTarget(assignedSoupLocation);
                currentAction = "Going To Soup";
                return;
            }
        }else if(pathing.completed()){
            for(Direction dir : Direction.allDirections()){
                if(rc.isReady() && rc.canMove(dir)){
                    rc.move(dir);
                    return;
                }
            }
        }else{
            pathing.pathing();
        }
    }

    public Direction whereToBuild (MapLocation location, RobotType buildingType) throws GameActionException {
        Direction bestDir = null;
        int maxElevation = 0;
        for(Direction dir : Direction.allDirections()){
            if(hqLocation.distanceSquaredTo(location.add(dir)) > 5 && 
                rc.canBuildRobot(buildingType, dir)){
                if(maxElevation < rc.senseElevation(location.add(dir))){
                    maxElevation = rc.senseElevation(location.add(dir));
                    bestDir = dir;
                }
            }
        }
        return bestDir;
    }

    public void scanForSoup () throws GameActionException {
        MapLocation[] nearbySoups = rc.senseNearbySoup();
        for(MapLocation soupLocation : nearbySoups){
            if(rc.canSenseLocation(soupLocation) && accessibleSoup(soupLocation)){
                if(!soupLocations.contains(soupLocation)){
                    if(isWorthyToSend(soupLocation)){
                        sendBlock(soupLocation, "Found Soup", 1);
                    }
                    soupLocations.add(soupLocation);
                }
            }
        }
    }

    public boolean isWorthyToSend (MapLocation location) {
        for(MapLocation soupLocation : soupLocations){
            if(location.distanceSquaredTo(soupLocation) <= 30){
                return false;
            }
        }
        return true;
    }

    public boolean accessibleSoup (MapLocation location) throws GameActionException {
        for(Direction dir : Direction.allDirections()){
            if(!dir.equals(Direction.CENTER) && (!rc.canSenseLocation(location.add(dir)) 
                        || !rc.senseFlooding(location.add(dir)))){
                return true;
            }
        }
        return false;
    }

    public MapLocation closestSoup () {
        int minDistance = Integer.MAX_VALUE;
        MapLocation closestSoupLocation = null;
        MapLocation location = rc.getLocation();
        for(MapLocation soupLocation : soupLocations){
            if(minDistance > location.distanceSquaredTo(soupLocation) && !assignedSoupLocations.contains(soupLocation)){
                minDistance = location.distanceSquaredTo(soupLocation);
                closestSoupLocation = soupLocation;
            }
        }
        return closestSoupLocation;
    }

    public MapLocation closestRefinery () {
        int minDistance = Integer.MAX_VALUE;
        MapLocation closestRefineryLocation = null;
        MapLocation location = rc.getLocation();
        HashSet<MapLocation> refineryLocations = buildingLocations.get(indexOf(buildingTypes, RobotType.REFINERY));
        for(MapLocation refineryLocation : refineryLocations){
            if(minDistance > location.distanceSquaredTo(refineryLocation)){
                closestRefineryLocation = refineryLocation;
                minDistance = location.distanceSquaredTo(refineryLocation);
            }
        }
        return closestRefineryLocation;
    }

    public void sendBlock (MapLocation soupLocation, String action, int cost) {
        int[] message = new int[7];
        message[0] = PassWord;
        message[1] = soupLocation.x;
        message[2] = soupLocation.y;
        message[3] = indexOf(dataTypes, action);
        message[4] = rc.getID();
        queue.add(new Pair(message, cost));
    }

    public void readBlockat (int round) throws GameActionException {
        Transaction[] transactions = rc.getBlock(round);
        for(Transaction transaction : transactions){
            int[] data = transaction.getMessage();
            if(PassWord == data[0]) {
                switch(dataTypes[data[3]]) {
                    case "Found Soup":
                        soupLocations.add(new MapLocation(data[1], data[2]));
                        break;
                    /*
                    case "Claim Soup":
                        assignedSoupLocations.add(new MapLocation(data[1], data[2]));
                        break;
                    */
                    case "Soup Depleted":
                        soupLocations.remove(new MapLocation(data[1], data[2]));
                        assignedSoupLocations.remove(new MapLocation(data[1], data[2]));
                        break;
                    case "Finish Construction":
                        if(buildingTypes[data[4]] == RobotType.HQ){
                            hqLocation = new MapLocation(data[1], data[2]);
                            buildingLocations.get(indexOf(buildingTypes, RobotType.REFINERY)).add(hqLocation);
                            hqID = data[5];
                        }
                        buildingLocations.get(data[4]).add(new MapLocation(data[1], data[2]));
                        if(buildingTypes[data[4]] == RobotType.REFINERY){
                            if(currentAction == "Return Soup" || currentAction == "Build Refinery"){
                                currentAction = "Return Soup";
                                pathing.setTarget(closestRefinery());
                            }
                        }
                        if(buildingTypes[data[4]] == RobotType.DESIGN_SCHOOL){
                            if(currentAction == "Build Design School"){
                                currentAction = "Going To Soup";
                                pathing.setTarget(assignedSoupLocation);
                            }
                        }
                        if(buildingTypes[data[4]] == RobotType.FULFILLMENT_CENTER){
                            if(currentAction == "Build Fulfillment Center"){
                                currentAction = "Going To Soup";
                                pathing.setTarget(assignedSoupLocation);
                            }
                        }
                        break;
                    /*
                    case "Construct Building":
                        if(data[5] == ID){
                            if(currentAction.substring(0, 5) != "Build"){
                                prevAction = currentAction;
                            }
                            pathing.setTarget(new MapLocation(data[1], data[2]));
                            if(buildingTypes[data[4]] == RobotType.DESIGN_SCHOOL){
                                currentAction = "Build Design School";
                            }
                            if(buildingTypes[data[4]] == RobotType.FULFILLMENT_CENTER){
                                currentAction = "Build Fulfillment Center";
                            }
                        }
                        break;
                    */
                    case "Surrounded":
                        if(data[5] == 0){
                            if(currentAction == "Return Soup" && pathing.currTarget.equals(hqLocation)){
                                currentAction = "Build Refinery";
                                pathing.setTarget(assignedSoupLocation);
                            }
                        }
                        break;
                }
            }
        }
    }

}
