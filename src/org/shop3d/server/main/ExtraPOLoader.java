/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.server.main;

import java.util.Arrays;
import java.util.List;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;

/**
 *
 * @author Fahad
 */
public class ExtraPOLoader {

    public static ParseObject GetShippingRate(ParseObject mainOrder) {
        ParseObject shippingRates = null;
        try {
            ParseQuery<ParseObject> shipList = new ParseQuery("Shipping_Rates");
            shipList.whereEqualTo("objectId", mainOrder.getParseObject("shipping_rate_id").getObjectId());
            shippingRates = shipList.find().get(0);
        } catch (Exception er) {
            System.out.println("Error in Shipping Rates - Exception: " + er.getMessage());
        }
        return shippingRates;
    }

    public static ParseObject GetShipping(ParseObject mainOrder) {
        ParseObject shipping = null;
        try {
            ParseQuery<ParseObject> shipList = new ParseQuery("Shipping_Address");
            shipList.whereEqualTo("objectId", mainOrder.getParseObject("shipping_address_id").getObjectId());
            shipping = shipList.find().get(0);
        } catch (Exception er) {
            System.out.println("Error in Shipping Addresses - Exception: " + er.getMessage());
        }
        return shipping;
    }

    public static List<ParseObject> GetTaxCountries() {
        List<ParseObject> tax = null;
        try {
            ParseQuery<ParseObject> taxCountries = new ParseQuery("Tax");
            taxCountries.orderByDescending("country");
            tax = taxCountries.find();
        } catch (Exception er) {
            System.out.println("Error in getting TAX countries  Exception: " + er.getMessage());
        }
        return tax;
    }

    public static List<String> GetEuropeanCountries() {
      
        String[] eu = {
            "United Kingdom,GB",
            "United States,US",
            "Belgium,BE",
            "Bulgaria,BG",
            "Czech Republic,CZ",
            "Denmark,DK",
            "Germany,DE",
            "Estonia,EE",
            "Ireland,IE",
            "Greece,GR",
            "Spain,ES",
            "Croatia,HR",
            "Italy,IT",
            "Cyprus,CY",
            "Latvia,LV",
            "Lithuania,LT",
            "Luxembourg,LU",
            "Hungary,HU",
            "Malta,MT",
            "Netherlands,NL",
            "Austria,AT",
            "Poland,PL",
            "Portugal,PT",
            "Romania,RO",
            "Slovakia,SK",
            "Slovenia,SI",
            "Finland,FI",
            "Sweden,SE"};

        return Arrays.asList(eu);
    }

    public static ParseObject GetModelFromUsermade(ParseObject userMade) {
        ParseObject originalModel = null;
        try {
            ParseQuery<ParseObject> modelList = new ParseQuery("Model");
            modelList.whereEqualTo("objectId", userMade.getParseObject("original_model_id").getObjectId());
            originalModel = modelList.find().get(0);
        } catch (Exception er) {
            System.out.println("The model is an original model - Exception: " + er.getMessage());
        }
        return originalModel;
    }

    public static ParseObject GetModel(ParseObject orderItem) {
        ParseObject originalModel = null;
        try {
            ParseQuery<ParseObject> modelList = new ParseQuery("Model");
            modelList.whereEqualTo("objectId", orderItem.getParseObject("model_id").getObjectId());
            originalModel = modelList.find().get(0);
        } catch (Exception er) {
            //System.out.println(" The model is usermade - Exception: " + er.getMessage());
        }
        return originalModel;
    }

    public static ParseObject GetModelUserMade(ParseObject orderItem) {
        ParseObject usermadeModel = null;
        try {
            ParseQuery<ParseObject> modelList = new ParseQuery("Model_UserMade");
            modelList.whereEqualTo("objectId", orderItem.getParseObject("usermade_model_id").getObjectId());
            usermadeModel = modelList.find().get(0);
        } catch (Exception er) {
            // System.out.println("Error in getting model usermade from OrderItem - Exception: " + er.getMessage());
        }
        return usermadeModel;
    }
}
