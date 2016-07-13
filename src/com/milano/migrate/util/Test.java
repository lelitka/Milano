package com.milano.migrate.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
 
/**
 * A Simple service to register as Windows Service, the program will log on a
 * file when it starts, ends and every 10 seconds it will log is alive state
 * 
 * 
 */
public class Test {
 
 public static void main(String[] args) throws FileNotFoundException {
 
  
  
  
  /* Set the custom message to display */
  String customMessage = "default";
  if (args.length > 0) {
   customMessage = args[0];
  }
  /*
   * Set the standard output stream as on a log file
   */
  String pathname = "./log.txt";
  FileOutputStream out = new FileOutputStream(new File(pathname), true);
  PrintStream printStream = new PrintStream(out);
  System.setOut(printStream);
 
  /*
   * Add the shutdown hook
   */
  Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
  
 
  onStart();
 
  doWork(customMessage);
  
  
  
 }
 
 /**
  * Log the alive state of the service
  * 
  * @param customMessage
  *          A custom message
  */
 private static void doWork(String customMessage) {
  while (true) {
   try {
    Thread.sleep(10000);
   } catch (InterruptedException e) {
    System.out.println("Interrupted at " + new Date());
   }
   System.out.println("Alive at " + new Date() + " " + customMessage);
  }
 }
 
 /**
  * Log the boot of the service
  */
 private static void onStart() {
  System.out.println("Starts at " + new Date());
 }
 
 /**
  * A shutdown hook
  * 
  * 
  */
 private static class ShutdownHook implements Runnable {
 
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {
   onStop();
  }
 
  /**
   * Logs when the service is stopped
   */
  private void onStop() {
   System.out.println("Ends at " + new Date());
   System.out.flush();
   System.out.close();
  }
 
 }
 
}
