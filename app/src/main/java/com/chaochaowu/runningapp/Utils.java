package com.chaochaowu.runningapp;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;


/**
 * @author chaochaowu
 */
class Utils {

    static ArrayList<LatLng> getLatLngs(){
        ArrayList<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(30.3126719672,120.3566998243));
        latLngs.add(new LatLng(30.3121857093,120.3566837311));
        latLngs.add(new LatLng(30.3116485074,120.3563940525));
        latLngs.add(new LatLng(30.3119078466,120.3556001186));
        latLngs.add(new LatLng(30.3128757317,120.3556591272));
        latLngs.add(new LatLng(30.3133573552,120.3570002317));
        return latLngs;
    }

}
