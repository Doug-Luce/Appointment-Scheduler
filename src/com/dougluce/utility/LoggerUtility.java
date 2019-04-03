package com.dougluce.utility;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtility {
  private Logger logger = Logger.getLogger(LoggerUtility.class.getName());
  private FileHandler fileHandler = null;

  public LoggerUtility() {
    try {
      fileHandler = new FileHandler("./userLogFile.txt", true);
    } catch (Exception exception) {
      exception.printStackTrace();
    }

    fileHandler.setFormatter(new SimpleFormatter());
    logger.addHandler(fileHandler);
  }

  public void log(String message) {
    logger.info(message);
  }
}
