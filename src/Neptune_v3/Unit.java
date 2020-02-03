package Neptune_v3;
import battlecode.common.*;

public class Unit extends Robot {

    MapLocation hqLocation;
    int hqID;

    Pathing pathing;

    public Unit (RobotController rc) throws GameActionException {
        super(rc);
        pathing = new Pathing(rc);
    }

    public void takeTurn () throws GameActionException {
        super.takeTurn();
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
