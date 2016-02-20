/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.thop.web.zrna;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.foi.nwtis.thop.ws.klijenti.MeteoWSKlijent;
import org.foi.nwtis.thop.ws.serveri.Adresa;
import org.foi.nwtis.thop.ws.serveri.MeteoPodaci;

/**
 *
 * @author NWTiS_3
 */
//TODO razmisliti primjeni session ili request
@ManagedBean
@SessionScoped
public class OdabirAdresa implements Serializable {

    public List<Adresa> popisAdresa;
    public String odabranaAdresa;
    public String[] odabraneAdrese;
    public List<MeteoPodaci> meteoPodaci;
    public MeteoPodaci[] poljeMP;
    public String testiranje = "";

    /**
     * Creates a new instance of OdabirAdresa
     */
    public OdabirAdresa() {
    }

    public List<Adresa> getPopisAdresa() {
        popisAdresa = MeteoWSKlijent.dajSveAdrese();
        return popisAdresa;
    }

    public void setPopisAdresa(List<Adresa> popisAdresa) {
        this.popisAdresa = popisAdresa;
    }

    public String getOdabranaAdresa() {
        return odabranaAdresa;
    }

    public void setOdabranaAdresa(String odabranaAdresa) {
        this.odabranaAdresa = odabranaAdresa;
    }

    public String[] getOdabraneAdrese() {
        return odabraneAdrese;
    }

    public void setOdabraneAdrese(String[] odabraneAdrese) {
        this.odabraneAdrese = odabraneAdrese;
    }

    public List<MeteoPodaci> getMeteoPodaci() {
        return meteoPodaci;
    }

    public void setMeteoPodaci(List<MeteoPodaci> meteoPodaci) {
        this.meteoPodaci = meteoPodaci;
    }

    public MeteoPodaci[] getPoljeMP() {
        return poljeMP;
    }

    public void setPoljeMP(MeteoPodaci[] poljeMP) {
        this.poljeMP = poljeMP;
    }

    public String getTestiranje() {
        return testiranje;
    }

    public void setTestiranje(String testiranje) {
        this.testiranje = testiranje;
    }

    /**
     * Metoda uzima prvu adresu iz polja adresa i za nju dohvaća sve
     * meteopodatke iz baze
     */
    public void odaberiAdresu() {
        if (this.odabraneAdrese.length != 1) {
            System.out.println("Odabrano više od jedne adrese!");
            return;
        }
        meteoPodaci = MeteoWSKlijent.dajSveMeteoPodatkeZaAdresu(this.odabraneAdrese[0]);
        int duzina = meteoPodaci.size();
        poljeMP = new MeteoPodaci[duzina];
        for (int i = 0; i < duzina; i++) {
            poljeMP[i] = meteoPodaci.get(i);
            poljeMP[i].setWindDirectionName(this.odabraneAdrese[0]);
        }
    }

    /**
     * Metoda za svaku adresu u polju adresa dohvaća zadnje meteopodatke te iste
     * upisuje u polje meteopodaka koje koristim kod ispisa u odabirAdresa.xhtml
     */
    public void odaberiAdrese() {
        MeteoPodaci mp = new MeteoPodaci();

        int brojOdabranih = odabraneAdrese.length;
        poljeMP = new MeteoPodaci[brojOdabranih];

        int i = 0;
        for (String odabraneAdrese1 : odabraneAdrese) {
            System.out.println("Odabrana adresa je: " + odabraneAdrese1);
            mp = MeteoWSKlijent.dajZadnjeMeteoPodatkeZaAdresu(odabraneAdrese1);
            //Spremanje vrijednosti grada u nekorištenu varijablu koja se nalazi u meteo podaci
            mp.setWindDirectionName(odabraneAdrese1);
            try {
                poljeMP[i] = mp;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            i++;
        }
    }

    /**
     * Metoda uzima meteopodatak koji joj vraća metoda pozivRest i sprema ga u
     * listu meteopodaka koje kasnije prikazujemo
     */
    public void restServis() {
        testiranje = "";
        int brojOdabranih = odabraneAdrese.length;
        poljeMP = new MeteoPodaci[brojOdabranih];
        int i = 0;
        for (Adresa adresa : popisAdresa) {
            for (String odabraneAdrese1 : odabraneAdrese) {
                if (odabraneAdrese1.equals(adresa.getAdresa())) {
                    poljeMP[i] = pozivRest(adresa.getIdadresa());
                    i++;
                }
            }
        }

    }

    /**
     * Metoda na osnovi id-a odlazi na url restservisa te dohvaća json koji
     * sprema u String testiranje i u varijablu mp koju također vraća
     *
     * @param id
     * @return
     */
    public MeteoPodaci pozivRest(long id) {
        MeteoPodaci mp = new MeteoPodaci();

        try {
            URL url = new URL("http://localhost:8080/thop_zadaca_3_1/webresources/meteoREST/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            String json = "";
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                json += output;
            }

            try {
                JSONObject obj = new JSONObject(json);
                JSONArray podaci = obj.getJSONArray("meteoPodaci");
                for (int i = 0; i < podaci.length(); i++) {
                    JSONObject objekt = podaci.optJSONObject(i);
                    testiranje += "Adresa: " + objekt.getString("adresa") + " Vrijeme: " + objekt.getString("vrijeme") + " Temperatura: " + objekt.getString("temperatura")
                            + " Vlaga: " + objekt.getString("vlaga") + " Pritisak: " + objekt.getString("pritisak") + "\n";

                    //System.out.println(testiranje);
                    mp.setWeatherValue(objekt.getString("vrijeme"));
                    mp.setTemperatureValue(Float.parseFloat(objekt.getString("temperatura")));
                    mp.setPressureValue(Float.parseFloat(objekt.getString("vlaga")));
                    mp.setHumidityValue(Float.parseFloat(objekt.getString("pritisak")));
                    mp.setWindSpeedValue(Float.parseFloat(objekt.getString("vjetar")));
                    mp.setWindDirectionName(objekt.getString("adresa"));
                    return mp;
                }
            } catch (JSONException ex) {
                Logger.getLogger(OdabirAdresa.class.getName()).log(Level.SEVERE, null, ex);
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return null;
    }
}
