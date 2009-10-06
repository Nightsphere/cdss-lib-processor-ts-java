package rti.tscommandprocessor.commands.ensemble;

import javax.swing.JFrame;

import java.util.List;
import java.util.Vector;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import RTi.TS.TS;
import RTi.TS.TSAnalyst;
import RTi.TS.TSEnsemble;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;

import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;

/**
<p>
This class initializes, checks, and runs the NewStatisticTimeSeriesFromEnsemble() command.
*/
public class NewStatisticTimeSeriesFromEnsemble_Command extends AbstractCommand
implements Command
{

/**
Constructor.
*/
public NewStatisticTimeSeriesFromEnsemble_Command ()
{	super();
	setCommandName ( "NewStatisticTimeSeriesFromEnsemble" );
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
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String Alias = parameters.getValue ( "Alias" );
	String EnsembleID = parameters.getValue ( "EnsembleID" );
	String Statistic = parameters.getValue ( "Statistic" );
	String TestValue = parameters.getValue ( "TestValue" );
	String AllowMissingCount = parameters.getValue ( "AllowMissingCount" );
	String AnalysisStart = parameters.getValue ( "AnalysisStart" );
	String AnalysisEnd = parameters.getValue ( "AnalysisEnd" );
	String SearchStart = parameters.getValue ( "SearchStart" );
	String warning = "";
    String message;
	
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);

	if ( (Alias == null) || Alias.equals("") ) {
		message = "The time series alias must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify an alias." ) );
	}
	if ( (EnsembleID == null) || EnsembleID.equals("") ) {
		message = "The time series ensemble identifier must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify an ensemble identifier." ) );
	}
	// TODO SAM 2005-08-29
	// Need to decide whether to check NewTSID - it might need to support
	// wildcards.
	if ( (Statistic == null) || Statistic.equals("") ) {
		message = "The statistic must be specified.";
		warning += "\n" + message;
		status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify a Statistic." ) );
	}
	if ( (TestValue != null) && !TestValue.equals("") ) {
		// If a test value is specified, for now make sure it is a
		// number.  It is possible that in the future it could be a
		// special value (date, etc.) but for now focus on numbers.
		if ( !StringUtil.isDouble(TestValue) ) {
			message = "The test value (" + TestValue + ") is not a number.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a number for the test value." ) );
		}
	}
	// TODO SAM 2005-09-12
	// Need to evaluate whether the test value is needed, depending on the
	// statistic
	if ( (AllowMissingCount != null) && !AllowMissingCount.equals("") ) {
		if ( !StringUtil.isInteger(AllowMissingCount) ) {
			message = "The AllowMissingCount value (" + AllowMissingCount + ") is not an integer.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify an integer for AllowMissingCount." ) );
		}
		else {	// Make sure it is an allowable value >= 0...
			if ( StringUtil.atoi(AllowMissingCount) < 0 ) {
				message = "The AllowMissingCount value (" +	AllowMissingCount + ") must be >= 0.";
				warning += "\n" + message;
				status.addToLog ( CommandPhaseType.INITIALIZATION,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Specify a value >= 0." ) );
			}
		}
	}
	if (	(AnalysisStart != null) && !AnalysisStart.equals("") &&
		!AnalysisStart.equalsIgnoreCase("OutputStart") &&
		!AnalysisStart.equalsIgnoreCase("OutputEnd") ) {
		try {	DateTime.parse(AnalysisStart);
		}
		catch ( Exception e ) {
			message = "The analysis start \"" + AnalysisStart + "\" is not a valid date/time.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a valid date/time, OutputStart, or OutputEnd." ) );
		}
	}
	if (	(AnalysisEnd != null) && !AnalysisEnd.equals("") &&
		!AnalysisEnd.equalsIgnoreCase("OutputStart") &&
		!AnalysisEnd.equalsIgnoreCase("OutputEnd") ) {
		try {	DateTime.parse( AnalysisEnd );
		}
		catch ( Exception e ) {
			message = "The analysis end \"" + AnalysisEnd + "\" is not a valid date/time.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a valid date/time, OutputStart, or OutputEnd." ) );
		}
	}
	if (	(SearchStart != null) && !SearchStart.equals("") &&
		!SearchStart.equalsIgnoreCase("OutputStart") &&
		!SearchStart.equalsIgnoreCase("OutputEnd") ) {
		try {	DateTime.parse( SearchStart );
		}
		catch ( Exception e ) {
			message = "The search start date \"" + SearchStart + "\" is not a valid date/time.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Specify a valid date/time, OutputStart, or OutputEnd." ) );
		}
	}
	
	// Check for invalid parameters...
	List valid_Vector = new Vector();
	valid_Vector.add ( "Alias" );
	valid_Vector.add ( "EnsembleID" );
	valid_Vector.add ( "NewTSID" );
	valid_Vector.add ( "Statistic" );
	//valid_Vector.add ( "TestValue" );
	valid_Vector.add ( "AllowMissingCount" );
	valid_Vector.add ( "AnalysisStart" );
	valid_Vector.add ( "AnalysisEnd" );
	//valid_Vector.add ( "SearchStart" );
	warning = TSCommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );
	
	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),
		warning );
		throw new InvalidCommandParameterException ( warning );
	}
	status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new NewStatisticTimeSeriesFromEnsemble_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.
