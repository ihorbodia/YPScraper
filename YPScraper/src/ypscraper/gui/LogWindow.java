/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ypscraper.gui;


import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Ihor
 */
class LogWindow extends JFrame {
  private JTextArea textArea = new JTextArea();

  public LogWindow() {
    super("");
    setSize(300, 300);
    DefaultCaret caret = (DefaultCaret)textArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    add(new JScrollPane(textArea));
    setVisible(true);
  }

  public void showInfo(String data) {
    textArea.append(data);
    this.validate();
  }
}


