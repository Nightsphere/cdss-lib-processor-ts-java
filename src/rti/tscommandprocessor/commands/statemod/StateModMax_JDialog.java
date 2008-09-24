package rti.tscommandprocessor.commands.statemod;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.IO.Command;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

public class StateModMax_JDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
    
private final String __AddWorkingDirectory1 = "Add Working Directory (File 1)";
private final String __AddWorkingDirectory2 = "Add Working Directory (File 2)";
private final String __RemoveWorkingDirectory1 = "Remove Working Directory (File 1)";
private final String __RemoveWorkingDirectory2 = "Remove Working Directory (File 2)";
    
private SimpleJButton	__browse1_JButton = null,	// File browse button
			__browse2_JButton = null,	// Second file browse button
			__cancel_JButton = null,	// Cancel Button
			__ok_JButton = null,		// Ok Button
			__path1_JButton = null,		// Buttons to convert relative/
			__path2_JButton = null;		// absolute path.
private StateModMax_Command __command = null;
private JTextArea __command_JTextArea=null;
private JTextField __InputFile1_JTextField = null;
private String __working_dir = null;		// Working directory.
private JTextField __InputFile2_JTextField = null;
private boolean __error_wait = false;	// Is there an error to be cleared up?
private boolean __first_time = true;
private boolean __ok = false; // Whether OK was pressed when closing the dialog.

/**
Command editor constructor.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
public StateModMax_JDialog ( JFrame parent, Command command )
{	super(parent, true);
	initialize ( parent, command );
}

/**
Responds to ActionEvents.
@param event ActionEvent object
*/
public void actionPerformed( ActionEvent event )
{	Object o = event.getSource();

	if ( o == __browse1_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {	fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle( "Select StateMod Time Series File (File 1)");
		// REVISIT - maybe need to list all recognized StateMod file
		// extensions for data sets.
		SimpleFileFilter sff = new SimpleFileFilter("stm", "StateMod Time Series File");
		fc.addChoosableFileFilter(sff);
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName(); 
			String path = fc.getSelectedFile().getPath(); 
	
			if (filename == null || filename.equals("")) {
				return;
			}
	
			if (path != null) {
				__InputFile1_JTextField.setText(path );
				JGUIUtil.setLastFileDialogDirectory(directory );
				refresh();
			}
		}
	}
	else if ( o == __browse2_JButton ) {
		String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
		JFileChooser fc = null;
		if ( last_directory_selected != null ) {
			fc = JFileChooserFactory.createJFileChooser( last_directory_selected );
		}
		else {
		    fc = JFileChooserFactory.createJFileChooser( __working_dir );
		}
		fc.setDialogTitle( "Select StateMod Time Series File (File 2)");
		// TODO - maybe need to list all recognized StateMod file
		// extensions for data sets.
		SimpleFileFilter sff = new SimpleFileFilter("stm","StateMod Time Series File");
		fc.addChoosableFileFilter(sff);
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String directory = fc.getSelectedFile().getParent();
			String filename = fc.getSelectedFile().getName(); 
			String path = fc.getSelectedFile().getPath(); 
	
			if (filename == null || filename.equals("")) {
				return;
			}
	
			if (path != null) {
				__InputFile2_JTextField.setText(path );
				JGUIUtil.setLastFileDialogDirectory(directory);
				refresh();
			}
		}
	}
	else if ( o == __cancel_JButton ) {
		response ( false );
	}
	else if ( o == __ok_JButton ) {
		refresh ();
		if ( !__error_wait ) {
			response ( true );
		}
	}
	else if ( o == __path1_JButton ) {
		if ( __path1_JButton.getText().equals( __AddWorkingDirectory1) ) {
			__InputFile1_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __InputFile1_JTextField.getText() ) );
		}
		else if ( __path1_JButton.getText().equals(	__RemoveWorkingDirectory1) ) {
			try {	__InputFile1_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir, __InputFile1_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"StateModMax_JDialog",
				"Error converting file to relative path." );
			}
		}
		refresh ();
	}
	else if ( o == __path2_JButton ) {
		if ( __path2_JButton.getText().equals( __AddWorkingDirectory2 ) ) {
			__InputFile2_JTextField.setText (
			IOUtil.toAbsolutePath(__working_dir, __InputFile2_JTextField.getText() ) );
		}
		else if ( __path2_JButton.getText().equals( __RemoveWorkingDirectory2) ) {
			try {
			    __InputFile2_JTextField.setText (
				IOUtil.toRelativePath ( __working_dir, __InputFile2_JTextField.getText() ) );
			}
			catch ( Exception e ) {
				Message.printWarning ( 1,"StateModMax_JDialog",
				"Error converting file to relative path." );
			}
		}
		refresh ();
	}
}

