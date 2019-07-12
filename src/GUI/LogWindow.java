package GUI;


import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

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


