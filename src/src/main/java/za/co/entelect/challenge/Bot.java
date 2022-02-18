/** @file Bot.java
 *  @brief This is the main strategy file for running the bot.
 *  
 *  This contains the implementation of the bot's strategy 
 *  using decision tree and weighting algorithm 
 *  to determine which best strategy at that time 
 *  using greedy algorithm.
 * 
 *  @author Fikri Khoiron Fadhila           (13520056 / fikrikhoironn)
 *  @author Malik Akbar Hashemi Rafsanjani  (13520105 / malikrafsan)
 *  @author Hafidz Nur Rahman Ghozali       (13520117 / hafidznrg)
 *  @bug No known bugs.
 */

package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.*;
import za.co.entelect.challenge.helper.*;
import za.co.entelect.challenge.test.*;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.abs;

public class Bot {
    private static int maxSpeed = 9;
    private int BOOST_SPEED = 15;
    private List<Command> directionList = new ArrayList<>();
    private LookupPowerups lookupPowerups;

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

    /**
     * @brief Constructor for the Bot class.
     */
    public Bot() {
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    /**
     * @brief This is the main function to decide which best strategy at that time.
     * 
     *        This function use decision tree based on game state
     *        at that time using greedy algorithm. In order to cut down
     *        branches of decision tree, we also use weighting algorithm.
     * 
     *        More detail explanations are available in the report.
     * 
     * @param gameState The current state of the game.
     * @return The best strategy at that time.
     */
    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        maxSpeed = calculateMaxSpeed(myCar.damage);
        lookupPowerups = new LookupPowerups(myCar.powerups);
        FileMaker fileMaker = new FileMaker(gameState.currentRound);

        // GetBlocks
        Hashtable<String, Integer> obstacleStraight = getBlocks(myCar.position.lane, myCar.position.block, myCar.speed,
                gameState);
        Hashtable<String, Integer> obstacleLeft = new Hashtable<>();
        Hashtable<String, Integer> obstacleRight = new Hashtable<>();
        Hashtable<String, Integer> obstacleAccel = getBlocks(myCar.position.lane,
                myCar.position.block, next_speed(myCar.speed), gameState);
        Hashtable<String, Integer> obstacleBoost = getBlocks(myCar.position.lane, myCar.position.block,
                maxSpeed, gameState);

        if (myCar.position.lane > 1) {
            obstacleLeft = getBlocks(myCar.position.lane - 1, myCar.position.block, myCar.speed - 1, gameState);
        }

        if (myCar.position.lane < 4) {
            obstacleRight = getBlocks(myCar.position.lane + 1, myCar.position.block, myCar.speed - 1, gameState);
        }

        fileMaker.logger("START ROUND " + gameState.currentRound);
        fileMaker.logger("KIRI   " + obstacleLeft.toString());
        fileMaker.logger("TENGAH " + obstacleStraight.toString());
        fileMaker.logger("KANAN  " + obstacleRight.toString());
        if (myCar.speed == 0 && myCar.damage < 2) {
            fileMaker.logger("SPEED == 0 AND DAMAGE < 2");
            fileMaker.logger("USE COMMAND ACCELERATE");
            return ACCELERATE;
        } else if (myCar.damage >= 2) {
            fileMaker.logger("DAMAGE >= 2, WHICH = " + Integer.toString(myCar.damage));
            fileMaker.logger("USE COMMAND FIX");
            return FIX;
        }

        // Use EMP Command
        if (lookupPowerups.hasPowerUp(PowerUps.EMP) && (myCar.position.block < opponent.position.block)
                && (myCar.position.lane == opponent.position.lane
                        || (myCar.position.lane == 2 && opponent.position.lane == 1)
                        || (myCar.position.lane == 3 && opponent.position.lane == 4))) {
            fileMaker.logger("USE COMMAND EMP");
            return EMP;
        }

        if (lookupPowerups.hasPowerUp(PowerUps.BOOST) && myCar.speed == 3 && obstacleBoost.get("TOTALDAMAGE") <= 2) {
            fileMaker.logger("USE COMMAND BOOST");
            return BOOST;
        }

        // Decision Tree based on current lane
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
                if (lookupPowerups.hasPowerUp(PowerUps.BOOST) && !myCar.boosting && myCar.speed <= maxSpeed
                        && obstacleBoost.get("TOTALDAMAGE") == 0) {
                    fileMaker.logger("USE " + (myCar.damage == 0 ? "COMMAND BOOST" : "COMMAND FIX"));
                    return myCar.damage == 0 ? BOOST : FIX;
                }

                if (myCar.speed != maxSpeed) {
                    fileMaker.logger("SPEED != MAXSPEED");

                    if (obstacleAccel.get("TOTALDAMAGE") == 0) {
                        fileMaker.logger("USE COMMAND ACCELERATE");
                        return ACCELERATE;
                    }
                    fileMaker.logger("DOESNT ACCELERATE");
                }

                if (lookupPowerups.hasPowerUp(PowerUps.EMP) && (myCar.position.block < opponent.position.block)
                        && (abs(myCar.position.lane - opponent.position.lane) <= 1)) {
                    fileMaker.logger("USE COMMAND EMP");
                    return EMP;
                }

                if (lookupPowerups.hasPowerUp(PowerUps.TWEET)) {
                    fileMaker.logger("USE COMMAND TWEET");
                    return new TweetCommand(opponent.position.lane, opponent.position.block + BOOST_SPEED + 1);
                }

                if (lookupPowerups.hasPowerUp(PowerUps.OIL) && myCar.position.block > opponent.position.block) {
                    fileMaker.logger("USE COMMAND OIL");
                    return OIL;
                } else {
                    if (lookupPowerups.hasPowerUp(PowerUps.BOOST) && obstacleBoost.get("TOTALDAMAGE") <= 2) {
                        fileMaker.logger("USE COMMAND BOOST");
                        return BOOST;
                    } else {
                        if (obstacleAccel.get("TOTALDAMAGE") <= 1) {
                            fileMaker.logger("USE COMMAND ACCELERATE");
                            return ACCELERATE;
                        } else {
                            fileMaker.logger("DO NOTHING");
                            return DO_NOTHING;
                        }
                    }
                }
            case "LEFT":
                fileMaker.logger("USE COMMAND TURN_LEFT");
                return TURN_LEFT;
            case "RIGHT":
                fileMaker.logger("USE COMMAND TURN_RIGHT");
                return TURN_RIGHT;
            case "LIZARD":
                fileMaker.logger("USE COMMAND LIZARD");
                return LIZARD;
            case "ALLDAMAGED":
                if (lookupPowerups.hasPowerUp(PowerUps.BOOST) && obstacleBoost.get("TOTALDAMAGE") <= 2) {
                    fileMaker.logger("USE COMMAND BOOST");
                    return BOOST;
                } else {
                    if ((myCar.position.lane == 1 || obstacleAccel.get("TOTALDAMAGE") <= obstacleLeft.get("TOTALDAMAGE"))
                        && (myCar.position.lane == 4 || obstacleAccel.get("TOTALDAMAGE") <= obstacleRight.get("TOTALDAMAGE"))) {
                        fileMaker.logger("ALL DAMAGED : USE COMMAND ACCELERATE");
                        return ACCELERATE;
                    } else {
                        fileMaker.logger("ALL DAMAGED : TURN LEFT OR RIGHT");
                        if (myCar.position.lane == 1) return TURN_RIGHT;
                        else if (myCar.position.lane == 4) return TURN_LEFT;
                        else {
                            if (obstacleLeft.get("TOTALDAMAGE") == obstacleRight.get("TOTALDAMAGE")) {
                                return (myCar.position.lane == 2) ? TURN_RIGHT : TURN_LEFT;
                            } else {
                                return (obstacleLeft.get("TOTALDAMAGE") < obstacleRight.get("TOTALDAMAGE")) ? TURN_LEFT : TURN_RIGHT;
                            }
                        }
                    }
                }
        }

