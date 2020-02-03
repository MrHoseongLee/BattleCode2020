package Neptune_v2;
import battlecode.common.*;

public class Refinery extends Building {

    public Refinery(RobotController rc) throws GameActionException{
        super(rc);
        sendBlock(rc.getLocation(), RobotType.REFINERY, 1);
        trySendingMessage();
    }

}

