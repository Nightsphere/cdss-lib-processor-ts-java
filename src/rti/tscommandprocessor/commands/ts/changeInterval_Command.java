// ----------------------------------------------------------------------------
// changeInterval_Command - editor for TS X = changeInterval()
//
// REVISIT SAM 2005-02-12
//		In the future may also support changeInterval() to operate on
//		multiple time series.
// ----------------------------------------------------------------------------
// Copyright:	See the COPYRIGHT file.
// ----------------------------------------------------------------------------
// History: 
//
// 2005-02-16	Steven A. Malers, RTi	Initial version, initialized from
//					normalize_JDialog().
// 2005-02-18	SAM, RTi		Comment out AllowMissingPercent - it
//					is causing problems in some of the
//					computations so re-evaluate later.
// 2005-03-14	SAM, RTi		Add OutputFillMethod and
//					HandleMissingInputHow parameters.
// 2005-05-24	Luiz Teixeira, RTi	Copied the original class 
//					changeInterval_JDialog() from TSTool and
//					started splitting the code into the new
//					changeInterval_JDialog() and
//					changeInterval_Command().
// 2005-05-26	Luiz Teixeira, RTi	Cleanup and documentation.
// 2007-02-16	SAM, RTi		Use new CommandProcessor interface.
//					Clean up code based on Eclipse feedback.
// ----------------------------------------------------------------------------
package rti.tscommandprocessor.commands.ts;

import java.util.Vector;

import javax.swing.JFrame;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;

import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.InvalidCommandSyntaxException;
import RTi.Util.IO.MeasTimeScale;
import RTi.Util.IO.Prop;
import RTi.Util.IO.PropList;
import RTi.Util.IO.SkeletonCommand;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;

