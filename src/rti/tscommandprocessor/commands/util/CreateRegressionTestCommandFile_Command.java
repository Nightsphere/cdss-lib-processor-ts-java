// CreateRegressionTestCommandFile_Command - This class initializes, checks, and runs the CreateRegressionTestCommandFile() command.

/* NoticeStart

CDSS Time Series Processor Java Library
CDSS Time Series Processor Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2019 Colorado Department of Natural Resources

CDSS Time Series Processor Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Time Series Processor Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Time Series Processor Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package rti.tscommandprocessor.commands.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;

import rti.tscommandprocessor.core.TSCommandProcessorUtil;
import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;

/**
This class initializes, checks, and runs the CreateRegressionTestCommandFile() command.
*/
public class CreateRegressionTestCommandFile_Command extends AbstractCommand
implements FileGenerator
{

/**
Data members used for parameter values.
*/
protected final String _False = "False";
protected final String _True = "True";

/**
Output file that is created by this command.
*/
private File __OutputFile_File = null;

/**
Constructor.
*/
public CreateRegressionTestCommandFile_Command ()
{	super();
	setCommandName ( "CreateRegressionTestCommandFile" );
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
{	String routine = getClass().getSimpleName() + ".checkCommandParameters";
    String SearchFolder = parameters.getValue ( "SearchFolder" );
	String OutputFile = parameters.getValue ( "OutputFile" );
	String SetupCommandFile = parameters.getValue ( "SetupCommandFile" );
	String EndCommandFile = parameters.getValue ( "EndCommandFile" );
	//String FilenamePattern = parameters.getValue ( "FilenamePattern" );
	String Append = parameters.getValue ( "Append" );
	String warning = "";
    String message;

    CommandProcessor processor = getCommandProcessor();
	CommandStatus status = getCommandStatus();
	status.clearLog(CommandPhaseType.INITIALIZATION);
	
	// The existence of the parent directories or files is not checked
	// because the files may be created dynamically after the command is edited.

	if ( (SearchFolder == null) || (SearchFolder.length() == 0) ) {
        message = "The search folder must be specified.";
		warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
			message, "Specify the search folder."));
	}
    else if ( SearchFolder.indexOf("${") < 0 ) {
        String working_dir = null;
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
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report the problem to software support." ) );
            }
    
        try {
            //String adjusted_path = 
            IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                TSCommandProcessorUtil.expandParameterValue(processor,this,SearchFolder)));
        }
        catch ( Exception e ) {
            message = "The search folder:\n" +
            "    \"" + SearchFolder +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that search folder and working directory paths are compatible." ) );
        }
    }
	
	if ( (OutputFile == null) || (OutputFile.length() == 0) ) {
        message = "The output file must be specified.";
		warning += "\n" + message;
        status.addToLog(CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
			message, "Specify the output file name."));
	}
	else if ( OutputFile.indexOf("${") < 0 ) {
        String working_dir = null;
        try {
            Object o = processor.getPropContents ( "WorkingDir" );
            if ( o != null ) {
                working_dir = (String)o;
            }
        }
        catch ( Exception e ) {
            message = "Error requesting WorkingDir from processor.";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Software error - report the problem to support." ) );
        }

        try {
            String adjusted_path = IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)));
            File f = new File ( adjusted_path );
            File f2 = new File ( f.getParent() );
            if ( !f2.exists() ) {
                message = "The output file parent directory does not exist for: \"" + adjusted_path + "\".";
                warning += "\n" + message;
                status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Create the output directory." ) );
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
            status.addToLog ( CommandPhaseType.INITIALIZATION,new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that output file and working directory paths are compatible." ) );
        }
    }
	
    if ( (SetupCommandFile != null) && (SetupCommandFile.length() != 0) && (SetupCommandFile.indexOf("${") < 0) ) {
        String working_dir = null;
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
                Message.printWarning(3, routine, message );
                status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                    message, "Report problem to software support." ) );
            }
    
        try {
            //String adjusted_path = 
            IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                TSCommandProcessorUtil.expandParameterValue(processor,this,SetupCommandFile)));
        }
        catch ( Exception e ) {
            message = "The setup command file:\n" +
            "    \"" + SetupCommandFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that setup command file and working directory paths are compatible." ) );
        }
    }
    
    if ( (EndCommandFile != null) && !EndCommandFile.isEmpty() && (EndCommandFile.indexOf("${") < 0) ) {
        String working_dir = null;
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
            Message.printWarning(3, routine, message );
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Report problem to software support." ) );
        }
    
        try {
            //String adjusted_path = 
            IOUtil.verifyPathForOS(IOUtil.adjustPath (working_dir,
                TSCommandProcessorUtil.expandParameterValue(processor,this,EndCommandFile)));
        }
        catch ( Exception e ) {
            message = "The end command file:\n" +
            "    \"" + EndCommandFile +
            "\"\ncannot be adjusted using the working directory:\n" +
            "    \"" + working_dir + "\".";
            warning += "\n" + message;
            status.addToLog ( CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
                message, "Verify that end command file and working directory paths are compatible." ) );
        }
    }
	
	if ( (Append != null) && !Append.equals("") ) {
		if ( !Append.equalsIgnoreCase(_False) && !Append.equalsIgnoreCase(_True) ) {
            message = "The Append parameter \"" + Append + "\" must be False or True.";
			warning += "\n" + message;
            status.addToLog(CommandPhaseType.INITIALIZATION, new CommandLogRecord(CommandStatusType.FAILURE,
				message, "Specify the parameter as False or True."));
		}
	}

	// Check for invalid parameters...
	List<String> validList = new ArrayList<String>(9);
	validList.add ( "SearchFolder" );
	validList.add ( "OutputFile" );
    validList.add ( "SetupCommandFile" );
    validList.add ( "EndCommandFile" );
    validList.add ( "FilenamePattern" );
	validList.add ( "Append" );
	validList.add ( "IncludeTestSuite" );
	validList.add ( "IncludeOS" );
	validList.add ( "TestResultsTableID" );
    warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

    // Throw an InvalidCommandParameterException in case of errors.
    if ( warning.length() > 0 ) {       
        Message.printWarning ( warning_level,
            MessageUtil.formatMessageTag(
                command_tag, warning_level ), warning );
        throw new InvalidCommandParameterException ( warning );
    }
    
    status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
}

