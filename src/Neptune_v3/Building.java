package Neptune_v3;
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
        queue.add(new Pair(message, cost));
    }

}
