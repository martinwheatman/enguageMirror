package opt.swing;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import opt.swing.LogInPanel.ButtonAction;


@SuppressWarnings("serial")
public class MainWindow extends JFrame implements ActionListener {
	 Container container = getContentPane();
	 JPanel pane = new JPanel(new GridBagLayout());
	 GridBagConstraints c = new GridBagConstraints();
	 JButton testone = new JButton();
	 JButton testtwo = new JButton();
	 GridBagLayout layout = new GridBagLayout(); 
	 
	
	public MainWindow() {
        setLayoutManager();
        this.setTitle("Main Window");
	    this.setVisible(true);
	    this.setBounds(100, 100, 1000, 600);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setResizable(false);
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
	    this.setLocation(x, y);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 0;
	    c.gridy = 0;
	    this.add(testone, c);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 1;
	    c.gridy = 1;
	    this.add(testtwo, c);
    }
	  public void setLayoutManager() {
	        container.setLayout(layout);
	    //    load();
	    }
	 
	 
//	public static void main(String[] a) throws NoPlayerException, CannotRealizeException, IOException {
//	    
//		MainWindow frame = new MainWindow();
//	    frame.setTitle("Login Window");
//	    frame.setVisible(true);
//	    frame.setBounds(10, 10, 370, 600);
//	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	    frame.setResizable(false);
//	    
//	}
       


  
   



		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		
    


}
