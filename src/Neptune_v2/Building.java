package Neptune_v2;
import battlecode.common.*;

public class Building extends Robot {

    public Building(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void sendBlock (MapLocation location, RobotType robotType, int cost) {
        int[] message = new int[7];
        message[0] = PassWord;
        message[1] = location.x;
        message[2] = location.y;
        message[3] = indexOf(dataTypes, "Finish Construction");
        message[4] = indexOf(buildingTypes, robotType);
        message[5] = rc.getID();
        queue.add(new Pair(message, cost));
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
