/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.web.slusaci;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.thop.konfiguracije.Konfiguracija;
import org.foi.nwtis.thop.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.thop.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.thop.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.thop.rest.klijenti.OWMKlijent;
import org.foi.nwtis.thop.web.podaci.Adresa;
import org.foi.nwtis.thop.web.podaci.Lokacija;
import org.foi.nwtis.thop.web.podaci.MeteoPodaci;

/**
 *
 * @author NWTiS_3
 */
public class MeteoPodaciPreuzimanje extends Thread {

    private Konfiguracija konf;

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        //TODO testiranja funkcija OBRISI
        //int brojac = 0;
        //List<MeteoPodaci> meteoPodaci = new ArrayList<>();
        //meteoPodaci = dohvatiMeteoPodatke("Varaždin, Pavlinska 2", true);

        /**
         * Svakim prolazom kroz while spremam meteopodatke za svaku adresu u
         * bazi u tablicu thop_meteo
         */
        while (true) {
            String konfig = SlusacAplikacije.getPutanjaDoKonfig();
            List<Adresa> adrese = new ArrayList<>();

            adrese = dohvatiAdrese(konfig);

            try {
                konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(konfig);
            } catch (NemaKonfiguracije ex) {
                Logger.getLogger(MeteoPodaciPreuzimanje.class.getName()).log(Level.SEVERE, null, ex);
            }

            String apiKey = konf.dajPostavku("apiKey");
            OWMKlijent owmk = new OWMKlijent(apiKey);

            for (Adresa a : adrese) {
                MeteoPodaci mp = owmk.getRealTimeWeather(
                        a.getGeoloc().getLatitude(),
                        a.getGeoloc().getLongitude());

                String adrID = Long.toString(a.getIdadresa());
                String vrijeme = mp.getWeatherValue();
                String hpa = mp.getHumidityValue().toString();
                String temp = mp.getTemperatureValue().toString();
                String vlaga = mp.getPressureValue().toString();
                String vjetar = mp.getWindSpeedValue().toString();
                spremanjeMeteoPodataka(adrID, vrijeme, hpa, temp, vlaga, vjetar);
            }

            try {
                String sleepkonf = konf.dajPostavku("sleep");
                int trajanje = Integer.parseInt(sleepkonf);
                System.out.println("SLEEP: " + trajanje);
                sleep(trajanje * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MeteoPodaciPreuzimanje.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
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

    /**
     * Metoda koja sve podatke koje smo jos prosljedili ubacuje u tablicu
     * thop_meteo
     *
     * @param id
     * @param vrijeme
     * @param hpa
     * @param temperatura
     * @param vlaga
     * @param vjetar
     */
    public void spremanjeMeteoPodataka(String id, String vrijeme, String hpa, String temperatura, String vlaga, String vjetar) {
        //System.out.println("ID: " + id + "\nVrijeme: " + vrijeme + "\nTlak zraka: " + hpa + "\nTemperatura: " + temperatura + "\nVlaga: " + vlaga);
        String datoteka = SlusacAplikacije.getPutanjaDoKonfig();
        BP_Konfiguracija bpk = new BP_Konfiguracija(datoteka);
        String server = bpk.getServer_database();
        String baza = server + bpk.getUser_database();
        String korisnik = bpk.getUser_username();
        String lozinka = bpk.getUser_password();
        String sql = "INSERT INTO thop_meteo VALUES(default, '" + vlaga + "','" + vjetar + "','" + vrijeme + "','" + hpa + "','" + temperatura + "'," + Integer.parseInt(id) + ")";
        System.out.println("SQL: " + sql);
        String driver = bpk.getDriver_database(server);
        try {
            Class.forName(driver);
            Connection veza = DriverManager.getConnection(baza, korisnik, lozinka);
            Statement naredba = veza.createStatement();
            naredba.executeUpdate(sql);
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(MeteoPodaciPreuzimanje.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
