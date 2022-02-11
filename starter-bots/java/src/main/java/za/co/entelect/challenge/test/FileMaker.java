package za.co.entelect.challenge.test;

import za.co.entelect.challenge.enums.*;
import java.io.*;
import java.util.*;

public class FileMaker {
  public Integer round;
  
  public FileMaker(int round) {
    this.round = round;
  }

  public void write(String buffer) {
    try {
      FileWriter myWriter = new FileWriter("rounds/test_" + round.toString() + ".txt");
      myWriter.write(buffer);
      myWriter.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  public void write(List<String> buffer) {
    try {
      FileWriter myWriter = new FileWriter("rounds/test_" + round.toString() + ".txt");
      for (int i = 0; i < buffer.size(); i++) {
        myWriter.write(buffer.get(i));
        myWriter.write('\n');
      }
      myWriter.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }
}