        fileMaker.logger("END OF DECISION TREE");
        fileMaker.logger("USE COMMAND ACCELERATE");
        return ACCELERATE;
    }

    /**
     * @brief Calculate the next speed based on current speed.
     * 
     * @param speed current speed of the car
     * @return next speed of the car
     */
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
     * @brief Calculate max speed based on car damage
     * 
     * @param damage current damage of the car
     * @return max speed of the car
     */
    private Integer calculateMaxSpeed(int damage) {
        switch (damage) {
            case 0:
                return 15;
            case 1:
                return 9;
            case 2:
                return 8;
            case 3:
                return 6;
            case 4:
                return 3;
            case 5:
                return 0;
        }
        return 9;
    }

    /**
     * @brief Decide type strategy if current lane is 2 or 3
     * 
     * @param obstacleLeft     hashtable of obstacles and powerups on left lane
     * @param obstacleStraight hashtable of obstacles and powerups on middle lane
     * @param obstacleRight    hashtable of obstacles and powerups on right lane
     * @param lane             current lane of the car
     * @return string decision
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
     * @brief Decide type strategy if current lane is 1
     * 
     * @param obstacleStraight hashtable of obstacles and powerups on middle lane
     * @param obstacleRight    hashtable of obstacles and powerups on right lane
     * @return string decision
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
     * @brief Decide type strategy if current lane is 4
     * 
     * @param obstacleLeft     hashtable of obstacles and powerups on left lane
     * @param obstacleStraight hashtable of obstacles and powerups on middle lane
     * @return string decision
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

    /**
     * @brief calculate max value of several integers
     * 
     * @param values several arguments of integers
     * @return idx of max value
     */
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
     * @brief calculate point of block based on obstacle or powerups on that block
     * 
     * @param obj String of keyword for obstacle or powerups
     * @return point of block
     */
    private Integer blockPoint(String obj) {
        switch (obj) {
            case "MUD":
                return -1;
            case "WALL":
                return -3;
            case "OIL_SPILL":
                return -1;
            case "OIL_POWER":
                return 0;
            case "LIZARD":
                return lookupPowerups.countPowerUps(PowerUps.LIZARD) > 2 ? 0 : 3;
            case "EMP":
                return 3;
            case "BOOST":
                return lookupPowerups.countPowerUps(PowerUps.BOOST) > 3 ? 0 : 2;
            case "TWEET":
                return lookupPowerups.countPowerUps(PowerUps.TWEET) > 0 ? 0 : 1;
            case "PLAYER":
                return -1;
            case "CYBERTRUCK":
                return -3;
        }
        return 0;
    }

    /**
     * @brief calculate point of list of blocks on one lane based on obstacles and
     *        powerups
     * 
     * @param lane      lane number
     * @param block     block number
     * @param speed     speed/interval
     * @param gameState gameState
     * @return hashtable of obstacles and powerups
     */
    private Hashtable<String, Integer> getBlocks(int lane, int block, int speed, GameState gameState) {
        Hashtable<String, Integer> hashtable = new Hashtable<>();
        List<String> keywords = Arrays.asList("MUD", "WALL", "OIL_SPILL", "OIL_POWER", "LIZARD", "EMP", "BOOST",
                "TWEET", "TOTALDAMAGE", "TOTALPOWERUPS", "EMPTY", "PLAYER", "CYBERTRUCK");
        for (String key : keywords) {
            hashtable.put(key, 0);
        }

        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            String blockObj = laneList[i].terrain.toString();
            if (laneList[i].occupiedByPlayerId != 0 && i != block - startBlock
                    && (i + gameState.opponent.speed) < (block - startBlock + speed)) {
                blockObj = "PLAYER";
                hashtable.put("PLAYER", hashtable.get("PLAYER") + 1);
            } else if (laneList[i].isOccupiedByCyberTruck) {
                blockObj = "CYBERTRUCK";
                hashtable.put("CYBERTRUCK", hashtable.get("CYBERTRUCK") + 1);
            } else {
                Terrain terrain = laneList[i].terrain;
                hashtable.put(terrain.toString(), hashtable.get(terrain.toString()) + 1);
            }
            Integer poin = blockPoint(blockObj);
            hashtable.put(poin > 0 ? "TOTALPOWERUPS" : "TOTALDAMAGE",
                    hashtable.get(poin > 0 ? "TOTALPOWERUPS" : "TOTALDAMAGE") + poin);
        }

        hashtable.put("TOTALDAMAGE", -1 * hashtable.get("TOTALDAMAGE"));
        return hashtable;
    }
}
