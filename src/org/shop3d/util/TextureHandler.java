/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.util;

import com.threed.jpct.TextureManager;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author Fahad
 */
public class TextureHandler {

    // Holds the names of all textures for the object3D.
    public ArrayList<String> allTexNames;

    // Has only the flat RG colors.
    public ArrayList<String> allFlatColorNames;

    // Has all image asset textures.
    public ArrayList<String> allTexImageNames;

    public TextureHandler() {
        allTexNames = new ArrayList<String>();
        allFlatColorNames = new ArrayList<String>();
        allTexImageNames = new ArrayList<String>();
    }

    public void init(TextureManager tm) {

        Enumeration er = tm.getNames();
        while (er.hasMoreElements()) {
            String sTexN = er.nextElement().toString();
            if (sTexN.contains("dummy") == false) {

                if (sTexN.contains("__obj-Color:")) {
                    allFlatColorNames.add(sTexN);
                } else {
                     if (sTexN.contains("Maps")) {
                         sTexN = sTexN.substring(sTexN.indexOf("Maps"), sTexN.length());
                     }
                    allTexImageNames.add(sTexN);
                }

                allTexNames.add(sTexN);
            }

        }
        System.out.println(allTexNames.size());
    }

    public String[] GetAllImageFilePaths(String modelDir, String objFile, String mtlFile) {
        ArrayList<String> allFileToZip = new ArrayList<String>();
        allFileToZip.add(objFile);
        allFileToZip.add(mtlFile);
        //allFileToZip.add(modelDir + "Maps/*");
        // allFileToZip.add(modelDir +"Maps\\");
        for (int i = 0; i < allTexImageNames.size(); i++) {
            String name = allTexImageNames.get(i);
            
            // Remove the starting "/" that exists sometimes in the name
            if (name.charAt(0) == '/') {
                name = name.substring(1, name.length());
            }
            
            // Replae the / with the other slash.
            name = name.replace("/", "\\");
            allFileToZip.add(modelDir + name);

        }

        String returnList[] = new String[allFileToZip.size()];
        for (int i = 0; i < allFileToZip.size(); i++) {
            returnList[i] = allFileToZip.get(i);
        }

        return returnList;
    }

    public boolean HasTextures() {
        if (allTexImageNames.size() > 0) {
            return true;
        }
        return false;
    }

    public String[] GetOnlyImageFilePaths(String modelDir) {
        ArrayList<String> allFileToZip = new ArrayList<String>();

        for (int i = 0; i < allTexImageNames.size(); i++) {
            allFileToZip.add(modelDir + allTexImageNames.get(i));
        }

        String returnList[] = new String[allFileToZip.size()];
        for (int i = 0; i < allFileToZip.size(); i++) {
            returnList[i] = allFileToZip.get(i);
        }

        return returnList;

    }
}
