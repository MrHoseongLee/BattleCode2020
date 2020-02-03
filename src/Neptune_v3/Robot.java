package Neptune_v3;
import battlecode.common.*;
import java.util.ArrayList;
import java.util.ArrayDeque;

public class Robot {

    RobotController rc;

    ArrayDeque<Pair> queue = new ArrayDeque<Pair>();

    ArrayList<ArrayList<MapLocation>> buildingLocations = new ArrayList<ArrayList<MapLocation>>(5);

    static final int PassWord = 809312;

    final int ID;

    static String[] dataTypes = {"Found Soup", "Claim Soup", "Soup Depleted", "Finish Construction", "Construct Building"};
    static RobotType[] buildingTypes = {RobotType.HQ, RobotType.REFINERY, RobotType.DESIGN_SCHOOL, 
        RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc;
        this.ID = rc.getID();
        for(int i=0; i<5; ++i){
            buildingLocations.add(new ArrayList<MapLocation>());
        }
    }

    public void takeTurn() throws GameActionException {
        readBlockat(rc.getRoundNum() - 1);
    }

    static class Pair { 
        public final int [] message;
        public final int cost; 

        public Pair (int[] x, int y) { 
            this.message = x; 
            this.cost = y; 
        }
    }

    public <K> int indexOf (K[] list, K item){
        for(int i=0; i<list.length; ++i){
            if(list[i].equals(item)){
                return i;
            }
        }
        return -1;
    }

    public void trySendingMessage () throws GameActionException {
        while(!queue.isEmpty()) {
            Pair nextPair = queue.peek();
            if(rc.canSubmitTransaction(nextPair.message, nextPair.cost)){
                rc.submitTransaction(nextPair.message, nextPair.cost);
                queue.pop();
            }else{ return; }
        }
    }

    public void readAllBlocks () throws GameActionException {
        for(int i=1; i<rc.getRoundNum(); ++i){
            readBlockat(i);
        }
    }

    public void readBlockat (int round) throws GameActionException {
    }


}