public class changeInterval_Command extends SkeletonCommand
	implements Command
{

// Defines used by this class and its changeInterval_Dialog counterpart.
protected final String __Interpolate = "Interpolate";
protected final String __KeepMissing = "KeepMissing";
protected final String __Repeat      = "Repeat";
protected final String __SetToZero   = "SetToZero";

private final boolean  __read_one    = true;	// For now only enable the
						// TS X notation.
/**
changeInterval_Command constructor.
*/
public changeInterval_Command ()
{	
	super();
	setCommandName ( "changeInterval" );
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
public void checkCommandParameters ( PropList parameters,
				     String command_tag,
				     int warning_level )
throws InvalidCommandParameterException
{	String warning = "";
	
	// Get the properties from the PropList parameters.
	String	Alias        = parameters.getValue( "Alias" );
	String	TSID         = parameters.getValue( "TSID"  );
	String	NewInterval  = parameters.getValue( "NewInterval"  );
	String	OldTimeScale = parameters.getValue( "OldTimeScale"  );
	String	NewTimeScale = parameters.getValue( "NewTimeScale"  );
	String	AllowMissingCount  = parameters.getValue("AllowMissingCount"  );
	/* REVISIT SAM 2005-02-18 may enable later
	String	AllowMissingPercent= parameters.getValue("AllowMissingPercent");
	*/
	String	OutputFillMethod  =
		parameters.getValue( "OutputFillMethod"      );
	String	HandleMissingInputHow =
		parameters.getValue( "HandleMissingInputHow" );

	// Alias must be specified.
	// REVISIT [LT 2005-05-24] How about the __read_one issue
	//			   (see parseCommand() method)
	if ( Alias == null || Alias.length() == 0 ) {
		warning += "\nThe \"Alias\" must be specified.";
	}
	
	// TSID - TSID will always be set from the changeInterval_JDialog when
	// the OK button is pressed, but the user may edit the command without
	// using the changeInterval_JDialog editor and try to run it, so this
	// method should at least make sure the TSID property is given.
	// REVISIT [LT 2005-05-26] Better test may be put in place here, to make
	// sure the given TSID is actually a valid time series in the system.
	if ( TSID == null || TSID.length() == 0 ) {
		warning +="\nThe \"Time series to convert\" must be specified.";
	}

	// Check if the alias for the new time series is the same as the 
	// alias used by one of the time series in memory.
	// If so print a warning...
// REVISIT [LT 2005-05-26] This is used in all other command but it 
// is not working here.  Why?	Temporarely using the alternative below.
/*	Vector tsids = (Vector) getCommandProcessor().getPropContents (
			"TSIDListNoInput" );
	if ( StringUtil.indexOf( tsids, Alias ) >= 0 ) {
		warning += "\nTime series alias \""
			+ Alias + "\" is already used above.";
	}
*/		
	// Check if the alias for the new time series is the same as the 
	// alias used by the original time series.  If so print a warning...
	// REVISIT [LT 2005-05-26] Would this alternative be more appropriated?
	// Notice: The version above is the one used by the others commands.
	if ( Alias != null && TSID.indexOf( Alias ) >= 0 ) {
		warning += "\nThe alias \"" + Alias
			+ "\" for the new time series is equal to the alias "
			+ "of the original time series.";
	}
	
	// NewInterval - NewInterval will always be set from the 
	// changeInterval_JDialog when the OK button is pressed, but the user
	// may edit the command without using the changeInterval_JDialog editor
	// and try to run it, so this method should at least make sure the 
	// NewInterval property is given.
	// REVISIT [LT 2005-05-26] Better test may be put in place here, to make
	// sure the given NewInterval is actually a valid value for interval.
	if ( NewInterval != null && NewInterval.length() == 0 ) {
		warning +="\nThe \"New interval\" must be specified.";
	}
	
	// OldTimeScale - OldTimeScale will always be set from the 
	// changeInterval_JDialog when the OK button is pressed, but the user
	// may edit the command without using the changeInterval_JDialog editor
	// and try to run it, so this method should at least make sure the 
	// OldTimeScale property is given.
	if ( OldTimeScale != null && OldTimeScale.length() == 0 ) {
		warning +="\nThe \"Old time scale\" must be specified.";
	}
	if ( 	!OldTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM) &&
		!OldTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN) &&
		!OldTimeScale.equalsIgnoreCase(MeasTimeScale.INST) ) {
		warning +="\nThe \"Old time scale\" (" + OldTimeScale
			+ ") is not valid.\n" 
			+ "Valid options are: ACCM, MEAM and INST";
	}
	
	// NewTimeScale - NewTimeScale will always be set from the 
	// changeInterval_JDialog when the OK button is pressed, but the user
	// may edit the command without using the changeInterval_JDialog editor
	// and try to run it, so this method should at least make sure the 
	// NewTimeScale property is given.
	if ( NewTimeScale != null && NewTimeScale.length() == 0 ) {
		warning +="\nThe \"New time scale\" must be specified.";
	}
	if ( 	!NewTimeScale.equalsIgnoreCase(MeasTimeScale.ACCM) &&
		!NewTimeScale.equalsIgnoreCase(MeasTimeScale.MEAN) &&
		!NewTimeScale.equalsIgnoreCase(MeasTimeScale.INST) ) {
		warning +="\nThe \"New time scale\" (" + NewTimeScale
			+ ") is not valid.\n" 
			+ "Valid options are: ACCM, MEAM and INST";
	}
	
	// If the AllowMissingCount is specified, it should be an integer.
	if ( AllowMissingCount!=null && (AllowMissingCount.length()>0) &&
		!StringUtil.isInteger(AllowMissingCount) ) {
		warning += "\nAllow missing count \"" + AllowMissingCount
			+ "\" is not an integer."; 
	}
	
	// If the AllowMissingPercent is specified, it should be an number.
	/* REVISIT SAM 2005-02-18 may enable later
	if ( AllowMissingPercent!=null && (AllowMissingPercent.length()>0) &&
		!StringUtil.isDouble(AllowMissingPercent) ) {
		warning += "\nAllow missing percent \"" + AllowMissingPercent
			+ "\" is not a number.";
	}
	
	// Only one of AllowMissingCount and AllowMissingPercent can be specified
	if ( (AllowMissingCount.length() > 0) &&
	     (AllowMissingPercent.length() > 0) ) {
		warning += "\nOnly one of AllowMissingCount and "
			+ "AllowMissingPercent can be specified.";
	} */
	
	// If the OutputFillMethod is specified, make sure it is valid.
	if ( OutputFillMethod != null && OutputFillMethod.length() > 0 ) {
		if (	!OutputFillMethod.equalsIgnoreCase( __Repeat      ) &&
			!OutputFillMethod.equalsIgnoreCase( __Interpolate ) ) {
			warning += "OutputFillMethod must be "
				+ "\""       + __Interpolate
				+ "\" or \"" + __Repeat
				+ "\".";
		}
	}

	// If the HandleMissingInputHow is specified, make sure it is valid.
	if ( HandleMissingInputHow!=null && HandleMissingInputHow.length()>0 ) {
		if (	!HandleMissingInputHow.equalsIgnoreCase(__KeepMissing)&&
			!HandleMissingInputHow.equalsIgnoreCase(__Repeat     )&&
			!HandleMissingInputHow.equalsIgnoreCase(__SetToZero  )){
			warning += "HandleMissingInputHow must be "
			    + "\""        + __KeepMissing 
			    + "\", \""    + __Repeat
			    + "\", or \"" + __SetToZero
			    + "\".";
		}
	}
	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, warning_level ),
			warning );
		throw new InvalidCommandParameterException ( warning );
	}
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	
	// The command will be modified if changed...
	return ( new changeInterval_JDialog ( parent, this ) ).ok();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{	
	super.finalize ();
}

