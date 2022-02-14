package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.*;
import za.co.entelect.challenge.helper.*;
import za.co.entelect.challenge.test.*;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private int BOOST_SPEED = 15;
    private List<Command> directionList = new ArrayList<>();

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command DECELERATE = new DecelerateCommand();
    private final static Command DO_NOTHING = new DoNothingCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    private LookupPowerups lookupPowerups;

    public Bot() {
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        lookupPowerups = new LookupPowerups(myCar.powerups);
        FileMaker fileMaker = new FileMaker(gameState.currentRound);

        // GetBlocks
        List<Object> blocksLeft = Collections.emptyList();
        List<Object> blocksRight = Collections.emptyList();
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed, gameState);

        if (myCar.position.lane > 1) {
            blocksLeft = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed - 1, gameState);
        }

        if (myCar.position.lane < 4) {
            blocksRight = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed - 1, gameState);
        }

        fileMaker.logger("START ROUND " + gameState.currentRound);
        if (myCar.speed == 0 && myCar.damage < 2) {
            fileMaker.logger("SPEED == 0 AND DAMAGE < 2");
            fileMaker.logger("USE COMMAND ACCELERATE");
            fileMaker.printLog();
            return ACCELERATE;
        } else if (myCar.damage >= 2) {
            fileMaker.logger("DAMAGE >= 2, WHICH = " + Integer.toString(myCar.damage));
            fileMaker.logger("USE COMMAND FIX");
            fileMaker.printLog();
            return FIX;
        }

        // Akan menggunakan EMP
        if (lookupPowerups.hasPowerUp(PowerUps.EMP) && (myCar.position.block < opponent.position.block)
                && (myCar.position.lane == opponent.position.lane
                        || (myCar.position.lane == 2 && opponent.position.lane == 1)
                        || (myCar.position.lane == 3 && opponent.position.lane == 4))) {
            fileMaker.logger("USE COMMAND EMP");
            fileMaker.printLog();
            return EMP;
        }

        Hashtable<String, Integer> obstacleLeft = checkBlocks(blocksLeft);
        Hashtable<String, Integer> obstacleStraight = checkBlocks(blocks);
        Hashtable<String, Integer> obstacleRight = checkBlocks(blocksRight);

        String decision = "";

        if (myCar.position.lane == 1) {
            fileMaker.logger("LANE 1");
            decision = laneOneDecision(obstacleStraight, obstacleRight);
        } else if (myCar.position.lane == 2 || myCar.position.lane == 3) {
            fileMaker.logger("LANE 2 OR 3");
            decision = middleDecision(obstacleLeft, obstacleStraight, obstacleRight, myCar.position.lane);
        } else if (myCar.position.lane == 4) {
            fileMaker.logger("LANE 4");
            decision = laneFourDecision(obstacleLeft, obstacleStraight);
        }
        fileMaker.logger("DECISION = " + decision);

        switch (decision) {
            case "STRAIGHT":
                List<Object> blocksBoost = getBlocksInFront(myCar.position.lane, myCar.position.block, BOOST_SPEED,
                        gameState);
                Hashtable<String, Integer> obstacleBoost = checkBlocks(blocksBoost);
                if (lookupPowerups.hasPowerUp(PowerUps.BOOST) && !myCar.boosting && myCar.speed < maxSpeed
                        && obstacleBoost.get("TOTALDAMAGE") == 0) {
                    fileMaker.logger("USE " + (myCar.damage == 0 ? "COMMAND BOOST" : "COMMAND FIX"));
                    fileMaker.printLog();
                    return myCar.damage == 0 ? BOOST : FIX;
                }

                if (myCar.speed != maxSpeed) {
                    fileMaker.logger("SPEED != MAXSPEED");
                    List<Object> blocksAccel = getBlocksInFront(myCar.position.lane, myCar.position.block,
                            next_speed(myCar.speed), gameState);
                    Hashtable<String, Integer> obstacleAccel = checkBlocks(blocksAccel);

                    if (obstacleAccel.get("TOTALDAMAGE") == 0) {
                        fileMaker.logger("USE COMMAND ACCELERATE");
                        fileMaker.printLog();
                        return ACCELERATE;
                    }
                    fileMaker.logger("DOESNT ACCELERATE");
                }

                if (lookupPowerups.hasPowerUp(PowerUps.TWEET)) {
                    fileMaker.logger("USE COMMAND TWEET");
                    fileMaker.printLog();
                    return new TweetCommand(opponent.position.lane, opponent.position.block + BOOST_SPEED + 1);
                }

                if (lookupPowerups.hasPowerUp(PowerUps.OIL) && myCar.position.block > opponent.position.block) {
                    fileMaker.logger("USE COMMAND OIL");
                    fileMaker.printLog();
                    return OIL;
                } else {
                    fileMaker.logger("USE COMMAND DO_NOTHING");
                    fileMaker.printLog();
                    return DO_NOTHING;
                }
            case "LEFT":
                fileMaker.logger("USE COMMAND TURN_LEFT");
                fileMaker.printLog();
                return TURN_LEFT;
            case "RIGHT":
                fileMaker.logger("USE COMMAND TURN_RIGHT");
                fileMaker.printLog();
                return TURN_RIGHT;
            case "LIZARD":
                fileMaker.logger("USE COMMAND LIZARD");
                fileMaker.printLog();
                return LIZARD;
            case "ALLDAMAGED":
                fileMaker.logger("USE COMMAND DO_NOTHING");
                fileMaker.printLog();
                return DO_NOTHING;
        }

        fileMaker.logger("END OF DECISION TREE");
        fileMaker.logger("USE COMMAND ACCELERATE");
        fileMaker.printLog();
        return ACCELERATE;
    }

    private Integer next_speed(int speed) {
        switch (speed) {
            case 0:
                return 3;
            case 3:
                return 6;
            case 5:
                return 6;
            case 6:
                return 8;
            case 8:
                return 9;
            case 9:
                return 9;
        }
        return 0;
    }

    /**
     * Decision tree if car lane in 2 or 3
     * return value {STRAIGHT, LEFT, RIGHT, LIZARD, ALLDAMAGED}
     */
    private String middleDecision(Hashtable<String, Integer> obstacleLeft, Hashtable<String, Integer> obstacleStraight,
            Hashtable<String, Integer> obstacleRight, int lane) {
        if (obstacleStraight.get("TOTALDAMAGE") == 0) {
            if (obstacleLeft.get("TOTALDAMAGE") == 0 && obstacleRight.get("TOTALDAMAGE") == 0) {
                int idx = getMax(obstacleStraight.get("TOTALPOWERUPS"), obstacleLeft.get("TOTALPOWERUPS"),
                        obstacleRight.get("TOTALPOWERUPS"));
                if (idx == 0)
                    return "STRAIGHT";
                else
                    return idx == 1 ? "LEFT" : "RIGHT";
            } else if (obstacleLeft.get("TOTALDAMAGE") == 0) {
                int idx = getMax(obstacleStraight.get("TOTALPOWERUPS"), obstacleLeft.get("TOTALPOWERUPS"));
                return idx == 0 ? "STRAIGHT" : "LEFT";
            } else if (obstacleRight.get("TOTALDAMAGE") == 0) {
                int idx = getMax(obstacleStraight.get("TOTALPOWERUPS"), obstacleRight.get("TOTALPOWERUPS"));
                return idx == 0 ? "STRAIGHT" : "RIGHT";
            } else {
                return "STRAIGHT";
            }
        } else if (obstacleLeft.get("TOTALDAMAGE") == 0 && obstacleRight.get("TOTALDAMAGE") == 0) {
            return lane == 2 ? "RIGHT" : "LEFT";
        } else if (obstacleLeft.get("TOTALDAMAGE") == 0) {
            return "LEFT";
        } else if (obstacleRight.get("TOTALDAMAGE") == 0) {
            return "RIGHT";
        } else if (lookupPowerups.hasPowerUp(PowerUps.LIZARD)) {
            return "LIZARD";
        } else {
            return "ALLDAMAGED";
        }
    }

    /**
     * Decision tree if car lane in 1
     * return value {STRAIGHT, RIGHT, LIZARD, ALLDAMAGED}
     */
    private String laneOneDecision(Hashtable<String, Integer> obstacleStraight,
            Hashtable<String, Integer> obstacleRight) {
        if (obstacleStraight.get("TOTALDAMAGE") == 0) {
            if (obstacleRight.get("TOTALDAMAGE") == 0) {
                int idx = getMax(obstacleStraight.get("TOTALPOWERUPS"), obstacleRight.get("TOTALPOWERUPS"));
                if (idx == 0)
                    return "STRAIGHT";
                else
                    return "RIGHT";
            } else {
                return "STRAIGHT";
            }
        } else if (obstacleRight.get("TOTALDAMAGE") == 0) {
            return "RIGHT";
        } else if (lookupPowerups.hasPowerUp(PowerUps.LIZARD)) {
            return "LIZARD";
        } else {
            return "ALLDAMAGED";
        }
    }

    /**
     * Decision tree if car lane in 4
     * return value {STRAIGHT, LEFT, LIZARD, ALLDAMAGED}
     */
    private String laneFourDecision(Hashtable<String, Integer> obstacleLeft,
            Hashtable<String, Integer> obstacleStraight) {
        if (obstacleStraight.get("TOTALDAMAGE") == 0) {
            if (obstacleLeft.get("TOTALDAMAGE") == 0) {
                int idx = getMax(obstacleStraight.get("TOTALPOWERUPS"), obstacleLeft.get("TOTALPOWERUPS"));
                if (idx == 0)
                    return "STRAIGHT";
                else
                    return "LEFT";
            } else {
                return "STRAIGHT";
            }
        } else if (obstacleLeft.get("TOTALDAMAGE") == 0) {
            return "LEFT";
        } else if (lookupPowerups.hasPowerUp(PowerUps.LIZARD)) {
            return "LIZARD";
        } else {
            return "ALLDAMAGED";
        }
    }

    private int getMax(int... values) {
        int max = values[0];
        int idx = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
                idx = i;
            }
        }
        return idx;
    }

    /**
     * Return map of obstacle/powerups and count of each
     **/
    private Hashtable<String, Integer> checkBlocks(List<Object> blocks) {
        Hashtable<String, Integer> map = new Hashtable<>();
        List<String> keywords = Arrays.asList("MUD", "WALL", "OIL_SPILL", "OIL_POWER", "LIZARD", "EMP", "BOOST",
                "TWEET", "TOTALDAMAGE", "TOTALPOWERUPS", "EMPTY");
        for (String key : keywords) {
            map.put(key, 0);
        }
        for (Object block : blocks) {
            String obj = block.toString();
            map.put(obj, map.get(obj) + 1);
            Integer poin = blockPoint(obj);
            map.put(poin > 0 ? "TOTALPOWERUPS" : "TOTALDAMAGE",
                    map.get(poin > 0 ? "TOTALPOWERUPS" : "TOTALDAMAGE") + poin);
        }
        map.put("TOTALDAMAGE", -1 * map.get("TOTALDAMAGE"));
        return map;
    }

    /**
     * check point of given block
     * 
     * @param obj object type of block
     * @return point of the given block
     **/
    private Integer blockPoint(String obj) {
        switch (obj) {
            case "MUD":
                return -1;
            case "WALL":
                return -2;
            case "OIL_SPILL":
                return -1;
            case "OIL_POWER":
                return 0;
            case "LIZARD":
                return lookupPowerups.countPowerUps(PowerUps.LIZARD) > 1 ? 0 : 3;
            case "EMP":
                return 3;
            case "BOOST":
                return lookupPowerups.countPowerUps(PowerUps.BOOST) > 2 ? 0 : 2;
            case "TWEET":
                return lookupPowerups.countPowerUps(PowerUps.TWEET) > 0 ? 0 : 1;
        }
        return 0;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, int speed, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            blocks.add(laneList[i].terrain);
        }
        return blocks;
    }
}
