/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shop3d.http;

import org.json.JSONArray;

/**
 *
 * @author Fahad
 */
public class Req_GetMaterials extends Req_Base{

    public Req_GetMaterials(RunMode iRunMode) {
        super(iRunMode, "api/material/list/" );
    }
    
   
}
