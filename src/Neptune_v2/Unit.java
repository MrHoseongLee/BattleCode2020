package Neptune_v2;
import battlecode.common.*;

public class Unit extends Robot {

    Pathing pathing;

    public Unit (RobotController rc) throws GameActionException {
        super(rc);
        pathing = new Pathing(rc);
    }

    public void takeTurn () throws GameActionException {
        super.takeTurn();
    }

}
