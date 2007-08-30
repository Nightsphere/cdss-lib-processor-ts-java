//------------------------------------------------------------------------------
// fillHistYearAverage_Command - handle the fillHistYearAverage() command
//------------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
//------------------------------------------------------------------------------
// History:
//
// 2005-05-18	Steven A. Malers, RTi	Initial version.  Copy and modify
//					fillHistMonthYearAverage().
// 2005-05-19	SAM, RTi		Move from TSTool package to TS.
// 2007-02-16	SAM, RTi		Use new CommandProcessor interface.
//					Clean up code based on Eclipse feedback.
//------------------------------------------------------------------------------
// EndHeader

package rti.tscommandprocessor.commands.ts;

import javax.swing.JFrame;

import java.util.Vector;

import RTi.TS.TS;
import RTi.TS.TSLimits;
import RTi.TS.TSUtil;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.SkeletonCommand;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;

/**
<p>
This class initializes, checks, and runs the fillHistYearAverage() command.
</p>
<p>The CommandProcessor must return the following properties:  TSResultsList.
</p>
*/
public class fillHistYearAverage_Command extends SkeletonCommand
implements Command
{

/**
Protected data members shared with the dialog and other related classes.
*/
protected final String _AllTS = "AllTS";
protected final String _SelectedTS = "SelectedTS";
protected final String _AllMatchingTSID = "AllMatchingTSID";

/**
Constructor.
*/
public fillHistYearAverage_Command ()
{	super();
	setCommandName ( "fillHistYearAverage" );
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
{	String TSList = parameters.getValue ( "TSList" );
	String TSID = parameters.getValue ( "TSID" );
	String FillStart = parameters.getValue ( "FillStart" );
	String FillEnd = parameters.getValue ( "FillEnd" );
	String FillFlag = parameters.getValue ( "FillFlag" );
	String warning = "";
	if ( (TSList != null) && !TSList.equalsIgnoreCase(_AllMatchingTSID) ) {
		if ( TSID != null ) {
			warning +=
			"\nTSID should only be specified when TSList=" +
			_AllMatchingTSID + ".";
		}
	}
	if ( TSList == null ) {
		// Probably legacy command...
		// REVISIT SAM 2005-05-17 Need to require TSList when legacy
		// commands are safely nonexistent...  At that point the
		// following check can occur in any case.
		if ( (TSID == null) || (TSID.length() == 0) ) {
			warning += "\nA TSID must be specified.";
		}
	}
	if (	(FillStart != null) && !FillStart.equals("") &&
		!FillStart.equalsIgnoreCase("OutputStart")){
		try {	DateTime.parse(FillStart);
		}
		catch ( Exception e ) {
			warning += 
				"\nThe fill start date/time \"" + FillStart +
				"\" is not a valid date/time.\n"+
				"Specify a date or OutputStart.";
		}
	}
	if (	(FillEnd != null) && !FillEnd.equals("") &&
		!FillEnd.equalsIgnoreCase("OutputEnd") ) {
		try {	DateTime.parse( FillEnd);
		}
		catch ( Exception e ) {
			warning +=
				"\nThe fill end date/time \"" + FillEnd +
				"\" is not a valid date/time.\n"+
				"Specify a date or OutputEnd.";
		}
	}
	if ( (FillFlag != null) && (FillFlag.length() != 1) ) {
		warning += "\nThe fill flag must be 1 character long.";
	}
	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
		throw new InvalidCommandParameterException ( warning );
	}
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new fillHistYearAverage_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.  This method currently
supports old syntax and new parameter-based syntax.
@param command A string command to parse.
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2).
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand (	String command, String command_tag,
				int warning_level )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	int warning_count = 0;
	String routine = "fillHistYearAverage_Command.parseCommand", message;

	if ( command.indexOf('=') < 0 ) {
		// REVISIT SAM 2005-04-29 This whole block of code needs to be
		// removed as soon as commands have been migrated to the new
		// syntax.
		//
		// Old syntax where the only parameter is a single TSID or *
		// to fill all.
		Vector v = StringUtil.breakStringList(command,
			"(),\t", StringUtil.DELIM_SKIP_BLANKS |
			StringUtil.DELIM_ALLOW_STRINGS );
		int ntokens = 0;
		if ( v != null ) {
			ntokens = v.size();
		}
		if ( ntokens != 2 ) {
			message = "Syntax error in \"" + command +
			"\".  Two tokens expected.";
			Message.printWarning ( warning_level, routine, message);
			throw new InvalidCommandSyntaxException ( message );
		}

		// Get the individual tokens of the expression...

		String TSID = ((String)v.elementAt(1)).trim();

		// Set parameters and new defaults...

		_parameters = new PropList ( getCommandName() );
		_parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
		if ( TSID.length() > 0 ) {
			_parameters.set ( "TSID", TSID );
			_parameters.setHowSet(Prop.SET_AS_RUNTIME_DEFAULT);
			_parameters.set ( "TSList", _AllMatchingTSID );
		}
		_parameters.setHowSet ( Prop.SET_UNKNOWN );
	}

	else {	// Current syntax...
		Vector tokens = StringUtil.breakStringList ( command,
			"()", StringUtil.DELIM_SKIP_BLANKS );
		if ( (tokens == null) || tokens.size() < 2 ) {
			// Must have at least the command name, TSID, and
			// IndependentTSID...
			message = "Syntax error in \"" + command +
				"\".  Not enough tokens.";
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message);
			throw new InvalidCommandSyntaxException ( message );
		}
		// Get the input needed to process the file...
		try {	_parameters = PropList.parse ( Prop.SET_FROM_PERSISTENT,
				(String)tokens.elementAt(1), routine, "," );
		}
		catch ( Exception e ) {
			message = "Syntax error in \"" + command +
				"\".  Not enough tokens.";
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message);
			throw new InvalidCommandSyntaxException ( message );
		}
	}
}

