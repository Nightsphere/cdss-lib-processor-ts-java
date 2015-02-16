package rti.tscommandprocessor.commands.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the Wait() command.
*/
public class Wait_Command extends AbstractCommand implements Command
{

/**
Constructor.
*/
public Wait_Command ()
{	super();
	setCommandName ( "Wait" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String routine = getCommandName() + "_checkCommandParameters";
	String WaitTime = parameters.getValue ( "WaitTime" );
	String ProgressIncrement = parameters.getValue ( "ProgressIncrement" );
	String warning = "";
	String message;
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);

    if ( (WaitTime == null) || WaitTime.equals("") || !StringUtil.isDouble(WaitTime)) {
        message = "The wait time must be specified in seconds.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE, message, "Specify the wait time in seconds." ) );
    }
    
    if ( (ProgressIncrement != null) && !ProgressIncrement.equals("") ) {
    	if ( !StringUtil.isDouble(ProgressIncrement)) {
	        message = "The progress increment (" + ProgressIncrement + ") is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE, message, "Specify the progress increment in seconds." ) );
    	}
    }

	// Check for invalid parameters...
    List<String> validList = new ArrayList<String>(1);
	validList.add ( "WaitTime" );
	validList.add ( "ProgressIncrement" );
	warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );
	
	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level), routine,
		warning );
		throw new InvalidCommandParameterException ( warning );
	}
	
	status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new Wait_JDialog ( parent, this )).ok();
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws CommandWarningException, CommandException, InvalidCommandParameterException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.RUN);
	PropList parameters = getCommandParameters();

	String WaitTime = parameters.getValue ( "WaitTime" );
	long waitTimeMs = (long)(Double.parseDouble(WaitTime)*1000);
	String ProgressIncrement = parameters.getValue ( "ProgressIncrement" );
	int progressIncrementMs = 0; 
	if ( (ProgressIncrement != null) && !ProgressIncrement.equals("") && StringUtil.isDouble(ProgressIncrement) ) {
		progressIncrementMs = (int)(Double.parseDouble(ProgressIncrement)*1000);
	}
	
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}
	
	try {
		long waitDelta = waitTimeMs/10; // Default
		// Change the delta to be the update if specified
		if ( progressIncrementMs > 0 ) {
			waitDelta = progressIncrementMs;
		}
		int numSteps = (int)(waitTimeMs/waitDelta);
		int i = 0;
		for ( long wait = 0; wait < waitTimeMs; wait += waitDelta, i++ ) {
			// Do a simple sleep
			Thread.sleep(waitDelta);
			message = "Waited " + wait/1000 + " seconds of " + WaitTime;
            notifyCommandProgressListeners ( i, numSteps, (float)-1.0, message );
		}
    }
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Error waiting (" + e + ").";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),	routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Should not occur." ) );
		throw new CommandException ( message );
	}
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{   if ( props == null ) {
        return getCommandName() + "()";
    }
    String WaitTime = props.getValue( "WaitTime" );
    String ProgressIncrement = props.getValue( "ProgressIncrement" );
    StringBuffer b = new StringBuffer ();
    if ( (WaitTime != null) && (WaitTime.length() > 0) ) {
        b.append ( "WaitTime=\"" + WaitTime + "\"" );
    }
    if ( (ProgressIncrement != null) && (ProgressIncrement.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
        b.append ( "ProgressIncrement=\"" + ProgressIncrement + "\"" );
    }
    return getCommandName() + "(" + b.toString() + ")";
}

}