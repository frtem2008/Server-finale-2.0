package com.livefish.Output.Console;

/* Init -> Add colors -> Use by name or from enum */

import java.util.HashSet;
import java.util.Set;

/**
 * Print color record (Color name and a color associated with it)
 *
 * @param name  A color name in system
 * @param color A color associated with this name in system
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 * @see OutputColor
 */
record PrintColor(String name, OutputColor color) {
    /**
     * For + in strings, default object to string conversion
     *
     * @return String from ANSI byte of color
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return color.toString();
    }
}

/**
 * Logger implementation (Singleton)
 * Logs to console with current color, or with selected one
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 2.0
 * @see OutputColor
 * @see PrintColor
 */
public class Logger {
    /**
     * Logger singleton instance
     */
    private static Logger instance = null;
    /**
     * Set of all color bytes with their names for better usage
     *
     * @see Logger#addPrintColor(String, OutputColor)
     */

    private final Set<PrintColor> colors;
    /**
     * Does the user want to use colored text
     *
     * @see Logger#print(String, String)
     * @see Logger#print(String, OutputColor)
     */
    private boolean useColoredText;
    /**
     * Current logger printing color
     * Some methods ignore it and use the one they got from parameters
     *
     * @see Logger#print(String)
     */
    private OutputColor current;

    /**
     * Private singleton constructor
     * Initializes colors set and adds default color to system
     *
     * @see Logger#addPrintColor(String, OutputColor)
     */
    private Logger() {
        this.colors = new HashSet<>();
        this.current = OutputColor.RESET;
        addPrintColor("Default", OutputColor.RESET);
    }

    /**
     * Logger singleton getInstance function
     *
     * @return Singleton logger object
     * @see Logger#instance
     */
    public static Logger getInstance() {
        if (instance == null)
            instance = new Logger();
        return instance;
    }


    /**
     * Enables logger colored output
     *
     * @see Logger#useColoredText
     */
    public void enableColoredText() {
        useColoredText = true;
    }

    /**
     * Disables logger colored output
     *
     * @see Logger#useColoredText
     */
    public void disableColoredText() {
        useColoredText = false;
    }

    /**
     * Sets logger output color to one of the accessible
     *
     * @param color A new logger output color
     * @see OutputColor
     * @see Logger#print(String)
     */
    public void setOutputColor(OutputColor color) {
        current = color;
    }

    /**
     * Sets logger output color to one of the accessible, or to default, if there is no color with name name
     *
     * @param name A name of a color in logger system
     * @see OutputColor
     * @see Logger#current
     * @see Logger#print(String)
     * @see Logger#getColorByName(String)
     */
    public void setOutputColor(String name) {
        current = getColorByName(name).color();
    }

    /**
     * Sets logger current output color back to default
     *
     * @see Logger#current
     * @see Logger#getColorByName(String)
     */
    public void setDefaultOutputColor() {
        current = getColorByName("Default").color();
    }


    /**
     * Adds new print color with name name to system
     *
     * @param name  A name of a new color
     * @param color A color to draw when this print color is active
     * @see Logger#colors
     * @see PrintColor
     * @see OutputColor
     */
    public void addPrintColor(String name, OutputColor color) {
        colors.add(new PrintColor(name, color));
    }

    /**
     * Get a color reset ANSI string function
     * Is used to be print in the end of every string to restore color to default
     *
     * @return ANSI color reset string
     * @see OutputColor#RESET
     */
    private String getResetString() {
        return OutputColor.RESET.toString();
    }

    /**
     * Color getting by name function
     *
     * @param name A color name to get
     * @return A print color with name name in the system, or a default color, if the one with name name does not exist
     * @see PrintColor
     */
    private PrintColor getColorByName(String name) {
        for (PrintColor color : colors)
            if (color.name().equalsIgnoreCase(name))
                return color;
        return new PrintColor("default", OutputColor.RESET);
    }

    /**
     * Prints a string with current color with thread mark
     *
     * @param toPrint A string to print
     * @see Logger#print(String, OutputColor)
     */
    public void print(String toPrint) {
        print(toPrint, current);
    }

    /**
     * Prints a string with color with name colorName and thread mark
     *
     * @param toPrint   A string to print
     * @param colorName A color name in logger system to print with
     *
     * @see Logger#print(String, OutputColor)
     */
    public void print(String toPrint, String colorName) {
        print(toPrint, getColorByName(colorName).color());
    }

    /**
     * Prints a string from new line with color with name colorName and thread mark
     *
     * @param toPrint   A string to print
     * @param colorName A color name in logger system to print with
     *
     * @see Logger#print(String, OutputColor)
     */
    public void println(String toPrint, String colorName) {
        System.out.println();
        print(toPrint, getColorByName(colorName).color());
    }

    /**
     * Prints a string with color with color color and thread mark
     *
     * @param toPrint A string to print
     * @param color   A color name in logger system to print with
     *
     * @see Logger#useColoredText
     * @see OutputColor
     */
    public void print(String toPrint, OutputColor color) {
        toPrint = "[" + Thread.currentThread().getName() + "]" + toPrint;
        if (useColoredText)
            System.out.println(color.toString() + toPrint + getResetString());
        else
            System.out.println(toPrint);
    }
}
