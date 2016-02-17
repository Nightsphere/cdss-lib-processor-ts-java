package rti.tscommandprocessor.commands.ts;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import rti.tscommandprocessor.core.TSListType;

import java.util.ArrayList;
import java.util.List;

import RTi.TS.TS;
import RTi.TS.TSUtil_ARMA;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.AbstractCommand;
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
import RTi.Util.Time.TimeInterval;

/**
This class initializes, checks, and runs the ARMA() command.
*/
public class ARMA_Command extends AbstractCommand implements Command
{
	
/**
Parameter values requiring sum to 1.
*/
protected final String _False = "False";
protected final String _True = "True";
    
/**
"a" coefficients, parsed during checks to improve performance.
*/
private double __a[] = null;

/**
"b" coefficients, parsed during checks to improve performance.
*/
private double __b[] = null;

/**
Input initial values, parsed during checks to improve performance.
*/
private double __inputInitialValues[] = null;

/**
Constructor.
*/
public ARMA_Command ()
{	super();
	setCommandName ( "ARMA" );
}

/**
Check the command parameter for valid values, combination, etc.
@param parameters The parameters for the command.
@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
@param warning_level The warning level to use when printing parse warnings
(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
*/
public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
throws InvalidCommandParameterException
{	String TSList = parameters.getValue ( "TSList" );
	String TSID = parameters.getValue ( "TSID" );
	String ARMAInterval = parameters.getValue ( "ARMAInterval" );
	String a = parameters.getValue ( "a" );
	String b = parameters.getValue ( "b" );
	String RequireCoefficientsSumTo1 = parameters.getValue ( "RequireCoefficientsSumTo1" );
	String InputInitialValues = parameters.getValue ( "InputInitialValues" );
	String OutputStart = parameters.getValue ( "OutputStart" );
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	String OutputMinimum = parameters.getValue ( "OutputMinimum" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
    
    if ( (TSList != null) && !TSListType.ALL_MATCHING_TSID.equals(TSList) &&
            !TSListType.FIRST_MATCHING_TSID.equals(TSList) &&
            !TSListType.LAST_MATCHING_TSID.equals(TSList) ) {
        if ( TSID != null ) {
            message = "TSID should only be specified when TSList=" +
            TSListType.ALL_MATCHING_TSID.toString() + " or " +
            TSListType.FIRST_MATCHING_TSID.toString() + " or " +
            TSListType.LAST_MATCHING_TSID.toString() + ".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Do not specify the TSID parameter." ) );
        }
    }
    /*
	if ( TSList == null ) {
		// Probably legacy command...
		// TODO SAM 2005-05-17 Need to require TSList when legacy
		// commands are safely nonexistent...  At that point the
		// following check can occur in any case.
		if ( (TSID == null) || (TSID.length() == 0) ) {
            message = "A TSID must be specified.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a TSList parameter value." ) );
		}
	}
    */

