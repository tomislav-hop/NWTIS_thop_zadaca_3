/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.ws.klijenti;

import org.foi.nwtis.thop.ws.serveri.MeteoPodaci;

/**
 *
 * @author NWTiS_3
 */
public class MeteoWSKlijent {

    public static java.util.List<org.foi.nwtis.thop.ws.serveri.Adresa> dajSveAdrese() {
        org.foi.nwtis.thop.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.thop.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.thop.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajSveAdrese();
    }

    public static java.util.List<org.foi.nwtis.thop.ws.serveri.MeteoPodaci> dajSveMeteoPodatkeZaAdresu(java.lang.String adresa) {
        org.foi.nwtis.thop.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.thop.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.thop.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajSveMeteoPodatkeZaAdresu(adresa);
    }

    public static MeteoPodaci dajZadnjeMeteoPodatkeZaAdresu(java.lang.String adresa) {
        org.foi.nwtis.thop.ws.serveri.GeoMeteoWS_Service service = new org.foi.nwtis.thop.ws.serveri.GeoMeteoWS_Service();
        org.foi.nwtis.thop.ws.serveri.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajZadnjeMeteoPodatkeZaAdresu(adresa);
    }
    
    
    
}
