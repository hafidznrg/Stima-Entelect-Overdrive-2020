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
        fileMaker.write(lookupPowerups.getHashtable());

        if (myCar.speed == 0 && myCar.damage < 2) {
            return ACCELERATE;
        } else if (myCar.damage >= 2) {
            return FIX;
        }


        return ACCELERATE;
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
