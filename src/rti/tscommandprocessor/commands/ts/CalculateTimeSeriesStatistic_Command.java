package rti.tscommandprocessor.commands.ts;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import rti.tscommandprocessor.core.TSListType;

import java.util.List;
import java.util.Vector;

import RTi.TS.TS;
import RTi.TS.TSStatisticType;
import RTi.TS.TSUtil_CalculateTimeSeriesStatistic;
import RTi.Util.Math.Regression;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the CheckTimeSeries() command.
*/
public class CalculateTimeSeriesStatistic_Command extends AbstractCommand
implements Command, CommandDiscoverable, ObjectListProvider
{
    
/**
Values for IfNotFound parameter.
*/
protected final String _Ignore = "Ignore";
protected final String _Warn = "Warn";
protected final String _Fail = "Fail";

/**
Values for ProblemType parameter.
*/
protected final String _PROBLEM_TYPE_Check = "Check";

/**
The table that is created (when not operating on an existing table).
*/
private DataTable __table = null;

/**
Constructor.
*/
public CalculateTimeSeriesStatistic_Command ()
{   super();
    setCommandName ( "CalculateTimeSeriesStatistic" );
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
{   //String TSID = parameters.getValue ( "TSID" );
    String Statistic = parameters.getValue ( "Statistic" );
    String AnalysisStart = parameters.getValue ( "AnalysisStart" );
    String AnalysisEnd = parameters.getValue ( "AnalysisEnd" );
    String Value1 = parameters.getValue ( "Value1" );
    String Value2 = parameters.getValue ( "Value2" );
    String Value3 = parameters.getValue ( "Value3" );
    String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);
    
    if ( (Statistic == null) || Statistic.equals("") ) {
        message = "The statistic must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Provide the statistic to calculate." ) );
    }
    else {
        // Make sure that the statistic is known in general
        boolean supported = false;
        TSStatisticType statisticType = TSStatisticType.valueOfIgnoreCase(Statistic);
        if ( statisticType == null ) {
            message = "The statistic (" + Statistic + ") is not recognized.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Select a supported statistic using the command editor." ) );
        }
        else {
            // Make sure that it is in the supported list
            supported = false;
            List<TSStatisticType> statistics = TSUtil_CalculateTimeSeriesStatistic.getStatisticChoices();
            for ( TSStatisticType statistic : statistics ) {
                if ( statisticType == statistic ) {
                    supported = true;
                }
            }
            if ( !supported ) {
                message = "The statistic (" + Statistic + ") is not supported by this command.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Select a supported statistic using the command editor." ) );
            }
        }
       
        // Additional checks that depend on the statistic
        
        if ( supported ) {
            int nRequiredValues = -1;
            try {
                nRequiredValues = TSUtil_CalculateTimeSeriesStatistic.getRequiredNumberOfValuesForStatistic ( statisticType );
            }
            catch ( Exception e ) {
                message = "Statistic \"" + statisticType + "\" is not recognized.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Contact software support." ) );
            }
            
            if ( nRequiredValues >= 1 ) {
                if ( (Value1 == null) || Value1.equals("") ) {
                    message = "Value1 must be specified for the statistic.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Provide Value1." ) );
                }
                else if ( !StringUtil.isDouble(Value1) ) {
                    message = "Value1 (" + Value1 + ") is not a number.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify Value1 as a number." ) );
                }
            }
            
            if ( nRequiredValues >= 2 ) {
                if ( (Value2 == null) || Value2.equals("") ) {
                    message = "Value2 must be specified for the statistic.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Provide Value2." ) );
                }
                else if ( !StringUtil.isDouble(Value2) ) {
                    message = "Value2 (" + Value2 + ") is not a number.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify Value2 as a number." ) );
                }
            }
            
            if ( nRequiredValues == 3 ) {
                if ( (Value3 == null) || Value3.equals("") ) {
                    message = "Value3 must be specified for the statistic.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Provide Value3." ) );
                }
                else if ( !StringUtil.isDouble(Value2) ) {
                    message = "Value3 (" + Value3 + ") is not a number.";
                    warning += "\n" + message;
                    status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify Value3 as a number." ) );
                }
            }
    
            if ( nRequiredValues > 3 ) {
                message = "A maximum of 3 values are supported as input to statistic computation.";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Refer to documentation for statistic.  Contact software support if necessary." ) ); 
            }
        }
    }

    if ( (AnalysisStart != null) && !AnalysisStart.equals("") &&
        !AnalysisStart.equalsIgnoreCase("OutputStart") && !AnalysisStart.equalsIgnoreCase("OutputEnd") ) {
        try {
            DateTime.parse(AnalysisStart);
        }
        catch ( Exception e ) {
            message = "The analysis start date/time \"" + AnalysisStart + "\" is not a valid date/time.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid date/time, OutputStart, or output end." ) );
        }
    }
    if ( (AnalysisEnd != null) && !AnalysisEnd.equals("") &&
        !AnalysisEnd.equalsIgnoreCase("OutputStart") && !AnalysisEnd.equalsIgnoreCase("OutputEnd") ) {
        try {
            DateTime.parse( AnalysisEnd );
        }
        catch ( Exception e ) {
            message = "The analysis end date/time \"" + AnalysisEnd + "\" is not a valid date/time.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Specify a valid date/time, OutputStart, or OutputEnd." ) );
        }
    }
    
    // Check for invalid parameters...
    List<String> valid_Vector = new Vector();
    valid_Vector.add ( "TSList" );
    valid_Vector.add ( "TSID" );
    valid_Vector.add ( "EnsembleID" );
    valid_Vector.add ( "Statistic" );
    valid_Vector.add ( "Value1" );
    valid_Vector.add ( "Value2" );
    valid_Vector.add ( "Value3" );
    valid_Vector.add ( "AnalysisStart" );
    valid_Vector.add ( "AnalysisEnd" );
    valid_Vector.add ( "TableID" );
    valid_Vector.add ( "TableTSIDColumn" );
    valid_Vector.add ( "TableTSIDFormat" );
    valid_Vector.add ( "TableStatisticColumn" );
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
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{   List<String> tableIDChoices =
        TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            (TSCommandProcessor)getCommandProcessor(), this);
    return (new CalculateTimeSeriesStatistic_JDialog ( parent, this, tableIDChoices )).ok();
}

