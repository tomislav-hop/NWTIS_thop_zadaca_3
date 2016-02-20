<%-- 
    Document   : index
    Created on : May 12, 2015, 3:33:22 PM
    Author     : Tomislav Hop
--%>

<%@page import="java.io.File"%>
<%@page import="org.foi.nwtis.thop.web.slusaci.SlusacAplikacije"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.Connection"%>
<%@page import="org.foi.nwtis.thop.konfiguracije.bp.BP_Konfiguracija"%>
<%@page import="org.foi.nwtis.thop.web.podaci.Adresa"%>
<%@page import="org.foi.nwtis.thop.rest.klijenti.GMKlijent"%>
<%@page import="org.foi.nwtis.thop.web.podaci.Lokacija"%>
<%@page import="org.foi.nwtis.thop.web.podaci.MeteoPodaci"%>
<%@page import="org.foi.nwtis.thop.ws.serveri.GeoMeteoWS"%>
<%@page import="org.foi.nwtis.thop.rest.klijenti.OWMKlijent"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Unos adresa</title>
    </head>
    <body>
        <h1>Unos adresa</h1>
        <%
            /**
             * Dohvaćanje adrese i inicijalizacija varijabli koje koristim kod
             * ispisa
             */
            String adresa = request.getParameter("adresa");
            GeoMeteoWS gmws = new GeoMeteoWS();
            MeteoPodaci mp = new MeteoPodaci();
            Lokacija loc = new Lokacija();
            GMKlijent gmk = new GMKlijent();
            Adresa adr = new Adresa();

            /**
             * Ako je adresa prazna ispisujem upozorenje
             */
            if (adresa == null || adresa.length() == 0) {
        %>
        <b><font color="red">Nije upisan podatak!</font></b>
            <%
                } else {
                    mp = gmws.dajVazeceMeteoPodatkeZaAdresu(adresa);
                    loc = gmk.getGeoLocation(adresa);
                    adr.setAdresa(adresa);

                    request.setAttribute("mp", mp);
                    request.setAttribute("adr", adr);
                    request.setAttribute("loc", loc);
                }
            %>
        <form method="GET">
            Unesite adresu: 
            <input value="" name="adresa"/><br/><br/>
            <input name="geoPodaci" type="submit" value="Dohvat geo podataka"/><br/><br/>
            <input name="spremiGeoPodatke" type="submit" value="Spremanje podataka o adresi "/><br/><br/>
            <input name="meteoPodaci" type="submit" value="Dohvat važećih meteopodataka"/><br/><br/>
            <%
                /**
                 * Ako je postavljen parametar meteoPodaci ispisujem podatke
                 * koje sam dobio od servisa
                 */
                if (request.getParameter("meteoPodaci") != null && adresa.length() != 0) {
            %>
            <b>METEOPODATCI: </b><br/>
            Adresa: ${adr.getAdresa()}<br/>
            Vrijeme: ${mp.getWeatherValue()}<br/>
            Temperatura: ${mp.getTemperatureValue()} ${mp.getTemperatureUnit()}<br/>
            Vlaga: ${mp.getPressureValue()} ${mp.getPressureUnit()}<br/>          
            Tlak: ${mp.getHumidityValue()} ${mp.getHumidityUnit()}<br/>
            Brzina vjetra: ${mp.getWindSpeedValue()}<br/>

            <%/**
                 * Ako je postavljen parametar geoPodaci ispisujem podatke koje
                 * sam dobio od servisa
                 */
            } else if (request.getParameter("geoPodaci") != null && adresa.length() != 0) {
            %>
            <b>GEOPODACI: </b><br/>
            Adresa: ${adr.getAdresa()}<br/>
            Lat: ${loc.getLatitude()}<br/>
            Lng: ${loc.getLongitude()}<br/>
            <%
                /**
                 * Ako je postavljen pamatetar spremigeoPodatke spremam podatke
                 * koje sam dobio od servise u bazu podataka
                 */
            } else if (request.getParameter("spremiGeoPodatke") != null && adresa.length() != 0) {

                String datoteka = SlusacAplikacije.getPutanjaDoKonfig();
                BP_Konfiguracija bpk = new BP_Konfiguracija(datoteka);
                String server = bpk.getServer_database();
                String baza = server + bpk.getUser_database();
                String korisnik = bpk.getUser_username();
                String lozinka = bpk.getUser_password();
                String sql = "INSERT INTO adrese (idAdresa,adresa,latitude,longitude)VALUES (default, '" + adresa + "','" + loc.getLatitude() + "','" + loc.getLongitude() + "')";
                System.out.println("SQL: " + sql);
                String driver = bpk.getDriver_database(server);
                try {
                    Class.forName(driver);
                    Connection veza = DriverManager.getConnection(baza, korisnik, lozinka);
                    Statement naredba = veza.createStatement();
                    naredba.executeUpdate(sql);
                } catch (Exception e) {
                }

            %>
            <b>SPREMANJE GEOPODATAKA!</b>
            <%                }

            %>

        </form>
    </body>
</html>

<%
%>