    double total = 0.0;
    // a is optional
    if ( (a != null) && !a.equals("") ) {
        // Make sure coefficients are doubles...
    	List aVector = StringUtil.breakStringList ( a, ", ", StringUtil.DELIM_SKIP_BLANKS );
        int aSize = 0;
        if ( aVector != null ) {
            aSize = aVector.size();
        }
        __a = new double[aSize];
        String aVal;
        for ( int i = 0; i < aSize; i++ ) {
            aVal = ((String)aVector.get(i)).trim();
            if ( !StringUtil.isDouble(aVal)) {
                message = "The a-coefficient " + aVal + " is not a number.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Correct the value of the a-coefficient." ) );
            }
            else {
                __a[i] = StringUtil.atod(aVal);
                total += __a[i];
            }
        }
    }
    
    if ( (b == null) || b.equals("") ) {
        message = "No b-coefficients are specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify b-coefficients." ) );
    }
    else {
        if ( (b != null) && !b.equals("") ) {
            // Make sure coefficients are doubles...
        	List bVector = StringUtil.breakStringList ( b, ", ", StringUtil.DELIM_SKIP_BLANKS );
            int bSize = 0;
            if ( bVector != null ) {
                bSize = bVector.size();
            }
            __b = new double[bSize];
            String bVal;
            for ( int i = 0; i < bSize; i++ ) {
                bVal = ((String)bVector.get(i)).trim();
                if ( !StringUtil.isDouble(bVal)) {
                    message = "The b-coefficient " + bVal + " is not a number.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Correct the value of the b-coefficient." ) );
                }
                else {
                    __b[i] = StringUtil.atod(bVal);
                    total += __b[i];
                }
            }
        }
    }

    boolean requireCoefficientsSumTo1 = true;
    if ( (RequireCoefficientsSumTo1 != null) && !RequireCoefficientsSumTo1.isEmpty() ) {
    	if ( !RequireCoefficientsSumTo1.equalsIgnoreCase(_False) && !RequireCoefficientsSumTo1.equalsIgnoreCase(_True) ) {
	        message = "The RequireCoefficientsSumTo1 parameter is invalid.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
	            message, "Specify RequireCoefficientsSumTo1 as " + _False + " or " + _True + " (default).") );
    	}
    	if ( RequireCoefficientsSumTo1.equalsIgnoreCase(_False) ) {
    		requireCoefficientsSumTo1 = false;
    	}
    }

    if ( requireCoefficientsSumTo1 ) {
	    String total_String = StringUtil.formatString(total,"%.6f");
	    if ( !total_String.equals("1.000000") ) {
	        message = "\nSum of a and b coefficients (" +
	            StringUtil.formatString(total,"%.6f") + ") does not equal 1.000000.";
	        warning += "\n" + message;
	        status.addToLog ( CommandPhaseType.INITIALIZATION,
	            new CommandLogRecord(CommandStatusType.FAILURE,
	                message, "Verify that a and b coefficents sum to 1.000000." ) );
	    }
    }

	if ( (ARMAInterval == null) || ARMAInterval.equals("") ) {
        message = "The ARMA interval is not specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the ARMA interval (e.g., 2Hour)." ) );
	}
	else {
		try {
		    TimeInterval.parseInterval(ARMAInterval);
		}
		catch ( Exception e ) {
            message = "The ARMA interval \"" + ARMAInterval + "\" is not a valid interval.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a valid time interval." ) );
		}
	}
	
    if ( (InputInitialValues != null) && !InputInitialValues.equals("") ) {
        // Make sure values are doubles...
    	List<String> strings = StringUtil.breakStringList ( a, ", ", StringUtil.DELIM_SKIP_BLANKS );
        int size = 0;
        if ( strings != null ) {
            size = strings.size();
        }
        __inputInitialValues = new double[size];
        String s;
        for ( int i = 0; i < size; i++ ) {
            s = strings.get(i).trim();
            double val;
            try {
            	val = Double.parseDouble(s);
                __inputInitialValues[i] = val;
            }
            catch ( NumberFormatException e ) {
                message = "The input initial value " + s + " is not a number.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Correct the value of the initial value." ) );
            }
        }
    }
    
	if ( (OutputStart != null) && !OutputStart.isEmpty() && !OutputStart.startsWith("${") ) {
		try {	DateTime datetime1 = DateTime.parse(OutputStart);
			if ( datetime1 == null ) {
				throw new Exception ("bad date");
			}
		}
		catch (Exception e) {
			message = "Output start date/time \"" + OutputStart + "\" is not a valid date/time.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify a valid output start date/time." ) );
		}
	}
	if ( (OutputEnd != null) && !OutputEnd.isEmpty() && !OutputEnd.startsWith("${") ) {
		try {	DateTime datetime2 = DateTime.parse(OutputEnd);
			if ( datetime2 == null ) {
				throw new Exception ("bad date");
			}
		}
		catch (Exception e) {
			message = "Output end date/time \"" + OutputEnd + "\" is not a valid date/time.";
			warning += "\n" + message;
			status.addToLog ( CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify a valid output end date/time." ) );
		}
	}
	
	if ( (OutputMinimum != null) && OutputMinimum.isEmpty() && !StringUtil.isDouble(OutputMinimum) ) {
        message = "The output minimum value is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify the minimum value as a floating point number." ) );
	}
    
	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(11);
    validList.add ( "TSList" );
    validList.add ( "TSID" );
    validList.add ( "EnsembleID" );
    validList.add ( "ARMAInterval" );
    validList.add ( "a" );
    validList.add ( "b" );
    validList.add ( "RequireCoefficientsSumTo1" );
    validList.add ( "InputInitialValues" );
    validList.add ( "OutputStart" );
    validList.add ( "OutputEnd" );
    validList.add ( "OutputMinimum" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );
    
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
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new ARMA_JDialog ( parent, this )).ok();
}

