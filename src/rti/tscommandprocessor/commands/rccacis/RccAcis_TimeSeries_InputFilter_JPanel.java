// RccAcis_TimeSeries_InputFilter_JPanel - This class is an input filter for querying RCC ACIS web services.

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

package rti.tscommandprocessor.commands.rccacis;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;

import RTi.Util.String.StringUtil;

/**
This class is an input filter for querying RCC ACIS web services.
*/
@SuppressWarnings("serial")
public class RccAcis_TimeSeries_InputFilter_JPanel extends InputFilter_JPanel //implements ItemListener, KeyListener
{
    
/**
RCC ACIS data store.
*/
private RccAcisDataStore __dataStore = null;

/**
Constructor.
@param dataStore the data store to use to connect to the web services.  Cannot be null.
@param numFilterGroups the number of filter groups to display
*/
public RccAcis_TimeSeries_InputFilter_JPanel( RccAcisDataStore dataStore, int numFilterGroups )
{   super();
    __dataStore = dataStore;
    setFilters ( numFilterGroups );
}

/**
Set the filter data.  This method is called at setup.
Always use the most current parameter name from the API (translate when filter is initialized from input).
@param numFilterGroups the number of filter groups to display
*/
public void setFilters ( int numFilterGroups )
{   //String routine = getClass().getName() + ".setFilters";
    List<InputFilter> filters = new Vector<InputFilter>();

    filters.add(new InputFilter("", "", StringUtil.TYPE_STRING, null, null, false)); // Blank

    // Get lists for choices...
    //List<String> geolocCountyList = dmi.getGeolocCountyList();
    //List<String> geolocStateList = dmi.getGeolocStateList();
    
    // Get the global state FIPS data
    List<FIPSState> states = FIPSState.getData();
    String [] stateArray = new String[states.size()];
    int i = 0;
    for ( FIPSState fips : states ) {
        stateArray[i++] = fips.getAbbreviation() + " - " + fips.getName() + " (" + fips.getCode() + ")";
    }
    
    // Get the global county FIPS data
    List<FIPSCounty> counties = FIPSCounty.getData();
    String [] countyFipsArray = new String[counties.size()];
    i = 0;
    for ( FIPSCounty fips : counties ) {
        countyFipsArray[i++] = fips.getCode() + " - " + fips.getName() + ", " + fips.getStateAbbreviation();
    }
    
    // Get the global climate division data
    List<ClimateDivision> climateDivs = ClimateDivision.getData();
    String [] climateDivsArray = new String[climateDivs.size()];
    i = 0;
    for ( ClimateDivision div : climateDivs ) {
        // Look up the state abbreviation
        FIPSState state = FIPSState.lookupByName(div.getStateName());
        if ( state == null ) {
            // Should not happen
            continue;
        }
        // Format for ACIS web service is state abbreviation and 2 digit climate division
        climateDivsArray[i++] = state.getAbbreviation() +
            StringUtil.formatString(div.getCode(),"%02d") + " - " + div.getName();
    }

    filters.add(new InputFilter("Bounding Box",
        "bbox", "bbox",
        StringUtil.TYPE_STRING, null, null, true, "Bounding box in decimal degrees west,south,east,north " +
            " (e.g., -90,40,-88,41)"));
    
    List<String> climateDivsList = Arrays.asList(climateDivsArray);
    filters.add(new InputFilter("Climate Division",
        "climdiv", "climdiv",
        StringUtil.TYPE_STRING, climateDivsList, climateDivsList,
        true,
        "Specify 2 digits (e.g., 01, 10) if state is specified or combine here (e.g., NY01, NY10)" +
        " (see: http://www.esrl.noaa.gov/psd/data/usclimate/map.html)"));
    
    filters.add(new InputFilter("Drainage Basin (HUC)",
        "basin", "basin",
        StringUtil.TYPE_STRING, null, null, true,
        "For example 01080205 (see: http://water.usgs.gov/GIS/huc.html)"));
    
    List<String> countyFipsList = Arrays.asList(countyFipsArray);
    filters.add(new InputFilter("FIPS County",
        "county", "county",
        StringUtil.TYPE_STRING, countyFipsList, countyFipsList,
        true, // Allow edits because more than one county can be specified
        "Federal Information Processing Standard (FIPS) county (e.g., 09001)"));
    
    filters.add(new InputFilter("NWS County Warning Area",
        "cwa", "cwa",
        StringUtil.TYPE_STRING, null, null, true,
        "For example BOI (see: http://www.aprs-is.net/WX/NWSZones.aspx)"));
    
    List<String>stateList = Arrays.asList(stateArray);
    filters.add(new InputFilter("State Code",
        "state", "state",
        StringUtil.TYPE_STRING, stateList, stateList,
        true, // Allow edits because more than one state can be specified
        "State abbreviation (can specify more than one separated by commas)") );
    
    setToolTipText("RCC ACIS queries can be filtered based on location and time series metadata");
    setInputFilters(filters, numFilterGroups, 25);
}

/**
Return the data store corresponding to this input filter panel.
@return the data store corresponding to this input filter panel.
*/
public RccAcisDataStore getDataStore ( )
{
    return __dataStore;
}

}