/**
Determine the expected status parameter by searching the command file for an "@expectedStatus" string.
@param filename Name of file to open to scan.
@return a string for the ExpectedStatus parameter or empty string if no expected status is known.
*/
private String determineExpectedStatusParameter ( String filename )
throws FileNotFoundException
{   String expectedStatusParameter = "";
    BufferedReader in = new BufferedReader ( new FileReader( filename ) );
    try {
        String line;
        int index;
        while ( true ) {
            line = in.readLine();
            if ( line == null ) {
                break;
            }
            index = line.indexOf("@expectedStatus");
            if ( index >= 0 ) {
                // Get the status as the next token after the tag
                String expectedStatus = StringUtil.getToken(line.substring(index), " \t",
                        StringUtil.DELIM_SKIP_BLANKS, 1);
                // Translate variations to the official name recognized by RunCommands()
                if ( expectedStatus.equalsIgnoreCase("Warn") ) {
                    expectedStatus = "Warning";
                }
                else if ( expectedStatus.equalsIgnoreCase("Fail") ) {
                    expectedStatus = "Failure";
                }
                expectedStatusParameter = ",ExpectedStatus=" + expectedStatus;
                break;
            }
        }
    }
    catch ( IOException e ) {
        // Ignore - just don't have the tag that is being searched for
    }
    finally {
        try {
            in.close();
        }
        catch ( IOException e ) {
            // Not much to do but absorb - should not happen
        }
    }
    return expectedStatusParameter;
}

/**
Edit the command.
@param parent The parent JFrame to which the command dialog will belong.
@return true if the command was edited (e.g., "OK" was pressed), and false if
not (e.g., "Cancel" was pressed.
*/
public boolean editCommand ( JFrame parent )
{	// The command will be modified if changed...
	return (new CreateRegressionTestCommandFile_JDialog ( parent, this )).ok();
}

/**
Return the list of files that were created by this command.
*/
public List<File> getGeneratedFileList ()
{
	List<File> list = new Vector<File>();
    if ( getOutputFile() != null ) {
        list.add ( getOutputFile() );
    }
    return list;
}