/**
Parse the command string into a PropList of parameters.  This method currently
supports old syntax and new parameter-based syntax.
@param command_string A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is
determined to have invalid syntax.
syntax of the command are bad.
@exception InvalidCommandParameterException if during parsing the command
parameters are determined to be invalid.
*/
public void parseCommand ( String command_string )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{	int warning_level = 2;
	String routine = "ARMA_Command.parseCommand", message;

	if ( (command_string.indexOf('=') > 0) || command_string.endsWith("()") ) {
        // Current syntax...
        super.parseCommand( command_string);
    }
    else {
		// TODO SAM 2009-09-15 This whole block of code needs to be
		// removed as soon as commands have been migrated to the new syntax.
    	List v = StringUtil.breakStringList(command_string,
			"(),\t", StringUtil.DELIM_SKIP_BLANKS |	StringUtil.DELIM_ALLOW_STRINGS );
		int ntokens = 0;
		if ( v != null ) {
			ntokens = v.size();
		}
		if ( ntokens < 6 ) {
			// Command name, TSID, and constant...
			message = "Syntax error in \"" + command_string +
			"\".  Expecting ARMA(TSID,ARMAInterval,pP,a1,...,aP,qQ,b0,...bQ).";
			Message.printWarning ( warning_level, routine, message);
			throw new InvalidCommandSyntaxException ( message );
		}

		// Get the individual tokens of the expression...

		String TSID = ((String)v.get(1)).trim();
		String ARMAInterval = ((String)v.get(2)).trim();
		String p = ((String)v.get(3)).trim();
		StringBuffer a = new StringBuffer();
		int aSize = StringUtil.atoi(p.substring(1));
		for ( int i = 0; i < aSize; i++ ) {
		    if ( i > 0 ) {
		        a.append(",");
		    }
		    a.append((String)v.get(4+i));
		}
		String q = ((String)v.get(4 + aSize)).trim();
        StringBuffer b = new StringBuffer();
        int bSize = StringUtil.atoi(q.substring(1)) + 1;
        for ( int i = 0; i < bSize; i++ ) {
            if ( i > 0 ) {
                b.append(",");
            }
            b.append((String)v.get(4+aSize+1+i));
        }

		// Set parameters and new defaults...

		PropList parameters = new PropList ( getCommandName() );
		parameters.setHowSet ( Prop.SET_FROM_PERSISTENT );
		if ( TSID.length() > 0 ) {
			parameters.set ( "TSID", TSID );
			parameters.setHowSet(Prop.SET_AS_RUNTIME_DEFAULT);
            // Legacy behavior was to match last matching TSID if no wildcard
            if ( TSID.indexOf("*") >= 0 ) {
                parameters.set ( "TSList", TSListType.ALL_MATCHING_TSID.toString() );
            }
            else {
                parameters.set ( "TSList", TSListType.LAST_MATCHING_TSID.toString() );
            }
		}
		parameters.set ( "ARMAInterval", ARMAInterval );
		parameters.set ( "a", a.toString() );
		parameters.set ( "b", b.toString() );
		parameters.setHowSet ( Prop.SET_UNKNOWN );
		setCommandParameters ( parameters );
	}
}

