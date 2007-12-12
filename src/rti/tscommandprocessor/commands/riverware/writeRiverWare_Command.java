//------------------------------------------------------------------------------
// writeRiverWare_Command - handle the writeRiverWare() command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-05-31	Steven A. Malers, RTi	Initial version.  Copy and modify
//					sortTimeSeries().
// 2005-06-01	SAM, RTi		Add Precision parameter.
// 2005-08-30	SAM, RTi		Check for CreateOutput property in the
//					processor when running.
// 2005-05-24	SAM, RTi		Check for OutputStart and OutputEnd
//					when running - was not recognizing the
//					global output period.
// 2007-02-11	SAM, RTi		Remove direct dependency on TSCommandProcessor.
//					Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package rti.tscommandprocessor.commands.riverware;

import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

// FIXME SAM 2007-08-30 Need to move RiverWare to its own DAO package
import RTi.TS.RiverWareTS;
import RTi.TS.TS;

import RTi.Util.IO.AbstractCommand;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the writeRiverWare() command.
</p>
*/
public class writeRiverWare_Command extends AbstractCommand implements Command, FileGenerator
{

/**
Protected data members shared with the dialog and other related classes.
*/
protected final String _AllTS = "AllTS";
protected final String _SelectedTS = "SelectedTS";
protected final String _AllMatchingTSID = "AllMatchingTSID";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public writeRiverWare_Command ()
{	super();
	setCommandName ( "WriteRiverWare" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor
dialogs).
*/
public void checkCommandParameters (	PropList parameters, String command_tag,
					int warning_level )
throws InvalidCommandParameterException
{	String OutputFile = parameters.getValue ( "OutputFile" );
	//String Units = parameters.getValue ( "Units" );
	String Scale = parameters.getValue ( "Scale" );
	//String SetUnits = parameters.getValue ( "SetUnits" );
	String SetScale = parameters.getValue ( "SetScale" );
	String Precision = parameters.getValue ( "Precision" );
	String warning = "";
	String routine = getCommandName() + ".checkCommandParameters";

	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	String message;
	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
		message = "The output file: \"" + OutputFile + "\" must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify an output file." ) );
	}
	else {
        String working_dir = null;
		try { Object o = processor.getPropContents ( "WorkingDir" );
			if ( o != null ) {
				working_dir = (String)o;
			}
		}
		catch ( Exception e ) {
			message = "Error requesting WorkingDir from processor.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Software error - report problem to support." ) );
		}

		try {
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir, OutputFile));
			File f = new File ( adjusted_path );
			File f2 = new File ( f.getParent() );
			if ( !f2.exists() ) {
				message = "The output file parent directory does " +
					"not exist: \"" + adjusted_path + "\".";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Create output directory." ) );
			}
			f = null;
			f2 = null;
		}
		catch ( Exception e ) {
			message = "The output file:\n" +
				"    \"" + OutputFile +
				"\"\ncannot be adjusted using the working directory:\n" +
				"    \"" + working_dir + "\".";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify that output file and working directory paths are compatible." ) );
		}
	}

	/* TODO SAM 2005-05-31 Might need to check units against global
	list - right now units are not universally choosable.
	if ( (Units != null) && !Units.equals("") ) {
	}
	*/
	if ( (Scale != null) && !Scale.equals("") ) {
		if ( !StringUtil.isDouble(Scale) ) {
			message = "The scale: \"" + Scale + "\" is not a number.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a number > 0 for the scale." ) );

		}
		else if ( StringUtil.atod(Scale) <= 0 ) {
			message = "The scale: \"" + Scale +	"\" must be > 0.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a scale > 0." ) );
			
		}
	}
	/* TODO SAM 2005-05-31 Might need to check units against global
	list - right now units are not universally choosable.
	if ( (SetUnits != null) && !SetUnits.equals("") ) {
	}
	*/
	if ( (SetScale != null) && !SetScale.equals("") ) {
		if ( !StringUtil.isDouble(SetScale) ) {
			message = "The set_scale: \"" + SetScale + "\" is not a number.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a set scale as a number > 0." ) );
		}
		else if ( StringUtil.atod(SetScale) <= 0 ) {
			message = "The set_scale: \"" + SetScale + "\" must be > 0.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a set scale > 0." ) );
		}
	}
	if ( (Precision != null) && !Precision.equals("") ) {
		if ( !StringUtil.isInteger(Precision) ) {
			message = "The precision: \"" + Precision +	"\" is not an integer.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify the precision as an integer." ) );
		}
		else if ( StringUtil.atoi(Precision) < 0 ) {
			message = "The precision: \"" + Precision + "\" must be >= 0.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a precision >= 0." ) );
		}
	}
	// Check for invalid parameters...
	Vector valid_Vector = new Vector();
	valid_Vector.add ( "TSList" );
	valid_Vector.add ( "TSID" );
	valid_Vector.add ( "OutputFile" );
	valid_Vector.add ( "Units" );
	valid_Vector.add ( "Scale" );
	valid_Vector.add ( "Set_scale" );
	valid_Vector.add ( "Precision" );
	valid_Vector.add ( "Set_units" );
	Vector warning_Vector = null;
	try {	warning_Vector = parameters.validatePropNames (
			valid_Vector, null, null, "parameter" );
	}
	catch ( Exception e ) {
		// Ignore.  Should not happen.
		warning_Vector = null;
	}
	if ( warning_Vector != null ) {
		int size = warning_Vector.size();
		StringBuffer b = new StringBuffer();
		for ( int i = 0; i < size; i++ ) {
			warning += "\n" + (String)warning_Vector.elementAt (i);
			b.append ( (String)warning_Vector.elementAt(i));
		}
		status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.WARNING,
					b.toString(),
					"Specify only valid parameters - see documentation."));
	}
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
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new writeRiverWare_JDialog ( parent, this )).ok();
}

