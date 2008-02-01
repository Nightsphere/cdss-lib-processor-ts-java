/**
 *
 * Created on August 23, 2007, 2:19 PM
 *
 */

package rti.tscommandprocessor.commands.delimited;

import RTi.Util.Time.DateTime;

/**
 * A converter converts some object to another. An identity null-patterned converter is provided which simply returns the object passed in.
 * 
 * @author iws
 */
public interface Converter {

    /**
     * Provide a converted representation of the provided object.
     * @param o The object to convert
     * @return The converted object.
     * @throws java.lang.IllegalArgumentException If the Converter is unable
     * to convert the provided object.
     */
    Object convert(Object o) throws IllegalArgumentException;

    /**
     * The Identity Converter does nothing.
     */
    public static Converter IDENTITY = new Converter() {
        public Object convert(Object o) {
            return o;
        }
    };

    /**
     * A String to Double converter that uses standard double parsing.
     */
    public static Converter STRING_TO_DOUBLE = new Converter() {
        public Object convert(Object o) throws IllegalArgumentException {
            //return Double.parseDouble(o.toString());
            // FIXME SAM 2008-01-28 Need to talk to Ian about what is going on
            return Double.valueOf(o.toString());
        }
    };

    /**
     * A String to DateTime converter that uses all formats accepted by
     * DateTime.
     */
    public static Converter STRING_TO_DATE_TIME = new Converter() {
        public Object convert(Object o) throws IllegalArgumentException {
            String s = o.toString();
            try {
                return DateTime.parse(s);
            } catch (Exception ex) {
                //throw new IllegalArgumentException(ex);
                throw new IllegalArgumentException(ex.toString());
            }
        }
    };
}