/**
Check the input.  If errors exist, warn the user and set the __error_wait flag
to true.  This should be called before response() is allowed to complete.
*/
private void checkInput ()
{   // Put together a list of parameters to check...
    PropList props = new PropList ( "" );
    String InputFile1 = __InputFile1_JTextField.getText().trim();
    String InputFile2 = __InputFile2_JTextField.getText().trim();
    __error_wait = false;
    if ( InputFile1.length() > 0 ) {
        props.set ( "InputFile1", InputFile1 );
    }
    if ( InputFile2.length() > 0 ) {
        props.set ( "InputFile2", InputFile2 );
    }
    try {   // This will warn the user...
        __command.checkCommandParameters ( props, null, 1 );
    }
    catch ( Exception e ) {
        // The warning would have been printed in the check code.
        __error_wait = true;
    }
}

/**
Commit the edits to the command.  In this case the command parameters have
already been checked and no errors were detected.
*/
private void commitEdits ()
{   String InputFile1 = __InputFile1_JTextField.getText().trim();
    String InputFile2 = __InputFile2_JTextField.getText().trim();
    __command.setCommandParameter ( "InputFile1", InputFile1 );
    __command.setCommandParameter ( "InputFile2", InputFile2 );
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	__browse1_JButton = null;
	__browse2_JButton = null;
	__cancel_JButton = null;
	__command_JTextArea = null;
	__InputFile1_JTextField = null;
	__InputFile2_JTextField = null;
	__command = null;
	__ok_JButton = null;
	__path1_JButton = null;
	__path2_JButton = null;
	super.finalize ();
}