/**
Run the commands:
<pre>
fillHistYearAverage(TSList=X,TSID="X",FillFlag="X")
</pre>
@param command_tag an indicator to be used when printing messages, to allow a
cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2).
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more
parameter values are invalid.
*/
public void runCommand ( String command_tag, int warning_level )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "fillHistYearAverage_Command.runCommand", message;
	int warning_count = 0;
	int log_level = 3;	// Warning level for non-user messages.

	// Make sure there are time series available to operate on...

	String TSList = _parameters.getValue ( "TSList" );
	String TSID = _parameters.getValue ( "TSID" );

	// Get the time series to process...
	
	PropList request_params = new PropList ( "" );
	request_params.set ( "TSList", TSList );
	request_params.set ( "TSID", TSID );
	CommandProcessorRequestResultsBean bean = null;
	try { bean =
		_processor.processRequest( "GetTimeSeriesToProcess", request_params);
	}
	catch ( Exception e ) {
		message = "Error requesting GetTimeSeriesToProcess(TSList=\"" + TSList +
		"\", TSID=\"" + TSID + "\" from processor.";
		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
	}
	PropList bean_PropList = bean.getResultsPropList();
	Object o_TSList = bean_PropList.getContents ( "TSToProcessList" );
	Vector tslist = null;
	if ( o_TSList == null ) {
		message = "Unable to find time series to fill using TSList=\"" + TSList +
		"\" TSID=\"" + TSID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
	}
	else {	tslist = (Vector)o_TSList;
		if ( tslist.size() == 0 ) {
			message = "Unable to find time series to fill using TSList=\"" + TSList +
			"\" TSID=\"" + TSID + "\".";
			Message.printWarning ( warning_level,
					MessageUtil.formatMessageTag(
							command_tag,++warning_count), routine, message );
		}
	}
	Object o_Indices = bean_PropList.getContents ( "Indices" );
	int [] tspos = null;
	if ( o_Indices == null ) {
		message = "Unable to find indices for time series to fill using TSList=\"" + TSList +
		"\" TSID=\"" + TSID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
	}
	else {	tspos = (int [])o_Indices;
		if ( tspos.length == 0 ) {
			message = "Unable to find indices for time series to fill using TSList=\"" + TSList +
			"\" TSID=\"" + TSID + "\".";
			Message.printWarning ( warning_level,
					MessageUtil.formatMessageTag(
							command_tag,++warning_count), routine, message );
		}
	}
	
	int nts = 0;
	if ( tslist != null ) {
		nts = tslist.size();
	}
	if ( nts == 0 ) {
		message = "Unable to find time series to fill using TSID \"" +
		TSID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
	}

	// Fill period...

	String FillStart = _parameters.getValue("FillStart");
	String FillEnd = _parameters.getValue("FillEnd");
	String FillFlag = _parameters.getValue("FillFlag");

	// Figure out the dates to use for the analysis...
	DateTime FillStart_DateTime = null;
	DateTime FillEnd_DateTime = null;

	try {
		if ( FillStart != null ) {
			request_params = new PropList ( "" );
			request_params.set ( "DateTime", FillStart );
			bean = null;
			try { bean =
				_processor.processRequest( "DateTime", request_params);
			}
			catch ( Exception e ) {
				message = "Error requesting FillStart DateTime(DateTime=" +
				FillStart + "\" from processor.";
				Message.printWarning(log_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
				throw new InvalidCommandParameterException ( message );
			}

			bean_PropList = bean.getResultsPropList();
			Object prop_contents = bean_PropList.getContents ( "DateTime" );
			if ( prop_contents == null ) {
				message = "Null value for FillStart DateTime(DateTime=" +
				FillStart +	"\") returned from processor.";
				Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				throw new InvalidCommandParameterException ( message );
			}
			else {	FillStart_DateTime = (DateTime)prop_contents;
			}
		}
		}
		catch ( Exception e ) {
			message = "FillStart \"" + FillStart + "\" is invalid.";
			Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
			throw new InvalidCommandParameterException ( message );
		}
		
		try {
		if ( FillEnd != null ) {
			request_params = new PropList ( "" );
			request_params.set ( "DateTime", FillEnd );
			bean = null;
			try { bean =
				_processor.processRequest( "DateTime", request_params);
			}
			catch ( Exception e ) {
				message = "Error requesting FillEnd DateTime(DateTime=" +
				FillEnd + "\" from processor.";
				Message.printWarning(log_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
				throw new InvalidCommandParameterException ( message );
			}

			bean_PropList = bean.getResultsPropList();
			Object prop_contents = bean_PropList.getContents ( "DateTime" );
			if ( prop_contents == null ) {
				message = "Null value for FillStart DateTime(DateTime=" +
				FillStart +	"\") returned from processor.";
				Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				throw new InvalidCommandParameterException ( message );
			}
			else {	FillEnd_DateTime = (DateTime)prop_contents;
			}
		}
		}
		catch ( Exception e ) {
			message = "FillEnd \"" + FillEnd + "\" is invalid.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			throw new InvalidCommandParameterException ( message );
		}

	if ( warning_count > 0 ) {
		// Input error (e.g., missing time series)...
		message = "Insufficient data to run command.";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	// Now process the time series...

	PropList props = new PropList ( "fillHistYearAverage" );
	if ( FillFlag != null ) {
		props.set ( "FillFlag", FillFlag );
	}
	props.set ( "DescriptionSuffix", ", fill w/ hist year ave" );

	TS ts = null;
	String nl = System.getProperty ( "line.separator" );
	for ( int its = 0; its < nts; its++ ) {
		ts = null;
		request_params = new PropList ( "" );
		request_params.setUsingObject ( "Index", new Integer(tspos[its]) );
		bean = null;
		try { bean =
			_processor.processRequest( "GetTimeSeries", request_params);
		}
		catch ( Exception e ) {
			Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, "Error requesting GetTimeSeries(Index=" + tspos[its] +
					"\" from processor." );
		}
		bean_PropList = bean.getResultsPropList();
		Object prop_contents = bean_PropList.getContents ( "TS" );
		if ( prop_contents == null ) {
			Message.printWarning(warning_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, "Null value for GetTimeSeries(Index=" + tspos[its] +
				"\") returned from processor." );
			// TODO SAM 2007-02-17 Evaluate whether user needs to know
			continue;
		}
		else {	ts = (TS)prop_contents;
		}
		
		if ( ts.getDataIntervalBase() != TimeInterval.YEAR) {
			message =
			"Filling with historic year average is only " +
			"implemented for yearly time series.  Skipping:\n" +
			ts.getIdentifier();
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
		}
		// Do the filling...
		Message.printStatus ( 2, routine, "Filling \"" +
		ts.getIdentifier()+"\" with yearly average." );
		try {	TSLimits average_limits = ts.getDataLimitsOriginal();
			// For log file...
			Message.printStatus ( 2, routine,
			"Historical averages for time series follow..." + nl +
			average_limits.toString () );
			// Now fill the time series...
			TSUtil.fillConstant ( ts,
					FillStart_DateTime,
					FillEnd_DateTime,
					average_limits.getMean(),
					props );
		}
		catch ( Exception e ) {
			message = "Unable to fill time series \""+
				ts.getIdentifier() +
				"\" with historical averages.";
			++warning_count;
			Message.printWarning(2,routine,message);
			Message.printWarning(3,routine,e);
		}
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag, ++warning_count),
			routine,message);
		throw new CommandWarningException ( message );
	}
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
	String TSList = props.getValue( "TSList" );
	String TSID = props.getValue( "TSID" );
	String FillStart = props.getValue("FillStart");
	String FillEnd = props.getValue("FillEnd");
	String FillFlag = props.getValue("FillFlag");
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
	if ( (FillStart != null) && (FillStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillStart=\"" + FillStart + "\"" );
	}
	if ( (FillEnd != null) && (FillEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillEnd=\"" + FillEnd + "\"" );
	}
	if ( (FillFlag != null) && (FillFlag.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "FillFlag=\"" + FillFlag + "\"" );
	}
	return getCommandName() + "(" + b.toString() + ")";
}

}