/**
Return the table that is read by this class when run in discovery mode.
*/
private DataTable getDiscoveryTable()
{
    return __table;
}

/**
Return a list of objects of the requested type.  This class only keeps a list of DataTable objects.
*/
public List getObjectList ( Class c )
{   DataTable table = getDiscoveryTable();
    List v = null;
    if ( (table != null) && (c == table.getClass()) ) {
        v = new Vector();
        v.add ( table );
    }
    return v;
}

// Parse command is in the base class

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the
command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{   
    runCommandInternal ( command_number, CommandPhaseType.RUN );
}

/**
Run the command in discovery mode.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommandDiscovery ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{
    runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
}

/**
Run the command.
@param command_number Number of command in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
*/
private void runCommandInternal ( int command_number, CommandPhaseType commandPhase )
throws InvalidCommandParameterException,
CommandWarningException, CommandException
{   String message, routine = getCommandName() + "_Command.runCommand";
    int warning_level = 2;
    String command_tag = "" + command_number;
    int warning_count = 0;
    int log_level = 3;  // Level for non-use messages for log file.
    
    CommandProcessor processor = getCommandProcessor();
    CommandStatus status = getCommandStatus();
    status.clearLog(commandPhase);
    PropList parameters = getCommandParameters();
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        setDiscoveryTable ( null );
    }
    
    // Get the input parameters...
    
    String TSList = parameters.getValue ( "TSList" );
    if ( (TSList == null) || TSList.equals("") ) {
        TSList = TSListType.ALL_TS.toString();
    }
    String TSID = parameters.getValue ( "TSID" );
    String EnsembleID = parameters.getValue ( "EnsembleID" );
    String Statistic = parameters.getValue ( "Statistic" );
    String Value1 = parameters.getValue ( "Value1" );
    Double Value1_Double = null;
    if ( (Value1 != null) && !Value1.equals("") ) {
        Value1_Double = new Double(Value1);
    }
    String Value2 = parameters.getValue ( "Value2" );
    Double Value2_Double = null;
    if ( (Value2 != null) && !Value2.equals("") ) {
        Value2_Double = new Double(Value2);
    }
    String Value3 = parameters.getValue ( "Value3" );
    Double Value3_Double = null;
    if ( (Value3 != null) && !Value3.equals("") ) {
        Value3_Double = new Double(Value3);
    }
    String AnalysisStart = parameters.getValue ( "AnalysisStart" );
    String AnalysisEnd = parameters.getValue ( "AnalysisEnd" );
    String TableID = parameters.getValue ( "TableID" );
    String TableTSIDColumn = parameters.getValue ( "TableTSIDColumn" );
    String TableTSIDFormat = parameters.getValue ( "TableTSIDFormat" );
    String TableStatisticColumn = parameters.getValue ( "TableStatisticColumn" );
    String [] tableStatisticResultsColumn = new String[0];
    if ( (TableStatisticColumn != null) && !TableStatisticColumn.equals("") ) {
        if ( Statistic.equalsIgnoreCase("" + TSStatisticType.TREND_OLS) ) {
            // Output will consist of multiple statistics corresponding to parameter-assigned column + suffix
            tableStatisticResultsColumn = new String[3];
            tableStatisticResultsColumn[0] = "" + TableStatisticColumn + "_Intercept";
            tableStatisticResultsColumn[1] = "" + TableStatisticColumn + "_Slope";
            tableStatisticResultsColumn[2] = "" + TableStatisticColumn + "_R2";
        }
        else {
            // Output will consist of single statistic corresponding to parameter-assigned column
            tableStatisticResultsColumn = new String[1];
            tableStatisticResultsColumn[0] = TableStatisticColumn;
        }
    }
    int [] statisticColumnNum = new int[tableStatisticResultsColumn.length]; // Integer columns for performance
    for ( int i = 0; i < tableStatisticResultsColumn.length; i++ ) {
        statisticColumnNum[i] = -1;
    }

    // Figure out the dates to use for the analysis.
    // Default of null means to analyze the full period.
    DateTime AnalysisStart_DateTime = null;
    DateTime AnalysisEnd_DateTime = null;
    
    try {
        if ( (AnalysisStart != null) && !AnalysisStart.equals("") ) {
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
                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
                throw new InvalidCommandParameterException ( message );
            }
    
            PropList bean_PropList = bean.getResultsPropList();
            Object prop_contents = bean_PropList.getContents ( "DateTime" );
            if ( prop_contents == null ) {
                message = "Null value for AnalysisStart DateTime(DateTime=" +
                AnalysisStart + ") returned from processor.";
                Message.printWarning(log_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify a valid date/time, OutputStart, or OutputEnd." ) );
                throw new InvalidCommandParameterException ( message );
            }
            else {
                AnalysisStart_DateTime = (DateTime)prop_contents;
            }
        }
    }
    catch ( Exception e ) {
        message = "AnalysisStart \"" + AnalysisStart + "\" is invalid.";
        Message.printWarning(warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify a valid date/time, OutputStart, or OutputEnd." ) );
        throw new InvalidCommandParameterException ( message );
    }
    
    try {
        if ( (AnalysisEnd != null) && !AnalysisEnd.equals("") ) {
            PropList request_params = new PropList ( "" );
            request_params.set ( "DateTime", AnalysisEnd );
            CommandProcessorRequestResultsBean bean = null;
            try {
                bean = processor.processRequest( "DateTime", request_params);
            }
            catch ( Exception e ) {
                message = "Error requesting AnalysisEnd DateTime(DateTime=" + AnalysisEnd + ") from processor.";
                Message.printWarning(log_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
                throw new InvalidCommandParameterException ( message );
            }
    
            PropList bean_PropList = bean.getResultsPropList();
            Object prop_contents = bean_PropList.getContents ( "DateTime" );
            if ( prop_contents == null ) {
                message = "Null value for AnalysisStart DateTime(DateTime=" +
                AnalysisStart + "\") returned from processor.";
                Message.printWarning(log_level,
                    MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Specify a valid date/time, OutputStart, or OutputEnd." ) );
                throw new InvalidCommandParameterException ( message );
            }
            else {
                AnalysisEnd_DateTime = (DateTime)prop_contents;
            }
        }
    }
    catch ( Exception e ) {
        message = "AnalysisEnd \"" + AnalysisEnd + "\" is invalid.";
        Message.printWarning(warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Specify a valid date/time, OutputStart, or OutputEnd." ) );
        throw new InvalidCommandParameterException ( message );
    }
    
    // Get the time series to process.  Allow TSID to be a pattern or specific time series...

    List<TS> tslist = null;
    if ( commandPhase == CommandPhaseType.DISCOVERY ) {
        // Get the discovery time series list from all time series above this command
        tslist = TSCommandProcessorUtil.getDiscoveryTSFromCommandsBeforeCommand(
            (TSCommandProcessor)processor, this, TSList, TSID, null, null );
    }
    else if ( commandPhase == CommandPhaseType.RUN ) {
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
            Message.printWarning(log_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report the problem to software support." ) );
        }
        if ( bean == null ) {
            Message.printStatus ( 2, routine, "Bean is null.");
        }
        PropList bean_PropList = bean.getResultsPropList();
        Object o_TSList = bean_PropList.getContents ( "TSToProcessList" );
        if ( o_TSList == null ) {
            message = "Null TSToProcessList returned from processor for GetTimeSeriesToProcess(TSList=\"" + TSList +
            "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\").";
            Message.printWarning ( log_level, MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE, message,
                "Verify that the TSID parameter matches one or more time series - may be OK for partial run." ) );
        }
        else {
            tslist = (List)o_TSList;
            if ( tslist.size() == 0 ) {
                message = "No time series are available from processor GetTimeSeriesToProcess (TSList=\"" + TSList +
                "\" TSID=\"" + TSID + "\", EnsembleID=\"" + EnsembleID + "\").";
                Message.printWarning ( log_level, MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE, message,
                    "Verify that the TSID parameter matches one or more time series - may be OK for partial run." ) );
            }
        }
    }
    
    int nts = tslist.size();
    if ( nts == 0 ) {
        message = "Unable to find time series to process using TSList=\"" + TSList + "\" TSID=\"" + TSID +
            "\", EnsembleID=\"" + EnsembleID + "\".";
        Message.printWarning ( warning_level,
        MessageUtil.formatMessageTag(
        command_tag,++warning_count), routine, message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE, message,
            "Verify that the TSID parameter matches one or more time series - may be OK for partial run." ) );
    }

    // Get the table to process.

    DataTable table = null;
    PropList request_params = null;
    CommandProcessorRequestResultsBean bean = null;
    if ( (TableID != null) && !TableID.equals("") ) {
        // Get the table to be updated/created
        request_params = new PropList ( "" );
        request_params.set ( "TableID", TableID );
        try {
            bean = processor.processRequest( "GetTable", request_params);
            PropList bean_PropList = bean.getResultsPropList();
            Object o_Table = bean_PropList.getContents ( "Table" );
            if ( o_Table != null ) {
                // Found the table so no need to create it
                table = (DataTable)o_Table;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting GetTable(TableID=\"" + TableID + "\") from processor.";
            Message.printWarning(warning_level,
                MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report problem to software support." ) );
        }
    }
    
    if ( warning_count > 0 ) {
        // Input error...
        message = "Insufficient data to run command.";
        status.addToLog ( commandPhase,
            new CommandLogRecord(CommandStatusType.FAILURE, message, "Check input to command." ) );
        Message.printWarning(3, routine, message );
        throw new CommandException ( message );
    }
    
    // Now process...
    
    try {
        if ( commandPhase == CommandPhaseType.DISCOVERY ) {
            if ( table == null ) {
                // Did not find table so is being created in this command
                // Create an empty table and set the ID
                table = new DataTable();
                table.setTableID ( TableID );
                setDiscoveryTable ( table );
            }
        }
        else if ( commandPhase == CommandPhaseType.RUN ) {
            if ( table == null ) {
                // Did not find the table above so create it
                table = new DataTable( /*columnList*/ );
                table.setTableID ( TableID );
                Message.printStatus(2, routine, "Was not able to match existing table \"" + TableID + "\" so created new table.");
                
                // Set the table in the processor...
                
                request_params = new PropList ( "" );
                request_params.setUsingObject ( "Table", table );
                try {
                    processor.processRequest( "SetTable", request_params);
                }
                catch ( Exception e ) {
                    message = "Error requesting SetTable(Table=...) from processor.";
                    Message.printWarning(warning_level,
                            MessageUtil.formatMessageTag( command_tag, ++warning_count),
                            routine, message );
                    status.addToLog ( commandPhase,
                            new CommandLogRecord(CommandStatusType.FAILURE,
                               message, "Report problem to software support." ) );
                }
            }
            // Make sure that the output table includes the TSID columns.
            // Cannot add columns for statistics yet because the statistic type is determined from the
            // analysis object below.  This could result in the command NOT adding statistic columns, which
            // could negatively impact later commands.
            int tableTSIDColumnNumber = -1;
            try {
                tableTSIDColumnNumber = table.getFieldIndex(TableTSIDColumn);
            }
            catch ( Exception e2 ) {
                tableTSIDColumnNumber =
                    table.addField(new TableField(TableField.DATA_TYPE_STRING, TableTSIDColumn, -1, -1), null);
                Message.printStatus(2, routine, "Did not match TableTSIDColumn \"" + TableTSIDColumn +
                    "\" as column table so added to table." );
            }
            // Process the time series and add statistics columns to the table if not found...
            TS ts = null;
            Object o_ts = null;
            for ( int its = 0; its < nts; its++ ) {
                // The the time series to process, from the list that was returned above.
                o_ts = tslist.get(its);
                if ( o_ts == null ) {
                    message = "Time series to process is null.";
                    Message.printWarning(warning_level, MessageUtil.formatMessageTag( command_tag, ++warning_count),
                        routine, message );
                    status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE, message,
                        "Verify that the TSID parameter matches one or more time series - may be OK for partial run." ) );
                    // Go to next time series.
                    continue;
                }
                ts = (TS)o_ts;
                notifyCommandProgressListeners ( its, nts, (float)-1.0, "Calculating statistic for " +
                    ts.getIdentifier().toStringAliasAndTSID() );
                
                try {
                    // Do the calculation...
                    TSStatisticType statisticType = TSStatisticType.valueOfIgnoreCase(Statistic);
                    TSUtil_CalculateTimeSeriesStatistic tsu = new TSUtil_CalculateTimeSeriesStatistic(ts, statisticType,
                        AnalysisStart_DateTime, AnalysisEnd_DateTime, Value1_Double, Value2_Double, Value3_Double );
                    tsu.calculateTimeSeriesStatistic();
                    // Now set the statistic value(s) in the table by matching the row (via TSID) and column
                    // (via statistic column name)
                    if ( table != null ) {
                        if ( (TableStatisticColumn != null) && !TableStatisticColumn.equals("") ) {
                            // See if a matching row exists using the specified TSID column...
                            String tsid = null;
                            if ( (TableTSIDFormat != null) && !TableTSIDFormat.equals("") ) {
                                // Format the TSID using the specified format
                                tsid = ts.formatLegend ( TableTSIDFormat );
                            }
                            else {
                                // Use the alias if available and then the TSID
                                tsid = ts.getAlias();
                                if ( (tsid == null) || tsid.equals("") ) {
                                    tsid = ts.getIdentifierString();
                                }
                            }
                            Message.printStatus(2,routine, "Searching column \"" + TableTSIDColumn + "\" for TSID \"" +
                                tsid + "\"" );
                            TableRecord rec = table.getRecord ( TableTSIDColumn, tsid );
                            Message.printStatus(2,routine, "Searched column \"" + TableTSIDColumn + "\" for TSID \"" +
                                tsid + "\" ... found " + rec );
                            if ( rec == null ) {
                                // Add a blank record.
                                rec = table.addRecord(table.emptyRecord().setFieldValue(tableTSIDColumnNumber, tsid));
                            }
                            Class c = tsu.getStatisticDataClass();
                            for ( int iStat = 0; iStat < statisticColumnNum.length; iStat++ ) {
                                if ( statisticColumnNum[iStat] < 0 ) {
                                    // Have not previously checked for or added the column for the statistic
                                    try {
                                        statisticColumnNum[iStat] = table.getFieldIndex(tableStatisticResultsColumn[iStat]);
                                        // Have a column in the table.  Make sure that it matches in type the statistic
                                        if ( (c == Integer.class) &&
                                            (table.getFieldDataType(statisticColumnNum[iStat]) != TableField.DATA_TYPE_INT) ) {
                                            message = "Existing table column \"" + tableStatisticResultsColumn[iStat] +
                                                "\" is not of correct integer type for statistic";
                                            Message.printWarning ( warning_level,
                                                MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
                                            status.addToLog ( commandPhase,new CommandLogRecord(CommandStatusType.FAILURE,
                                                message, "Change the table column type or let this command create the column." ) );
                                            continue;
                                        }
                                        // TODO SAM 2013-02-10 Handle datetime and other types
                                        if ( ((c == Double.class) || (c == Regression.class)) &&
                                            (table.getFieldDataType(statisticColumnNum[iStat]) != TableField.DATA_TYPE_DOUBLE) ) {
                                            message = "Existing table column \"" + tableStatisticResultsColumn[iStat] +
                                                "\" is not of correct double type for statistic";
                                            Message.printWarning ( warning_level,
                                                MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
                                            status.addToLog ( commandPhase,new CommandLogRecord(CommandStatusType.FAILURE,
                                                message, "Change the table column type or let this command create the column." ) );
                                            continue;
                                        }
                                    }
                                    catch ( Exception e2 ) {
                                        // Column was not found.
                                        // Automatically add the statistic column to the table, initialize with null (not nonValue)
                                        // Create the column using an appropriate type for the statistic
                                        // This call is needed because the statistic could be null or NaN
                                        if ( c == Integer.class ) {
                                            statisticColumnNum[iStat] = table.addField(new TableField(TableField.DATA_TYPE_INT,tableStatisticResultsColumn[iStat],-1,-1), null );
                                        }
                                        else if ( c == DateTime.class ) {
                                            statisticColumnNum[iStat] = table.addField(new TableField(TableField.DATA_TYPE_DATE,tableStatisticResultsColumn[iStat],-1,-1), null );
                                        }
                                        else if ( c == Double.class ) {
                                            statisticColumnNum[iStat] = table.addField(new TableField(TableField.DATA_TYPE_DOUBLE,tableStatisticResultsColumn[iStat],10,4), null );
                                        }
                                        else if ( c == Regression.class ) {
                                            // Intercept, slope, and R2 all are doubles, column names were set up previously
                                            statisticColumnNum[iStat] = table.addField(new TableField(TableField.DATA_TYPE_DOUBLE,tableStatisticResultsColumn[iStat],10,4), null );
                                        }
                                        else {
                                            // Put this in to help software developers
                                            message = "Don't know how to handle statistic result class \"" + c +
                                                "\" for \"" + tableStatisticResultsColumn[iStat] + "\"";
                                            Message.printWarning ( warning_level,
                                                MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
                                            status.addToLog ( commandPhase,new CommandLogRecord(CommandStatusType.FAILURE,
                                                message, "See the log file for details - report the problem to software support." ) );
                                            continue;
                                        }
                                    }
                                    //Message.printStatus(2, routine, "Added column \"" + tableStatisticResultsColumn[iStat] +
                                    //    "\" to table in position " + statisticColumnNum[iStat] + " statistic count [" + iStat + "]" );
                                }
                                // Set the value in the table column...
                                if ( statisticColumnNum.length == 1 ) {
                                    rec.setFieldValue(statisticColumnNum[iStat], tsu.getStatisticResult());
                                }
                                else {
                                    // A statistic with multiple results
                                    if ( statisticType == TSStatisticType.TREND_OLS ) {
                                        // Have 3 statistic values to set - an error computing regression will never
                                        // get to this point
                                        Regression r = (Regression)tsu.getStatisticResult();
                                        if ( iStat == 0 ) {
                                            rec.setFieldValue(statisticColumnNum[iStat], r.getA());
                                        }
                                        else if ( iStat == 1 ) {
                                            rec.setFieldValue(statisticColumnNum[iStat], r.getB());
                                        }
                                        else if ( iStat == 2 ) {
                                            rec.setFieldValue(statisticColumnNum[iStat],
                                                r.getCorrelationCoefficient()*r.getCorrelationCoefficient() );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch ( Exception e ) {
                    message = "Unexpected error calculating time series statistic for \""+ ts.getIdentifier() + " (" + e + ").";
                    Message.printWarning ( warning_level,
                        MessageUtil.formatMessageTag(command_tag,++warning_count),routine,message );
                    Message.printWarning(3,routine,e);
                    status.addToLog ( commandPhase,new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "See the log file for details - report the problem to software support." ) );
                }
            }
        }
    }
    catch ( Exception e ) {
        message = "Unexpected error computing statistic for time series (" + e + ").";
        Message.printWarning ( warning_level, 
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
        Message.printWarning ( 3, routine, e );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Check log file for details." ) );
        throw new CommandException ( message );
    }
    
    if ( warning_count > 0 ) {
        message = "There were " + warning_count + " warnings processing the command.";
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(command_tag, ++warning_count),routine,message);
        throw new CommandWarningException ( message );
    }
    
    status.refreshPhaseSeverity(commandPhase,CommandStatusType.SUCCESS);
}

/**
Set the table that is read by this class in discovery mode.
*/
private void setDiscoveryTable ( DataTable table )
{
    __table = table;
}

/**
Return the string representation of the command.
@param parameters parameters for the command.
*/
public String toString ( PropList parameters )
{   
    if ( parameters == null ) {
        return getCommandName() + "()";
    }
    
    String TSList = parameters.getValue( "TSList" );
    String TSID = parameters.getValue( "TSID" );
    String EnsembleID = parameters.getValue( "EnsembleID" );
    String Statistic = parameters.getValue( "Statistic" );
    String Value1 = parameters.getValue( "Value1" );
    String Value2 = parameters.getValue( "Value2" );
    String Value3 = parameters.getValue( "Value3" );
    String AnalysisStart = parameters.getValue( "AnalysisStart" );
    String AnalysisEnd = parameters.getValue( "AnalysisEnd" );
    String IfNotFound = parameters.getValue ( "IfNotFound" );
    String TableID = parameters.getValue ( "TableID" );
    String TableTSIDColumn = parameters.getValue ( "TableTSIDColumn" );
    String TableTSIDFormat = parameters.getValue ( "TableTSIDFormat" );
    String TableStatisticColumn = parameters.getValue ( "TableStatisticColumn" );
        
    StringBuffer b = new StringBuffer ();

    if ( (TSList != null) && (TSList.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TSList=" + TSList );
    }
    if ( (TSID != null) && (TSID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TSID=\"" + TSID + "\"" );
    }
    if ( (EnsembleID != null) && (EnsembleID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "EnsembleID=\"" + EnsembleID + "\"" );
    }
    if ( (Statistic != null) && (Statistic.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Statistic=\"" + Statistic + "\"" );
    }
    if ( (Value1 != null) && (Value1.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Value1=" + Value1 );
    }
    if ( (Value2 != null) && (Value2.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Value2=" + Value2 );
    }
    if ( (Value3 != null) && (Value3.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Value3=" + Value3 );
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
    if ( IfNotFound != null && IfNotFound.length() > 0 ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IfNotFound=" + IfNotFound );
    }
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (TableTSIDColumn != null) && (TableTSIDColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableTSIDColumn=\"" + TableTSIDColumn + "\"" );
    }
    if ( (TableTSIDFormat != null) && (TableTSIDFormat.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableTSIDFormat=\"" + TableTSIDFormat + "\"" );
    }
    if ( (TableStatisticColumn != null) && (TableStatisticColumn.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableStatisticColumn=\"" + TableStatisticColumn + "\"" );
    }
    
    return getCommandName() + "(" + b.toString() + ")";
}

}