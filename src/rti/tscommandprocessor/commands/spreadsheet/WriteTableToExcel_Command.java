package rti.tscommandprocessor.commands.spreadsheet;

import javax.swing.JFrame;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.Command;
//import RTi.Util.IO.CommandDiscoverable;
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
//import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;
import RTi.Util.IO.IOUtil;
import RTi.Util.String.StringDictionary;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableColumnType;
import RTi.Util.Table.TableField;
import RTi.Util.Time.DateTime;

/**
This class initializes, checks, and runs the WriteTableToExcel() command, using Apache POI.
A useful link is:  http://poi.apache.org/spreadsheet/quick-guide.html
*/
public class WriteTableToExcel_Command extends AbstractCommand implements Command
{

/**
Possible values for ExcelColumnNames parameter.
*/
protected final String _FirstRowInRange = "FirstRowInRange";
protected final String _None = "None";
protected final String _RowBeforeRange = "RowBeforeRange";

/**
Possible values for WriteAllAsText parameter.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Possible values for ColumnWidths, CellTypes, ColumnDecimalPlaces parameter.
*/
protected final String _Auto = "Auto";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public WriteTableToExcel_Command ()
{	super();
	setCommandName ( "WriteTableToExcel" );
}

// TODO SAM 2013-08-12 This can be optimized to not have to check column name and do upper-case conversions
/**
Evaluate whether a cell value matches an exclude pattern.
@param columnName name of Excel column being checked
@param cellValue cell value as string, to check
@param filtersMap map of column 
@return true if the cell matches a filter
*/
private boolean cellMatchesFilter ( String columnName, String cellValue, Hashtable<String,String> filtersMap )
{
    if ( filtersMap == null ) {
        return false;
    }
    Enumeration keys = filtersMap.keys();
    String key = null;
    // Compare as upper case to treat as case insensitive
    String cellValueUpper = null;
    if ( cellValue != null ) {
        cellValueUpper = cellValue.toUpperCase();
    }
    String columnNameUpper = columnName.toUpperCase();
    String pattern;
    while ( keys.hasMoreElements() ) {
        key = (String)keys.nextElement();
        pattern = filtersMap.get(key);
        //Message.printStatus(2,"","Checking column \"" + columnNameUpper + "\" against key \"" + key +
        //    "\" for cell value \"" + cellValueUpper + "\" and pattern \"" + pattern + "\"" );
        if ( columnNameUpper.equals(key) ) {
            if ( ((cellValue == null) || (cellValue.length() == 0)) &&
                ((pattern == null) || (pattern.length() == 0)) ) {
                // Blank cell should be ignored
                return true;
            }
            else if ( (cellValueUpper != null) && cellValueUpper.matches(pattern) ) {
                return true;
            }
        }
    }
    return false;
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
{	String TableID = parameters.getValue ( "TableID" );
    String OutputFile = parameters.getValue ( "OutputFile" );
    String ExcelColumnNames = parameters.getValue ( "ExcelColumnNames" );
	String KeepOpen = parameters.getValue ( "KeepOpen" );
	String warning = "";
    String message;
    
    CommandStatus status = getCommandStatus();
    status.clearLog(CommandPhaseType.INITIALIZATION);

	// If the input file does not exist, warn the user...

	String working_dir = null;
	
	CommandProcessor processor = getCommandProcessor();
	
    if ( (TableID == null) || (TableID.length() == 0) ) {
        message = "The table identifier must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify the table identifier." ) );
    }
	try {
	    Object o = processor.getPropContents ( "WorkingDir" );
		// Working directory is available so use it...
		if ( o != null ) {
			working_dir = (String)o;
		}
	}
	catch ( Exception e ) {
        message = "Error requesting WorkingDir from processor.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Report the problem to software support." ) );
	}

	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
        message = "The Excel output file must be specified.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
                new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Specify an existing Excel output file." ) );
	}
	/** TODO SAM 2014-01-12 Evaluate whether to only do this check at run-time
	else {
        try {
            String adjusted_path = IOUtil.verifyPathForOS (IOUtil.adjustPath ( working_dir, OutputFile) );
			File f = new File ( adjusted_path );
			if ( !f.exists() ) {
                message = "The Excel output file does not exist:  \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,
                        new CommandLogRecord(CommandStatusType.FAILURE,
                                message, "Verify that the Excel output file exists - may be OK if created at run time." ) );
			}
			f = null;
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
	*/
	
    if ( ExcelColumnNames != null &&
        !ExcelColumnNames.equalsIgnoreCase(_FirstRowInRange) && !ExcelColumnNames.equalsIgnoreCase(_None) &&
        !ExcelColumnNames.equalsIgnoreCase(_RowBeforeRange) && !ExcelColumnNames.equalsIgnoreCase("")) {
        message = "ExcelColumnNames is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "ExcelColumnNames must be " + _FirstRowInRange +
                ", " + _None + " (default), or " + _RowBeforeRange ) );
    }
    
    if ( KeepOpen != null && !KeepOpen.equalsIgnoreCase(_True) && 
        !KeepOpen.equalsIgnoreCase(_False) && !KeepOpen.equalsIgnoreCase("") ) {
        message = "KeepOpen is invalid.";
        warning += "\n" + message;
        status.addToLog ( CommandPhaseType.INITIALIZATION,
            new CommandLogRecord(CommandStatusType.FAILURE,
                message, "KeepOpen must be specified as " + _False + " (default) or " + _True ) );
    }

	// TODO SAM 2005-11-18 Check the format.
    
	//  Check for invalid parameters...
	List<String> validList = new ArrayList<String>(13);
    validList.add ( "TableID" );
    validList.add ( "IncludeColumns" );
    validList.add ( "OutputFile" );
    validList.add ( "Worksheet" );
    validList.add ( "ExcelAddress" );
    validList.add ( "ExcelNamedRange" );
    validList.add ( "ExcelTableName" );
    validList.add ( "ExcelColumnNames" );
    validList.add ( "ColumnExcludeFilters" );
    validList.add ( "NumberPrecision" );
    validList.add ( "WriteAllAsText" );
    validList.add ( "ColumnNamedRanges" );
    validList.add ( "KeepOpen" );
    validList.add ( "ColumnCellTypes" );
    validList.add ( "ColumnWidths" );
    validList.add ( "ColumnDecimalPlaces" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );    

	if ( warning.length() > 0 ) {
		Message.printWarning ( warning_level,
		MessageUtil.formatMessageTag(command_tag,warning_level),warning );
		throw new InvalidCommandParameterException ( warning );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed).
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
    List<String> tableIDChoices = TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
        (TSCommandProcessor)getCommandProcessor(), this);
	return (new WriteTableToExcel_JDialog ( parent, this, tableIDChoices )).ok();
}

