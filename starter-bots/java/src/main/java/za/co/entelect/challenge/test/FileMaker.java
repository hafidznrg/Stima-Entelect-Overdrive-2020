package za.co.entelect.challenge.test;

import java.io.*;
import java.util.*;

public class FileMaker {
  private static Integer round;
  private static String log_round;
  private String path_unit_test = "unit_test";
  private String path_logger = "rounds";

  public FileMaker(int roundArg) {
    round = roundArg;
    log_round = "";
  }

  public void createDir() {
    File f1 = new File(System.getProperty("user.dir") + "\\" + this.path_unit_test);
    File f2 = new File(System.getProperty("user.dir") + "\\" + this.path_logger);
    boolean bool1 = f1.exists() ? true : f1.mkdir();
    boolean bool2 = f2.exists() ? true : f2.mkdir();
    if (bool1 && bool2) {
      System.out.println("Folder is created successfully");
    } else {
      System.out.println(
          "Error Found! Folder is not created for " + (bool1 ? " unit_test " : "") + (bool2 ? " logger " : ""));
    }
  }

  public void write(String buffer) {
    try {
      FileWriter myWriter = new FileWriter(this.path_unit_test + "/round_" + round.toString() + ".txt");
      myWriter.write(buffer);
      myWriter.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  public void write(List<String> buffer) {
    try {
      FileWriter myWriter = new FileWriter(this.path_unit_test + "/round_" + round.toString() + ".txt");
      for (int i = 0; i < buffer.size(); i++) {
        myWriter.write(buffer.get(i));
        myWriter.write('\n');
      }
      myWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void logger(String str) {
    log_round += str;
    log_round += '\n';
  }

  public void printLog() {
    try {
      FileWriter myWriter = new FileWriter(this.path_logger + "/" + round.toString() + "/round_log" + ".txt");
      myWriter.write(log_round);
      myWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
