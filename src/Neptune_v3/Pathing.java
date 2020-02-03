package Neptune_v3;
import battlecode.common.*;

public class Pathing {

    RobotController rc;

    MapLocation prevTarget = null;
    MapLocation currTarget = null;

    boolean freeMode;

    int distance = 0;

    Boolean rotateLeft = null;

    Direction wallDir;
    Direction prevReversedDirection;

    public Pathing (RobotController rc) {
        this.rc = rc;
    }

    public void setTarget(MapLocation target) {
        prevTarget = currTarget;
        currTarget = target;
        rotateLeft = null;
        prevReversedDirection = Direction.CENTER;
    }

    public void setToPrevTarget() {
        currTarget = prevTarget;
        prevTarget = null;
        rotateLeft = null;
        prevReversedDirection = Direction.CENTER;
    }

    public boolean pathing () throws GameActionException {
        MapLocation location = rc.getLocation();
        Direction dir = location.directionTo(currTarget);
        freeMode = true;

        if(freeMode){
            if(rc.canMove(dir) && !rc.senseFlooding(location.add(dir)) && !dir.equals(prevReversedDirection)){
                rc.move(dir);
                prevReversedDirection = dir.opposite();
                return true;
            }else{
                freeMode = false;
                wallDir = dir;
            }
        }
        if(!freeMode){
            if(rotateLeft == null){
                rotateLeft = distanceIfRotateLeft() < distanceIfRotateRight();
            }
            for(int i=0; i<7; ++i){
                if(rotateLeft){
                    wallDir = wallDir.rotateLeft();
                }else{
                    wallDir = wallDir.rotateRight();
                }
                if(rc.canMove(wallDir) && !rc.senseFlooding(location.add(wallDir))){
                    rc.move(wallDir);
                    prevReversedDirection = wallDir.opposite();
                    if(rotateLeft){
                        wallDir = wallDir.rotateRight();
                        wallDir = wallDir.rotateRight();
                    }else{
                        wallDir = wallDir.rotateLeft();
                        wallDir = wallDir.rotateLeft();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public int distanceIfRotateLeft () throws GameActionException {
        MapLocation location = rc.getLocation();
        Direction testWallDir = wallDir;
        for(int n=0; n<3; ++n){
            for(int i=0; i<7; ++i){
                testWallDir = testWallDir.rotateLeft();
                if(canMove(location, testWallDir)){
                    location = location.add(testWallDir);
                    break;
                }
            }
        }
        return location.distanceSquaredTo(currTarget);
    }

    public int distanceIfRotateRight () throws GameActionException {
        MapLocation location = rc.getLocation();
        Direction testWallDir = wallDir;
        for(int n=0; n<3; ++n){
            for(int i=0; i<7; ++i){
                testWallDir = testWallDir.rotateRight();
                if(canMove(location, testWallDir)){
                    location = location.add(testWallDir);
                    break;
                }
            }
        }
        return location.distanceSquaredTo(currTarget);
    }

    public boolean canMove (MapLocation location, Direction dir) throws GameActionException {
        MapLocation nextLocation = location.add(dir);
        int deltaDirtLevel = rc.senseElevation(location) - rc.senseElevation(nextLocation);
        boolean isOccupied = rc.isLocationOccupied(nextLocation);
        boolean isNotFlooded = rc.senseFlooding(nextLocation);
        return Math.abs(deltaDirtLevel) <= GameConstants.MAX_DIRT_DIFFERENCE && !isOccupied && isNotFlooded;
    }

    public boolean completed () {
        return currTarget.isAdjacentTo(rc.getLocation());
    }

    public boolean isAdjacentToTarget () {
        MapLocation location = rc.getLocation();
        return currTarget.isAdjacentTo(location) && !currTarget.equals(location);
    }

}
