/*
 * CSVParser.java
 *
 * Created on May 9, 2006, 3:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package rti.tscommandprocessor.commands.delimited;

import java.util.ArrayList;
import java.util.List;

/**
 * @todo - add more configurable options, if neccessary
 */

/**
 * Parse comma-separated values (CSV), a common Windows file format. Sample input: "LU",86.25,"11/4/1998","2:19PM",+4.0625
 * <p>
 * Inner logic adapted from a C++ original that was Copyright (C) 1999 Lucent Technologies Excerpted from 'The Practice of Programming' by Brian W. Kernighan and Rob
 * Pike.
 * <p>
 * Included by permission of the http://tpop.awl.com/ web site, which says: "You may use this code for any purpose, as long as you leave the copyright notice and book
 * citation attached." I have done so.
 * 
 * @author Brian W. Kernighan and Rob Pike (C++ original)
 * @author Ian F. Darwin (translation into Java and removal of I/O)
 * @author Ben Ballard (rewrote advQuoted to handle '""' and for readability)
 * @author Ian Schneider optimized and use modern classes
 */
public final class CSVParser {

    /** Construct a CSV parser, with the default separator (`,'). */
    public CSVParser() {
        this(',');
    }

    /**
     * Construct a CSV parser with a given separator.
     * 
     * @param sep The single char for the separator (not a list of separator characters)
     */
    public CSVParser(char sep) {
        fieldSep = sep;
    }

    /**
     * Construct a CSV parser with a given separator.
     * 
     * @param sep The string version of the single char for the separator (not a list of separator characters)
     */
    public CSVParser(String sep) {
        if ((sep != null) && (sep.length() > 0)) {
            fieldSep = sep.charAt(0);
        } else {
            fieldSep = ',';
        }
    }

    /** The fields in the current String */
    private final ArrayList list = new ArrayList/*String*/();

    private final StringBuffer sb = new StringBuffer();

    /** the separator char for this parser */
    private final char fieldSep;

    /**
     * parse: break the input String into fields
     * 
     * @return List containing each field from the original as a String, in order.
     */
    public List parse(final String line) {
        list.clear(); // recycle to initial state
        int i = 0;

        if (line.length() == 0) {
            list.add(line);
            return list;
        }
        final int len = line.length();
        do {
            sb.delete(0, sb.length());
            if ((i < len) && (line.charAt(i) == '"')) {
                i = advQuoted(line, sb, ++i); // skip quote
                list.add(sb.toString());
            } else {
                i = advPlain(line, sb, i);
                list.add(trimmed(sb));
            }
            i++;
        } while (i <= len);
        return list;
    }

    /** advQuoted: quoted field; return index of next separator */
    private int advQuoted(final String s, final StringBuffer sb, final int i) {
        int j;
        final int len = s.length();
        for (j = i; j < len; j++) {
            final char ch = s.charAt(j);
            if ((ch == '"') && (j + 1 < len)) {
                final char ch1 = s.charAt(j + 1);
                if (ch1 == '"') {
                    j++; // skip escape char
                } else if (ch1 == fieldSep) { // next delimeter
                    j++; // skip end quotes
                    break;
                }
            } else if ((ch == '"') && (j + 1 == len)) { // end quotes at end of line
                break; // done
            }
            sb.append(ch); // regular character.
        }
        return j;
    }

    private String trimmed(StringBuffer sb) {
        final int len = sb.length();
        int l = sb.length();
        int st = 0;

        while ((st < len) && (sb.charAt(st) <= ' ')) {
            st++;
        }
        while ((st < len) && (sb.charAt(l - 1) <= ' ')) {
            l--;
        }
        return ((st > 0) || (l < sb.length())) ? sb.substring(st, l) : sb.toString();
    }

    /** advPlain: unquoted field; return index of next separator */
    private int advPlain(String s, StringBuffer sb, int i) {
        final int j = s.indexOf(fieldSep, i); // look for separator

        if (j == -1) { // none found
            sb.append( s.substring(i,s.length()) );
            // FIXME SAM 2008-01-28 Java 1.5 use...
            //sb.append(s, i, s.length());
            return s.length();
        } else {
            sb.append( s.substring(i,j) );
            // FIXME SAM 2008-01-28 Java 1.5 use...
            //sb.append(s, i, j);
            return j;
        }
    }

}