/**
Get the array of cell ranges based on one of the input address methods.
@param wb the Excel workbook object
@param sheet the sheet in the workbook, read in entirety if no other address information is given
@param excelAddress Excel address range (e.g., A1:D10 or $A1:$D10 or variant)
@param excelNamedRange a named range
@param excelTableName a table name, treated as named range
@return null if no area reference can be determined
*/
private AreaReference getAreaReference ( Workbook wb, Sheet sheet,
    String excelAddress, String excelNamedRange, String excelTableName )
{   String routine = "WriteTableToExcel_Command.getAreaReference";
    if ( (excelTableName != null) && (excelTableName.length() > 0) ) {
        // Table name takes precedence as range name
        excelNamedRange = excelTableName;
    }
    // If sheet is specified but excelAddress, String excelNamedRange, String excelTableName are not,
    // read the entire sheet
    if ( ((excelAddress == null) || (excelAddress.length() == 0)) &&
        ((excelNamedRange == null) || (excelNamedRange.length() == 0)) ) {
        // Examine the sheet for blank columns/cells.  POI provides methods for the rows...
        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();
        Message.printStatus(2, routine, "Sheet firstRow=" + firstRow + ", lastRow=" + lastRow );
        // ...but have to iterate through the rows as per:
        //  http://stackoverflow.com/questions/2194284/how-to-get-the-last-column-index-reading-excel-file
        Row row;
        int firstCol = -1;
        int lastCol = -1;
        int cellNum; // Index of cell in row (not column number?)
        int col;
        for ( int iRow = firstRow; iRow <= lastRow; iRow++ ) {
            row = sheet.getRow(iRow);
            if ( row == null ) {
                // TODO SAM 2013-06-28 Sometimes this happens with extra rows at the end of a worksheet?
                continue;
            }
            cellNum = row.getFirstCellNum(); // Not sure what this returns if no columns.  Assume -1
            if ( cellNum >= 0 ) {
                col = row.getCell(cellNum).getColumnIndex();
                if ( firstCol < 0 ) {
                    firstCol = col;
                }
                else {
                    firstCol = Math.min(firstCol, col);
                }
            }
            cellNum = row.getLastCellNum() - 1; // NOTE -1, as per API docs
            if ( cellNum >= 0 ) {
                col = row.getCell(cellNum).getColumnIndex();
                if ( lastCol < 0 ) {
                    lastCol = col;
                }
                else {
                    lastCol = Math.max(lastCol, col);
                }
            }
            Message.printStatus(2, routine, "row " + iRow + ", firstCol=" + firstCol + ", lastCol=" + lastCol );
        }
        // Return null if the any of the row column limits were not determined
        if ( (firstRow < 0) || (firstCol < 0) || (lastRow < 0) || (lastCol < 0) ) {
            return null;
        }
        else {
            return new AreaReference(new CellReference(firstRow,firstCol), new CellReference(lastRow,lastCol));
        }
    }
    if ( (excelAddress != null) && (excelAddress.length() > 0) ) {
        return new AreaReference(excelAddress);
    }
    else if ( (excelNamedRange != null) && (excelNamedRange.length() > 0) ) {
        int namedCellIdx = wb.getNameIndex(excelNamedRange);
        if ( namedCellIdx < 0 ) {
            Message.printWarning(3, routine, "Unable to get Excel internal index for named range \"" +
                excelNamedRange + "\"" );
            return null;
        }
        Name aNamedCell = wb.getNameAt(namedCellIdx);

        // Retrieve the cell at the named range and test its contents
        // Will get back one AreaReference for C10, and
        //  another for D12 to D14
        AreaReference[] arefs = AreaReference.generateContiguous(aNamedCell.getRefersToFormula());
        // Can only handle one area
        if ( arefs.length != 1 ) {
            return null;
        }
        else {
            return arefs[0];
        }
    }
    else {
        return null;
    }
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
    List<File> list = new Vector();
    if ( getOutputFile() != null ) {
        list.add ( getOutputFile() );
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
Parse the command string into a PropList of parameters.  Use this to translate old syntax to new.
@param command_string A string command to parse.
@exception InvalidCommandSyntaxException if during parsing the command is determined to have invalid syntax.
@exception InvalidCommandParameterException if during parsing the command parameters are determined to be invalid.
*/
public void parseCommand ( String command_string )
throws InvalidCommandSyntaxException, InvalidCommandParameterException
{
    // First parse as usual
    super.parseCommand(command_string);
    PropList props = getCommandParameters();
    // Translate NumberPrecision=N to ColumnDecimalPlaces=Default:N
    String prop = props.getValue("NumberPrecision");
    if ( prop != null ) {
        String ColumnDecimalPlaces = props.getValue ( "ColumnDecimalPlaces" );
        StringDictionary columnDecimalPlaces = new StringDictionary(ColumnDecimalPlaces,":",",");
        LinkedHashMap<String,String> hm = columnDecimalPlaces.getLinkedHashMap();
        String prop2 = hm.get("Default");
        if ( prop2 == null ) {
            // Set the property
            hm.put("Default",prop2);
            props.set("ColumnDecimalPlaces",columnDecimalPlaces.toString());
        }
        props.unSet("NumberPrecision");
    }
    // WriteAllAsText=True|False to ColumnCellType=Default:Text
    prop = props.getValue("WriteAllAsText");
    if ( prop != null ) {
        String ColumnCellTypes = props.getValue ( "ColumnCellTypes" );
        StringDictionary columnCellTypes = new StringDictionary(ColumnCellTypes,":",",");
        LinkedHashMap<String,String> hm = columnCellTypes.getLinkedHashMap();
        String prop2 = hm.get("Default");
        if ( prop2 == null ) {
            // Set the property
            hm.put("Default","Text");
            props.set("ColumnCellTypes",columnCellTypes.toString());
        }
        props.unSet("NumberPrecision");
        props.unSet("WriteAllAsText");
    }
}

/**
Run the command.
@param command_number Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException
{	String routine = "WriteTableToExcel_Command.runCommand",message = "";
	int warning_level = 2;
	String command_tag = "" + command_number;	
	int warning_count = 0;
    
    CommandStatus status = getCommandStatus();
    CommandPhaseType commandPhase = CommandPhaseType.RUN;
    status.clearLog(commandPhase);
    
    // Clear the output file
    setOutputFile ( null );

	// Make sure there are time series available to operate on...
	
	PropList parameters = getCommandParameters();
	CommandProcessor processor = getCommandProcessor();

    String TableID = parameters.getValue ( "TableID" );
    String IncludeColumns = parameters.getValue ( "IncludeColumns" );
    String [] includeColumns = null;
    if ( (IncludeColumns != null) && (IncludeColumns.length() != 0) ) {
        // Use the provided columns
        includeColumns = IncludeColumns.split(",");
        for ( int i = 0; i < includeColumns.length; i++ ) {
            includeColumns[i] = includeColumns[i].trim();
        }
    }
	String OutputFile = parameters.getValue ( "OutputFile" );
	String Worksheet = parameters.getValue ( "Worksheet" );
	String ExcelAddress = parameters.getValue ( "ExcelAddress" );
	String ExcelNamedRange = parameters.getValue ( "ExcelNamedRange" );
	String ExcelTableName = parameters.getValue ( "ExcelTableName" );
	String ExcelColumnNames = parameters.getValue ( "ExcelColumnNames" );
	if ( (ExcelColumnNames == null) || ExcelColumnNames.equals("") ) {
	    ExcelColumnNames = _None; // Default
	}
    String ColumnExcludeFilters = parameters.getValue ( "ColumnExcludeFilters" );
    Hashtable<String,String> columnExcludeFiltersMap = null;
    if ( (ColumnExcludeFilters != null) && (ColumnExcludeFilters.length() > 0) && (ColumnExcludeFilters.indexOf(":") > 0) ) {
        columnExcludeFiltersMap = new Hashtable<String,String>();
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(ColumnExcludeFilters, ",", 0 );
        // Now break pairs and put in hashtable
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            String tableColumn = parts[0].trim().toUpperCase();
            String pattern = "";
            if ( parts.length > 1 ) {
                // Use upper-case to facilitate case-independent comparisons, and replace * globbing with internal Java notation
                pattern = parts[1].trim().toUpperCase().replace("*", ".*");
            }
            columnExcludeFiltersMap.put(tableColumn, pattern );
        }
    }
    String ColumnNamedRanges = parameters.getValue ( "ColumnNamedRanges" );
    Hashtable columnNamedRanges = new Hashtable();
    if ( (ColumnNamedRanges != null) && (ColumnNamedRanges.length() > 0) && (ColumnNamedRanges.indexOf(":") > 0) ) {
        // First break map pairs by comma
        List<String>pairs = StringUtil.breakStringList(ColumnNamedRanges, ",", 0 );
        // Now break pairs and put in hashtable
        for ( String pair : pairs ) {
            String [] parts = pair.split(":");
            columnNamedRanges.put(parts[0].trim(), parts[1].trim() );
        }
    }
    String KeepOpen = parameters.getValue ( "KeepOpen" );
    boolean keepOpen = false; // default
    if ( (KeepOpen != null) && KeepOpen.equalsIgnoreCase("True") ) {
        keepOpen = true;
    }
    String ColumnCellTypes = parameters.getValue ( "ColumnCellTypes" );
    StringDictionary columnCellTypes = new StringDictionary(ColumnCellTypes,":",",");
    String ColumnWidths = parameters.getValue ( "ColumnWidths" );
    StringDictionary columnWidths = new StringDictionary(ColumnWidths,":",",");
    String ColumnDecimalPlaces = parameters.getValue ( "ColumnDecimalPlaces" );
    StringDictionary columnDecimalPlaces = new StringDictionary(ColumnDecimalPlaces,":",",");
	
	// Get the table to process
	
    PropList request_params = new PropList ( "" );
    request_params.set ( "TableID", TableID );
    CommandProcessorRequestResultsBean bean = null;
    try {
        bean = processor.processRequest( "GetTable", request_params);
    }
    catch ( Exception e ) {
        message = "Error requesting GetTable(TableID=\"" + TableID + "\") from processor.";
        Message.printWarning(warning_level,
            MessageUtil.formatMessageTag( command_tag, ++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Report problem to software support." ) );
    }
    PropList bean_PropList = bean.getResultsPropList();
    Object o_Table = bean_PropList.getContents ( "Table" );
    DataTable table = null;
    if ( o_Table == null ) {
        message = "Unable to find table to process using TableID=\"" + TableID + "\".";
        Message.printWarning ( warning_level,
        MessageUtil.formatMessageTag( command_tag,++warning_count), routine, message );
        status.addToLog ( CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that a table exists with the requested ID." ) );
    }
    else {
        table = (DataTable)o_Table;
    }

	String OutputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),OutputFile) );
	if ( (ExcelUtil.getOpenWorkbook(OutputFile_full) == null) && !IOUtil.fileExists(OutputFile_full) ) {
		message += "\nThe Excel workbook file \"" + OutputFile_full + "\" is not open from a previous command and does not exist.";
		++warning_count;
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the Excel workbook file is open in memory or exists as a file." ) );
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings for command parameters.";
		Message.printWarning ( 2,
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine,message);
		throw new InvalidCommandParameterException ( message );
	}

	// Now process the file...

	List<String> problems = new ArrayList<String>();
	try {
        // Check that named ranges match columns
	    if ( columnNamedRanges != null ) {
    	    Enumeration keys = columnNamedRanges.keys();
            String key = null;
            while ( keys.hasMoreElements() ) {
                key = (String)keys.nextElement(); // Column name
                // Find the table column
                if ( table.getFieldIndex(key) < 0 ) {
                    message += "\nThe column \"" + key + "\" for a named range does not exist in the table.";
                    ++warning_count;
                    status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                        message, "Verify that the column name to be used for the named range." ) );
                }
            }
	    }
	    int [] includeColumnNumbers = null;
	    if ( (includeColumns != null) && (includeColumns.length > 0) ) {
	        // Get the column numbers to output
	        includeColumnNumbers = new int[includeColumns.length];
	        for ( int i = 0; i < includeColumns.length; i++ ) {
	            try {
	                includeColumnNumbers[i] = table.getFieldIndex(includeColumns[i]);
	            }
	            catch ( Exception e ) {
	                message = "Table colument to include in output \"" + includeColumns[i] + "\" does not exist in table.";
	                Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
	                status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
	                    message, "Check the table column names." ) );
	                includeColumnNumbers[i] = -1;
	            }
	        }
	        // Remove -1 so only valid columns are output
	        int count = 0;
	        for ( int i = 0; i < includeColumnNumbers.length; i++ ) {
	            if ( includeColumnNumbers[i] >= 0 ) {
	                ++count;
	            }
	        }
	        int [] includeColumnNumbers2 = new int[count];
	        count = 0;
	        for ( int i = 0; i < includeColumnNumbers.length; i++ ) {
	            if ( includeColumnNumbers[i] >= 0 ) {
	                includeColumnNumbers2[count++] = includeColumnNumbers[i];
	            }
	        }
	        includeColumnNumbers = includeColumnNumbers2;
	    }
	    else {
	        // Output all the columns
	        includeColumnNumbers = new int[table.getNumberOfFields()];
	        for ( int i = 0; i < includeColumnNumbers.length; i++ ) {
	            includeColumnNumbers[i] = i;
	        }
	    }
        writeTableToExcelFile ( table, includeColumnNumbers, OutputFile_full, Worksheet,
            ExcelAddress, ExcelNamedRange, ExcelTableName, ExcelColumnNames, columnExcludeFiltersMap,
            columnNamedRanges, keepOpen, columnCellTypes, columnWidths, columnDecimalPlaces, problems );
        for ( String problem: problems ) {
            Message.printWarning ( 3, routine, problem );
            message = "Error writing to Excel: " + problem;
            Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
            status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Check the log file for exceptions." ) );
        }
        // Save the output file name...
        setOutputFile ( new File(OutputFile_full));
	}
	catch ( Exception e ) {
		Message.printWarning ( 3, routine, e );
		message = "Unexpected error writing table to Excel workbook file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( 2, MessageUtil.formatMessageTag(command_tag, ++warning_count), routine,message );
        status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
            message, "Verify that the file exists and is writeable." ) );
		throw new CommandWarningException ( message );
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
Set the output file that is created by this command.  This is only used internally.
*/
private void setOutputFile ( File file )
{
    __OutputFile_File = file;
}

/**
Return the string representation of the command.
*/
public String toString ( PropList props )
{	if ( props == null ) {
		return getCommandName() + "()";
	}
    String TableID = props.getValue( "TableID" );
    String IncludeColumns = props.getValue( "IncludeColumns" );
	String OutputFile = props.getValue( "OutputFile" );
	String Worksheet = props.getValue( "Worksheet" );
	String ExcelAddress = props.getValue("ExcelAddress");
	String ExcelNamedRange = props.getValue("ExcelNamedRange");
	String ExcelTableName = props.getValue("ExcelTableName");
	String ExcelColumnNames = props.getValue("ExcelColumnNames");
	String ColumnExcludeFilters = props.getValue("ColumnExcludeFilters");
	String ColumnNamedRanges = props.getValue("ColumnNamedRanges");
	String KeepOpen = props.getValue("KeepOpen");
	String ColumnCellTypes = props.getValue("ColumnCellTypes");
	String ColumnWidths = props.getValue("ColumnWidths");
	String ColumnDecimalPlaces = props.getValue("ColumnDecimalPlaces");
	StringBuffer b = new StringBuffer ();
    if ( (TableID != null) && (TableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TableID=\"" + TableID + "\"" );
    }
    if ( (IncludeColumns != null) && (IncludeColumns.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeColumns=\"" + IncludeColumns + "\"" );
    }
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"" );
	}
    if ( (Worksheet != null) && (Worksheet.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "Worksheet=\"" + Worksheet + "\"" );
    }
	if ( (ExcelAddress != null) && (ExcelAddress.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ExcelAddress=\"" + ExcelAddress + "\"" );
	}
	if ( (ExcelNamedRange != null) && (ExcelNamedRange.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ExcelNamedRange=\"" + ExcelNamedRange + "\"" );
	}
	if ( (ExcelTableName != null) && (ExcelTableName.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "ExcelTableName=\"" + ExcelTableName + "\"" );
	}
    if ( (ExcelColumnNames != null) && (ExcelColumnNames.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ExcelColumnNames=" + ExcelColumnNames );
    }
    if ( (ColumnExcludeFilters != null) && (ColumnExcludeFilters.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnExcludeFilters=\"" + ColumnExcludeFilters + "\"" );
    }
    if ( (ColumnNamedRanges != null) && (ColumnNamedRanges.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnNamedRanges=\"" + ColumnNamedRanges + "\"");
    }
    if ( (KeepOpen != null) && (KeepOpen.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "KeepOpen=" + KeepOpen );
    }
    if ( (ColumnCellTypes != null) && (ColumnCellTypes.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnCellTypes=\"" + ColumnCellTypes + "\"");
    }
    if ( (ColumnWidths != null) && (ColumnWidths.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnWidths=\"" + ColumnWidths + "\"");
    }
    if ( (ColumnDecimalPlaces != null) && (ColumnDecimalPlaces.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "ColumnDecimalPlaces=\"" + ColumnDecimalPlaces + "\"");
    }
	return getCommandName() + "(" + b.toString() + ")";
}

/**
Read the table from an Excel worksheet.  The cells must be specified by a contiguous address block specified
by one of the parameters excelAddress, excelNamedRange, excelTableName.
@param table the table to output
@param includeColumnNumbers an array of table column numbers to output, guaranteed to be non null and filled with valid
columns
@param workbookFile the name of the workbook file (*.xls or *.xlsx)
@param sheetName the name of the sheet in the workbook
@param excelAddress Excel address range (e.g., A1:D10 or $A1:$D10 or variant)
@param excelNamedRange a named range
@param excelTableName a table name
@param excelColumnNames indicate how to determine column names from the Excel worksheet
@param columnExcludeFiltersMap a map indicating patters for column values, to exclude rows
@param columnNamedRanges column names and name range name to define
@param keepOpen if True, the Excel workbook will be kept open and not written
@param columnCellTypes column names and Excel cell types
@param columnWidths column names and widths (Auto to auto-size, or integer points)
@param columnDecimalPlaces column names and number of decimal places (used for floating point data)
@param problems list of problems encountered during read, for formatted logging in calling code
@return a DataTable with the Excel contents
*/
private void writeTableToExcelFile ( DataTable table, int [] includeColumnNumbers, String workbookFile, String sheetName,
    String excelAddress, String excelNamedRange, String excelTableName, String excelColumnNames,
    Hashtable<String,String> columnExcludeFiltersMap,
    Hashtable<String,String> columnNamedRanges, boolean keepOpen,
    StringDictionary columnCellTypes, StringDictionary columnWidths,
    StringDictionary columnDecimalPlaces, List<String> problems )
throws FileNotFoundException, IOException
{   String routine = "WriteTableToExcel_Command.writeTableToExcelFile", message;
    
    Workbook wb = null;
    InputStream inp = null;
    try {
        // See if an open workbook by the same name exists
        wb = ExcelUtil.getOpenWorkbook(workbookFile);
        if ( wb == null ) {
            // Workbook is not open in memory so Open the file
            try {
                inp = new FileInputStream(workbookFile);
            }
            catch ( IOException e ) {
                problems.add ( "Error opening workbook file \"" + workbookFile + "\" (" + e + ")." );
                return;
            }
            try {
                // Open an existing workbook if it exists...
                wb = WorkbookFactory.create(inp);
            }
            catch ( InvalidFormatException e ) {
                problems.add ( "Error creating workbook object from \"" + workbookFile + "\" (" + e + ")." );
                return;
            }
            finally {
                // Close the sheet because will need to write it below and close
                if ( !keepOpen ) {
                    inp.close();
                }
            }
        }
        Sheet sheet = null;
        // TODO SAM 2013-02-22 In the future sheet may be determined from named address (e.g., named ranges
        // are global in workbook)
        if ( (sheetName == null) || (sheetName.length() == 0) ) {
            // Default is to use the first sheet
            sheet = wb.getSheetAt(0);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include any worksheets" );
                return;
            }
        }
        else {
            sheet = wb.getSheet(sheetName);
            if ( sheet == null ) {
                problems.add ( "Workbook does not include worksheet named \"" + sheetName + "\"" );
                return;
            }
        }
        // Get the contiguous block of data to process by evaluating user input
        AreaReference area = getAreaReference ( wb, sheet, excelAddress, excelNamedRange, excelTableName );
        if ( area == null ) {
            problems.add ( "Unable to get worksheet area reference from address information (empty worksheet?)." );
            return;
        }
        Message.printStatus(2,routine,"Excel address block to write: " + area );
        // Get the upper left row/column to write from the addresses
        int cols = includeColumnNumbers.length;
        int rows = table.getNumberOfRecords();
        // Upper left, including column headings if included
        int colOutStart = 0;
        int rowOutStart = 0;
        int rowOutColumnNames = 0;
        if ( area != null ) {
            colOutStart = area.getFirstCell().getCol();
            rowOutStart = area.getFirstCell().getRow();
            rowOutColumnNames = rowOutStart;
        }
        // Upper left, for first data row (assume no column headings and adjust accordingly below)
        int colOutDataStart = colOutStart;
        int rowOutDataStart = rowOutStart;
        int colOutDataEnd = colOutDataStart + cols - 1; // 0-index
        int rowOutDataEnd = rowOutDataStart + rows - 1; // 0-index
        // Adjust the data locations based on whether column headings are in the block
        boolean doWriteColumnNames = true;
        if ( excelColumnNames.equalsIgnoreCase(_FirstRowInRange) ) {
            ++rowOutDataStart;
            ++rowOutDataEnd;
        }
        else if ( excelColumnNames.equalsIgnoreCase(_RowBeforeRange) ) {
            --rowOutColumnNames;
            // OK as is
        }
        else if ( excelColumnNames.equalsIgnoreCase(_None) ) {
            // OK as is
            doWriteColumnNames = false;
        }
        else {
            problems.add ( "Unknown ExcelColumnNames value \"" + excelColumnNames +
                "\" - assuming no column headings but may not be correct" );
        }
        // Process column metadata:
        //  1) determine column Excel data type
        //  2) write column names (if requested)
        //  3) indicate whether to auto-size column or set column specifically
        //  4) set up column styles/formatting for data values
        DataFormat [] cellFormats = new DataFormat[cols];
        CellStyle [] cellStyles = new CellStyle[cols];
        int tableFieldType;
        int precision;
        int colOut = colOutDataStart;
        if ( excelColumnNames.equalsIgnoreCase(_None) ) {
            doWriteColumnNames = false;
        }
        int [] excelColumnTypes = new int[includeColumnNumbers.length];
        // Get the default cell type for all columns if set
        String defaultCellType = columnCellTypes.get("Default");
        for ( int col = 0; col < includeColumnNumbers.length; col++, colOut++ ) {
            // 1. Determine the Excel output cell types for each column
            tableFieldType = table.getFieldDataType(includeColumnNumbers[col]);
            // If the user has specified a column type (even the default), then use it
            if ( defaultCellType != null ) {
                // Only "Text" is allowed
                if ( defaultCellType.equalsIgnoreCase("Text") ) {
                    excelColumnTypes[col] = Cell.CELL_TYPE_STRING;
                }
            }
            else {
                // Else set the type to something reasonable for the table column data type
                if ( (tableFieldType == TableField.DATA_TYPE_DOUBLE) ||
                    (tableFieldType == TableField.DATA_TYPE_FLOAT) ||
                    (tableFieldType == TableField.DATA_TYPE_INT) ||
                    (tableFieldType == TableField.DATA_TYPE_LONG) ||
                    (tableFieldType == TableField.DATA_TYPE_SHORT)) {
                    excelColumnTypes[col] = Cell.CELL_TYPE_NUMERIC;
                }
                // TODO SAM 2015-05-03 Need to handle DATE and DATETIME
                else {
                    // Default is text
                    excelColumnTypes[col] = Cell.CELL_TYPE_STRING;
                }
            }
            // 2. Write the column names
            // First try to get an existing cell for the heading
            // First try to get an existing row
            Row wbRowColumnNames = sheet.getRow(rowOutColumnNames);
            // If it does not exist, create it
            if ( wbRowColumnNames == null ) {
                wbRowColumnNames = sheet.createRow(rowOutColumnNames);
            }
            Cell wbCell = wbRowColumnNames.getCell(colOut);
            String tableColumnName = table.getFieldName(includeColumnNumbers[col]);
            if ( wbCell == null ) {
                wbCell = wbRowColumnNames.createCell(colOut);
            }
            try {
                if ( doWriteColumnNames ) {
                    wbCell.setCellValue(tableColumnName);
                }
                Message.printStatus(2, routine, "Setting [" + rowOutColumnNames + "][" + col + "] = " + tableColumnName );
            }
            catch ( Exception e ) {
                // Log but let the output continue
                Message.printWarning(3, routine, "Unexpected error writing table heading at Excel row [" + rowOutColumnNames + "][" +
                    colOut + "] (" + e + ")." );
                Message.printWarning(3, routine, e);
            }
            // 3. Set the column width
            //    Actually, have to do this after the data have been set
            // 4. Create the styles for the data values, including number of decimals (precision)
            cellFormats[col] = wb.createDataFormat();
            cellStyles[col] = wb.createCellStyle();
            if ( (tableFieldType == TableField.DATA_TYPE_FLOAT) || (tableFieldType == TableField.DATA_TYPE_DOUBLE) ) {
                precision = table.getFieldPrecision(includeColumnNumbers[col]);
                String numDec = columnDecimalPlaces.get(tableColumnName);
                if ( numDec != null ) {
                    try {
                        precision = Integer.parseInt(numDec.trim());
                    }
                    catch ( Exception e ) {
                        problems.add ( "Column \"" + tableColumnName + "\" number of decimals " + numDec + "\" is not an integer." );
                    }
                }
                else {
                    // Use the number of decimal places if specified
                    if ( precision < 0 ) {
                        precision = 6;
                    }
                }
                String format = "0.";
                if ( precision == 0 ) {
                    // No decimal
                    format= "0";
                }
                else {
                    for ( int i = 0; i < precision; i++ ) {
                        format += "0";
                    }
                }
                cellStyles[col].setDataFormat(cellFormats[col].getFormat(format));
            }
            else if ( (tableFieldType == TableField.DATA_TYPE_INT) || (tableFieldType == TableField.DATA_TYPE_LONG) ) {
                String format = "0";
                cellStyles[col].setDataFormat(cellFormats[col].getFormat(format));
            }
            // If named ranges are to be written, match the table columns and do it
            if ( columnNamedRanges != null ) {
                // Iterate through hashtable
                Enumeration keys = columnNamedRanges.keys();
                int ikey = -1;
                String key = null;
                boolean found = false;
                String namedRange = null;
                while ( keys.hasMoreElements() ) {
                    ++ikey;
                    key = (String)keys.nextElement(); // Column name
                    // Find the table column
                    int namedRangeCol = table.getFieldIndex(key);
                    namedRange = columnNamedRanges.get(key); // Named range
                    if ( namedRangeCol == includeColumnNumbers[col] ) {
                        found = true;
                        break;
                    }
                }
                if ( found ) {
                    // Define a named range.  First try to retrieve
                    int nrid = wb.getNameIndex(namedRange);
                    Name nr;
                    if ( nrid >= 0 ) {
                        nr = wb.getNameAt(nrid);
                    }
                    else {
                        nr = wb.createName();
                        nr.setNameName(namedRange);
                    }
                    // Convert the 0-index row and column range to an Excel address range
                    CellReference ref1 = new CellReference(rowOutDataStart,colOut);
                    CellReference ref2 = new CellReference(rowOutDataEnd,colOut);
                    String reference = "'" + sheetName + "'!$" + ref1.getCellRefParts()[2] +"$" + ref1.getCellRefParts()[1] +
                        ":$" + ref2.getCellRefParts()[2] + "$" + ref2.getCellRefParts()[1];
                    nr.setRefersToFormula(reference);
                }
            }
        }
        // Write the table data
        Object fieldValue;
        Double fieldValueDouble;
        Float fieldValueFloat;
        Integer fieldValueInteger;
        Long fieldValueLong;
        String NaNValue = "";
        String cellString;
        int rowOut = rowOutDataStart;
        Row wbRowData;
        for ( int row = 0; (row < rows) && (rowOut <= rowOutDataEnd); row++, rowOut++) {
            // First try to get an existing row
            wbRowData = sheet.getRow(rowOut);
            // If it does not exist, create it
            if ( wbRowData == null ) {
                wbRowData = sheet.createRow(rowOut);
            }
            colOut = colOutDataStart;
            for ( int col = 0; (col < cols) && (colOut <= colOutDataEnd); col++, colOut++) {
                // First try to get an existing cell
                Cell wbCell = wbRowData.getCell(colOut);
                if ( wbCell == null ) {
                    wbCell = wbRowData.createCell(colOut);
                }
                try {
                    tableFieldType = table.getFieldDataType(includeColumnNumbers[col]);
                    precision = table.getFieldPrecision(includeColumnNumbers[col]);
                    fieldValue = table.getFieldValue(row,includeColumnNumbers[col]);
                    if ( fieldValue == null ) {
                        cellString = "";
                        wbCell.setCellValue(cellString);
                    }
                    else if ( tableFieldType == TableField.DATA_TYPE_FLOAT ) {
                        fieldValueFloat = (Float)fieldValue;
                        if ( fieldValueFloat.isNaN() ) {
                            cellString = NaNValue;
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            if ( excelColumnTypes[col] == Cell.CELL_TYPE_STRING ) {
                                if ( precision > 0 ) {
                                    // Format according to the precision if floating point
                                    cellString = StringUtil.formatString(fieldValue,"%." + precision + "f");
                                }
                                else {
                                    // Use default formatting.
                                    cellString = "" + fieldValue;
                                }
                                wbCell.setCellValue(cellString);
                            }
                            else {
                                wbCell.setCellValue(fieldValueFloat);
                                wbCell.setCellStyle(cellStyles[col]);
                            }
                        }
                    }
                    else if ( tableFieldType == TableField.DATA_TYPE_DOUBLE ) {
                        fieldValueDouble = (Double)fieldValue;
                        if ( fieldValueDouble.isNaN() ) {
                            cellString = NaNValue;
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            if ( excelColumnTypes[col] == Cell.CELL_TYPE_STRING ) {
                                if ( precision > 0 ) {
                                    // Format according to the precision if string
                                    cellString = StringUtil.formatString(fieldValue,"%." + precision + "f");
                                }
                                else {
                                    // Use default formatting.
                                    cellString = "" + fieldValue;
                                }
                                wbCell.setCellValue(cellString);
                            }
                            else {
                                wbCell.setCellValue(fieldValueDouble);
                                wbCell.setCellStyle(cellStyles[col]);
                            }
                        }
                    }
                    else if ( tableFieldType == TableField.DATA_TYPE_INT ) {
                        fieldValueInteger = (Integer)fieldValue;
                        if ( fieldValueInteger == null ) {
                            cellString = "";
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            if ( excelColumnTypes[col] == Cell.CELL_TYPE_STRING ) {
                                cellString = "" + fieldValue;
                                wbCell.setCellValue(cellString);
                            }
                            else {
                                wbCell.setCellValue(fieldValueInteger);
                                wbCell.setCellStyle(cellStyles[col]);
                            }
                        }
                    }
                    else if ( tableFieldType == TableField.DATA_TYPE_LONG ) {
                        fieldValueLong = (Long)fieldValue;
                        if ( fieldValueLong == null ) {
                            cellString = "";
                            wbCell.setCellValue(cellString);
                        }
                        else {
                            if ( excelColumnTypes[col] == Cell.CELL_TYPE_STRING ) {
                                cellString = "" + fieldValue;
                                wbCell.setCellValue(cellString);
                            }
                            else {
                                wbCell.setCellValue(fieldValueLong);
                                wbCell.setCellStyle(cellStyles[col]);
                            }
                        }
                    }
                    else {
                        // Use default formatting.
                        if ( fieldValue == null ) {
                            // TODO SAM 2014-01-21 Need to handle as blanks in output, if user indicates to do so
                            cellString = "";
                        }
                        else {
                            cellString = "" + fieldValue;
                        }
                        wbCell.setCellValue(cellString);
                    }
                }
                catch ( Exception e ) {
                    // Log but let the output continue
                    Message.printWarning(3, routine, "Unexpected error writing table [" + row + "][" +
                        includeColumnNumbers[col] + "] (" + e + ")." );
                    Message.printWarning(3, routine, e);
                }
            }
        }
        // Now do post-data set operations
        // Set the column width
        colOut = colOutDataStart;
        for ( int col = 0; col < includeColumnNumbers.length; col++, colOut++ ) {
            String tableColumnName = table.getFieldName(includeColumnNumbers[col]);
            String width = columnWidths.get(tableColumnName);
            if ( width == null ) {
                // Try default
                width = columnWidths.get("Default");
            }
            if ( width != null ) {
                // Set the column width
                if ( width.equalsIgnoreCase("Auto") ) {
                    sheet.autoSizeColumn(colOut);
                    Message.printStatus(2,routine,"Setting column \"" + tableColumnName + "\" width to auto.");
                }
                else {
                    // Set the column width to 1/256 of character width, max of 256*256 since 256 is max characters shown
                    try {
                        int w = Integer.parseInt(width.trim());
                        sheet.setColumnWidth(colOut, w);
                        Message.printStatus(2,routine,"Setting column \"" + tableColumnName + "\" width to " + w + ".");
                    }
                    catch ( NumberFormatException e ) {
                        problems.add ( "Column \"" + tableColumnName + "\" width \"" + width + "\" is not an integer." );
                    }
                }
            }
        }
    }
    catch ( Exception e ) {
        problems.add ( "Error writing to workbook \"" + workbookFile + "\" (" + e + ")." );
        Message.printWarning(3,routine,e);
    }
    finally {
        // Now write the workbook and close.  If keeping open skip because it will be written by a later command.
        if ( keepOpen ) {
            // Save the open workbook for other commands to use
            ExcelUtil.setOpenWorkbook(workbookFile,wb);
        }
        else {
            // Close the workbook and remove from the cache
            wb.setForceFormulaRecalculation(true); // Will cause Excel to recalculate formulas when it opens
            FileOutputStream fout = new FileOutputStream(workbookFile);
            wb.write(fout);
            fout.close();
            ExcelUtil.removeOpenWorkbook(workbookFile);
        }
    }
}

}