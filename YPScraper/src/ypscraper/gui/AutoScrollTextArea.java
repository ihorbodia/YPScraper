package gui;

import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

public class AutoScrollTextArea extends JScrollPane {
	
	private int linesShown = 0;
	private int linesToShow = 400;
    
    private JTextPane textPane;
    private SimpleAttributeSet attributeSet;
    
    public AutoScrollTextArea() {
        super();
        this.setViewportView(getTextPane());
        redirectSystemStreams();
    }
    
    private void redirectSystemStreams() {
    	OutputStream out = new OutputStream() {
    		
    		private StringBuffer lineBuffer = new StringBuffer();
		  
    		@Override  
    		public void write(int b) throws IOException {
    			lineBuffer.append(b);
    			if(b == 13) { // newline
    				try {
						AutoScrollTextArea.this.append(lineBuffer.toString());
					} catch (BadLocationException e) {
					}
    				lineBuffer.setLength(0);
    			}
    		}  
	  
    		@Override  
    		public void write(byte[] b, int off, int len) throws IOException {
    			lineBuffer.append(new String(b, off, len));
    			int indexOfNewLineChar = -1;
    			while((indexOfNewLineChar = lineBuffer.indexOf("\n")) >= 0) {
    				try {
						AutoScrollTextArea.this.append(lineBuffer.substring(0, 1 + indexOfNewLineChar).toString());
					} catch (BadLocationException e) {
					}
    				lineBuffer.delete(0, 1 + indexOfNewLineChar);
    			}
    		}  
	  
    		@Override  
    		public void write(byte[] b) throws IOException {  
    			write(b, 0, b.length);  
    		}  
    	};  
	  
    	System.setOut(new PrintStream(out, true));  
    	System.setErr(new PrintStream(out, true));  
	}  
    
    private void scrollToBottom() {
    	getTextPane().setCaretPosition(getTextPane().getDocument().getLength());
    }
    
    public JTextPane getTextPane() {
    	if(textPane == null) {
    		textPane = new JTextPane(new DefaultStyledDocument());
            textPane.setEditable(false);
            textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
    	}
        return textPane;
    }
    
    public void append(String str) throws BadLocationException {
    	Document document = getTextPane().getDocument();
    	document.insertString(document.getLength(), str, getSimpleAttributeSet());
    	getTextPane().setDocument(document);
        if (++linesShown > linesToShow) {
            // We must remove a line!
            int len = getTextPane().getText().indexOf('\n');
            document.remove(0, len);
            linesShown--;
        }
        scrollToBottom();
    }
    
    public Document getDocument() {
        return getTextPane().getDocument();
    }
    
    public SimpleAttributeSet getSimpleAttributeSet() {
    	if(attributeSet == null) {
    		attributeSet = new SimpleAttributeSet();
    	}
        return attributeSet;
    }

	public int getLinesToShow() {
		return linesToShow;
	}
}