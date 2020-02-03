package Neptune_v2;
import battlecode.common.*;

public class Pathing {

    RobotController rc;

    MapLocation prevTarget = null;
    MapLocation currTarget = null;

    MapLocation freeConvertLocation = null;

    boolean freeMode;

    int distance = 0;

    boolean rotateLeft;

    int width, height;

    Direction wallDir;
    Direction prevReversedDirection;

    public Pathing (RobotController rc) {
        this.rc = rc;
        width = rc.getMapWidth() - 1;
        height = rc.getMapHeight() - 1;
    }

    public void setTarget(MapLocation target) {
        prevTarget = currTarget;
        currTarget = target;
        freeConvertLocation = null;
        rotateLeft = true;
        freeMode = true;
        prevReversedDirection = Direction.CENTER;
    }

    public void setToPrevTarget() {
        currTarget = prevTarget;
        prevTarget = null;
        freeConvertLocation = null;
        rotateLeft = true;
        freeMode = true;
        prevReversedDirection = Direction.CENTER;
    }

    public boolean dronePathing () throws GameActionException {
        MapLocation location = rc.getLocation();
        Direction dir = location.directionTo(currTarget);
        freeMode = true;

        if(freeMode){
            if(rc.canMove(dir) && !dir.equals(prevReversedDirection)){
                rc.move(dir);
                prevReversedDirection = dir.opposite();
                return true;
            }else{
                freeMode = false;
                wallDir = dir;
            }
        }
        if(!freeMode){
            for(int i=0; i<7; ++i){
                if(isOnBorder()){
                    rotateLeft = !rotateLeft;
                }
                if(rotateLeft){
                    wallDir = wallDir.rotateLeft();
                }else{
                    wallDir = wallDir.rotateRight();
                }
                if(rc.canMove(wallDir)){
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

    public void droneCircle () throws GameActionException {
        MapLocation location = rc.getLocation();
        if(freeMode){
            wallDir = location.directionTo(currTarget);
            freeMode = false;
        }
        if(!freeMode){
            for(int i=0; i<7; ++i){
                wallDir = wallDir.rotateLeft();
                if(rc.isReady() && rc.onTheMap(location.add(wallDir)) && !rc.isLocationOccupied(location.add(wallDir)) 
                        && location.add(wallDir).distanceSquaredTo(currTarget) <= 8){
                    rc.move(wallDir);
                    wallDir = wallDir.rotateRight();
                    wallDir = wallDir.rotateRight();
                    return;
                }
            }
        }
    }

    public boolean pathing () throws GameActionException {
        MapLocation location = rc.getLocation();
        Direction dir = location.directionTo(currTarget);
        if(canMoveRobot(location, dir) && !dir.equals(prevReversedDirection)){
            freeMode = true;
        }
        if(freeMode){
            if(canMoveRobot(location, dir) && !dir.equals(prevReversedDirection)){
                rc.move(dir);
                prevReversedDirection = dir.opposite();
                return true;
            }else{
                if(freeConvertLocation == null || freeConvertLocation.equals(location)){
                    rotateLeft = !rotateLeft;
                }
                freeConvertLocation = location;
                freeMode = false;
                wallDir = dir;
            }
        }
        if(!freeMode){
            if(isOnBorder()){
                rotateLeft = !rotateLeft;
                if(rotateLeft){
                    wallDir = wallDir.rotateRight();
                    wallDir = wallDir.rotateRight();
                    wallDir = wallDir.rotateRight();
                    wallDir = wallDir.rotateRight();
                }else{
                    wallDir = wallDir.rotateLeft();
                    wallDir = wallDir.rotateLeft();
                    wallDir = wallDir.rotateLeft();
                    wallDir = wallDir.rotateLeft();
                }
            }
            for(int i=0; i<8; ++i){
                if(rotateLeft){
                    wallDir = wallDir.rotateLeft();
                }else{
                    wallDir = wallDir.rotateRight();
                }
                if(canMoveRobot(location, wallDir)){
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

    public boolean isOnBorder () {
        MapLocation location = rc.getLocation();
        return location.x == 0 || location.x == width || location.y == 0 || location.y == height;
    }

    public boolean canMoveRobot (MapLocation location, Direction dir) throws GameActionException {
        return rc.canMove(dir) && !rc.senseFlooding(location.add(dir));
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

    public boolean isOnTopOfTarget () {
        return currTarget.equals(rc.getLocation());
    }

    public boolean isAdjacentToTarget () {
        MapLocation location = rc.getLocation();
        return currTarget.isAdjacentTo(location) && !currTarget.equals(location);
    }

}
