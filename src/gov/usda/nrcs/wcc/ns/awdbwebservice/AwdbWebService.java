
package gov.usda.nrcs.wcc.ns.awdbwebservice;

import java.math.BigDecimal;
import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebService(name = "AwdbWebService", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface AwdbWebService {


    /**
     * 
     * @param stationTriplet
     * @param forecastPeriod
     * @param elementCd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.Forecast>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecasts", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecasts")
    @ResponseWrapper(localName = "getForecastsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastsResponse")
    public List<Forecast> getForecasts(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "forecastPeriod", targetNamespace = "")
        String forecastPeriod);

    /**
     * 
     * @param beginPublicationDate
     * @param stationTriplet
     * @param endPublicationDate
     * @param forecastPeriod
     * @param elementCd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.Forecast>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastsByPubDate", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastsByPubDate")
    @ResponseWrapper(localName = "getForecastsByPubDateResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastsByPubDateResponse")
    public List<Forecast> getForecastsByPubDate(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "forecastPeriod", targetNamespace = "")
        String forecastPeriod,
        @WebParam(name = "beginPublicationDate", targetNamespace = "")
        String beginPublicationDate,
        @WebParam(name = "endPublicationDate", targetNamespace = "")
        String endPublicationDate);

    /**
     * 
     * @param elementCds
     * @param maxElevation
     * @param networkCds
     * @param heightDepths
     * @param maxLongitude
     * @param ordinals
     * @param minElevation
     * @param hucs
     * @param stateCds
     * @param minLatitude
     * @param countyNames
     * @param maxLatitude
     * @param minLongitude
     * @param stationIds
     * @param logicalAnd
     * @return
     *     returns java.util.List<java.lang.String>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getStations", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStations")
    @ResponseWrapper(localName = "getStationsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStationsResponse")
    public List<String> getStations(
        @WebParam(name = "stationIds", targetNamespace = "")
        List<String> stationIds,
        @WebParam(name = "stateCds", targetNamespace = "")
        List<String> stateCds,
        @WebParam(name = "networkCds", targetNamespace = "")
        List<String> networkCds,
        @WebParam(name = "hucs", targetNamespace = "")
        List<String> hucs,
        @WebParam(name = "countyNames", targetNamespace = "")
        List<String> countyNames,
        @WebParam(name = "minLatitude", targetNamespace = "")
        BigDecimal minLatitude,
        @WebParam(name = "maxLatitude", targetNamespace = "")
        BigDecimal maxLatitude,
        @WebParam(name = "minLongitude", targetNamespace = "")
        BigDecimal minLongitude,
        @WebParam(name = "maxLongitude", targetNamespace = "")
        BigDecimal maxLongitude,
        @WebParam(name = "minElevation", targetNamespace = "")
        BigDecimal minElevation,
        @WebParam(name = "maxElevation", targetNamespace = "")
        BigDecimal maxElevation,
        @WebParam(name = "elementCds", targetNamespace = "")
        List<String> elementCds,
        @WebParam(name = "ordinals", targetNamespace = "")
        List<Integer> ordinals,
        @WebParam(name = "heightDepths", targetNamespace = "")
        List<HeightDepth> heightDepths,
        @WebParam(name = "logicalAnd", targetNamespace = "")
        boolean logicalAnd);

    /**
     * 
     * @param getFlags
     * @param beginMonth
     * @param heightDepth
     * @param duration
     * @param stationTriplets
     * @param endDay
     * @param beginDay
     * @param elementCd
     * @param endMonth
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.AveragesData>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getAveragesData", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetAveragesData")
    @ResponseWrapper(localName = "getAveragesDataResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetAveragesDataResponse")
    public List<AveragesData> getAveragesData(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "heightDepth", targetNamespace = "")
        HeightDepth heightDepth,
        @WebParam(name = "duration", targetNamespace = "")
        Duration duration,
        @WebParam(name = "getFlags", targetNamespace = "")
        boolean getFlags,
        @WebParam(name = "beginMonth", targetNamespace = "")
        int beginMonth,
        @WebParam(name = "beginDay", targetNamespace = "")
        int beginDay,
        @WebParam(name = "endMonth", targetNamespace = "")
        int endMonth,
        @WebParam(name = "endDay", targetNamespace = "")
        int endDay);

    /**
     * 
     * @param heightDepth
     * @param stationTriplets
     * @param beginHour
     * @param ordinal
     * @param endDate
     * @param beginDate
     * @param endHour
     * @param elementCd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.HourlyData>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getHourlyData", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetHourlyData")
    @ResponseWrapper(localName = "getHourlyDataResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetHourlyDataResponse")
    public List<HourlyData> getHourlyData(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "ordinal", targetNamespace = "")
        int ordinal,
        @WebParam(name = "heightDepth", targetNamespace = "")
        HeightDepth heightDepth,
        @WebParam(name = "beginDate", targetNamespace = "")
        String beginDate,
        @WebParam(name = "endDate", targetNamespace = "")
        String endDate,
        @WebParam(name = "beginHour", targetNamespace = "")
        Integer beginHour,
        @WebParam(name = "endHour", targetNamespace = "")
        Integer endHour);

    /**
     * 
     * @return
     *     returns gov.usda.nrcs.wcc.ns.awdbwebservice.Diagnostics
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "runDiagnostics", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.RunDiagnostics")
    @ResponseWrapper(localName = "runDiagnosticsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.RunDiagnosticsResponse")
    public Diagnostics runDiagnostics();

    /**
     * 
     * @param stationTriplet
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.ForecastEquation>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastEquations", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastEquations")
    @ResponseWrapper(localName = "getForecastEquationsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastEquationsResponse")
    public List<ForecastEquation> getForecastEquations(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet);

    /**
     * 
     * @param unitCd
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getUnitName", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetUnitName")
    @ResponseWrapper(localName = "getUnitNameResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetUnitNameResponse")
    public String getUnitName(
        @WebParam(name = "unitCd", targetNamespace = "")
        String unitCd);

    /**
     * 
     * @param stationTriplets
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.StationMetaData>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getStationMetadataMultiple", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStationMetadataMultiple")
    @ResponseWrapper(localName = "getStationMetadataMultipleResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStationMetadataMultipleResponse")
    public List<StationMetaData> getStationMetadataMultiple(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets);

    /**
     * 
     * @param stationTriplet
     * @return
     *     returns gov.usda.nrcs.wcc.ns.awdbwebservice.ForecastPoint
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastPoint", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPoint")
    @ResponseWrapper(localName = "getForecastPointResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPointResponse")
    public ForecastPoint getForecastPoint(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet);

    /**
     * 
     * @param beginWaterYear
     * @param heightDepth
     * @param stationTriplets
     * @param endWaterYear
     * @param ordinal
     * @param elementCd
     * @param durationCd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.StationDataAssuredFlags>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getStationDataAssuredFlags", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStationDataAssuredFlags")
    @ResponseWrapper(localName = "getStationDataAssuredFlagsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStationDataAssuredFlagsResponse")
    public List<StationDataAssuredFlags> getStationDataAssuredFlags(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "ordinal", targetNamespace = "")
        int ordinal,
        @WebParam(name = "heightDepth", targetNamespace = "")
        HeightDepth heightDepth,
        @WebParam(name = "durationCd", targetNamespace = "")
        String durationCd,
        @WebParam(name = "beginWaterYear", targetNamespace = "")
        int beginWaterYear,
        @WebParam(name = "endWaterYear", targetNamespace = "")
        int endWaterYear);

    /**
     * 
     * @param stationTriplets
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.ForecastEquation>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastEquationsMultiple", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastEquationsMultiple")
    @ResponseWrapper(localName = "getForecastEquationsMultipleResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastEquationsMultipleResponse")
    public List<ForecastEquation> getForecastEquationsMultiple(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets);

    /**
     * 
     * @param periods
     * @param stationTriplets
     * @param centralTendencyType
     * @param elementCd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.ForecastPeriodCentralTendency>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastPeriodCentralTendency", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPeriodCentralTendency")
    @ResponseWrapper(localName = "getForecastPeriodCentralTendencyResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPeriodCentralTendencyResponse")
    public List<ForecastPeriodCentralTendency> getForecastPeriodCentralTendency(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "periods", targetNamespace = "")
        List<String> periods,
        @WebParam(name = "centralTendencyType", targetNamespace = "")
        CentralTendencyType centralTendencyType);

    /**
     * 
     * @param elementCd
     * @return
     *     returns gov.usda.nrcs.wcc.ns.awdbwebservice.Element
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getElement", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetElement")
    @ResponseWrapper(localName = "getElementResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetElementResponse")
    public Element getElement(
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd);

    /**
     * 
     * @param publicationMonth
     * @param stationTriplet
     * @param publicationDay
     * @param publicationYear
     * @param probability
     * @param forecastPeriod
     * @param elementCd
     * @return
     *     returns java.math.BigDecimal
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastValue", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastValue")
    @ResponseWrapper(localName = "getForecastValueResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastValueResponse")
    public BigDecimal getForecastValue(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "forecastPeriod", targetNamespace = "")
        String forecastPeriod,
        @WebParam(name = "probability", targetNamespace = "")
        int probability,
        @WebParam(name = "publicationYear", targetNamespace = "")
        int publicationYear,
        @WebParam(name = "publicationMonth", targetNamespace = "")
        int publicationMonth,
        @WebParam(name = "publicationDay", targetNamespace = "")
        int publicationDay);

    /**
     * 
     * @param forecaster
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.Configuration>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastConfigurations", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastConfigurations")
    @ResponseWrapper(localName = "getForecastConfigurationsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastConfigurationsResponse")
    public List<Configuration> getForecastConfigurations(
        @WebParam(name = "forecaster", targetNamespace = "")
        String forecaster);

    /**
     * 
     * @param getFlags
     * @param heightDepth
     * @param duration
     * @param stationTriplets
     * @param centralTendencyType
     * @param elementCd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.CentralTendencyPeakData>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getCentralTendencyPeakData", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetCentralTendencyPeakData")
    @ResponseWrapper(localName = "getCentralTendencyPeakDataResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetCentralTendencyPeakDataResponse")
    public List<CentralTendencyPeakData> getCentralTendencyPeakData(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "heightDepth", targetNamespace = "")
        HeightDepth heightDepth,
        @WebParam(name = "duration", targetNamespace = "")
        Duration duration,
        @WebParam(name = "getFlags", targetNamespace = "")
        boolean getFlags,
        @WebParam(name = "centralTendencyType", targetNamespace = "")
        CentralTendencyType centralTendencyType);

    /**
     * 
     * @param stationTriplets
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.ReservoirMetadata>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getReservoirMetadataMultiple", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetReservoirMetadataMultiple")
    @ResponseWrapper(localName = "getReservoirMetadataMultipleResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetReservoirMetadataMultipleResponse")
    public List<ReservoirMetadata> getReservoirMetadataMultiple(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets);

    /**
     * 
     * @param stationTriplet
     * @param forecastPeriod
     * @param elementCd
     * @param publicationDate
     * @return
     *     returns gov.usda.nrcs.wcc.ns.awdbwebservice.Forecast
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecast", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecast")
    @ResponseWrapper(localName = "getForecastResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastResponse")
    public Forecast getForecast(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "forecastPeriod", targetNamespace = "")
        String forecastPeriod,
        @WebParam(name = "publicationDate", targetNamespace = "")
        String publicationDate);

    /**
     * 
     * @param stationTriplet
     * @return
     *     returns gov.usda.nrcs.wcc.ns.awdbwebservice.ReservoirMetadata
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getReservoirMetadata", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetReservoirMetadata")
    @ResponseWrapper(localName = "getReservoirMetadataResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetReservoirMetadataResponse")
    public ReservoirMetadata getReservoirMetadata(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet);

    /**
     * 
     * @param beginPublicationDate
     * @param stationTriplet
     * @param endPublicationDate
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.ForecastFull>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getAllForecastsForStation", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetAllForecastsForStation")
    @ResponseWrapper(localName = "getAllForecastsForStationResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetAllForecastsForStationResponse")
    public List<ForecastFull> getAllForecastsForStation(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet,
        @WebParam(name = "beginPublicationDate", targetNamespace = "")
        String beginPublicationDate,
        @WebParam(name = "endPublicationDate", targetNamespace = "")
        String endPublicationDate);

    /**
     * 
     * @param periods
     * @param stationTriplets
     * @param elementCd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.ForecastPeriodAverage>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastPeriodAverages", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPeriodAverages")
    @ResponseWrapper(localName = "getForecastPeriodAveragesResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPeriodAveragesResponse")
    public List<ForecastPeriodAverage> getForecastPeriodAverages(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "periods", targetNamespace = "")
        List<String> periods);

    /**
     * 
     * @param stationTriplet
     * @param endDate
     * @param beginDate
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.StationElement>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getStationElements", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStationElements")
    @ResponseWrapper(localName = "getStationElementsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStationElementsResponse")
    public List<StationElement> getStationElements(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet,
        @WebParam(name = "beginDate", targetNamespace = "")
        String beginDate,
        @WebParam(name = "endDate", targetNamespace = "")
        String endDate);

    /**
     * 
     * @param heightDepth
     * @param stationTriplets
     * @param ordinal
     * @param endDate
     * @param unitSystem
     * @param beginDate
     * @param elementCd
     * @param filter
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.InstantaneousData>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getInstantaneousData", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetInstantaneousData")
    @ResponseWrapper(localName = "getInstantaneousDataResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetInstantaneousDataResponse")
    public List<InstantaneousData> getInstantaneousData(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "ordinal", targetNamespace = "")
        int ordinal,
        @WebParam(name = "heightDepth", targetNamespace = "")
        HeightDepth heightDepth,
        @WebParam(name = "beginDate", targetNamespace = "")
        String beginDate,
        @WebParam(name = "endDate", targetNamespace = "")
        String endDate,
        @WebParam(name = "filter", targetNamespace = "")
        InstantaneousDataFilter filter,
        @WebParam(name = "unitSystem", targetNamespace = "")
        UnitSystem unitSystem);

    /**
     * 
     * @param getFlags
     * @param heightDepth
     * @param duration
     * @param stationTriplets
     * @param ordinal
     * @param endDate
     * @param beginDate
     * @param elementCd
     * @param alwaysReturnDailyFeb29
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.Data>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getData", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetData")
    @ResponseWrapper(localName = "getDataResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetDataResponse")
    public List<Data> getData(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "ordinal", targetNamespace = "")
        int ordinal,
        @WebParam(name = "heightDepth", targetNamespace = "")
        HeightDepth heightDepth,
        @WebParam(name = "duration", targetNamespace = "")
        Duration duration,
        @WebParam(name = "getFlags", targetNamespace = "")
        boolean getFlags,
        @WebParam(name = "beginDate", targetNamespace = "")
        String beginDate,
        @WebParam(name = "endDate", targetNamespace = "")
        String endDate,
        @WebParam(name = "alwaysReturnDailyFeb29", targetNamespace = "")
        Boolean alwaysReturnDailyFeb29);

    /**
     * 
     * @param getFlags
     * @param beginMonth
     * @param heightDepth
     * @param duration
     * @param stationTriplets
     * @param endDay
     * @param centralTendencyType
     * @param beginDay
     * @param elementCd
     * @param endMonth
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.CentralTendencyData>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getCentralTendencyData", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetCentralTendencyData")
    @ResponseWrapper(localName = "getCentralTendencyDataResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetCentralTendencyDataResponse")
    public List<CentralTendencyData> getCentralTendencyData(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "heightDepth", targetNamespace = "")
        HeightDepth heightDepth,
        @WebParam(name = "duration", targetNamespace = "")
        Duration duration,
        @WebParam(name = "getFlags", targetNamespace = "")
        boolean getFlags,
        @WebParam(name = "beginMonth", targetNamespace = "")
        int beginMonth,
        @WebParam(name = "beginDay", targetNamespace = "")
        int beginDay,
        @WebParam(name = "endMonth", targetNamespace = "")
        int endMonth,
        @WebParam(name = "endDay", targetNamespace = "")
        int endDay,
        @WebParam(name = "centralTendencyType", targetNamespace = "")
        CentralTendencyType centralTendencyType);

    /**
     * 
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.ForecastPeriod>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastPeriods", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPeriods")
    @ResponseWrapper(localName = "getForecastPeriodsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPeriodsResponse")
    public List<ForecastPeriod> getForecastPeriods();

    /**
     * 
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.HeightDepth>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getHeightDepths", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetHeightDepths")
    @ResponseWrapper(localName = "getHeightDepthsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetHeightDepthsResponse")
    public List<HeightDepth> getHeightDepths();

    /**
     * 
     * @param forecastPointNames
     * @param stateCds
     * @param networkCds
     * @param forecasters
     * @param stationIds
     * @param hucs
     * @param logicalAnd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.ForecastPoint>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getForecastPoints", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPoints")
    @ResponseWrapper(localName = "getForecastPointsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetForecastPointsResponse")
    public List<ForecastPoint> getForecastPoints(
        @WebParam(name = "stationIds", targetNamespace = "")
        List<String> stationIds,
        @WebParam(name = "stateCds", targetNamespace = "")
        List<String> stateCds,
        @WebParam(name = "networkCds", targetNamespace = "")
        List<String> networkCds,
        @WebParam(name = "forecastPointNames", targetNamespace = "")
        List<String> forecastPointNames,
        @WebParam(name = "hucs", targetNamespace = "")
        List<String> hucs,
        @WebParam(name = "forecasters", targetNamespace = "")
        List<String> forecasters,
        @WebParam(name = "logicalAnd", targetNamespace = "")
        boolean logicalAnd);

    /**
     * 
     * @param stationTriplet
     * @return
     *     returns gov.usda.nrcs.wcc.ns.awdbwebservice.StationMetaData
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getStationMetadata", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStationMetadata")
    @ResponseWrapper(localName = "getStationMetadataResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetStationMetadataResponse")
    public StationMetaData getStationMetadata(
        @WebParam(name = "stationTriplet", targetNamespace = "")
        String stationTriplet);

    /**
     * 
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "areYouThere", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.AreYouThere")
    @ResponseWrapper(localName = "areYouThereResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.AreYouThereResponse")
    public boolean areYouThere();

    /**
     * 
     * @param getFlags
     * @param heightDepth
     * @param duration
     * @param stationTriplets
     * @param elementCd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.AveragesPeakData>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getAveragesPeak", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetAveragesPeak")
    @ResponseWrapper(localName = "getAveragesPeakResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetAveragesPeakResponse")
    public List<AveragesPeakData> getAveragesPeak(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "heightDepth", targetNamespace = "")
        HeightDepth heightDepth,
        @WebParam(name = "duration", targetNamespace = "")
        Duration duration,
        @WebParam(name = "getFlags", targetNamespace = "")
        boolean getFlags);

    /**
     * 
     * @param getFlags
     * @param endYear
     * @param heightDepth
     * @param duration
     * @param stationTriplets
     * @param beginYear
     * @param ordinal
     * @param elementCd
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.PeakData>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getPeakData", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetPeakData")
    @ResponseWrapper(localName = "getPeakDataResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetPeakDataResponse")
    public List<PeakData> getPeakData(
        @WebParam(name = "stationTriplets", targetNamespace = "")
        List<String> stationTriplets,
        @WebParam(name = "elementCd", targetNamespace = "")
        String elementCd,
        @WebParam(name = "ordinal", targetNamespace = "")
        int ordinal,
        @WebParam(name = "heightDepth", targetNamespace = "")
        HeightDepth heightDepth,
        @WebParam(name = "duration", targetNamespace = "")
        Duration duration,
        @WebParam(name = "getFlags", targetNamespace = "")
        boolean getFlags,
        @WebParam(name = "beginYear", targetNamespace = "")
        int beginYear,
        @WebParam(name = "endYear", targetNamespace = "")
        int endYear);

    /**
     * 
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.Unit>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getUnits", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetUnits")
    @ResponseWrapper(localName = "getUnitsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetUnitsResponse")
    public List<Unit> getUnits();

    /**
     * 
     * @return
     *     returns java.util.List<gov.usda.nrcs.wcc.ns.awdbwebservice.Element>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getElements", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetElements")
    @ResponseWrapper(localName = "getElementsResponse", targetNamespace = "http://www.wcc.nrcs.usda.gov/ns/awdbWebService", className = "gov.usda.nrcs.wcc.ns.awdbwebservice.GetElementsResponse")
    public List<Element> getElements();

}
