package za.co.entelect.challenge.test;

import za.co.entelect.challenge.enums.*;
import java.io.*;
import java.util.*;

public class FileMaker {
  public Integer round;
  
  public FileMaker(int round) {
    this.round = round;
  }

  public void write(Hashtable<PowerUps, Boolean> hashtable) {
    try {
      FileWriter myWriter = new FileWriter("rounds/test_" + round.toString() + ".txt");
      myWriter.write(hashtable.toString());
      myWriter.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }
}