/**
Run the command.
@param command_number number of command to run.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_count = 0;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int log_level = 3; // Warning message level for non-user messages

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    CommandStatus status = getCommandStatus();
    Boolean clearStatus = new Boolean(true); // default
    try {
    	Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    	if ( o != null ) {
    		clearStatus = (Boolean)o;
    	}
    }
    catch ( Exception e ) {
    	// Should not happen
    }
    if ( clearStatus ) {
		status.clearLog(commandPhase);
	}

	String TSList = parameters.getValue ( "TSList" );
    if ( (TSList == null) || TSList.equals("") ) {
        TSList = TSListType.ALL_TS.toString();
    }
	String TSID = parameters.getValue ( "TSID" );
	if ( (commandPhase == CommandPhaseType.RUN) && (TSID != null) && (TSID.indexOf("${") >= 0) ) {
		TSID = TSCommandProcessorUtil.expandParameterValue(processor, this, TSID);
	}
    String EnsembleID = parameters.getValue ( "EnsembleID" );
	if ( (commandPhase == CommandPhaseType.RUN) && (EnsembleID != null) && (EnsembleID.indexOf("${") >= 0) ) {
		EnsembleID = TSCommandProcessorUtil.expandParameterValue(processor, this, EnsembleID);
	}
    String OutputMinimum = parameters.getValue ( "OutputMinimum" );
    double outputMinimum = Double.NaN;
    if ( (OutputMinimum != null) && !OutputMinimum.isEmpty() ) {
    	outputMinimum = Double.parseDouble(OutputMinimum);
    }

	// Get the time series to process...
	
	PropList request_params = new PropList ( "" );
	request_params.set ( "TSList", TSList );
	request_params.set ( "TSID", TSID );
    request_params.set ( "EnsembleID", EnsembleID );
	CommandProcessorRequestResultsBean bean = null;
	try {
        bean = processor.processRequest( "GetTimeSeriesToProcess", request_params);
	}
	catch ( Exception e ) {
        message = "Error requesting GetTimeSeriesToProcess(TSList=\"" + TSList +
        "\", TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\") from processor.";
		Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
        status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
	}
	PropList bean_PropList = bean.getResultsPropList();
	Object o_TSList = bean_PropList.getContents ( "TSToProcessList" );
	List tslist = null;
	if ( o_TSList == null ) {
        message = "Null TSToProcessList returned from processor for GetTimeSeriesToProcess(TSList=\"" + TSList +
        "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\").";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message,
                "Verify that the TSList parameter matches one or more time series - may be OK for partial run." ) );
	}
	else {
        tslist = (List)o_TSList;
		if ( tslist.size() == 0 ) {
            message = "No time series are available from processor GetTimeSeriesToProcess (TSList=\"" + TSList +
            "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\").";
			Message.printWarning ( warning_level,
				MessageUtil.formatMessageTag(
					command_tag,++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.WARNING,
                    message,
                    "Verify that the TSList parameter matches one or more time series - may be OK for partial run." ) );
		}
	}
	Object o_Indices = bean_PropList.getContents ( "Indices" );
	int [] tspos = null;
	if ( o_Indices == null ) {
        message = "Unable to find indices for time series to process using TSList=\"" + TSList +
        "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report the problem to software support." ) );
	}
	else {
        tspos = (int [])o_Indices;
		if ( tspos.length == 0 ) {
            message = "Unable to find indices for time series to process using TSList=\"" + TSList +
            "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\".";
			Message.printWarning ( warning_level,
			    MessageUtil.formatMessageTag(
			        command_tag,++warning_count), routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
		}
	}
	
	int nts = 0;
	if ( tslist != null ) {
		nts = tslist.size();
	}
	if ( nts == 0 ) {
        message = "Unable to find any time series to process using TSList=\"" + TSList +
        "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\".";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN,
            new CommandLogRecord(CommandStatusType.WARNING,
                message,
                "Verify that the TSList parameter matches one or more time series - may be OK for partial run." ) );
	}

	// ARMA values...

	String ARMAInterval = parameters.getValue("ARMAInterval");
	// a and b are determined during parsing

	String OutputStart = parameters.getValue ( "OutputStart" );
	if ( (OutputStart == null) || OutputStart.isEmpty() ) {
		OutputStart = "${OutputStart}"; // Default global property
	}
	String OutputEnd = parameters.getValue ( "OutputEnd" );
	if ( (OutputEnd == null) || OutputEnd.isEmpty() ) {
		OutputEnd = "${OutputEnd}"; // Default global property
	}
	DateTime OutputStart_DateTime = null;
	DateTime OutputEnd_DateTime = null;
	if ( commandPhase == CommandPhaseType.RUN ) {
		try {
			OutputStart_DateTime = TSCommandProcessorUtil.getDateTime ( OutputStart, "OutputStart", processor,
				status, warning_level, command_tag );
		}
		catch ( InvalidCommandParameterException e ) {
			// Warning will have been added above...
			++warning_count;
		}
		try {
			OutputEnd_DateTime = TSCommandProcessorUtil.getDateTime ( OutputEnd, "OutputEnd", processor,
				status, warning_level, command_tag );
		}
		catch ( InvalidCommandParameterException e ) {
			// Warning will have been added above...
			++warning_count;
		}
	}
    
	if ( warning_count > 0 ) {
		// Input error (e.g., missing time series)...
		message = "Insufficient data to run command.";
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(
		command_tag,++warning_count), routine, message );
	}

	// Now process the time series...

	boolean readData = true;
	if ( commandPhase == CommandPhaseType.DISCOVERY ) {
		readData = false;
	}
	TS ts = null;
	for ( int its = 0; its < nts; its++ ) {
		ts = null;
		request_params = new PropList ( "" );
		request_params.setUsingObject ( "Index", new Integer(tspos[its]) );
		bean = null;
		try { bean =
			processor.processRequest( "GetTimeSeries", request_params);
		}
		catch ( Exception e ) {
            message = "Error requesting GetTimeSeries(Index=" + tspos[its] + "\") from processor.";
			Message.printWarning(log_level,
					MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Report the problem to software support." ) );
		}
		bean_PropList = bean.getResultsPropList();
		Object prop_contents = bean_PropList.getContents ( "TS" );
		if ( prop_contents == null ) {
            message = "Null value for GetTimeSeries(Index=" + tspos[its] + "\") returned from processor.";
			Message.printWarning(log_level,
			MessageUtil.formatMessageTag( command_tag, ++warning_count),
				routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Report the problem to software support." ) );
		}
		else {	ts = (TS)prop_contents;
		}
		
		if ( ts == null ) {
			// Skip time series.
            message = "Unable to set time series at position " + tspos[its] + " - null time series.";
			Message.printWarning(warning_level,
				MessageUtil.formatMessageTag( command_tag, ++warning_count),
					routine, message );
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
			continue;
		}
		
		notifyCommandProgressListeners ( its, nts, (float)-1.0, "Processing ARMA on " +
            ts.getIdentifier().toStringAliasAndTSID() );
		
		// Do the setting...
		Message.printStatus ( 2, routine, "Processing \"" + ts.getIdentifier()+ "\" using ARMA." );
		try {
			TSUtil_ARMA tsu = new TSUtil_ARMA();
		    ts = tsu.ARMA ( ts, ARMAInterval, __a, __b, __inputInitialValues, outputMinimum, OutputStart_DateTime, OutputEnd_DateTime, readData );
		}
		catch ( Exception e ) {
			message = "Unexpected error processing time series \"" + ts.getIdentifier() + "\" using ARMA (" + e + ").";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message);
			Message.printWarning(3,routine,e);
            status.addToLog ( CommandPhaseType.RUN,
                new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "See the log file for details - report the problem to software support." ) );
		}
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
		return getCommandName() + "()";
	}
    String TSList = props.getValue( "TSList" );
    String TSID = props.getValue( "TSID" );
    String EnsembleID = props.getValue( "EnsembleID" );
	String ARMAInterval = props.getValue( "ARMAInterval" );
    String a = props.getValue( "a" );
	String b = props.getValue("b");
	String RequireCoefficientsSumTo1 = props.getValue( "RequireCoefficientsSumTo1" );
	String InputInitialValues = props.getValue( "InputInitialValues" );
	String OutputStart = props.getValue ( "OutputStart" );
	String OutputEnd = props.getValue ( "OutputEnd" );
	String OutputMinimum = props.getValue( "OutputMinimum" );
	StringBuffer b2 = new StringBuffer ();
    if ( (TSList != null) && (TSList.length() > 0) ) {
        if ( b2.length() > 0 ) {
            b2.append ( "," );
        }
        b2.append ( "TSList=" + TSList );
    }
    if ( (TSID != null) && (TSID.length() > 0) ) {
        if ( b2.length() > 0 ) {
            b2.append ( "," );
        }
        b2.append ( "TSID=\"" + TSID + "\"" );
    }
    if ( (EnsembleID != null) && (EnsembleID.length() > 0) ) {
        if ( b2.length() > 0 ) {
            b2.append ( "," );
        }
        b2.append ( "EnsembleID=\"" + EnsembleID + "\"" );
    }
	if ( (ARMAInterval != null) && (ARMAInterval.length() > 0) ) {
		if ( b2.length() > 0 ) {
			b2.append ( "," );
		}
		b2.append ( "ARMAInterval=" + ARMAInterval );
	}
    if ( (a != null) && (a.length() > 0) ) {
        if ( b2.length() > 0 ) {
            b2.append ( "," );
        }
        b2.append ( "a=\"" + a + "\"");
    }
    if ( (b != null) && (b.length() > 0) ) {
        if ( b2.length() > 0 ) {
            b2.append ( "," );
        }
        b2.append ( "b=\"" + b + "\"");
    }
    if ( (RequireCoefficientsSumTo1 != null) && (RequireCoefficientsSumTo1.length() > 0) ) {
        if ( b2.length() > 0 ) {
            b2.append ( "," );
        }
        b2.append ( "RequireCoefficientsSumTo1=" + RequireCoefficientsSumTo1 );
    }
    if ( (InputInitialValues != null) && (InputInitialValues.length() > 0) ) {
        if ( b2.length() > 0 ) {
            b2.append ( "," );
        }
        b2.append ( "InputInitialValues=\"" + InputInitialValues + "\"");
    }
    if ( (OutputStart != null) && (OutputStart.length() > 0) ) {
        if ( b2.length() > 0 ) {
            b2.append ( "," );
        }
        b2.append ( "OutputStart=\"" + OutputStart + "\"" );
    }
	if ( (OutputEnd != null) && (OutputEnd.length() > 0) ) {
		if ( b2.length() > 0 ) {
			b2.append ( "," );
		}
		b2.append ( "OutputEnd=\"" + OutputEnd + "\"" );
	}
    if ( (OutputMinimum != null) && (OutputMinimum.length() > 0) ) {
        if ( b2.length() > 0 ) {
            b2.append ( "," );
        }
        b2.append ( "OutputMinimum=" + OutputMinimum );
    }
	return getCommandName() + "(" + b2.toString() + ")";
}

}