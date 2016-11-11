/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Fahad
 */
public class HtmlCostExtracter {

    String htmlData;

    public HtmlCostExtracter(String sculpteoId) {
        try {
            Document doc = Jsoup.connect("http://www.sculpteo.com/en/shop/tracking/reference/" + sculpteoId).get();
            htmlData = doc.toString();
        } catch (Exception er) {

        }
    }

    public String GetTax() {
        if (htmlData.contains("Tax")) {
            String[] docSplit = htmlData.split("Tax");
            int indexOfDollar = docSplit[1].indexOf("$");
            String tax = docSplit[1].substring(indexOfDollar + 1, docSplit[1].indexOf(")", indexOfDollar));
            return tax;
        } else {
            return "0";
        }
    }

    public String GetTotal() {
        if (htmlData.contains("Total")) {
            String[] docSplit = htmlData.split("Total");
            int indexOfDollar = docSplit[1].indexOf("$");
            String totalCost = docSplit[1].substring(indexOfDollar + 1, docSplit[1].indexOf("<", indexOfDollar));
            return totalCost;
        } else {
            return "0";
        }
    }

    public String GetShippingCost() {
        if (htmlData.contains("Shipping Method")) {
            String[] docSplit = htmlData.split("Shipping Method");
            int indexOfDollar = docSplit[1].indexOf("$");
            String dollarSplit = docSplit[1].substring(indexOfDollar + 1, docSplit[1].indexOf("<", indexOfDollar));

            return dollarSplit;
        } else {
            return "0";
        }
    }

}
