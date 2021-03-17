package opt.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.enguage.Enguage;
import org.enguage.util.Strings;

public class EnguagePanel extends JPanel {
	static final long serialVersionUID = 0L;
	
	static JFrame frame;
	protected JTextField t1, t2;
	protected JButton b;
	protected Action buttonAction;
 
	public EnguagePanel() {
		super(new BorderLayout());
 
		buttonAction = new ButtonAction( "Say", null,
							"Click to interpret what you type above",
							KeyEvent.VK_M );
		
		Font font1 = new Font("SansSerif", Font.PLAIN, 30);
		
		t1 = new JTextField("");
		t1.setFont( font1 );
		JPanel utterancePanel = new JPanel( new GridLayout( 1, 1 ));
		utterancePanel.add( t1 );

		b = new JButton( buttonAction );
		if (b.getIcon() != null) b.setText( "" );
		b.setFont( font1 );
		JPanel buttonPanel = new JPanel( new GridLayout( 1, 1 ));
		buttonPanel.add( b );

		t2 = new JTextField("Reply");
		t2.setEditable( false );
		t2.setFont( font1 );
		JPanel textFieldPanel = new JPanel( new GridLayout(1,1));
		textFieldPanel.add( t2 );
 
		add( utterancePanel, BorderLayout.PAGE_START );
		add( buttonPanel,	BorderLayout.CENTER	 );
		add( textFieldPanel, BorderLayout.PAGE_END   );
		setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));
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
			Strings utterance = new Strings( t1.getText() );
			Strings reply =  Enguage.mediate( uid, utterance );
			t2.setText( reply.toString() );
//			if (t2.getBackground() == Color.LIGHT_GRAY)
//				t2.setBackground( Color.YELLOW );
//			else
//				t2.setBackground( Color.LIGHT_GRAY );
	}	}
	
	private static void createAndShowGUI() {
		JComponent pane = new EnguagePanel();
		pane.setOpaque( true );
		
		frame = new JFrame("Enguage Panel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane( pane );
		frame.pack();
		frame.setSize( 800, 150 );
		frame.setVisible( true );
	}
	public static void main(String[] args) {
		
		Enguage.init( Enguage.RW_SPACE );
		
		javax.swing.SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
}   }
