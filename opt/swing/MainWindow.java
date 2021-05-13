package opt.swing;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.*;
import javax.swing.JButton;
import javax.swing.JFrame;

import opt.swing.LogInPanel.ButtonAction;


@SuppressWarnings("serial")
public class MainWindow extends JFrame implements ActionListener {
	 Container container = getContentPane();
	
	MainWindow() throws NoPlayerException, CannotRealizeException, IOException {
        setLayoutManager();
    }
	  public void setLayoutManager() throws NoPlayerException, CannotRealizeException, IOException {
	        container.setLayout(null);
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
       


		public void load() throws NoPlayerException, CannotRealizeException, IOException
        {
            

            String url = "https://www.youtube.com/watch?v=rXAkLKcxxM8";
            URL mediaURL;
			try {
			//	mediaURL = new URL("https://www.youtube.com/watch?v=rXAkLKcxxM8");
		
          //  Player mediaPlayer = Manager.createRealizedPlayer(mediaURL);
           // Component video = mediaPlayer.getVisualComponent();
          //  Component controls = mediaPlayer.getControlPanelComponent();
            
            MainWindow 
    		
    			frame = new MainWindow();
    			 frame.setTitle("Main Window");
    			    frame.setVisible(true);
    			    frame.setBounds(200, 50, 1000, 600);
    			    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    			    frame.setResizable(false);
    			//    container.add(video,BorderLayout.CENTER);
    	          //  container.add(controls,BorderLayout.SOUTH);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         
           
        }



		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		
    


}
