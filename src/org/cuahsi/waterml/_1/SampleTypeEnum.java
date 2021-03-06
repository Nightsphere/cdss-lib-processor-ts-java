
package org.cuahsi.waterml._1;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sampleTypeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="sampleTypeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="FD"/>
 *     &lt;enumeration value="FF"/>
 *     &lt;enumeration value="FL"/>
 *     &lt;enumeration value="LF"/>
 *     &lt;enumeration value="GW"/>
 *     &lt;enumeration value="PB"/>
 *     &lt;enumeration value="PD"/>
 *     &lt;enumeration value="PE"/>
 *     &lt;enumeration value="PI"/>
 *     &lt;enumeration value="PW"/>
 *     &lt;enumeration value="RE"/>
 *     &lt;enumeration value="SE"/>
 *     &lt;enumeration value="SR"/>
 *     &lt;enumeration value="SS"/>
 *     &lt;enumeration value="SW"/>
 *     &lt;enumeration value="TE"/>
 *     &lt;enumeration value="TI"/>
 *     &lt;enumeration value="TW"/>
 *     &lt;enumeration value="VE"/>
 *     &lt;enumeration value="VI"/>
 *     &lt;enumeration value="VW"/>
 *     &lt;enumeration value="Grab"/>
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="No Sample"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "sampleTypeEnum")
@XmlEnum
public enum SampleTypeEnum {

    FD("FD"),
    FF("FF"),
    FL("FL"),
    LF("LF"),
    GW("GW"),
    PB("PB"),
    PD("PD"),
    PE("PE"),
    PI("PI"),
    PW("PW"),
    RE("RE"),
    SE("SE"),
    SR("SR"),
    SS("SS"),
    SW("SW"),
    TE("TE"),
    TI("TI"),
    TW("TW"),
    VE("VE"),
    VI("VI"),
    VW("VW"),
    @XmlEnumValue("Grab")
    GRAB("Grab"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("No Sample")
    NO_SAMPLE("No Sample");
    private final String value;

    SampleTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SampleTypeEnum fromValue(String v) {
        for (SampleTypeEnum c: SampleTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
