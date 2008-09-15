package rti.tscommandprocessor.commands.ts;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.util.List;
import java.util.Vector;

import RTi.TS.TS;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.TimeInterval;

/**
<p>
This class initializes, checks, and runs the CreateFromList() command.
</p>
*/
public class CreateFromList_Command extends AbstractCommand implements Command, CommandDiscoverable, ObjectListProvider
{

protected final String _DefaultMissingTS = "DefaultMissingTS";
protected final String _IgnoreMissingTS = "IgnoreMissingTS";
protected final String _WarnIfMissingTS = "WarnIfMissingTS";

/**
List of time series read during discovery.  These are TS objects but with mainly the
metadata (TSIdent) filled in.
*/
private Vector __discovery_TS_Vector = null;

/**
Indicates whether the TS Alias version of the command is being used.
*/
protected boolean _use_alias = false;

/**
Constructor.
*/
public CreateFromList_Command ()
{
	super();
	setCommandName ( "CreateFromList" );
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
{
    String routine = getClass().getName() + ".checkCommandParameters";
	String warning = "";
    String message;
    
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
	
	// Get the property values. 
	String ListFile = parameters.getValue("ListFile");
	String IDCol = parameters.getValue("IDCol");
	String InputType = parameters.getValue("InputType");
	String Interval = parameters.getValue("Interval");
	String HandleMissingTSHow = parameters.getValue("HandleMissingTSHow");
    
    if ( (ListFile == null) || (ListFile.length() == 0) ) {
        message = "The list file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing list file." ) );
    }
    else {  String working_dir = null;
        try { Object o = processor.getPropContents ( "WorkingDir" );
                // Working directory is available so use it...
                if ( o != null ) {
                    working_dir = (String)o;
                }
            }
            catch ( Exception e ) {
                message = "Error requesting WorkingDir from processor.";
                warning += "\n" + message;
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Specify an existing list file." ) );
            }
    
        try {
            //String adjusted_path = 
            IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                    TSCommandProcessorUtil.expandParameterValue(processor,this,ListFile)));
        }
        catch ( Exception e ) {
            message = "The list file:\n" +
            "    \"" + ListFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that input file and working directory paths are compatible." ) );
        }
    }
    
    // IDCol
    if ((IDCol != null) && !IDCol.equals("") && !StringUtil.isInteger(IDCol)) {
        message = "The ID column is not an integer.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify a the ID column as an integer." ) );
    }

	// Interval
	if ((Interval == null) || Interval.equals("")) {
        message = "The interval has not been specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify a valid interval." ) );
	}
	else {
		try {
		    TimeInterval.parseInterval ( Interval );;
		} 
		catch (Exception e) {
            message = "The data interval \"" + Interval + "\" is not valid.";
			warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify a valid interval (e.g., 5Minute, 6Hour, Day, Month, Year)" ) );
		}
	}
	
	// Input type
	if ( (InputType == null) || InputType.equals("")) {
        message = "The input type has not been specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify a valid input type (e.g., HydroBase)." ) );
    }
	
	if ( (HandleMissingTSHow != null) && !HandleMissingTSHow.equals("") &&
	        !HandleMissingTSHow.equalsIgnoreCase(_IgnoreMissingTS) &&
            !HandleMissingTSHow.equalsIgnoreCase(_DefaultMissingTS) &&
            !HandleMissingTSHow.equalsIgnoreCase(_WarnIfMissingTS) ) {
            message = "Invalid HandleMissingTSHow flag \"" + HandleMissingTSHow + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,
                    new CommandLogRecord(CommandStatusType.FAILURE,
                            message, "Specify the HandleMissingTSHow as " + _DefaultMissingTS + ", " +
                            _DefaultMissingTS + ", or (default) " + _WarnIfMissingTS + "." ) );
                            
	}

	// Check for invalid parameters...
    Vector valid_Vector = new Vector();
    valid_Vector.add ( "ListFile" );
    valid_Vector.add ( "IDCol" );
    valid_Vector.add ( "Delim" );
    valid_Vector.add ( "ID" );
    valid_Vector.add ( "DataSource" );
    valid_Vector.add ( "DataType" );
    valid_Vector.add ( "Interval" );
    valid_Vector.add ( "Scenario" );
    valid_Vector.add ( "InputType" );
    valid_Vector.add ( "InputName" );
    valid_Vector.add ( "HandleMissingTSHow" );
    valid_Vector.add ( "DefaultUnits" );
    warning = TSCommandProcessorUtil.validateParameterNames ( valid_Vector, this, warning );

	// Throw an InvalidCommandParameterException in case of errors.
	if ( warning.length() > 0 ) {		
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, warning_level ),
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
{	
	// The command will be modified if changed...
	return ( new CreateFromList_JDialog ( parent, this ) ).ok();
}

