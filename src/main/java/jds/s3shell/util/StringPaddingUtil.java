/*
 *
 *  * Copyright woojoo.com, 2015. All rights reserved.
 *
 */

package jds.s3shell.util;

/**
 * @author jsvede
 */
public class StringPaddingUtil {

    private StringPaddingUtil() {}

    public static String padWith(String source, String paddingCharacter, int maxLength ) {

        if(source != null) {
            if(source.length() > maxLength) {
                return pad(source, maxLength);
            } else {
                return pad(source, maxLength, false, paddingCharacter);
            }
        }
        return pad("",maxLength,false,paddingCharacter);
    }

    /**
     * Same functionality as the other method but does not throw an exception if the length of
     * {@code source} is greater than {@code maxLength}.
     *
     * @param source - the {@code String} to consider.
     * @param maxLength - the maximum length; used to either pad or truncate.
     *
     * @return a {@code String} that is of the length specified by {@code maxLength}. Never
     * {@code null}. Returns either a {@code String} of spaces or a truncated value.
     */
    public static String pad(String source, int maxLength) {

        return pad(source, maxLength, false, " ");
    }

    public static String pad(String source, int maxLength, boolean failIfLarger) {

        return pad(source, maxLength, failIfLarger, " ");
    }

    /**
     * A utility method to format a {@code String} to be a certain length, either
     * by truncating the value or by padding with spaces.
     *
     * @param source - the {@code String} to consider.
     * @param maxLength - the maximum length; used to either pad or truncate.
     * @param failIfLarger - true if you want this method to throw a {@code RuntimeException} when the
     *                       length of {@code source} is longer than {@code maxLength}.
     *
     * @return a {@code String} that is of the length specified by {@code maxLength}. Never
     * {@code null}. Returns either a {@code String} of spaces or a truncated value.
     *
     */
    public static String pad(String source, int maxLength, boolean failIfLarger, String character) {

        if(source != null) {
            if(source.length() > maxLength) {
                if(failIfLarger) {
                    throw new RuntimeException("The String '" + source + "' was larger than " +
                                               maxLength + " and failIfLarger is " + failIfLarger);
                }
                return source.substring(0, maxLength+1);
            } else {
                int padding = maxLength - source.length();
                for(int x=0; x < padding; x++) {
                    source += character;
                }
                return source;
            }
        } else {
            String spaces = "";
            for(int x=0; x < maxLength; x++) {
                spaces += character;
            }
            return spaces;
        }
    }
}
