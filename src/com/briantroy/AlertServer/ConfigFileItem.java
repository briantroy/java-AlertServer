/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.briantroy.AlertServer;

/**
 * This class is represents a configuration file item.
 *
 * @author Brian Roy brian@briantroy.com
 */
public class ConfigFileItem {
    /*
     *  This class is a single KVP found in the config file.
     *
     */
    private String property_name;
    private String property_value;

    /*
    Setter for Name
     */
    public void setName(String n) {
        property_name = n;
    }
    /*
    Setter for Value
     */
    public void setValue(String v) {
        property_value = v;
    }
    /*
    Getter for Name
     */
    public String getName() {
        return property_name;
    }
    /*
    Getter for Value
     */
    public String getValue() {
       return property_value;
    }

}
