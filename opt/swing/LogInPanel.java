package opt.swing;

import javax.media.CannotRealizeException;
import javax.media.NoPlayerException;
import javax.swing.*;

import org.enguage.Enguage;
import org.enguage.objects.Variable;
import org.enguage.util.Strings;

import opt.swing.EnguagePanel.ButtonAction;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
 
@SuppressWarnings("serial")
public class LogInPanel extends JFrame implements ActionListener {
 
	static LogInPanel mainFrame;
    Container container = getContentPane();
    JLabel title = new JLabel("Enguage Sonar Panel Prototype");
    JTextField inputCommand = new JTextField("");
    JTextField replyField = new JTextField("");
    JButton confirmCommandButton = new JButton();
    protected Action buttonAction;
    static JPanel pane = new JPanel(new GridBagLayout());
    static GridBagConstraints c = new GridBagConstraints();
    static JButton testone = new JButton("ahfeehgwgh0wh");
    static JButton testtwo = new JButton("sgpojgrwghwni");
 
 
    LogInPanel() {
        setLayoutManager();
        setLocationAndSize();
        addComponentsToContainer();
        addActionEvent();
 
    }
    
 
    public void setLayoutManager() {
        container.setLayout(null);
    }
 
    public void setLocationAndSize() {
    	Font font1 = new Font("SansSerif", Font.PLAIN, 24);
		title.setFont(font1);
		inputCommand.setFont( font1 );
		replyField.setFont(font1);
		replyField.setEditable(false);
		buttonAction = new ButtonAction( "Confirm Command", null,
				"Click to interpret what you type above",
				KeyEvent.VK_M );
		confirmCommandButton = new JButton( buttonAction);
		if (confirmCommandButton.getIcon() != null) confirmCommandButton.setText( "" );
		confirmCommandButton.setFont( font1 );
		

		title.setBounds(125,10,400,100);
        inputCommand.setBounds(100, 100, 400, 30);
        confirmCommandButton.setBounds(150, 150, 300, 30);
        replyField.setBounds(100, 400, 400, 30);
        
 
 
    }
 
    public void addComponentsToContainer() {
    	container.add(title);
        container.add(inputCommand);
        container.add(replyField);
        container.add(confirmCommandButton);
    }
 
    public void addActionEvent() {
     
        confirmCommandButton.addActionListener(buttonAction);
    }
 
 
    @Override
    public void actionPerformed(ActionEvent e) {

    }
    
    public static void Logon() throws NoPlayerException, CannotRealizeException, IOException {
    	
		
		String user = Variable.get( "USER" );
		     if (user == null); // ignore a null return
		else if (user.equals(  "admin-one" )) {
			MainWindow frame = new MainWindow();
//		    frame.setTitle("Main Window");
//		    frame.setVisible(true);
//		    frame.setBounds(100, 100, 1000, 600);
//		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		    frame.setResizable(false);
//		    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
//		    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
//		    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
//		    frame.setLocation(x, y);
//		    c.fill = GridBagConstraints.HORIZONTAL;
//		    c.gridx = 0;
//		    c.gridy = 0;
//		    pane.add(testone, c);
//		    c.fill = GridBagConstraints.HORIZONTAL;
//		    c.gridx = 0;
//		    c.gridy = 1;
//		    pane.add(testtwo, c);
		    mainFrame.dispose();
		};
		
    	
    }
    
    public static void main(String[] a) throws NoPlayerException, CannotRealizeException, IOException {
    	Enguage.init( Enguage.RW_SPACE );
    mainFrame = new LogInPanel();
    mainFrame.setTitle("Login Window");
    mainFrame.setVisible(true);
    mainFrame.setBounds(10, 10, 600, 600);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setResizable(false);
    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (int) ((dimension.getWidth() - mainFrame.getWidth()) / 2);
    int y = (int) ((dimension.getHeight() - mainFrame.getHeight()) / 2);
    mainFrame.setLocation(x, y);
   
}
 
    public class ButtonAction extends AbstractAction {
		static final long serialVersionUID = 0L;
		public ButtonAction( String text, ImageIcon icon,
							 String desc, Integer mnemonic ) {
			super(text, icon);
			putValue( SHORT_DESCRIPTION, desc );
			putValue( MNEMONIC_KEY, mnemonic );
		}
		public void actionPerformed(ActionEvent e) {
			String uid= "swing"; // see ./var/swing
			Strings utterance = new Strings( inputCommand.getText() );
			Strings reply =  Enguage.mediate( uid, utterance );
			replyField.setText( reply.toString() );
			try {
				Logon();
			} catch (NoPlayerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (CannotRealizeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
	}	}
}

 
