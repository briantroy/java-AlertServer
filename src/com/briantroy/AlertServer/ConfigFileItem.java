/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.briantroy.AlertServer;

/**
 *
 * @author brian.roy
 */
public class ConfigFileItem {
    /*
     *  This class is a single KVP found in the config file.
     *
     */
    private String property_name;
    private String property_value;

    public void setName(String n) {
        property_name = n;
    }

    public void setValue(String v) {
        property_value = v;
    }

    public String getName() {
        return property_name;
    }

    public String getValue() {
       return property_value;
    }

}
