/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.ws.serveri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.codehaus.jettison.json.JSONObject;
import org.foi.nwtis.thop.konfiguracije.Konfiguracija;
import org.foi.nwtis.thop.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.thop.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.thop.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.thop.rest.klijenti.GMKlijent;
import org.foi.nwtis.thop.rest.klijenti.OWMKlijent;
import org.foi.nwtis.thop.web.podaci.Adresa;
import org.foi.nwtis.thop.web.podaci.Lokacija;
import org.foi.nwtis.thop.web.podaci.MeteoPodaci;
import org.foi.nwtis.thop.web.slusaci.MeteoPodaciPreuzimanje;
import org.foi.nwtis.thop.web.slusaci.SlusacAplikacije;

/**
 *
 * @author NWTiS_3
 */
@WebService(serviceName = "GeoMeteoWS")
public class GeoMeteoWS {

    /**
     * Web service operation
     */
    @WebMethod(operationName = "dajSveAdrese")
    public java.util.List<Adresa> dajSveAdrese() {
        String konfig = SlusacAplikacije.getPutanjaDoKonfig();
        List<Adresa> adrese = new ArrayList<>();
        adrese = dohvatiAdrese(konfig);
        return adrese;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "dajVazeceMeteoPodatkeZaAdresu")
    public MeteoPodaci dajVazeceMeteoPodatkeZaAdresu(@WebParam(name = "adresa") String adresa) {
        GMKlijent gmk = new GMKlijent();
        Lokacija l = gmk.getGeoLocation(adresa);

        Konfiguracija konf = null;
        String konfig = SlusacAplikacije.getPutanjaDoKonfig();
        try {
            konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(konfig);
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(MeteoPodaciPreuzimanje.class.getName()).log(Level.SEVERE, null, ex);
        }
        String apiKey = konf.dajPostavku("apiKey");
        OWMKlijent owmk = new OWMKlijent(apiKey);

        MeteoPodaci mp = owmk.getRealTimeWeather(l.getLatitude(), l.getLongitude());

        return mp;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "dajZadnjeMeteoPodatkeZaAdresu")
    public MeteoPodaci dajZadnjeMeteoPodatkeZaAdresu(@WebParam(name = "adresa") String adresa) {
        /**
         * Vraćam meteopodatke na indeksu 0 iz liste sa meteopodatcima
         */
        List<MeteoPodaci> meteoPodaci = new ArrayList<>();
        meteoPodaci = dohvatiMeteoPodatke(adresa, true);
        MeteoPodaci mp = meteoPodaci.get(0);
        if (mp != null) {
            return mp;
        } else {
            return null;
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "dajSveMeteoPodatkeZaAdresu")
    public List<org.foi.nwtis.thop.web.podaci.MeteoPodaci> dajSveMeteoPodatkeZaAdresu(@WebParam(name = "adresa") String adresa) {
        List<MeteoPodaci> meteoPodaci = new ArrayList<>();
        meteoPodaci = dohvatiMeteoPodatke(adresa, false);

        /**
         * Vraćam listu meteopodataka
         */
        if (meteoPodaci != null) {
            return meteoPodaci;
        } else {
            return null;
        }
    }

    /**
     * Metoda koja na osnovi adrese zadane dohvaća sve podatke iz tablice
     * thop_meteo. SQL upit vraća sve meteopodatke od zadnjega do prvoga
     *
     * @param adresa
     * @param zadnji ako trebamo samo zadnje meteopodatke o adresi onda funkcija
     * vraća listu odmah nakon prvog unosa u listu pa kasnije samo dohvaćam taj
     * unos preko indeksa 0
     * @return
     */
    public List<MeteoPodaci> dohvatiMeteoPodatke(String adresa, boolean zadnji) {
        List<MeteoPodaci> meteoPodaci = new ArrayList<>();
        String konfig = SlusacAplikacije.getPutanjaDoKonfig();
        BP_Konfiguracija bpk = new BP_Konfiguracija(konfig);
        String server = bpk.getServer_database();
        String baza = server + bpk.getUser_database();
        String korisnik = bpk.getUser_username();
        String lozinka = bpk.getUser_password();
        String sql = "SELECT * FROM thop_meteo WHERE idAdresa IN (SELECT idAdresa FROM adrese WHERE adresa = '" + adresa + "') ORDER BY idAdresa DESC";
        String driver = bpk.getDriver_database(server);
        try {
            Class.forName(driver);
            Connection veza = DriverManager.getConnection(baza, korisnik, lozinka);
            Statement naredba = veza.createStatement();
            ResultSet odg = naredba.executeQuery(sql);
            {
                while (odg.next()) {
                    MeteoPodaci mp = new MeteoPodaci();
                    //System.out.println("IDTHOP_METEO: " + odg.getString(1) + "\nVLAGA: " + odg.getString(2) + "\nVJETAR: " + odg.getString(3)
                    //       + " \nVRIJEME: " + odg.getString(4) + "\nHPA: " + odg.getString(5) + "\nTEMPERATURA: " + odg.getString(6) + "\nIDADRESA: " + odg.getString(5));
                    mp.setHumidityValue(Float.parseFloat(odg.getString(2)));
                    mp.setWindSpeedValue(Float.parseFloat(odg.getString(3)));
                    mp.setWeatherValue(odg.getString(4));
                    mp.setPressureValue(Float.parseFloat(odg.getString(5)));
                    mp.setTemperatureValue(Float.parseFloat(odg.getString(6)));
                    meteoPodaci.add(mp);
                    if (zadnji) {
                        return meteoPodaci;
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(MeteoPodaciPreuzimanje.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return meteoPodaci;
    }

    /**
     * Metoda koja uzima put do konfig datoteke nakon cega dohvaća sve unose u
     * tablici adrese i iste sprema u listu adresa koju vraća
     *
     * @param datoteka put do konfig datoteke
     * @return
     */
    public List<Adresa> dohvatiAdrese(String datoteka) {
        List<Adresa> adrese = new ArrayList<>();
        BP_Konfiguracija bpk = new BP_Konfiguracija(datoteka);
        String server = bpk.getServer_database();
        String baza = server + bpk.getUser_database();
        String korisnik = bpk.getUser_username();
        String lozinka = bpk.getUser_password();
        String sql = "SELECT * FROM adrese";
        String driver = bpk.getDriver_database(server);
        try {
            Class.forName(driver);
            Connection veza = DriverManager.getConnection(baza, korisnik, lozinka);
            Statement naredba = veza.createStatement();
            ResultSet odg = naredba.executeQuery(sql);
            {
                while (odg.next()) {
                    Adresa a = new Adresa(Long.parseLong(odg.getString(1)), odg.getString(2), new Lokacija(odg.getString(3), odg.getString(4)));
                    //System.out.println("ID: " + odg.getString(1) + " ADRESA: " + odg.getString(2) + "LAT: " + odg.getString(3) + "LONG: " + odg.getString(4));
                    adrese.add(a);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(MeteoPodaciPreuzimanje.class.getName()).log(Level.SEVERE, null, ex);
        }

        return adrese;
    }
}