/**
Parse the command string into a PropList of parameters.
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
public void parseCommand ( String command,
			   String command_tag,
			   int    warning_level )
throws 	InvalidCommandSyntaxException,
	InvalidCommandParameterException
{
	String mthd = "changeInterval_Command.parseCommand", mssg;

	int warning_count = 0;

	if ( Message.isDebugOn ) {
		mssg = "Command to parse is: " + command;
		Message.printDebug ( 10, mthd, mssg );
	}
	 
	String Alias = "";
	     
	// Since this command is of the type TS X = changeInterval (...), we
	// first need to parse the Alias (the X in the command). 
	String substring = "";
	if ( command.indexOf('=') >= 0 ) {
		// Because the parameters contain =, find the first = to break
		// the assignment TS X = changeInterval (...).
		int pos = -1;	// Will be incremented to zero if !__read_one.
		if ( __read_one ) {
			// TS X = changeInterval (...)
			pos = command.indexOf('=');
			substring = command.substring(0,pos).trim();
			Vector v = StringUtil.breakStringList (
				substring, " ",
				StringUtil.DELIM_SKIP_BLANKS ); 
			// First field has format "TS X"
			Alias = ((String)v.elementAt(1)).trim();		
		}
		
		// Substring, eliminating "TS X =" when __read_one is true.
		// The result substring in any case will contain only the
		// changeInterval (...) part of the command.
		substring = command.substring(pos + 1).trim();	
			
		// Split the substring into two parts: the command name and 
		// the parameters list within the parenthesis.
		Vector tokens = StringUtil.breakStringList ( substring,
			"()", StringUtil.DELIM_SKIP_BLANKS );
		if ( (tokens == null) || tokens.size() < 2 ) {
			// Must have at least the command name and the parameter
			// list.
			mssg = "Syntax error in \"" + command +
				"\".  Not enough tokens.";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag(
					command_tag,++warning_count),
				mthd, mssg);
			throw new InvalidCommandSyntaxException ( mssg );
		}
	
		// Parse the parameters (second token in the tokens vector)
		// needed to process the command.
		try {
			_parameters = PropList.parse ( Prop.SET_FROM_PERSISTENT,
				(String) tokens.elementAt(1), mthd, "," );
			// If the Alias was found in the command added it to the
			// parameters propList.	
			if ( Alias != null && Alias.length() > 0 ) {
				_parameters.set( "Alias", Alias );
				
				if ( Message.isDebugOn ) {
					mssg = "Alias is: " + Alias;
					Message.printDebug ( 10, mthd, mssg );
				}
			} 	
		}
		catch ( Exception e ) {
			mssg = "Syntax error in \"" + command
				+ "\".  Not enough tokens.";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag(
					command_tag, ++warning_count ),
				mthd, mssg );
			throw new InvalidCommandSyntaxException ( mssg );
		}
	}
}

/**
Run the command:
<pre>
Alias X = changeInterval (TSID="...",
			 NewInterval="...",
			 OldTimeScale="...",
			 NewTimeScale="...",
			 NewDataType="...",
			 AllowMissingCount="...",
			 OutputFillMethod="...",
			 HandleMissingInputHow="...")
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
public void runCommand ( String command_tag,
			 int warning_level )
throws InvalidCommandParameterException,
       CommandWarningException,
       CommandException
{
	String routine = getCommandName() + ".runCommand";
	String message = "";
            	
	int warning_count = 0;
	int log_level = 3;	// Warning message level for non-user messages
	
	String	Alias        = _parameters.getValue( "Alias" );
	String	TSID         = _parameters.getValue( "TSID"  );
	String	NewInterval  = _parameters.getValue( "NewInterval"  );
	String	OldTimeScale = _parameters.getValue( "OldTimeScale" );
	String	NewTimeScale = _parameters.getValue( "NewTimeScale" );
	String	NewDataType  = _parameters.getValue( "NewDataType"  );
	String	AllowMissingCount  = _parameters.getValue("AllowMissingCount"  );
	/* REVISIT SAM 2005-02-18 may enable later
	String	AllowMissingPercent= _parameters.getValue("AllowMissingPercent");
	*/
	String	OutputFillMethod  =
		_parameters.getValue( "OutputFillMethod"      );
	String	HandleMissingInputHow =
		_parameters.getValue( "HandleMissingInputHow" );
	
	// Set the properties for the method TSUtil.changeInterval()!
	PropList props = new PropList ( "TSUtil.changeInterval" );
	props.set ( "OldTimeScale", OldTimeScale );
	props.set ( "NewTimeScale", NewTimeScale );
	
	// Do not set these properties if they are "" (empty).
	// TSUtil.changeInterval expects "null" when calling getValue()
	// for these properties to set the internal defaults.
	if ( NewDataType != null && NewDataType.length() > 0  ) {
		props.set ( "NewDataType", NewDataType );
	}
	if ( AllowMissingCount != null && AllowMissingCount.length() > 0  ) {
		props.set ( "AllowMissingCount", AllowMissingCount );
	}
	if ( OutputFillMethod != null && OutputFillMethod.length() > 0  ) {
		props.set ( "OutputFillMethod", OutputFillMethod );
	}
	if ( HandleMissingInputHow != null &&
	     HandleMissingInputHow.length() > 0  ) {
		props.set ( "HandleMissingInputHow", HandleMissingInputHow );
	}
	
	// Get the reference (original_ts) to the time series to change interval
	// from.  Currently just one can be processed.
	
	PropList request_params = new PropList ( "" );
	request_params.set ( "CommandTag", command_tag );
	request_params.set ( "TSID", TSID );
	CommandProcessorRequestResultsBean bean = null;
	try { bean =
		_processor.processRequest( "GetTimeSeriesForTSID", request_params);
	}
	catch ( Exception e ) {
		message = "Error requesting GetTimeSeriesForTSID(TSID=\"" + TSID +
		"\" from processor.";
		Message.printWarning(log_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
	}
	PropList bean_PropList = bean.getResultsPropList();
	Object o_TS = bean_PropList.getContents ( "TS");
	TS original_ts = null;
	if ( o_TS == null ) {
		message = "Null TS requesting GetTimeSeriesForTSID(TSID=\"" + TSID +
		"\" from processor.";
		Message.printWarning(log_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
	}
	else {
		original_ts = (TS)o_TS;
	}
	
	if ( original_ts == null ){
		message = "Cannot determine the time series to process for TSID=\""
			+ TSID + "\".";
		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
		throw new CommandWarningException ( message);
	}
	
	// Change interval
	TS   result_ts = null;		// Result time series
	try {
		// Process the change of interval
		result_ts= TSUtil.changeInterval(original_ts,NewInterval,props);	
		
		// Update the newly created time series alias.
		TSIdent tsIdent = result_ts.getIdentifier();
		tsIdent.setAlias ( Alias );
		result_ts.setIdentifier( tsIdent );

		// Add the newly created time series to the software memory.
		
		Vector TSResultsList_Vector = null;
		try { Object o = _processor.getPropContents( "TSResultsList" );
				TSResultsList_Vector = (Vector)o;
		}
		catch ( Exception e ){
			message = "Cannot get time series list to add new time series.  Skipping.";
			Message.printWarning ( warning_level,
					MessageUtil.formatMessageTag(
					command_tag, ++warning_count),
					routine,message);
		}
		if ( TSResultsList_Vector != null ) {
			TSResultsList_Vector.addElement ( result_ts );
			try {	_processor.setPropContents ( "TSResultsList", TSResultsList_Vector );
			}
			catch ( Exception e ){
				message = "Cannot set updated time series list.  Skipping.";
				Message.printWarning ( warning_level,
					MessageUtil.formatMessageTag(
					command_tag, ++warning_count),
					routine,message);
			}
		}
	} 
	catch ( Exception e ) {
		message = "Error processing the changeInterval for TSID=\"" +
		TSID + "\"";
		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
		Message.printWarning ( log_level, routine, e );
		throw new CommandWarningException ( message );
	}

	// Clean up
	original_ts   = null;
	result_ts     = null;
	
	// Throw CommandWarningException in case of problems.
	if ( warning_count > 0 ) {
		message = "There were " + warning_count +
			" warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, ++warning_count ),
			routine, message );
		throw new CommandWarningException ( message );
	}
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{
	if ( props == null ) {
		return getCommandName() + "()";
	}

	// Get the properties from the command; 
	String Alias		     = props.getValue( "Alias"		     );
	String TSID		     = props.getValue( "TSID"		     );
	String NewInterval	     = props.getValue( "NewInterval"	     );
	String OldTimeScale	     = props.getValue( "OldTimeScale"	     );
	String NewTimeScale	     = props.getValue( "NewTimeScale"	     );
	String NewDataType	     = props.getValue( "NewDataType"	     );
	String AllowMissingCount    = props.getValue( "AllowMissingCount"    );
	/* REVISIT SAM 2005-02-18 may enable later
	String AllowMissingPercent  = props.getValue( "AllowMissingPercent"  );
	*/
	String OutputFillMethod     = props.getValue( "OutputFillMethod"     );
	String HandleMissingInputHow= props.getValue( "HandleMissingInputHow");
	
	// Creating the command string
	// This StringBuffer will contain all parameters for the command.
	StringBuffer b = new StringBuffer();

	// Adding the TSID
	if ( TSID != null && TSID.length() > 0 ) {
		if ( b.length() > 0 ) b.append ( "," );
		b.append ( "TSID=\"" + TSID + "\"" );
	}

	// Adding the NewInterval
	if ( NewInterval != null && NewInterval.length() > 0 ) {
		if ( b.length() > 0 ) b.append ( "," );
		b.append ( "NewInterval=" + NewInterval );
	}

	// Adding the OldTimeScale
	if ( OldTimeScale != null && OldTimeScale.length() > 0 ) {
		if ( b.length() > 0 ) b.append ( "," );
		b.append ( "OldTimeScale=" + OldTimeScale );
	}

	// Adding the NewTimeScale
	if ( NewTimeScale != null && NewTimeScale.length() > 0 ) {
		if ( b.length() > 0 ) b.append ( "," );
		b.append ( "NewTimeScale=" + NewTimeScale  );
	}

	// Adding the OutputFile
	if ( NewDataType != null && NewDataType.length() > 0 ) {
		if ( b.length() > 0 ) b.append ( "," );
		b.append ( "NewDataType=" + NewDataType );
	}
	
	// Adding the AllowMissingCount
	if ( AllowMissingCount != null && AllowMissingCount.length() > 0 ) {
		if ( b.length() > 0 ) b.append ( "," );
		b.append ( "AllowMissingCount=" + AllowMissingCount );
	}
	
	// Adding the AllowMissingPercent
	/* REVISIT SAM 2005-02-18 may enable later
	if ( AllowMissingPercent != null && AllowMissingPercent.length() > 0 ) {
		if ( b.length() > 0 ) b.append ( "," );
		b.append ( "AllowMissingPercent=" + AllowMissingPercent );
	} */
	
	// Adding the OutputFillMethod
	if ( OutputFillMethod != null && OutputFillMethod.length() > 0 ) {
		if ( b.length() > 0 ) b.append ( "," );
		b.append ( "OutputFillMethod=" + OutputFillMethod );
	}
	
	// Adding the HandleMissingInputHow
	if ( HandleMissingInputHow != null && HandleMissingInputHow.length() > 0 ) {
		if ( b.length() > 0 ) b.append ( "," );
		b.append ( "HandleMissingInputHow=" + HandleMissingInputHow );
	}
	
	String commandString = getCommandName() + "(" + b.toString() + ")";
	if ( __read_one ) {
		commandString = "TS " + Alias + " = " + commandString;
	} 
	
	return commandString;
}

} // end changeInterval_Command