/**
Return the list of files that were created by this command.
*/
public List getGeneratedFileList ()
{
	Vector list = new Vector();
	if ( getOutputFile() != null ) {
		list.addElement ( getOutputFile() );
	}
	return list;
}

/**
Return the output file generated by this file.  This method is used internally.
*/
private File getOutputFile ()
{
	return __OutputFile_File;
}

/**
Parse the command string into a PropList of parameters.
@param command_string A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String command_string )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	String routine = "writeRiverWare_Command.parseCommand", message;
	int warning_level = 2;
	if ( command_string.indexOf("=") > 0 ) {
		// New syntax...
		super.parseCommand ( command_string );
	}
	else {	// Parse the old command...
		Vector tokens = StringUtil.breakStringList ( command_string,
			"(,)", StringUtil.DELIM_ALLOW_STRINGS );
		if ( tokens.size() != 6 ) {
			message =
			"Invalid syntax for command.  Expecting 5 parameters.";
			Message.printWarning ( warning_level, routine, message);
			throw new InvalidCommandSyntaxException ( message );
		}
		String OutputFile = ((String)tokens.elementAt(1)).trim();
		String Units = ((String)tokens.elementAt(2)).trim();
		if ( Units.equals("*") ) {
			Units = "";
		}
		String Scale = ((String)tokens.elementAt(3)).trim();
		String SetUnits = ((String)tokens.elementAt(4)).trim();
		if ( SetUnits.equals("*") ) {
			SetUnits = "";
		}
		String SetScale = ((String)tokens.elementAt(5)).trim();
		// Defaults because not in the old command...
		String TSList = "AllTS";
		PropList parameters = new PropList ( getCommandName() );
		parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
		parameters.set ( "TSList", TSList );
		if ( OutputFile.length() > 0 ) {
			parameters.set ( "OutputFile", OutputFile );
		}
		if ( Units.length() > 0 ) {
			parameters.set("Units", Units);
		}
		if ( Scale.length() > 0 ) {
			parameters.set ( "Scale", Scale );
		}
		if ( SetUnits.length() > 0 ) {
			parameters.set ( "SetUnits", SetUnits );
		}
		if ( SetScale.length() > 0 ) {
			parameters.set ( "SetScale", SetScale );
		}
		parameters.setHowSet ( Prop.SET_UNKNOWN );
		setCommandParameters ( parameters );
	}
}

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "writeRiverWare_Command.runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
	// Clear the output file
	
	setOutputFile ( null );

	// Check whether the processor wants output files to be created...

	CommandProcessor processor = getCommandProcessor();
	if ( !TSCommandProcessorUtil.getCreateOutput(processor) ) {
			Message.printStatus ( 2, routine,
			"Skipping \"" + toString() + "\" because output is not being created." );
	}

	PropList parameters = getCommandParameters();
	String TSList = parameters.getValue ( "TSList" );
	String TSID = parameters.getValue ( "TSID" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);

	// Get the time series to process...
	PropList request_params = new PropList ( "" );
	request_params.set ( "TSList", TSList );
	if ( TSList == null ) {
		TSList = _AllTS;
	}
	request_params.set ( "TSID", TSID );
	CommandProcessorRequestResultsBean bean = null;
	try { bean =
		processor.processRequest( "GetTimeSeriesToProcess", request_params);
	}
	catch ( Exception e ) {
		message = "Error requesting GetTimeSeriesToProcess(TSList=\"" + TSList +
		"\", TSID=\"" + TSID + "\" from processor.";
		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report problem to software support." ) );
	}
	PropList bean_PropList = bean.getResultsPropList();
	Object o_TSList = bean_PropList.getContents ( "TSToProcessList" );
	if ( o_TSList == null ) {
		message = "Unable to find time series to write using TSList=\"" + TSList +
		"\" TSID=\"" + TSID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report problem to software support." ) );
	}
	Vector tslist = (Vector)o_TSList;
	if ( tslist.size() == 0 ) {
		message = "Unable to find time series to write using TSList=\"" + TSList +
		"\" TSID=\"" + TSID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.WARNING,
						message, "Confirm that time series are available (may be OK for partial run)." ) );
	}

	// TODO SAM 2007-02-12 Need to enable OutputStart, OutputEnd in command
	String OutputStart = null;
	DateTime OutputStart_DateTime = null;
	if ( (OutputStart != null) && !OutputStart.equals("") ) {
		request_params = new PropList ( "" );
		request_params.set ( "DateTime", OutputStart );
		try { bean =
			processor.processRequest( "DateTime", request_params);
		}
		catch ( Exception e ) {
			message = "Error requesting DateTime(DateTime=" + OutputStart + ") from processor.";
			Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
			status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Report problem to software support." ) );
		}
		bean_PropList = bean.getResultsPropList();
		Object prop_contents = bean_PropList.getContents ( "DateTime" );
		if ( prop_contents == null ) {
			message = "Null value for DateTime(DateTime=" + OutputStart +
				"\") returned from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Report problem to software support." ) );
		}
		else {	OutputStart_DateTime = (DateTime)prop_contents;
		}
	}
	else {	// Get from the processor (can be null)...
		try {	Object o_OutputStart = processor.getPropContents ( "OutputStart" );
			if ( o_OutputStart != null ) {
				OutputStart_DateTime = (DateTime)o_OutputStart;
			}
		}
		catch ( Exception e ) {
			// Not fatal, but of use to developers.
			message = "Error requesting OutputStart from processor - not using.";
			Message.printDebug(10, routine, message );
		}
	}
	String OutputEnd = null;
	DateTime OutputEnd_DateTime = null;
	if ( (OutputEnd != null) && !OutputEnd.equals("") ) {
		request_params = new PropList ( "" );
		request_params.set ( "DateTime", OutputEnd );
		try { bean =
			processor.processRequest( "DateTime", request_params);
		}
		catch ( Exception e ) {
			message = "Error requesting DateTime(DateTime=" + OutputEnd + ") from processor.";
			Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
			status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Report problem to software support." ) );
		}
		bean_PropList = bean.getResultsPropList();
		Object prop_contents = bean_PropList.getContents ( "DateTime" );
		if ( prop_contents == null ) {
			message = "Null value for DateTime(DateTime=" + OutputEnd +
			"\") returned from processor.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Report problem to software support." ) );
		}
		else {	OutputEnd_DateTime = (DateTime)prop_contents;
		}
	}
	else {	// Get from the processor...
		try {	Object o_OutputEnd = processor.getPropContents ( "OutputEnd" );
			if ( o_OutputEnd != null ) {
				OutputEnd_DateTime = (DateTime)o_OutputEnd;
			}
		}
		catch ( Exception e ) {
			// Not fatal, but of use to developers.
			message = "Error requesting OutputEnd from processor - not using.";
			Message.printDebug(10, routine, message );
		}
	}

	if ( warning_count > 0 ) {
		message = "Error preparing data to write RiverWare file.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new CommandException ( message );
	}

	// Now try to write...

	try {
		// Convert to an absolute path...
		String OutputFile_full = IOUtil.verifyPathForOS(
                IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),OutputFile) );
		Message.printStatus ( 2, routine, "Writing RiverWare file \"" + OutputFile_full + "\"" );
		// Only write the first time series...
		TS tsout = (TS)tslist.elementAt(0);
		// Don't pass units to below...
		RiverWareTS.writeTimeSeries ( tsout, OutputFile_full,
			OutputStart_DateTime,
			OutputEnd_DateTime, parameters, true );
		// Save the output file name...
		setOutputFile ( new File(OutputFile_full));
	}
	catch ( Exception e ) {
		message = "Error writing time series to RiverWare file.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "See log file for details." ) );
		throw new CommandException ( message );
	}
}

/**
Set the output file that is created by this command.  This is only used internally.
*/
private void setOutputFile ( File file )
{
	__OutputFile_File = file;
}

/**
Return the string representation of the command.
@param parameters Command parameters as strings.
*/
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	String TSList = parameters.getValue("TSList");
	String TSID = parameters.getValue("TSID");
	String OutputFile = parameters.getValue("OutputFile");
	String Units = parameters.getValue("Units");
	String Scale = parameters.getValue("Scale");
	String SetUnits = parameters.getValue("SetUnits");
	String SetScale = parameters.getValue("SetScale");
	String Precision = parameters.getValue("Precision");
	StringBuffer b = new StringBuffer ();
	if ( (TSList != null) && (TSList.length() > 0) ) {
		b.append ( "TSList=" + TSList );
	}
	if ( (TSID != null) && (TSID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TSID=\"" + TSID + "\"" );
	}
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
	if ( (Units != null) && (Units.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Units=\"" + Units + "\"" );
	}
	if ( (Scale != null) && (Scale.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Scale=" + Scale );
	}
	if ( (SetUnits != null) && (SetUnits.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetUnits=\"" + SetUnits + "\"" );
	}
	if ( (SetScale != null) && (SetScale.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SetScale=" + SetScale );
	}
	if ( (Precision != null) && (Precision.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Precision=" + Precision );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}
