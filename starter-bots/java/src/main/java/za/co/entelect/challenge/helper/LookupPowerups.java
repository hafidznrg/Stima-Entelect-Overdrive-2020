package za.co.entelect.challenge.helper;

import java.util.*;
import za.co.entelect.challenge.enums.*;

public class LookupPowerups {
  Hashtable<PowerUps, Boolean> hashtable;

  public LookupPowerups(PowerUps[] listPowerUps) {
    hashtable = new Hashtable<>();
    for (PowerUps powerUps : listPowerUps) {
      hashtable.put(powerUps, true);
    }
  }
  public Boolean hasPowerUp(PowerUps powerUps) {
    return hashtable.containsKey(powerUps);
  }
  public Hashtable<PowerUps, Boolean> getHashtable() {
    return hashtable;
  }
}