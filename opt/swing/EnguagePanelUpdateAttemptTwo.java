package opt.swing;
import javax.swing.*;

import org.enguage.Enguage;
import org.enguage.objects.Variable;
import org.enguage.util.Strings;

import opt.swing.EnguagePanel.ButtonAction;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
 
public class EnguagePanelUpdateAttemptTwo extends JFrame implements ActionListener {
 
    Container container = getContentPane();
    JLabel userLabel = new JLabel("USERNAME");
    JLabel passwordLabel = new JLabel("PASSWORD");
    JTextField userTextField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("LOGIN");
    JButton resetButton = new JButton("RESET");
    JCheckBox showPassword = new JCheckBox("Show Password");
    JTextField inputCommand = new JTextField("");
    JTextField replyField = new JTextField("");
    JButton b = new JButton();
    protected Action buttonAction;
 
 
    EnguagePanelUpdateAttemptTwo() {
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
		
		inputCommand.setFont( font1 );
		replyField.setFont(font1);
		replyField.setEditable(false);
		buttonAction = new ButtonAction( "Confirm Command", null,
				"Click to interpret what you type above",
				KeyEvent.VK_M );
		b = new JButton( buttonAction);
		if (b.getIcon() != null) b.setText( "" );
		b.setFont( font1 );
		

        userLabel.setBounds(50, 150, 100, 30);
        passwordLabel.setBounds(50, 220, 100, 30);
        userTextField.setBounds(150, 150, 150, 30);
        passwordField.setBounds(150, 220, 150, 30);
        showPassword.setBounds(150, 250, 150, 30);
        loginButton.setBounds(50, 300, 100, 30);
        resetButton.setBounds(200, 300, 100, 30);
        inputCommand.setBounds(10, 10, 300, 30);
        replyField.setBounds(10, 400, 300, 30);
        b.setBounds(10, 60, 300, 30);
 
 
    }
 
    public void addComponentsToContainer() {
        container.add(userLabel);
        container.add(passwordLabel);
        container.add(userTextField);
        container.add(passwordField);
        container.add(showPassword);
        container.add(loginButton);
        container.add(resetButton);
        container.add(inputCommand);
        container.add(replyField);
        container.add(b);
    }
 
    public void addActionEvent() {
        loginButton.addActionListener(this);
        resetButton.addActionListener(this);
        showPassword.addActionListener(this);
        b.addActionListener(buttonAction);
    }
 
 
    @Override
    public void actionPerformed(ActionEvent e) {
        //Coding Part of LOGIN button
        if (e.getSource() == loginButton) {
            String userText;
            String pwdText;
            userText = userTextField.getText();
            pwdText = passwordField.getPassword().toString();
            if (userText.equalsIgnoreCase("mehtab") && pwdText.equalsIgnoreCase("12345")) {
                JOptionPane.showMessageDialog(this, "Login Successful");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
 
        }
        //Coding Part of RESET button
        if (e.getSource() == resetButton) {
            userTextField.setText("");
            passwordField.setText("");
        }
       //Coding Part of showPassword JCheckBox
        if (e.getSource() == showPassword) {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
 
 
        }
    }
    
    public static void Logon() {
    	
		
		String user = Variable.get( "USER" );
		     if (user == null); // ignore a null return
		else if (user.equalsIgnoreCase(  "admin-one" )) {
			
		};
		
    	
    }
    
    public static void main(String[] a) {
    EnguagePanelUpdateAttemptTwo frame = new EnguagePanelUpdateAttemptTwo();
    frame.setTitle("Login Window");
    frame.setVisible(true);
    frame.setBounds(10, 10, 370, 600);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
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
//			t2.setBackground(yellow 
//				//whatColourShouldIbe( t2.getBackground() )
//			);
	}	}
}

 
