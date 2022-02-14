package za.co.entelect.challenge.helper;

import java.util.*;
import za.co.entelect.challenge.enums.*;

public class LookupPowerups {
  Hashtable<PowerUps, Integer> hashtable;

  public LookupPowerups(PowerUps[] listPowerUps) {
    hashtable = new Hashtable<>();
    List<PowerUps> powerUpsList = Arrays.asList(
        PowerUps.BOOST,
        PowerUps.OIL,
        PowerUps.TWEET,
        PowerUps.LIZARD,
        PowerUps.EMP);

    for (PowerUps powerUp : powerUpsList) {
      hashtable.put(powerUp, 0);
    }
    for (PowerUps powerUps : listPowerUps) {
      hashtable.put(powerUps, hashtable.get(powerUps) + 1);
    }
  }

  public Boolean hasPowerUp(PowerUps powerUps) {
    return hashtable.get(powerUps) > 0;
  }

  public Hashtable<PowerUps, Integer> getHashtable() {
    return hashtable;
  }

  public int countPowerUps(PowerUps powerUps) {
    return hashtable.get(powerUps);
  }
}