// FIXME SAM 2008-07-31 Should separate out this method from the checks for tags to simplify logic of each
/**
Visits all files and directories under the given directory and if
the file matches a valid commands file it is added to the test list.
All commands file that end with ".<product_name>" will be added to the list.
@param commandFileList List of command files that are matched, to be appended to.
@param path Folder in which to start searching for command files.
@param patterns Array of pattern to match when searching files, for example "test*.TSTool",
Java regular expressions.
@param includedTestSuites the test suites for test cases that
should be included, indicated by "@testSuite ABC" tags in the comments of command files.
@param includedOS the operating systems for test cases that
should be included, indicated by "@os Windows" and "@os UNIX" tags in the comments of command files.
@throws IOException 
 */
private void getMatchingFilenamesInTree ( List<String> commandFileList, File path, String[] patterns,
        String[] includedTestSuites, String[] includedOS ) 
throws IOException
{   String routine = getClass().getSimpleName() + ".getMatchingFilenamesInTree";
    // Determine if UNIX and Windows tests have been requested
    // Check the OS only if the specific  
    boolean needToCheckForUnixOS = false;
    boolean needToCheckForWindowsOS = false;
    for ( int i = 0; i < includedOS.length; i++ ) {
        if ( includedOS[i].equalsIgnoreCase("UNIX") ) {
            needToCheckForUnixOS = true;
            Message.printStatus ( 2, routine, "Will only include tests that are for UNIX." );
        }
    }
    for ( int i = 0; i < includedOS.length; i++ ) {
        if ( includedOS[i].equalsIgnoreCase("Windows") ) {
            needToCheckForWindowsOS = true;
            Message.printStatus ( 2, routine, "Will only include tests that are for Windows." );
        }
    }
    if (path.isDirectory()) {
        String[] children = path.list();
        for (int i = 0; i < children.length; i++) {
        	// Recursively call with full path using the directory and child name.
        	getMatchingFilenamesInTree(commandFileList,new File(path,children[i]), patterns,
    	        includedTestSuites, includedOS );
        }
    }
    else {
        //add to list if command file is valid
        String pathName = path.getName();
    	// Do comparison on file name without directory.
    	for ( int i = 0; i < patterns.length; i++ ) {
    		String pattern = patterns[i];
    		Message.printStatus(2, "", "Checking path \"" + pathName + "\" against pattern \"" + pattern + "\"" );
    	    if( pathName.matches( pattern )
    		    // FIXME SAM 2007-10-15 Need to enable something like the following to make more robust
    		    //&& isValidCommandsFile( dir )
    		    ) {
        	    Message.printStatus(2, "", "File matched." );
        	    // Exclude the command file if tag in the file indicates that it is not compatible with
        	    // this command's parameters.
        	    boolean doAddForOS = false;
        	    List<Object> tagValues = TSCommandProcessorUtil.getTagValues ( path.toString(), "os" );
        	    if ( !needToCheckForUnixOS && !needToCheckForWindowsOS ) {
        	        // Not checking for OS so go ahead and add
        	        doAddForOS = true;
        	    }
        	    if ( !doAddForOS && needToCheckForUnixOS ) {
                    boolean tagHasUNIX = false;
        	        // os tag needs to be blank or include "UNIX"
        	        for ( int ivalue = 0; ivalue < tagValues.size(); ivalue++ ) {
        	            Object o = tagValues.get(ivalue);
        	            if ( o instanceof String ) {
        	                String s = (String)o;
        	                if ( s.toUpperCase().matches("UNIX") ) {
        	                    tagHasUNIX = true;
        	                }
        	            }
        	        }
                    if ( (tagValues.size() == 0) || tagHasUNIX ) {
                        // Test is not OS-specific or test is for UNIX so include for UNIX
                        doAddForOS = true;
                    }
         	    }
        	    if ( !doAddForOS && needToCheckForWindowsOS ) {
                    boolean tagHasWindows = false;
                    // os tag needs to be blank or include "Windows"
                    for ( int ivalue = 0; ivalue < tagValues.size(); ivalue++ ) {
                        Object o = tagValues.get(ivalue);
                        if ( o instanceof String ) {
                            String s = (String)o;
                            if ( s.toUpperCase().matches("WINDOWS") ) {
                                tagHasWindows = true;
                            }
                        }
                    }
                    if ( (tagValues.size() == 0) || tagHasWindows ) {
                        // Test is not OS-specific or test is for Windows so include for Windows
                        doAddForOS = true;
                    }
        	    }
        	    // Check to see if the test suite has been specified and matches that in the file
        	    boolean doAddForTestSuite = false;
        	    if ( includedTestSuites.length == 0 ) {
        	        doAddForTestSuite = true;
        	    }
        	    else {
        	        // Check to see if the test suites in the test match the requested test suites
        	        List<Object> tagValues2 = TSCommandProcessorUtil.getTagValues ( path.toString(), "testSuite" );
        	        if ( tagValues2.size() == 0 ) {
        	            // Test case is not specified to belong to a specific suite so it is always included
        	            doAddForTestSuite = true;
        	        }
        	        else {
        	            // Check each value in the file against requested test suites
        	            for ( int itag = 0; itag < tagValues2.size(); itag++ ) {
        	                if ( !(tagValues2.get(itag) instanceof String) ) {
        	                    continue;
        	                }
        	                for ( int j = 0; j < includedTestSuites.length; j++ ) {
        	                    if ( ((String)tagValues2.get(itag)).toUpperCase().matches(includedTestSuites[j]) ) {
        	                        doAddForTestSuite = true;
        	                        break;
        	                    }
        	                }
        	                if ( doAddForTestSuite ) {
        	                    break;
        	                }
        	            }
        	        }
        	    }
        	    if ( doAddForOS && doAddForTestSuite ) {
        	        // Test is to be included for the OS and test suite.
        	        commandFileList.add(path.toString());
        	    }
    	    }
        }
    }
}