/**
Instantiates the GUI components.
@param parent JFrame class instantiating this class.
@param command Command to edit.
*/
private void initialize ( JFrame parent, Command command )
{	__command = (StateModMax_Command)command;
	__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( (TSCommandProcessor)__command.getCommandProcessor(), command );

	addWindowListener( this );

    Insets insetsTLBR = new Insets(2,2,2,2);

	JPanel main_JPanel = new JPanel();
	main_JPanel.setLayout( new GridBagLayout() );
	getContentPane().add ( "North", main_JPanel );
	int y = 0;

    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Create a list of time series where each time series" +
		" contains the maximum values for same-identifier time series from two StateMod files."),
		0, y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Typically all results are then written with other commands."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"This command is useful when computing StateMod demands as the"+
		" maximum of historical diversions and (irrigation water requirement)/efficiency."),
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
    JGUIUtil.addComponent(main_JPanel, new JLabel (
		"Specify a full or relative path (relative to working directory)." ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	if ( __working_dir != null ) {
        JGUIUtil.addComponent(main_JPanel, new JLabel ( "The working directory is: " + __working_dir ), 
		0, ++y, 7, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	}

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "First StateMod file to read:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile1_JTextField = new JTextField ( 50 );
	__InputFile1_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InputFile1_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse1_JButton = new SimpleJButton ( "Browse", this );
    JGUIUtil.addComponent(main_JPanel, __browse1_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Second StateMod file to read:" ), 
		0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	__InputFile2_JTextField = new JTextField ( 50 );
	__InputFile2_JTextField.addKeyListener ( this );
    JGUIUtil.addComponent(main_JPanel, __InputFile2_JTextField,
		1, y, 5, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	__browse2_JButton = new SimpleJButton ( "Browse", this );
    JGUIUtil.addComponent(main_JPanel, __browse2_JButton,
		6, y, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:"),
        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
    __command_JTextArea = new JTextArea ( 4, 55 );
    __command_JTextArea.setLineWrap ( true );
    __command_JTextArea.setWrapStyleWord ( true );
    __command_JTextArea.setEditable ( false );
    JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
        1, y, 6, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	// Refresh the contents...
	refresh ();

	// South Panel: North
	JPanel button_JPanel = new JPanel();
	button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JGUIUtil.addComponent(main_JPanel, button_JPanel, 
		0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	if ( __working_dir != null ) {
		// Add the button to allow conversion to/from relative path...
		__path1_JButton = new SimpleJButton( __RemoveWorkingDirectory1, this);
		button_JPanel.add ( __path1_JButton );
		__path2_JButton = new SimpleJButton( __RemoveWorkingDirectory2, this);
		button_JPanel.add ( __path2_JButton );
	}
	__cancel_JButton = new SimpleJButton("Cancel", this);
	button_JPanel.add ( __cancel_JButton );
	__ok_JButton = new SimpleJButton( "OK", this);
	button_JPanel.add ( __ok_JButton );

    setTitle ( "Edit " + __command.getCommandName() + "() Command" );
    pack();
    JGUIUtil.center( this );
	refresh();	// Sets the _path_Button status
    super.setVisible( true );
}

/**
Respond to KeyEvents.
*/
public void keyPressed ( KeyEvent event )
{	int code = event.getKeyCode();

	if ( code == KeyEvent.VK_ENTER ) {
		refresh ();
		checkInput();
		if ( !__error_wait ) {
			response ( true );
		}
	}
}

public void keyReleased ( KeyEvent event )
{	refresh();
}

public void keyTyped ( KeyEvent event ) {;}

/**
Indicate if the user pressed OK (cancel otherwise).
@return true if the edits were committed, false if the user cancelled.
*/
public boolean ok ()
{   return __ok;
}

/**
Refresh the command from the other text field contents.
*/
private void refresh ()
{   String InputFile1 = "";
    String InputFile2 = "";
    PropList props = null;
    if ( __first_time ) {
        __first_time = false;
        // Get the parameters from the command...
        props = __command.getCommandParameters();
        InputFile1 = props.getValue ( "InputFile1" );
        InputFile2 = props.getValue ( "InputFile2" );
        if ( InputFile1 != null ) {
            __InputFile1_JTextField.setText ( InputFile1 );
        }
        if ( InputFile2 != null ) {
            __InputFile2_JTextField.setText ( InputFile2 );
        }
    }
    // Regardless, reset the command from the fields...
    InputFile1 = __InputFile1_JTextField.getText().trim();
    InputFile2 = __InputFile2_JTextField.getText().trim();
    props = new PropList ( __command.getCommandName() );
    props.add ( "InputFile1=" + InputFile1 );
    props.add ( "InputFile2=" + InputFile2 );
    __command_JTextArea.setText( __command.toString ( props ) );
    // Check the path and determine what the label on the path button should be...
    if ( __path1_JButton != null ) {
        __path1_JButton.setEnabled ( true );
        File f = new File ( InputFile1 );
        if ( f.isAbsolute() ) {
            __path1_JButton.setText ( __RemoveWorkingDirectory1 );
        }
        else {
            __path1_JButton.setText ( __AddWorkingDirectory1 );
        }
    }
    if ( __path2_JButton != null ) {
        __path2_JButton.setEnabled ( true );
        File f = new File ( InputFile2 );
        if ( f.isAbsolute() ) {
            __path2_JButton.setText ( __RemoveWorkingDirectory2 );
        }
        else {
            __path2_JButton.setText ( __AddWorkingDirectory2 );
        }
    }
}

/**
React to the user response.
@param ok if false, then the edit is cancelled.  If true, the edit is committed
and the dialog is closed.
*/
private void response ( boolean ok )
{   __ok = ok;  // Save to be returned by ok()
    if ( ok ) {
        // Commit the changes...
        commitEdits ();
        if ( __error_wait ) {
            // Not ready to close out!
            return;
        }
    }
    // Now close out...
    setVisible( false );
    dispose();
}

/**
Responds to WindowEvents.
@param event WindowEvent object
*/
public void windowClosing( WindowEvent event )
{	response ( false );
}

public void windowActivated( WindowEvent evt ){;}
public void windowClosed( WindowEvent evt ){;}
public void windowDeactivated( WindowEvent evt ){;}
public void windowDeiconified( WindowEvent evt ){;}
public void windowIconified( WindowEvent evt ){;}
public void windowOpened( WindowEvent evt ){;}

}