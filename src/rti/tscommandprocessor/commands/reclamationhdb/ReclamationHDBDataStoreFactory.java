package rti.tscommandprocessor.commands.reclamationhdb;

import javax.swing.JFrame;

import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import riverside.datastore.DataStore;
import riverside.datastore.DataStoreConnectionUIProvider;
import riverside.datastore.DataStoreFactory;

/**
Factory to instantiate ReclamationHDBDataStore instances.
@author sam
*/
public class ReclamationHDBDataStoreFactory implements DataStoreFactory, DataStoreConnectionUIProvider
{

/**
Create a ReclamationHDBDataStore instance and open the encapsulated ReclamationHDB_DMI using the specified
properties.
*/
public DataStore create ( PropList props )
{   String routine = getClass().getName() + ".create";
    String name = props.getValue ( "Name" );
    String description = props.getValue ( "Description" );
    if ( description == null ) {
        description = "";
    }
    String databaseEngine = IOUtil.expandPropertyForEnvironment("DatabaseEngine",props.getValue ( "DatabaseEngine" ));
    String databaseServer = IOUtil.expandPropertyForEnvironment("DatabaseServer",props.getValue ( "DatabaseServer" ));
    String databaseName = IOUtil.expandPropertyForEnvironment("DatabaseName",props.getValue ( "DatabaseName" ));
    String databasePort = IOUtil.expandPropertyForEnvironment("DatabasePort",props.getValue("DatabasePort"));
    String systemLogin = IOUtil.expandPropertyForEnvironment("SystemLogin",props.getValue ( "SystemLogin" ));
    String systemPassword = IOUtil.expandPropertyForEnvironment("SystemPassword",props.getValue ( "SystemPassword" ));
    String tsidStyle = props.getValue("TSIDStyle");
    int port = -1;
    if ( (databasePort != null) && !databasePort.equals("") ) {
        try {
            port = Integer.parseInt(databasePort);
        }
        catch ( NumberFormatException e ) {
            port = -1;
        }
    }
    boolean tsidStyleSDI = true;
    if ( (tsidStyle != null) && tsidStyle.equalsIgnoreCase("CommonName") ) {
        tsidStyleSDI = false;
    }
    String readNHour = props.getValue("ReadNHourEndDateTime");
    boolean readNHourEndDateTime = true;
    if ( (readNHour != null) && readNHour.equalsIgnoreCase("StartDateTimePlusInterval") ) {
        readNHourEndDateTime = false;
    }
    String ConnectTimeout = props.getValue("ConnectTimeout");
    int connectTimeout = 0;
    if ( (ConnectTimeout != null) && !ConnectTimeout.equals("") ) {
        try {
            connectTimeout = Integer.parseInt(ConnectTimeout);
        }
        catch ( Exception e ) {
            connectTimeout = 0;
        }
    }
    String ReadTimeout = props.getValue("ReadTimeout");
    int readTimeout = 0;
    if ( (ReadTimeout != null) && !ReadTimeout.equals("") ) {
        try {
            readTimeout = Integer.parseInt(ReadTimeout);
        }
        catch ( Exception e ) {
            readTimeout = 0;
        }
    }
    // Create an initial datastore instance here with null DMI placeholder
    ReclamationHDBDataStore ds = new ReclamationHDBDataStore ( name, description, null );
    ReclamationHDB_DMI dmi = null;
    try {
        dmi = new ReclamationHDB_DMI (
            databaseEngine, // OK if null, will use Oracle
            databaseServer, // Required
            databaseName, // Required
            port,
            systemLogin,
            systemPassword );
        // Set the datastore here so it has a DMI instance, but DMI instance will not be open
        ds = new ReclamationHDBDataStore ( name, description, dmi );
        dmi.setTSIDStyleSDI ( tsidStyleSDI );
        dmi.setReadNHourEndDateTime( readNHourEndDateTime );
        dmi.setLoginTimeout(connectTimeout);
        dmi.setReadTimeout(readTimeout);
        // Open the database connection
        dmi.open();
    }
    catch ( Exception e ) {
        // Don't rethrow an exception because want datastore to be created with unopened DMI
        Message.printWarning(3,routine,e);
        ds.setStatus(1);
        ds.setStatusMessage("" + e);
    }
    return ds;
}

/**
Open a connection UI dialog that displays the connection information for the database.
@param props properties read from datastore configuration file
@param frame a JFrame to use as the parent of the editor dialog
*/
public DataStore openDataStoreConnectionUI ( PropList props, JFrame frame )
{
	return new ReclamationHDBConnectionUI ( this, props, frame ).getDataStore();
}

}