@param command A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String command )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	int warning_level = 2;
	String routine = "NewStatisticTimeSeriesFromEnsemble.parseCommand", message;

	// Get the part of the command after the TS Alias =...
	int pos = command.indexOf ( "=" );
	if ( pos < 0 ) {
		message = "Syntax error in \"" + command +
			"\".  Expecting:  TS Alias = NewStatisticTimeSeriesFromEnsemble(...)";
		Message.printWarning ( warning_level, routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
	String token0 = command.substring ( 0, pos ).trim();
	String token1 = command.substring ( pos + 1 ).trim();
	if ( (token0 == null) || (token1 == null) ) {
		message = "Syntax error in \"" + command +
			"\".  Expecting:  TS Alias = NewStatisticTimeSeriesFromEnsemble(...)";
		Message.printWarning ( warning_level, routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}

	List v = StringUtil.breakStringList ( token0, " ",StringUtil.DELIM_SKIP_BLANKS );
	if ( (v == null) || (v.size() != 2) ) {
		message = "Syntax error in \"" + command +
			"\".  Expecting:  TS Alias = NewStatisticTimeSeriesFromEnsemble(...)";
		Message.printWarning ( warning_level, routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
	String Alias = (String)v.get(1);
	List tokens = StringUtil.breakStringList ( token1, "()", 0 );
	if ( (tokens == null) || tokens.size() < 2 ) {
		// Must have at least the command name and its parameters...
		message = "Syntax error in \"" + command + "\". Expecting:  TS Alias = NewStatisticTimeSeriesFromEnsemble(...)";
		Message.printWarning ( warning_level, routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
	// Get the input needed to process the file...
	try {	 PropList parameters = PropList.parse ( Prop.SET_FROM_PERSISTENT,
			(String)tokens.get(1), routine, "," );
		parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
		parameters.set ( "Alias", Alias );
		parameters.setHowSet ( Prop.SET_UNKNOWN );
		setCommandParameters ( parameters );
	}
	catch ( Exception e ) {
		message = "Syntax error in \"" + command + "\".  Not enough tokens.";
		Message.printWarning ( warning_level, routine, message);
		throw new InvalidCommandSyntaxException ( message );
	}
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more
parameter values are invalid.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{	String routine = "NewStatisticTimeSeriesFromEnsemble.runCommand", message;
	int warning_count = 0;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int log_level = 3;	// Non-user warning level

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters ();
	CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.RUN);

	String Alias = parameters.getValue ( "Alias" );
	String EnsembleID = parameters.getValue ( "EnsembleID" );
	String NewTSID = parameters.getValue ( "NewTSID" );
	String Statistic = parameters.getValue ( "Statistic" );
	//TODO SAM 2007-11-05 Enable later with counts
	//String TestValue = parameters.getValue ( "TestValue" );
	String AllowMissingCount = parameters.getValue ( "AllowMissingCount" );
	String AnalysisStart = parameters.getValue ( "AnalysisStart" );
	String AnalysisEnd = parameters.getValue ( "AnalysisEnd" );
	//TODO SAM 2007-11-05 Probably not needed
	//String SearchStart = parameters.getValue ( "SearchStart" );

	// Figure out the dates to use for the analysis...

	DateTime AnalysisStart_DateTime = null;
	DateTime AnalysisEnd_DateTime = null;
	try {
		if ( AnalysisStart != null ) {
			PropList request_params = new PropList ( "" );
			request_params.set ( "DateTime", AnalysisStart );
			CommandProcessorRequestResultsBean bean = null;
			try {
                bean = processor.processRequest( "DateTime", request_params);
			}
			catch ( Exception e ) {
				message = "Error requesting AnalysisStart DateTime(DateTime=" + AnalysisStart + ") from processor.";
				Message.printWarning(log_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),routine, message );
				status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Report the problem to software support." ) );
				throw new InvalidCommandParameterException ( message );
			}

			PropList bean_PropList = bean.getResultsPropList();
			Object prop_contents = bean_PropList.getContents ( "DateTime" );
			if ( prop_contents == null ) {
				message = "Null value for AnalysisStart DateTime(DateTime=" + AnalysisStart + ") returned from processor.";
				Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify the AnalysisStart information." ) );
				throw new InvalidCommandParameterException ( message );
			}
			else {	AnalysisStart_DateTime = (DateTime)prop_contents;
			}
		}
		}
		catch ( Exception e ) {
			message = "AnalysisStart \"" + AnalysisStart + "\" is invalid.";
			Message.printWarning(warning_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
			status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify the AnalysisStart information." ) );
			throw new InvalidCommandParameterException ( message );
		}
		
		try {
		if ( AnalysisEnd != null ) {
			PropList request_params = new PropList ( "" );
			request_params.set ( "DateTime", AnalysisEnd );
			CommandProcessorRequestResultsBean bean = null;
			try { bean =
				processor.processRequest( "DateTime", request_params);
			}
			catch ( Exception e ) {
				message = "Error requesting AnalysisEnd DateTime(DateTime=" + AnalysisEnd + ") from processor.";
				Message.printWarning(log_level,
						MessageUtil.formatMessageTag( command_tag, ++warning_count),
						routine, message );
				status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Report the problem to software support." ) );
				throw new InvalidCommandParameterException ( message );
			}

			PropList bean_PropList = bean.getResultsPropList();
			Object prop_contents = bean_PropList.getContents ( "DateTime" );
			if ( prop_contents == null ) {
				message = "Null value for AnalysisEnd DateTime(DateTime=" +	AnalysisStart +	") returned from processor.";
				Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
				status.addToLog ( CommandPhaseType.RUN,
						new CommandLogRecord(CommandStatusType.FAILURE,
								message, "Verify the AnalysisEnd information." ) );
				throw new InvalidCommandParameterException ( message );
			}
			else {	AnalysisEnd_DateTime = (DateTime)prop_contents;
			}
		}
		}
		catch ( Exception e ) {
			message = "AnalysisEnd \"" + AnalysisEnd + "\" is invalid.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
			status.addToLog ( CommandPhaseType.RUN,
					new CommandLogRecord(CommandStatusType.FAILURE,
							message, "Verify the AnalysisEnd information." ) );
			throw new InvalidCommandParameterException ( message );
		}
	
	// TODO SAM 2007-02-13 Need to enable SearchStart or remove
	/*DateTime SearchStart_DateTime = null;
	if ( (SearchStart != null) && (SearchStart.length() > 0) ) {
		try {	// The following works with MM/DD and MM-DD
			SearchStart_DateTime =
				DateTime.parse ( SearchStart,
				DateTime.FORMAT_MM_SLASH_DD );
		}
		catch ( Exception e ) {
			message = "SearchStart \"" + SearchStart +
				"\" is invalid.  Expecting MM-DD or MM/DD";
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count), routine, message );
			throw new InvalidCommandParameterException ( message );
		}
	}*/

	// Get the time series to process.  The time series list is searched backwards until the first match...

	PropList request_params = new PropList ( "" );
	request_params.set ( "CommandTag", command_tag );
	request_params.set ( "EnsembleID", EnsembleID );
	CommandProcessorRequestResultsBean bean = null;
	try {
        bean = processor.processRequest( "GetEnsemble", request_params);
	}
	catch ( Exception e ) {
		message = "Error requesting GetEnsemble(EnsembleID=\"" + EnsembleID + "\") from processor.";
		Message.printWarning(log_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
		status.addToLog ( CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Report the problem to software support." ) );
	}
	PropList bean_PropList = bean.getResultsPropList();
	Object o_TSEnsemble = bean_PropList.getContents ( "TSEnsemble");
	TSEnsemble tsensemble = null;
	if ( o_TSEnsemble == null ) {
		message = "Null TS requesting GetEnsemble(EnsembleID=\"" + EnsembleID + "\") from processor.";
		Message.printWarning(log_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify the ensemble identifier.  A previous error may also cause this problem." ) );
	}
	else {
		tsensemble = (TSEnsemble)o_TSEnsemble;
	}
	
	if ( tsensemble == null ) {
		message = "Unable to find ensemble to analyze using EnsembleID \"" + EnsembleID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
		status.addToLog ( CommandPhaseType.RUN,
		new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Verify the ensemble identifier.  A previous error may also cause this problem." ) );
		throw new CommandWarningException ( message );
	}

	// Now process the time series...

	TS stats_ts = null;
	try {
        TSAnalyst tsa = new TSAnalyst ();
		PropList tsa_props = new PropList ( "TSAnalyst" );
		if ( (NewTSID != null) && (NewTSID.length() > 0) ) {
			tsa_props.set ( "NewTSID", NewTSID );	// Optional
		}
		tsa_props.set ( "Statistic", Statistic );	// Required
		/*
		if ( (TestValue != null) && (TestValue.length() > 0) ) {
			tsa_props.set ( "TestValue", TestValue);// Optional
		}
		*/
		if ( (AllowMissingCount != null) && (AllowMissingCount.length() > 0) ) {
			tsa_props.set ( "AllowMissingCount",AllowMissingCount);	// Optional
		}
		/*
		if ( (SearchStart != null) && (SearchStart.length() > 0) ) {
			tsa_props.set ( "SearchStart", SearchStart);
								// Optional
		}
		*/
		stats_ts = tsa.createStatisticTimeSeries ( tsensemble,
				AnalysisStart_DateTime, AnalysisEnd_DateTime,
				null, null,
				tsa_props );
		stats_ts.setAlias ( Alias );	// Do separate because setting
						// the NewTSID might cause the
						// alias set to fail below.
	}
	catch ( Exception e ) {
		message ="Unexpected error generating the statistic time series from ensemble \""+ EnsembleID + "\" (" + e + ").";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag,++warning_count),routine,message );
		Message.printWarning(3,routine,e);
		status.addToLog ( CommandPhaseType.RUN,
		new CommandLogRecord(CommandStatusType.FAILURE,
				message, "See the log file for details." ) );
	}
    
    // Update the data to the processor so that appropriate actions are taken...

    if ( stats_ts != null ) {
        warning_count += TSCommandProcessorUtil.appendTimeSeriesToResultsList(processor, this, stats_ts);
    }

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
			command_tag, ++warning_count),
			routine,message);
		throw new CommandWarningException ( message );
	}
	
	status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return "TS Alias = " + getCommandName() + "()";
	}
	String Alias = props.getValue( "Alias" );
	String EnsembleID = props.getValue( "EnsembleID" );
	String NewTSID = props.getValue( "NewTSID" );
	String Statistic = props.getValue( "Statistic" );
	//String TestValue = props.getValue( "TestValue" );
	String AllowMissingCount = props.getValue( "AllowMissingCount" );
	String AnalysisStart = props.getValue( "AnalysisStart" );
	String AnalysisEnd = props.getValue( "AnalysisEnd" );
	//String SearchStart = props.getValue( "SearchStart" );
	StringBuffer b = new StringBuffer ();
	if ( (EnsembleID != null) && (EnsembleID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "EnsembleID=\"" + EnsembleID + "\"" );
	}
	if ( (NewTSID != null) && (NewTSID.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "NewTSID=\"" + NewTSID + "\"" );
	}
	if ( (Statistic != null) && (Statistic.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Statistic=" + Statistic );
	}
	/*
	if ( (TestValue != null) && (TestValue.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "TestValue=" + TestValue );
	}
	*/
	if ( (AllowMissingCount != null) && (AllowMissingCount.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AllowMissingCount=" + AllowMissingCount );
	}
	if ( (AnalysisStart != null) && (AnalysisStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AnalysisStart=\"" + AnalysisStart + "\"" );
	}
	if ( (AnalysisEnd != null) && (AnalysisEnd.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "AnalysisEnd=\"" + AnalysisEnd + "\"" );
	}
	/*
	if ( (SearchStart != null) && (SearchStart.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "SearchStart=\"" + SearchStart + "\"" );
	}
	*/
	return "TS " + Alias + " = " + getCommandName() + "("+ b.toString()+")";
}

}