/**
Return the output file generated by this file.  This method is used internally.
*/
private File getOutputFile ()
{
    return __OutputFile_File;
}

/**
Include the setup command file in the regression test command file.
@param out PrintWriter to write to.
@param includeCommandFile full path for setup command file.
@param label a short label that indicates the type of file being included ("setup" or "end")
@exception IOException if there is an error including the file.
*/
private void includeCommandFile ( PrintWriter out, String includeCommandFile, String label )
throws IOException
{   //String routine = getClass().getName() + ".includeSetupCommandFile";
    if ( includeCommandFile == null ) {
        return;
    }
    BufferedReader in = new BufferedReader ( new InputStreamReader( IOUtil.getInputStream ( includeCommandFile )) );
    out.println ( "#----------------" );
    out.println ( "# The following " + label + " commands were imported from:  " + includeCommandFile );
    String line;
    while ( true ) {
        line = in.readLine();
        if ( line == null ) {
            break;
        }
        //Message.printStatus ( 2, routine, "Importing command: " + line );
        out.println ( line );
    }
    out.println ( "#----------------" );
    in.close();
}

// Use the base class parse()

/**
Run the command.
@param command_line Command number in sequence.
@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
*/
public void runCommand ( int command_number )
throws InvalidCommandParameterException, CommandWarningException, CommandException
{	String routine = getClass().getSimpleName() + ".runCommand", message;
	int warning_level = 2;
	String command_tag = "" + command_number;
	int warning_count = 0;
	
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
	
	String SearchFolder = parameters.getValue ( "SearchFolder" ); // Expanded below
    String OutputFile = parameters.getValue ( "OutputFile" ); // Expanded below
    String SetupCommandFile = parameters.getValue ( "SetupCommandFile" ); // Expanded below
    String EndCommandFile = parameters.getValue ( "EndCommandFile" ); // Expanded below
	String FilenamePattern = parameters.getValue ( "FilenamePattern" );
	String [] FilenamePattern_Java = new String[0];
	if ( FilenamePattern == null ) {
		// The default patterns are:
		//     "Test_*.TSTool" where the . is literal, and want ignore case
		//     "test_*.TSTool" where the . is literal, and want ignore case
		// For Java string matching, need to replace * with .* and . with \...
		// The \\x2e means literal period, so as to not be confused with regex period.
		FilenamePattern_Java = new String[2];
		FilenamePattern_Java[0] = "^[tT][Ee][Ss][Tt]_.*\\x2e[tT][Ss][tT][oO][oO][lL]";
		FilenamePattern_Java[1] = "^[tT][Ee][Ss][Tt]-.*\\x2e[tT][Ss][tT][oO][oO][lL]";
	}
	else {
		String [] parts = FilenamePattern.split(",");
		FilenamePattern_Java = new String[parts.length];
		for ( int i = 0; i < parts.length; i++ ) {
			FilenamePattern_Java[i] = StringUtil.replaceString(parts[i],"*",".*");
		}
	}
	String Append = parameters.getValue ( "Append" );
	boolean Append_boolean = true;	// Default
	if ( (Append != null) && Append.equalsIgnoreCase(_False)){
		Append_boolean = false;
	}
	String IncludeTestSuite = parameters.getValue ( "IncludeTestSuite" );
	if ( (IncludeTestSuite == null) || IncludeTestSuite.equals("") ) {
	    IncludeTestSuite = "*"; // Default - include all test suites
	}
	String IncludeOS = parameters.getValue ( "IncludeOS" );
    if ( (IncludeOS == null) || IncludeOS.equals("") ) {
        IncludeOS = "*"; // Default - include all OS
    }
    String TestResultsTableID = parameters.getValue ( "TestResultsTableID" );
    // Get Java regular expression pattern to match
    String IncludeTestSuitePattern = StringUtil.replaceString(IncludeTestSuite,"*",".*");
    String IncludeOSPattern = StringUtil.replaceString(IncludeOS,"*",".*");

	String SearchFolder_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
            TSCommandProcessorUtil.expandParameterValue(processor,this,SearchFolder)));
	String OutputFile_full = IOUtil.verifyPathForOS(
        IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
            TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)));
	if ( !IOUtil.fileExists(SearchFolder_full) ) {
		message = "The folder to search \"" + SearchFolder_full + "\" does not exist.";
		Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
		status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
			"Verify that the folder exists at the time the command is run."));
	}
	String SetupCommandFile_full = null;
	if ( (SetupCommandFile != null) && !SetupCommandFile.equals("") ) {
	    SetupCommandFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                TSCommandProcessorUtil.expandParameterValue(processor,this,SetupCommandFile)));
        if ( !IOUtil.fileExists(SetupCommandFile_full) ) {
            message = "The setup command file \"" + SetupCommandFile_full + "\" does not exist.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                "Verify that the file exists at the time the command is run."));
        }
	}
	String EndCommandFile_full = null;
	if ( (EndCommandFile != null) && !EndCommandFile.equals("") ) {
	    EndCommandFile_full = IOUtil.verifyPathForOS(
            IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
                TSCommandProcessorUtil.expandParameterValue(processor,this,EndCommandFile)));
        if ( !IOUtil.fileExists(EndCommandFile_full) ) {
            message = "The end command file \"" + EndCommandFile_full + "\" does not exist.";
            Message.printWarning ( warning_level,
                MessageUtil.formatMessageTag(command_tag,++warning_count), routine, message );
            status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE, message,
                "Verify that the file exists at the time the command is run."));
        }
	}

	if ( warning_count > 0 ) {
		message = "There were " + warning_count + " warnings about command parameters.";
		Message.printWarning ( warning_level, 
		MessageUtil.formatMessageTag(command_tag, ++warning_count),
		routine, message );
		throw new InvalidCommandParameterException ( message );
	}

	try {
	    // Get the list of files to run as test cases...
		List<String> files = new Vector<String>();
        String [] includedTestSuitePatterns = new String[0];
        includedTestSuitePatterns = StringUtil.toArray(StringUtil.breakStringList(IncludeTestSuitePattern,",",0));
        String [] includedOSPatterns = new String[0];
        includedOSPatterns = StringUtil.toArray(StringUtil.breakStringList(IncludeOSPattern,",",0));
        getMatchingFilenamesInTree ( files, new File(SearchFolder_full), FilenamePattern_Java,
            includedTestSuitePatterns, includedOSPatterns );
        // Sort the list because it may not be sorted, due to dates on files
        files = StringUtil.sortStringList(files);
        int size = files.size();
		// Open the output file...
		PrintWriter out = new PrintWriter(new FileOutputStream(OutputFile_full, Append_boolean));
		File OutputFile_full_File = new File(OutputFile_full);
		// Write a standard header to the file so that it is clear when the file was created
		IOUtil.printCreatorHeader(out, "#", 120, 0 );
		// Include the setup command file if requested
		//Message.printStatus ( 2, routine, "Adding commands from setup command file \"" + SetupCommandFile_full + "\"");
		includeCommandFile ( out, SetupCommandFile_full, "setup" );
		// Include the matching test cases
		out.println ( "#" );
		out.println ( "# The following " + size + " test cases will be run to compare results with expected results.");
		out.println ( "# Individual log files are generally created for each test.");
		if ( IncludeTestSuite.equals("") ) {
		    out.println ( "# All test cases will be included.");
		}
		else {
		    out.println ( "# The following test suites from @testSuite comments are included: " + IncludeTestSuite );
		}
        if ( IncludeOS.equals("") ) {
            out.println ( "# Test cases for all operating systems will be included.");
        }
        else {
            out.println ( "# Test cases for @os comments are included: " + IncludeOS );
        }
        // FIXME SAM 2007-11-20 Disable this for now because it might interfere with the
        // individual logs for each command file regression test
        String tableParam = "";
        if ( (TestResultsTableID != null) && !TestResultsTableID.isEmpty() ) {
        	tableParam = ",TestResultsTableID=\"" + TestResultsTableID + "\"";
        }
		out.println ( "StartRegressionTestResultsReport(OutputFile=\"" + OutputFile_full_File.getName() + ".out.txt\"" + tableParam + ")");
		// Find the list of matching files...
		String commandFileToRun;
		for ( int i = 0; i < size; i++ ) {
			// The command files to run are relative to the commands file being created.
			commandFileToRun = IOUtil.toRelativePath ( OutputFile_full_File.getParent(), files.get(i) );
			// Determine if the command file has @expectedStatus in it.  If so, define an ExpectedStatus
			// parameter for the command.
			out.println ( "RunCommands(InputFile=\"" + commandFileToRun + "\"" +
		        determineExpectedStatusParameter ( (String)files.get(i) ) + ")");
		}
		// Include the end command file if requested
		//Message.printStatus ( 2, routine, "Adding commands from end command file \"" + EndCommandFile_full + "\"");
		includeCommandFile ( out, EndCommandFile_full, "end" );
		out.close();
        // Save the output file name...
        setOutputFile ( new File(OutputFile_full));
	}
	catch ( Exception e ) {
		message = "Unexpected error creating regression command file \"" + OutputFile_full + "\" (" + e + ").";
		Message.printWarning ( warning_level, 
	        MessageUtil.formatMessageTag(command_tag, ++warning_count),routine, message );
		Message.printWarning ( 3, routine, e );
		status.addToLog(CommandPhaseType.RUN, new CommandLogRecord(CommandStatusType.FAILURE,
			message, "See the log file for details."));
		throw new CommandException ( message );
	}
    
    status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
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
public String toString ( PropList parameters )
{	if ( parameters == null ) {
		return getCommandName() + "()";
	}
	String SearchFolder = parameters.getValue("SearchFolder");
	String OutputFile = parameters.getValue("OutputFile");
	String SetupCommandFile = parameters.getValue("SetupCommandFile");
	String EndCommandFile = parameters.getValue("EndCommandFile");
	String FilenamePattern = parameters.getValue("FilenamePattern");
	String Append = parameters.getValue("Append");
	String IncludeTestSuite = parameters.getValue("IncludeTestSuite");
	String IncludeOS = parameters.getValue("IncludeOS");
	String TestResultsTableID = parameters.getValue("TestResultsTableID");
	StringBuffer b = new StringBuffer ();
	if ( (SearchFolder != null) && (SearchFolder.length() > 0) ) {
		b.append ( "SearchFolder=\"" + SearchFolder + "\"" );
	}
	if ( (OutputFile != null) && (OutputFile.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "OutputFile=\"" + OutputFile + "\"");
	}
    if ( (SetupCommandFile != null) && (SetupCommandFile.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "SetupCommandFile=\"" + SetupCommandFile + "\"");
    }
    if ( (EndCommandFile != null) && !EndCommandFile.isEmpty() ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "EndCommandFile=\"" + EndCommandFile + "\"");
    }
    if ( (FilenamePattern != null) && (FilenamePattern.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "FilenamePattern=\"" + FilenamePattern + "\"" );
    }
	if ( (Append != null) && (Append.length() > 0) ) {
		if ( b.length() > 0 ) {
			b.append ( "," );
		}
		b.append ( "Append=" + Append );
	}
    if ( (IncludeTestSuite != null) && (IncludeTestSuite.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeTestSuite=\"" + IncludeTestSuite + "\"" );
    }
    if ( (IncludeOS != null) && (IncludeOS.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "IncludeOS=\"" + IncludeOS + "\"" );
    }
    if ( (TestResultsTableID != null) && (TestResultsTableID.length() > 0) ) {
        if ( b.length() > 0 ) {
            b.append ( "," );
        }
        b.append ( "TestResultsTableID=\"" + TestResultsTableID + "\"" );
    }
	return getCommandName() + "(" + b.toString() + ")";
}

}
