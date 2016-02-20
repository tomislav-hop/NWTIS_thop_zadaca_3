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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.foi.nwtis.thop.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.thop.web.podaci.Adresa;
import org.foi.nwtis.thop.web.podaci.Lokacija;
import org.foi.nwtis.thop.web.slusaci.MeteoPodaciPreuzimanje;
import org.foi.nwtis.thop.web.slusaci.SlusacAplikacije;

/**
 * REST Web Service
 *
 * @author NWTiS_3
 */
@Path("/meteoREST")
public class MeteoRESTResourceContainer {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of MeteoRESTResourceContainer
     */
    public MeteoRESTResourceContainer() {
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.matnovak.rest.serveri.MeteoRESTResourceContainer
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
        int i = 0;
        for (Adresa a : adrese) {
            try {
                JSONObject joAdresa = new JSONObject();
                joAdresa.put("id", Long.toString(a.getIdadresa()));
                joAdresa.put("adresa", a.getAdresa());
                joAdresa.put("lat", a.getGeoloc().getLatitude());
                joAdresa.put("lon", a.getGeoloc().getLongitude());
                jaAdrese.put(i, joAdresa);
            } catch (JSONException ex) {
                Logger.getLogger(MeteoRESTResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
        }

        try {
            rezultat.put("adrese", jaAdrese);
        } catch (JSONException ex) {
            Logger.getLogger(MeteoRESTResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rezultat.toString();
    }

    /**
     * POST method for creating an instance of MeteoRESTResource
     *
     * @param content representation for the new resource
     * @return an HTTP response with content of the created resource
     */
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response postJson(String content) {
        //TODO
        return Response.created(context.getAbsolutePath()).build();
    }

    /**
     * Sub-resource locator method for {id}
     */
    @Path("{id}")
    public MeteoRESTResource getMeteoRESTResource(@PathParam("id") String id) {
        return MeteoRESTResource.getInstance(id);
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
