package Neptune;
import battlecode.common.*;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Arrays;

public strictfp class RobotPlayer {
    static RobotController rc;
    static MapLocation target = null;
    static MapLocation location;
    static MapLocation HQLocation;

    static boolean followMode;

    static int minerCount = 0;

    static int landscaperCount = 0;

    static int minDistanceBug;

    static int temp = 0;

    static int temp1 = 0;

    static Random random;

    static String currentAction;

    static boolean hasDesignSchool = false;

    static boolean onePass = false;

    static ArrayList<String> actionTypes = new ArrayList<String>();

    static ArrayList<String> dataTypes = new ArrayList<String>();

    static Direction currentDir = null;
    static Direction previDirection = null;

    static HashSet<Integer> soupLocations = new HashSet<Integer>();
    static HashSet<Integer> refineryLocations = new HashSet<Integer>();

    static int PassWord = -1;

    static ArrayList<Pair> transactionData = new ArrayList<Pair>();

    static HashMap<Direction, Direction> nextDir = new HashMap<Direction, Direction>();

    static Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    static ArrayList<RobotType> types = new ArrayList<RobotType>(Arrays.asList(RobotType.HQ, RobotType.COW, RobotType.MINER, 
                RobotType.NET_GUN, RobotType.REFINERY, RobotType.VAPORATOR, RobotType.LANDSCAPER, RobotType.DESIGN_SCHOOL, 
                RobotType.DELIVERY_DRONE, RobotType.FULFILLMENT_CENTER));

    static int[][] dL = new int[24][2];

    static class Pair { 
        public final int [] message;
        public final Integer cost; 

        public Pair(int[] x, Integer y) { 
            this.message = x; 
            this.cost = y; 
        }
    }


    public static void run(RobotController rc) throws GameActionException {

        RobotPlayer.rc = rc;

        random = new Random(rc.getID());

        nextDir.put(Direction.NORTH, Direction.NORTHEAST);
        nextDir.put(Direction.NORTHEAST, Direction.EAST);
        nextDir.put(Direction.EAST, Direction.SOUTHEAST);
        nextDir.put(Direction.SOUTHEAST, Direction.SOUTH);
        nextDir.put(Direction.SOUTH, Direction.SOUTHWEST);
        nextDir.put(Direction.SOUTHWEST, Direction.WEST);
        nextDir.put(Direction.WEST, Direction.NORTHWEST);
        nextDir.put(Direction.NORTHWEST, Direction.NORTH);

        actionTypes.add("Scouting");
        actionTypes.add("Go to Soup");
        actionTypes.add("Mining");
        actionTypes.add("IDLE");
        actionTypes.add("Return Soup");
        actionTypes.add("Communication");

        dataTypes.add("Found Soup");
        dataTypes.add("Soup Depleted");
        dataTypes.add("Building");

        dL[0][0] =  1;
        dL[0][1] =  1;
        dL[1][0] =  1;
        dL[1][1] =  0;
        dL[2][0] =  1;
        dL[2][1] = -1;
        dL[3][0] =  0;
        dL[3][1] =  1;
        dL[4][0] =  0;
        dL[4][1] = -1;
        dL[5][0] = -1;
        dL[5][1] =  1;
        dL[6][0] = -1;
        dL[6][1] =  0;
        dL[7][0] = -1;
        dL[7][1] = -1;


        dL[8][0] =  2;
        dL[8][1] =  2;
        dL[9][0] =  2;
        dL[9][1] =  1;
        dL[10][0] =  2;
        dL[10][1] =  0;
        dL[11][0] =  2;
        dL[11][1] = -1;
        dL[12][0] =  2;
        dL[12][1] = -2;

        dL[13][0] =  1;
        dL[13][1] =  2;
        dL[14][0] =  1;
        dL[14][1] = -2;

        dL[15][0] =  0;
        dL[15][1] =  2;
        dL[16][0] =  0;
        dL[16][1] = -2;

        dL[17][0] = -1;
        dL[17][1] =  2;
        dL[18][0] = -1;
        dL[18][1] = -2;

        dL[19][0] = -2;
        dL[19][1] =  2;
        dL[20][0] = -2;
        dL[20][1] =  1;
        dL[21][0] = -2;
        dL[21][1] =  0;
        dL[22][0] = -2;
        dL[22][1] = -1;
        dL[23][0] = -2;
        dL[23][1] = -2;

        followMode = false;

        try {
            //Init Robots
            switch (rc.getType()) {
                case HQ:                 initHQ();               break;
                case MINER:              initMiner();            break;
                case DESIGN_SCHOOL:      initDesignSchool();     break;
                case LANDSCAPER:         initLandscaper();       break;
                default:                                         break;
            }

        } catch (Exception e) {
            System.out.println(rc.getType() + " Exception");
            e.printStackTrace();
        }

        Clock.yield();

        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                //System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 runHQ();            break;
                    case MINER:              runMiner();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();  break;
                    case LANDSCAPER:         runLandscaper();    break;
                    default:                                     break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void initHQ() throws GameActionException {
        MapLocation center = new MapLocation((int)(rc.getMapWidth() / 2), (int)(rc.getMapHeight() / 2));
        location = rc.getLocation();
        currentDir = center.directionTo(location);
        currentDir = nextDir.get(currentDir);
        currentDir = nextDir.get(currentDir);
        currentDir = nextDir.get(currentDir);
        int [] message = new int[7];
        PassWord = random.nextInt(10000);
        message[0] = PassWord;                              // PassWord
        message[1] = location.x * 100 + location.y;         // Current Location
        message[2] = -1;                                    // Target Location
        message[3] = types.indexOf(RobotType.HQ);           // RobotType
        message[4] = 0;                              // ActionType
        message[5] = PassWord * 23;                         // Count
        message[6] = rc.getID();                            // Robot Id or Data Type
        transactionData.add(new Pair(message, 1));
        tryTransaction();
    }

    static void initMiner() throws GameActionException {
        readAllPrevBlocks();
        target = new MapLocation((int)(random.nextFloat() * rc.getMapWidth()), (int)(random.nextFloat() * rc.getMapHeight()));
        location = rc.getLocation();
        int [] message = new int[7];
        message[0] = PassWord;                              // PassWord
        message[1] = location.x * 100 + location.y;         // Current Location
        message[2] = target.x * 100 + target.y;             // Target Location
        message[3] = types.indexOf(RobotType.MINER);        // RobotType
        message[4] = actionTypes.indexOf("Scouting");       // ActionType
        message[5] = 0;                                     // Count
        message[6] = rc.getID();                            // Robot Id or Data Type
        currentAction = "Scouting";
        transactionData.add(new Pair(message, 1));
        tryTransaction();
    }

    static void initDesignSchool() throws GameActionException {
        readAllPrevBlocks();
    }

    static void initLandscaper() throws GameActionException {
        readAllPrevBlocks();
        target = HQLocation.translate(dL[landscaperCount][0], dL[landscaperCount][1]);
        location = rc.getLocation();
        int [] message = new int[7];
        message[0] = PassWord;                              // PassWord
        message[1] = location.x * 100 + location.y;         // Current Location
        message[2] = target.x * 100 + target.y;             // Target Location
        message[3] = types.indexOf(RobotType.LANDSCAPER);   // RobotType
        message[4] = 0;                                     // ActionType
        message[5] = 0;                                     // Count
        message[6] = rc.getID();                            // Robot Id or Data Type
        transactionData.add(new Pair(message, 1));
        tryTransaction();
    }

    static void tryTransaction() throws GameActionException {
        Iterator<Pair> iter = transactionData.iterator();
        while(iter.hasNext()){
            Pair data = iter.next();
            int [] message = data.message;
            int cost = data.cost;
            if(message[4] == 1 && soupLocations.contains(message[2])){
                iter.remove();
                continue;
            }
            if(rc.canSubmitTransaction(message, cost)){
                rc.submitTransaction(message, cost);
                iter.remove();
            }
        }
    }

    static void runHQ() throws GameActionException {
        if(minerCount < 10){
            if(tryBuild(RobotType.MINER, currentDir))
                minerCount++;
        }
        currentDir = nextDir.get(currentDir);
        if(!transactionData.isEmpty())
            tryTransaction();
        if(rc.getRoundNum() > 1)
            readPrevBlock(rc.getRoundNum() - 1);
    }

    static void runMiner() throws GameActionException {
        readPrevBlock(rc.getRoundNum() - 1);
        switch(currentAction){
            case "Scouting":
                action_Scouting();
                break;
            case "Go to Soup":
                action_GoToSoup();
				break;
            case "Mining":
                action_Mining();
                break;
            case "Return Soup":
                action_ReturnSoup();
                break;
            case "IDLE":
                action_IDLE();
                break;
            default:
                break;
        }
        tryTransaction();
    }

    static void runDesignSchool() throws GameActionException {
        location = rc.getLocation();
        for(Direction dir : directions){
            if(landscaperCount == 24)
                break;
            if(tryBuild(RobotType.LANDSCAPER, dir)){
                landscaperCount += 1;
            }
        }
    }

    static void runLandscaper() throws GameActionException {
        location = rc.getLocation();
        if(!target.equals(location)){
            pathfinding();
        }else{
            if(rc.isReady()){
                if(HQLocation.distanceSquaredTo(location) <= 2){
                    if(rc.getDirtCarrying() < 10){
                        rc.digDirt(Direction.CENTER);
                    }else{
                        if(temp % 3 == 0){
                            currentDir = HQLocation.directionTo(location);
                            currentDir = nextDir.get(currentDir);
                            currentDir = nextDir.get(currentDir);
                            currentDir = nextDir.get(currentDir);
                            currentDir = nextDir.get(currentDir);
                            currentDir = nextDir.get(currentDir);
                            currentDir = nextDir.get(currentDir);
                            currentDir = nextDir.get(currentDir);
                        }
                        RobotInfo robot = rc.senseRobotAtLocation(location.add(currentDir));
                        if(robot != null && robot.type == RobotType.LANDSCAPER){
                            temp1 += 1;
                        }else{
                            temp1 = 0;
                        }
                        if(temp1 > 1){
                            rc.depositDirt(currentDir);
                            currentDir = nextDir.get(currentDir);
                            temp += 1;
                        }
                    }
                }else{
                    currentDir = location.directionTo(HQLocation);
                    if(rc.getDirtCarrying() < 10){
                        rc.digDirt(currentDir);
                    }else{
                        rc.depositDirt(Direction.CENTER);
                    }
                }
            }
        }
    }

    static void action_Scouting() throws GameActionException {
        location = rc.getLocation();
        if(target == null){
            target = new MapLocation((int)(random.nextFloat() * rc.getMapWidth()), (int)(random.nextFloat() * rc.getMapHeight()));
        }
        pathfinding();
        scanForSoup();
        if(closestSoup()){
            currentAction = "Go to Soup";
            followMode = false;
        }
        if(target.equals(location)){
            currentAction = "IDLE";
            followMode = false;
        }
        if(HQLocation.distanceSquaredTo(location) > 100){
            if(!hasDesignSchool){
                action_Build(RobotType.DESIGN_SCHOOL);
            }
        }
    }

    static void action_GoToSoup() throws GameActionException {
        location = rc.getLocation();
        if(target.equals(location)){
            currentAction = "Mining";
            if(HQLocation.distanceSquaredTo(location) > 100){
                if(!scanForRefinery()){
                    action_Build(RobotType.REFINERY);
                }
                if(!hasDesignSchool){
                    action_Build(RobotType.DESIGN_SCHOOL);
                }
            }
            return;
        }
        if(rc.canSenseLocation(target) && rc.isLocationOccupied(target)){
            followMode = false;
            if(!closestSoup()){
                target = null;
                currentAction = "Scouting";
            }
            return;
        }
        pathfinding();
    }

    static void action_Mining() throws GameActionException {
        tryMine(Direction.CENTER);
		if(rc.senseSoup(target) == 0){
            int [] message = new int[7];
            message[0] = PassWord;                              // PassWord
            message[1] = location.x * 100 + location.y;         // Current Location
            message[2] = target.x * 100 + target.y;             // Target Location
            message[3] = types.indexOf(RobotType.MINER);        // RobotType
            message[4] = actionTypes.indexOf("Communication");  // ActionType
            message[5] = 0;                                     // Count
            message[6] = dataTypes.indexOf("Soup Depleted");    // Robot Id or Data Type
            transactionData.add(new Pair(message, 1));
            if(refineryLocations.isEmpty()){
			    target = HQLocation;
            }else{
                target = closestRefinery();
            }
			currentAction = "Return Soup";
		}
		if(rc.getSoupCarrying() == 100){
            if(refineryLocations.isEmpty()){
			    target = HQLocation;
            }else{
                target = closestRefinery();
            }
			currentAction = "Return Soup";
		}
    }

    static void action_ReturnSoup() throws GameActionException {
        location = rc.getLocation();
        Direction dir = location.directionTo(target);
        if(!refineryLocations.isEmpty()){
            target = closestRefinery();
        }
        if(rc.getSoupCarrying() == 0){
            currentAction = "Go to Soup";
            followMode = false;
            if(!closestSoup()){
                target = null;
                currentAction = "Scouting";
            }
            return;
        }
        if(rc.canDepositSoup(dir)){
            rc.depositSoup(dir, rc.getSoupCarrying());
        }else{
            pathfinding();
        }
    }

    static void action_IDLE() throws GameActionException {
        currentAction = "Scouting";
        target = null;
    }

    static void action_Build(RobotType type) throws GameActionException {
        location = rc.getLocation();
        for(Direction dir : directions){
            if(tryBuild(type, dir)){
                int [] message = new int[7];
                MapLocation temp = location.add(dir);
                message[0] = PassWord;                              // PassWord
                message[1] = location.x * 100 + location.y;         // Current Location
                message[2] = temp.x * 100 + temp.y;                 // Target Location
                message[3] = types.indexOf(type);                   // RobotType
                message[4] = actionTypes.indexOf("Communication");  // ActionType
                message[5] = 1;                                     // Count
                message[6] = dataTypes.indexOf("Building");         // Robot Id or Data Type
                Pair data = new Pair(message, 1);
                transactionData.add(data);
                break;
            }
        }
    }

    static MapLocation closestRefinery() throws GameActionException {
        location = rc.getLocation();
        int minDistance = Integer.MAX_VALUE;
        MapLocation refinery = null;
        for(Integer refineryLocationInteger : refineryLocations){
            MapLocation temp = new MapLocation(refineryLocationInteger / 100, refineryLocationInteger % 100);
            int dis = location.distanceSquaredTo(temp);
            if(minDistance > dis){
                minDistance = dis;
                refinery = temp;
            }
        }
        return refinery;
    }

    static boolean closestSoup() throws GameActionException{
        location = rc.getLocation();
        boolean foundSoup = false;
        int minDistance = Integer.MAX_VALUE;
        for(Integer soupLocationInteger : soupLocations){
            MapLocation soupLocation = new MapLocation(soupLocationInteger / 100, soupLocationInteger % 100);
            if(minDistance > location.distanceSquaredTo(soupLocation)){
                if(rc.canSenseLocation(soupLocation) && rc.isLocationOccupied(soupLocation)){
                    continue;
                }
                minDistance = location.distanceSquaredTo(soupLocation);
                target = soupLocation;
                foundSoup = true;
            }
        }
        return foundSoup;
    }

    static boolean scanForRefinery() throws GameActionException {
        location = rc.getLocation();
        for(int refineryLocationInteger : refineryLocations){
            MapLocation temp = new MapLocation(refineryLocationInteger / 100, refineryLocationInteger % 100);
            if(location.distanceSquaredTo(temp) < 100 || HQLocation.distanceSquaredTo(temp) < 100){
                return true;
            }
        }
        return false;
    }

    static boolean scanForSoup() throws GameActionException {
        boolean foundSoup = false;
        location = rc.getLocation();
        for(int dx=-4; dx<=4; ++dx){
            for(int dy=-4; dy<=4; ++dy){
                MapLocation senseHere = new MapLocation(location.x + dx, location.y + dy);
                if(!soupLocations.contains(senseHere.x * 100 + senseHere.y) && 
                        rc.canSenseLocation(senseHere) && !rc.senseFlooding(senseHere)){
                    int soupCount = rc.senseSoup(senseHere);
                    if(soupCount > 0){
                        currentAction = "Go to Soup";
                        int [] message = new int[7];
                        message[0] = PassWord;                              // PassWord
                        message[1] = location.x * 100 + location.y;         // Current Location
                        message[2] = senseHere.x * 100 + senseHere.y;       // Target Location
                        message[3] = types.indexOf(RobotType.MINER);        // RobotType
                        message[4] = actionTypes.indexOf("Communication");  // ActionType
                        message[5] = 0;                                     // Count
                        message[6] = dataTypes.indexOf("Found Soup");       // Robot Id or Data Type
                        Pair data = new Pair(message, 1);
                        transactionData.add(data);
                        soupLocations.add(senseHere.x * 100 + senseHere.y);
                        foundSoup = true;
                    }
                }
            }
        }
        return foundSoup;
    }

    static void readAllPrevBlocks() throws GameActionException {
        for(int round=1; round<rc.getRoundNum(); ++round){
            readPrevBlock(round);
        }
    }
    
    static void readPrevBlock(int round) throws GameActionException {
        Transaction[] transactions = rc.getBlock(round);
        int[] data;
        for(Transaction transaction : transactions){
            data = transaction.getMessage();
            if(PassWord == -1 && data[0] * 23 == data[5]){
                PassWord = data[0];
                HQLocation = new MapLocation(data[1] / 100, data[1] % 100);
                continue;
            }
            if(PassWord == data[0]){
                String action = actionTypes.get(data[4]);
                if(types.get(data[3]) == RobotType.LANDSCAPER){
                    landscaperCount += 1;
                }
                switch(action){
                    case "Communication":
                        String dataType = dataTypes.get(data[6]);
                        switch(dataType){
                            case "Found Soup":
                                soupLocations.add(data[2]);
                                break;
                            case "Soup Depleted":
                                soupLocations.remove(data[2]);
                            case "Building":
                                if(types.get(data[3]) == RobotType.DESIGN_SCHOOL){
                                    hasDesignSchool = true;
                                }
                                if(types.get(data[3]) == RobotType.REFINERY){
                                    refineryLocations.add(data[2]);
                                }
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    static void pathfinding() throws GameActionException {
        location = rc.getLocation();
        if(followMode){
            if(rc.canMove(previDirection) && !rc.senseFlooding(location.add(previDirection)) && !onePass){
                followMode = false;
                pathfinding();
                return;
            }
            onePass = false;
            for(int i=0; i<8; ++i){
                if(rc.canMove(currentDir) && rc.canSenseLocation(location.add(currentDir)) && !rc.senseFlooding(location.add(currentDir)))
                    break;
                currentDir = nextDir.get(currentDir);
            }
            tryMove(currentDir);
            if(currentDir.opposite().equals(previDirection)){
                onePass = true;
            }
            currentDir = nextDir.get(currentDir);
            currentDir = nextDir.get(currentDir);
            currentDir = nextDir.get(currentDir);
            currentDir = nextDir.get(currentDir);
            currentDir = nextDir.get(currentDir);
            currentDir = nextDir.get(currentDir);
            currentDir = nextDir.get(currentDir);
        }else{
            currentDir = location.directionTo(target);
            if(!tryMove(currentDir)){
                followMode = true;
                onePass = false;
                previDirection = currentDir;
                pathfinding();
                return;
            }
        }
    }

    /*

    static boolean isIntersecting(MapLocation point1, MapLocation point2, MapLocation point3, MapLocation point4){
        if(point3.x == point4.x){
            if(point1.x == point2.x){
                return isInRange(point1.y, point2.y, point3.y) || isInRange(point1.y, point2.y, point4.y);
            }
            int y = (point2.y - point1.y) / (point2.x - point1.x) * (point3.x - point1.x) + point1.y;
            return isInRange(point1.y, point2.y, y);
        }
        int y1 = (point2.y - point1.y) / (point2.x - point1.x) * (point3.x - point1.x);
        int y2 = (point2.y - point1.y) / (point2.x - point1.x) * (point4.x - point1.x);
        return isInRange(y1-point3.y, y2-point4.y, 0);
    }

    static boolean isInRange(int bound1, int bound2, int test){
        int upperBound = Integer.max(bound1, bound2);
        int lowerBound = Integer.min(bound1, bound2);
        return (lowerBound <= test) && (test <= upperBound);
    }

    */

    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
            rc.move(dir);
            return true;
        } else return false;
    }

    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }

}

