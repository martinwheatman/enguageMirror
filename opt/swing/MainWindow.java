package opt.swing;


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
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.enguage.Enguage;
import org.enguage.objects.Variable;
import org.enguage.util.Strings;

import opt.swing.LogInPanel.ButtonAction;


@SuppressWarnings("serial")
public class MainWindow extends JFrame implements ActionListener {
	 Container container = getContentPane();
	 JPanel pane = new JPanel(new GridBagLayout());
	 GridBagConstraints c = new GridBagConstraints();
	 JTextField inputCommand = new JTextField();
	 JButton confirmCommandButton = new JButton();
	 JTextField replyField = new JTextField();
	 GridBagLayout layout = new GridBagLayout(); 
	 JTable table = new JTable();
	 JScrollPane scrollPane = new JScrollPane();
	 ButtonAction buttonAction;
	 @SuppressWarnings("rawtypes")
	JComboBox screens = new JComboBox();
	 
	 String User;
	
	@SuppressWarnings({"rawtypes", "unchecked" })
	public MainWindow(String user) {
        setLayoutManager();
        User=user;
        JLabel loggedInLabel = new JLabel("You are logged in as user"+ User);
        this.setTitle("Main Window");
	    this.setVisible(true);
	    this.setBounds(100, 100, 1000, 600);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setResizable(false);
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
	    this.setLocation(x, y);
	    table = createTable();
	    buttonAction = new ButtonAction( "Confirm Command", null,
				"Click to interpret what you type above",
				KeyEvent.VK_M );
		confirmCommandButton = new JButton( buttonAction);
	   
		String[] screenList = { "No Screen Selected", "WideBand", "Narrowband"};

		
		screens = new JComboBox(screenList);
		screens.setSelectedIndex(0);
		
		screens.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	screens = (JComboBox)e.getSource();
		     //   String petName = (String)screens.getSelectedItem();
		        //updateLabel(petName);
		    }
		});
		
		
		c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 0;
	    c.gridy = 0;
	    c.gridwidth=2;
	    this.add(loggedInLabel, c);
	   // c.fill = GridBagConstraints.HORIZONTAL;
	    c.anchor =GridBagConstraints.WEST;
	    c.gridx = 0;
	    c.gridy = 1;
	    c.gridwidth=1;
	    this.add(inputCommand, c);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 0;
	    c.gridy = 2;
	    c.gridwidth=1;
	    this.add(confirmCommandButton, c);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 0;
	    c.gridy = 3;
	    c.gridwidth=1;
	    this.add(replyField,c);
	 //   c.fill = GridBagConstraints.VERTICAL;
	    c.anchor =GridBagConstraints.EAST;
	    c.gridx = 1;
	    c.gridy = 1;
	    c.gridwidth=1;
	    this.add(screens,c);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx=0;
	    c.gridy=4;
	    c.gridwidth=2;
	    c.anchor = GridBagConstraints.SOUTHWEST;
	    c.weightx = 0.25;
	    c.weighty = 0.25;
	   this.add(scrollPane,c);
	    
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
       


  
   private JTable createTable () {
	   String[] columnNames = {"Object ID",
               "Classification",
               "Distance away (m)",
               };
	   Object[][] tableData = {
			    {"0001", "Submarine",
			     new Integer(500)},
			    {"0002", "Submarine",
				     new Integer(1000)},
			    {"0003", "Unknown",
			     new Integer(600)},
			};
	   JTable tableToFilter = new JTable(tableData, columnNames);
	   scrollPane = new JScrollPane(tableToFilter);
	//    scrollPane.setBounds(36, 37, 407, 79);
	 //   getContentPane().add(scrollPane);

	return tableToFilter;
	   
   }



		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			confirmCommandButton.addActionListener(buttonAction);
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
			}
    


}
		
		public void switchScreen() {

			String screen = Variable.get( "SCREEN" );
			     if (screen == null); // ignore a null return
			else if (screen.equals(  "narrowband" )) {
			screens.setSelectedIndex(2);
			
			}
			else if (screen.equals(  "wideband" )) {
				screens.setSelectedIndex(1);
				};
		}
		
}