/**
Free memory for garbage collection.
*/
protected void finalize ()
throws Throwable
{
	super.finalize();
}

/**
Return the list of time series read in discovery phase.
*/
private Vector getDiscoveryTSList ()
{
    return __discovery_TS_Vector;
}

/**
Return the list of data objects read by this object in discovery mode.
*/
public List getObjectList ( Class c )
{
    Vector discovery_TS_Vector = getDiscoveryTSList ();
    if ( (discovery_TS_Vector == null) || (discovery_TS_Vector.size() == 0) ) {
        return null;
    }
    TS datats = (TS)discovery_TS_Vector.elementAt(0);
    // Use the most generic for the base class...
    TS ts = new TS();
    if ( (c == ts.getClass()) || (c == datats.getClass()) ) {
        return discovery_TS_Vector;
    }
    else {
        return null;
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
throws InvalidCommandParameterException, CommandWarningException, CommandException
{   
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run the command.
@param command_number The number of the command being run.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could
not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more
parameter values are invalid.
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException,
       CommandWarningException,
       CommandException
{	String routine = "CreateFromList_Command.runCommand", message;
	int warning_level = 2;
    //int log_level = 3;
	String command_tag = "" + command_number;
	int warning_count = 0;
	    
    // Get and clear the status and clear the run log...
    
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    CommandProcessor processor = getCommandProcessor();

	// Get the command properties not already stored as members.
	PropList parameters = getCommandParameters();
	String ListFile = parameters.getValue("ListFile");
    String IDCol = parameters.getValue ( "IDCol" );
    String Delim = parameters.getValue ( "Delim" );
    if ( Delim == null ) {
        Delim = " ,"; // Default
    }
    String ID = parameters.getValue ( "ID" );
    if ( ID == null ) {
        ID = "*"; // Default
    }
    int IDCol_int = 0;
    if ( IDCol != null ) {
        IDCol_int = StringUtil.atoi ( IDCol ) - 1;
    }
    String idpattern_Java = StringUtil.replaceString(ID,"*",".*");
    String DataSource = parameters.getValue ( "DataSource" );
    if ( DataSource == null ) {
        DataSource = "";
    }
    String DataType = parameters.getValue ( "DataType" );
    if ( DataType == null ) {
        DataType = "";
    }
    String Interval = parameters.getValue ( "Interval" );
    if ( Interval == null ) {
        Interval = "";
    }
    String Scenario = parameters.getValue ( "Scenario" );
    if ( Scenario == null ) {
        Scenario = "";
    }
    String InputType = parameters.getValue ( "InputType" );
    String InputName = parameters.getValue ( "InputName" );
    if ( InputName == null ) {
        // Set to empty string so check to facilitate processing...
        InputName = "";
    }
    String HandleMissingTSHow = parameters.getValue("HandleMissingTSHow");
    if ( (HandleMissingTSHow == null) || HandleMissingTSHow.equals("")) {
        HandleMissingTSHow = _IgnoreMissingTS; // default
    }
    String DefaultUnits = parameters.getValue("DefaultUnits");
    
	// Read the file.
    Vector tslist = new Vector();   // Keep the list of time series
    String ListFile_full = ListFile;
	try {
        boolean read_data = true;
        if ( commandPhase == CommandPhaseType.DISCOVERY ){
            read_data = false;
        }
        ListFile_full = IOUtil.verifyPathForOS(
                IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                        TSCommandProcessorUtil.expandParameterValue(processor,this,ListFile)));
     
        // Read using the table...
    
        PropList props = new PropList ("");
        props.set ( "Delimiter=" + Delim ); // see existing prototype
        props.set ( "CommentLineIndicator=#" ); // New - skip lines that start with this
        props.set ( "TrimStrings=True" );   // If true, trim strings after reading.
        DataTable table = DataTable.parseFile ( IOUtil.getPathUsingWorkingDir(ListFile_full), props );
        
        int tsize = 0;
        if ( table != null ) {
            tsize = table.getNumberOfRecords();
        }
    
        Message.printStatus ( 2, "", "List file has " + tsize + " records and " + table.getNumberOfFields() + " fields" );
    
        // Loop through the records in the table and match the identifiers...
    
        StringBuffer tsident_string = new StringBuffer();
        TableRecord rec = null;
        String id;
        TS ts = null;
        for ( int i = 0; i < tsize; i++ ) {
            rec = table.getRecord ( i );
            id = (String)rec.getFieldValue ( IDCol_int );
            if ( !StringUtil.matchesIgnoreCase(id,idpattern_Java) ) {
                // Does not match...
                continue;
            }
    
            tsident_string.setLength(0);
            tsident_string.append ( id + "." + DataSource + "." + DataType + "." + Interval + "~" + InputType );
            if ( InputName.length() > 0 ) {
                tsident_string.append ( "~" + InputName );
            }
            try {
                // Make a request to the processor...
                String TSID = tsident_string.toString();
                PropList request_params = new PropList ( "" );
                request_params.set ( "TSID", tsident_string.toString() );
                request_params.setUsingObject ( "WarningLevel", new Integer(warning_level) );
                request_params.set ( "CommandTag", command_tag );
                request_params.set ( "HandleMissingTSHow", HandleMissingTSHow );
                request_params.setUsingObject ( "ReadData", new Boolean(read_data) );
                CommandProcessorRequestResultsBean bean = null;
                try {
                    bean = processor.processRequest( "ReadTimeSeries", request_params);
                    PropList bean_PropList = bean.getResultsPropList();
                    Object o_TS = bean_PropList.getContents ( "TS" );
                    if ( o_TS != null ) {
                        ts = (TS)o_TS;
                    }
                }
                catch ( Exception e ) {
                    message = "Error requesting ReadTimeSeries(TSID=\"" + TSID + "\") from processor + (" +
                    e + ").";
                    //Message.printWarning(3, routine, e );
                    Message.printWarning(warning_level,
                            MessageUtil.formatMessageTag( command_tag, ++warning_count),
                            routine, message );
                    status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                    message, "Check the log file.  Report the problem to software support." ) );
                    ts = null;
                }
                if ( ts != null ) {
                    if ( (DefaultUnits != null) && (ts.getDataUnits().length() == 0) ) {
                        // Time series has no units so assign default.
                        ts.setDataUnits ( DefaultUnits );
                    }
                    tslist.add ( ts );
                }
            }
            catch ( Exception e1 ) {
                message = "Unexpected error reading time series \"" + tsident_string + "\" (" + e1 + ")";
                Message.printWarning ( warning_level,
                    MessageUtil.formatMessageTag(
                        command_tag, ++warning_count ),
                    routine, message );
                Message.printWarning ( 3, routine, e1 );
                status.addToLog(commandPhase,
                        new CommandLogRecord( CommandStatusType.FAILURE, message,"Check the log file for details."));
                throw new CommandException ( message );
            }
            /* TODO SAM 2008-09-15 Evaluate how to re-implement
            // Cancel processing if the user has indicated to do so...
            if ( __ts_processor.getCancelProcessingRequested() ) {
                return;
            }
            */
    	}
    }
	catch ( Exception e ) {
		message = "Unexpected error reading time series for list file. \"" + ListFile_full + "\" (" + e + ")";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(
				command_tag, ++warning_count ),
			routine, message );
		Message.printWarning ( 3, routine, e );
        status.addToLog(commandPhase,
                new CommandLogRecord( CommandStatusType.FAILURE, message,"Check the log file for details."));
		throw new CommandException ( message );
	}
    
    if ( commandPhase == CommandPhaseType.RUN ) {
        if ( tslist != null ) {
            // Further process the time series...
            // This makes sure the period is at least as long as the output period...
            int wc = TSCommandProcessorUtil.processTimeSeriesListAfterRead( processor, this, tslist );
            if ( wc > 0 ) {
                message = "Error post-processing series after read.";
                Message.printWarning ( warning_level, 
                    MessageUtil.formatMessageTag(command_tag,
                    ++warning_count), routine, message );
                    status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                    message, "Report the problem to software support." ) );
                throw new CommandException ( message );
            }
    
            // Now add the list in the processor...
            
            int wc2 = TSCommandProcessorUtil.appendTimeSeriesListToResultsList ( processor, this, tslist );
            if ( wc2 > 0 ) {
                message = "Error adding time series after read.";
                Message.printWarning ( warning_level, 
                    MessageUtil.formatMessageTag(command_tag,
                    ++warning_count), routine, message );
                    status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                                    message, "Report the problem to software support." ) );
                throw new CommandException ( message );
            }
        }
    }
    else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTSList ( tslist );
    }

	// Throw CommandWarningException in case of problems.
	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings processing the command.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag, ++warning_count ), routine, message );
		throw new CommandWarningException ( message );
	}
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the list of time series read in discovery phase.
*/
private void setDiscoveryTSList ( Vector discovery_TS_Vector )
{
    __discovery_TS_Vector = discovery_TS_Vector;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{
	if ( props == null ) {
		return getCommandName() + "()";
	}

    String ListFile = props.getValue ( "ListFile" );
    String IDCol = props.getValue ( "IDCol" );
    String Delim = props.getValue ( "Delim" );
    String ID = props.getValue ( "ID" );
    String DataSource = props.getValue ( "DataSource" );
    String DataType = props.getValue ( "DataType" );
    String Interval = props.getValue ( "Interval" );
    String Scenario = props.getValue ( "Scenario" );
    String InputType = props.getValue ( "InputType" );
    String InputName = props.getValue ( "InputName" );
    String HandleMissingTSHow = props.getValue ( "HandleMissingTSHow" );
    String DefaultUnits = props.getValue ( "DefaultUnits" );

	StringBuffer b = new StringBuffer ();

	if ((ListFile != null) && (ListFile.length() > 0)) {
		b.append("ListFile=\"" + ListFile + "\"");
	}
    if ((IDCol != null) && (IDCol.length() > 0)) {
        if (b.length() > 0) {
            b.append(",");
        }
        b.append("IDCol=" + IDCol );
    }
	if ((Delim != null) && (Delim.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("Delim=\"" + Delim + "\"");
	}
	if ((ID != null) && (ID.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("ID=\"" + ID + "\"");
	}
	if ((DataSource != null) && (DataSource.length() > 0)) {
		if (b.length() > 0) {
			b.append(",");
		}
		b.append("DataSource=\"" + DataSource + "\"");
	}
    if ((DataType != null) && (DataType.length() > 0)) {
        if (b.length() > 0) {
            b.append(",");
        }
        b.append("DataType=\"" + DataType + "\"");
    }
    if ((Interval != null) && (Interval.length() > 0)) {
        if (b.length() > 0) {
            b.append(",");
        }
        b.append("Interval=\"" + Interval + "\"");
    }
    if ((Scenario != null) && (Scenario.length() > 0)) {
        if (b.length() > 0) {
            b.append(",");
        }
        b.append("Scenario=\"" + Scenario + "\"");
    }
    if ((InputType != null) && (InputType.length() > 0)) {
        if (b.length() > 0) {
            b.append(",");
        }
        b.append("InputType=\"" + InputType + "\"");
    }
    if ((InputName != null) && (InputName.length() > 0)) {
        if (b.length() > 0) {
            b.append(",");
        }
        b.append("InputName=\"" + InputName + "\"");
    }
    if ((HandleMissingTSHow != null) && (HandleMissingTSHow.length() > 0)) {
        if (b.length() > 0) {
            b.append(",");
        }
        b.append("HandleMissingTSHow=" + HandleMissingTSHow );
    }
    if ((DefaultUnits != null) && (DefaultUnits.length() > 0)) {
        if (b.length() > 0) {
            b.append(",");
        }
        b.append("DefaultUnits=\"" + DefaultUnits + "\"");
    }

	return getCommandName() + "(" + b.toString() + ")";
}

}