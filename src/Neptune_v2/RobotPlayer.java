package Neptune_v2;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    public static void run(RobotController rc) throws GameActionException {
        Robot robot = null;

        switch (rc.getType()) {
            case HQ:                 robot = new HQ(rc);                break;
            case MINER:              robot = new Miner(rc);             break;
            case REFINERY:           robot = new Refinery(rc);          break;
            case DESIGN_SCHOOL:      robot = new DesignSchool(rc);      break;
            case LANDSCAPER:         robot = new LandScaper(rc);        break;
            case FULFILLMENT_CENTER: robot = new FulfillmentCenter(rc); break;
            case DELIVERY_DRONE:     robot = new DeliveryDrone(rc);     break;
            default:                                                    break;
        }

        while (true) {
            try {
                robot.takeTurn();
                Clock.yield();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } 
        }
    }
}
        
