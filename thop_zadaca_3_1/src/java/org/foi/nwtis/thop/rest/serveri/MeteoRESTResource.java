/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.rest.serveri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.foi.nwtis.thop.konfiguracije.Konfiguracija;
import org.foi.nwtis.thop.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.thop.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.thop.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.thop.rest.klijenti.OWMKlijent;
import org.foi.nwtis.thop.web.podaci.Adresa;
import org.foi.nwtis.thop.web.podaci.Lokacija;
import org.foi.nwtis.thop.web.podaci.MeteoPodaci;
import org.foi.nwtis.thop.web.slusaci.MeteoPodaciPreuzimanje;
import org.foi.nwtis.thop.web.slusaci.SlusacAplikacije;

/**
 * REST Web Service
 *
 * @author NWTiS_3
 */
public class MeteoRESTResource {

    private String id;

    /**
     * Creates a new instance of MeteoRESTResource
     */
    private MeteoRESTResource(String id) {
        this.id = id;
    }

    /**
     * Get instance of the MeteoRESTResource
     */
    public static MeteoRESTResource getInstance(String id) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of MeteoRESTResource class.
        return new MeteoRESTResource(id);
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.matnovak.rest.serveri.MeteoRESTResource
     *
     * Metoda dohvaća meteopodatke za dobiveni id i prebacuje ih u json format
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
        String konfig = SlusacAplikacije.getPutanjaDoKonfig();
        List<Adresa> adrese = new ArrayList<>();
        adrese = dohvatiAdrese(konfig);

        JSONObject rezultat = new JSONObject();
        JSONArray jaAdrese = new JSONArray();

        Konfiguracija konf = null;

        try {
            konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(konfig);
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(MeteoPodaciPreuzimanje.class.getName()).log(Level.SEVERE, null, ex);
        }

        String apiKey = konf.dajPostavku("apiKey");

        for (Adresa a : adrese) {
            if (a.getIdadresa() == Long.parseLong(id)) {
                OWMKlijent owmk = new OWMKlijent(apiKey);

                MeteoPodaci mp = owmk.getRealTimeWeather(
                        a.getGeoloc().getLatitude(),
                        a.getGeoloc().getLongitude());

                JSONObject joAdresa = new JSONObject();
                try {
                    joAdresa.put("id", Long.toString(a.getIdadresa()));
                    joAdresa.put("adresa", a.getAdresa());
                    joAdresa.put("vrijeme", mp.getWeatherValue());
                    joAdresa.put("temperatura", mp.getTemperatureValue());
                    joAdresa.put("vlaga", mp.getPressureValue());
                    joAdresa.put("pritisak", mp.getHumidityValue());
                    joAdresa.put("vjetar", mp.getWindSpeedValue());
                    jaAdrese.put(0, joAdresa);
                    rezultat.put("meteoPodaci", jaAdrese);
                } catch (JSONException ex) {
                    Logger.getLogger(MeteoRESTResource.class.getName()).log(Level.SEVERE, null, ex);
                }

                return rezultat.toString();
            }
        }

        return "ne postoji adresa sa id: " + id;
    }

    /**
     * PUT method for updating or creating an instance of MeteoRESTResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }

    /**
     * DELETE method for resource MeteoRESTResource
     */
    @DELETE
    public void delete() {
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
