/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.util.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Ihor
**/

public class WindowHandler extends Handler {
  private LogWindow window = null;

  private Formatter formatter = null;

  private Level level = null;

  private static WindowHandler handler = null;

  private WindowHandler() {
    setFormatter(new SimpleFormatter());
    LogManager manager = LogManager.getLogManager();
    String className = this.getClass().getName();
    String level = manager.getProperty(className + ".level");
    setLevel(level != null ? Level.parse(level) : Level.INFO);

    if (window == null)
      window = new LogWindow();
  }

  public static synchronized WindowHandler getInstance() {
    if (handler == null) {
      handler = new WindowHandler();
    }
    return handler;
  }

  public synchronized void publish(LogRecord record) {
    String message = null;
    if (!isLoggable(record))
      return;
    message = getFormatter().format(record);
    window.showInfo(message);
  }

  public void close() {
  }

  public void flush() {
  }
}