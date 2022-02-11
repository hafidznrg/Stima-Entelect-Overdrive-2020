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
    private List<Command> directionList = new ArrayList<>();

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        // GetBlocks
        // Belum dicek tapi ngga ada error
        List<Object> blocksLeft = Collections.emptyList();
        List<Object> blocksRight = Collections.emptyList();
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed, gameState);
        
        if (myCar.position.lane > 1){
            blocksLeft = getBlocksInFront(myCar.position.lane - 1, myCar.position.block, myCar.speed - 1, gameState);
        }
        
        if (myCar.position.lane < 4){
            blocksRight = getBlocksInFront(myCar.position.lane + 1, myCar.position.block, myCar.speed - 1, gameState);
        }
        
        List<Object> nextBlocks = blocks.subList(0,1);
        LookupPowerups lookupPowerups = new LookupPowerups(myCar.powerups);
        FileMaker fileMaker = new FileMaker(gameState.currentRound);
    //    fileMaker.write(lookupPowerups.getHashtable().toString();
        Hashtable<String, Integer> listObstacle = checkBlock(blocks);
        fileMaker.write(listObstacle.toString());

        if (myCar.speed == 0 && myCar.damage < 2) {
            return ACCELERATE;
        } else if (myCar.damage >= 2) {
            return FIX;
        }


        return ACCELERATE;
    }

    /**
     * Return map of obstacle/powerups and count of each
     **/
    private Hashtable<String, Integer> checkBlock(List<Object> blocks) {
        Hashtable<String, Integer> map = new Hashtable<>();
        List<String> keywords = Arrays.asList("MUD", "WALL", "OIL", "CYBERTRUCK", "OIL_POWER", "LIZARD", "EMP", "BOOST", "TWEET", "TOTALDAMAGE", "TOTALPOWERUPS", "EMPTY");
        for (String key : keywords){
            map.put(key, 0);
        }
        for (Object block : blocks) {
            String obstacle = block.toString();
            map.put(obstacle, map.get(obstacle) + 1);
            switch (obstacle) {
                case "MUD":
                    map.put("TOTALDAMAGE", map.get("TOTALDAMAGE") + 1);
                    break;
                case "WALL":
                    map.put("TOTALDAMAGE", map.get("TOTALDAMAGE") + 2);
                    break;
                case "OIL":
                    map.put("TOTALDAMAGE", map.get("TOTALDAMAGE") + 1);
                    break;
                case "CYBERTRUCK":
                    map.put("TOTALDAMAGE", map.get("TOTALDAMAGE") + 2);
                    break;
                case "OIL_POWER":
                    map.put("TOTALPOWERUPS", map.get("TOTALPOWERUPS") + 1);
                    break;
                case "LIZARD":
                    map.put("TOTALPOWERUPS", map.get("TOTALPOWERUPS") + 5);
                    break;
                case "EMP":
                    map.put("TOTALPOWERUPS", map.get("TOTALPOWERUPS") + 10);
                    break;
                case "BOOST":
                    map.put("TOTALPOWERUPS", map.get("TOTALPOWERUPS") + 5);
                    break;
                case "TWEET":
                    map.put("TOTALPOWERUPS", map.get("TOTALPOWERUPS") + 5);
                    break;
                default:
                    break;
            }
        }
        return map;
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
