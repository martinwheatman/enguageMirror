package opt.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;

import javax.swing.JFrame;

public class MainWindow {
	
	public class mediaPlayer extends JFrame
    {
        public mediaPlayer()
        {
            setLayout(new BorderLayout());

            //file you want to play
            URL mediaURL = Player mediaPlayer = Manager.createRealizedPlayer(mediaURL);
            //get components for video and playback controls
            Component video = mediaPlayer.getVisualComponent();
            Component controls = mediaPlayer.getControlPanelComponent();
            add(video,BorderLayout.CENTER);
            add(controls,BorderLayout.SOUTH);
        }
    }


}
