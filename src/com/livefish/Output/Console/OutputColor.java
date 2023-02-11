package com.livefish.Output.Console;

/**
 * Output color for better console visualisation
 * Done through ANSI codes of colors
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 * @see Logger
 */
public enum OutputColor {
    /**
     * Color reset ANSI byte (default color)
     */
    RESET("\u001B[0m"),
    /**
     * Black color ANSI byte
     */
    BLACK("\u001B[30m"),
    /**
     * Red color ANSI byte
     */
    RED("\u001B[31m"),
    /**
     * Green color ANSI byte
     */
    GREEN("\u001B[32m"),
    /**
     * Yellow color ANSI byte
     */
    YELLOW("\u001B[33m"),
    /**
     * Blue color ANSI byte
     */
    BLUE("\u001B[34m"),
    /**
     * Purple color ANSI byte
     */
    PURPLE("\u001B[35m"),
    /**
     * Cyan color ANSI byte
     */
    CYAN("\u001B[36m"),
    /**
     * White color ANSI byte
     */
    WHITE("\u001B[37m");

    /**
     * Text to print to get the color you need
     */
    private final String text;

    /**
     * Private construction for enum generation
     *
     * @param text ANSI bytes for particular color
     */
    OutputColor(final String text) {
        this.text = text;
    }


    /**
     * For + in strings, default object to string conversion
     *
     * @return String from ANSI byte
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
