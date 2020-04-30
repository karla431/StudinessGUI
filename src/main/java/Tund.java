import java.util.*;

public class Tund {
    private String nimi;
    private Set<String> päevad;
    private Set<String> vajalikudAsjad;
    static ArrayList<ArrayList<Tund>> tunniplaan = new ArrayList<ArrayList<Tund>>(); //Maatriks, kus on i-ndal real kõik i-nda päeva tunnid.
    static Set<Tund> olemasolevadTunnid = new HashSet<>();

    public Tund(String nimi, Set<String> päevad, Set<String> vajalikudAsjad) {
        this.nimi = nimi;
        this.päevad = päevad;
        this.vajalikudAsjad = vajalikudAsjad;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public Set<String> getPäevad() {
        return päevad;
    }

    public void setPäevad(Set<String> päevad) {
        this.päevad = päevad;
    }

    public Set<String> getVajalikudAsjad() {
        return vajalikudAsjad;
    }

    public void setVajalikudAsjad(Set<String> vajalikudAsjad) {
        this.vajalikudAsjad = vajalikudAsjad;
    }

    @Override
    public String toString() {
        return nimi;
    }

    public void eemaldaTund(){
        olemasolevadTunnid.remove(this);
        for (ArrayList<Tund> päev:tunniplaan) {
            if(päev.contains(this)){
                päev.remove(this);
            }
        }
    }

    public static boolean lisaTund(String nimi, Set<String> päevad, Set<String> asjad){
        Iterator<Tund> lugeja = olemasolevadTunnid.iterator();
        while(lugeja.hasNext()){
            if(lugeja.next().getNimi().equals(nimi)){ //Kontrollitakse, kas tundide seas eksisteerib juba antud nimega tundi või mitte.
                return false;
            }
        }
        Tund uus = new Tund(nimi,päevad,asjad);
        olemasolevadTunnid.add(uus); //Kui ei eksisteeri, siis lisatakse tund olemasolevate tundide sekka.
        HashMap<String,Integer> päevadeIndeksid = new HashMap<>(); //Et tundide tunniplaani panemiseks omistada igale päevale vastav indeks tunniplaanis.
        //Tunniplaan on maatriks, seega peab olema iga tund i-ndas tulbas vastavalt päevale.
        int lugeja2 = 0;
        for (String päev: new String[]{"E","T","K","N","R","L","P"}) {
            päevadeIndeksid.put(päev, lugeja2); //Igale päevale omistatakse üks indeks.
            lugeja2++;
        }
        for (String päev: uus.getPäevad()) { //Käiakse läbi kõik päevad, mil tund toimub.
            tunniplaan.get(päevadeIndeksid.get(päev)).add(uus); //Võetakse vastavalt tunniplaani maatriksi vastav rida ning lisatakse sinna antud tund.
        }
        return true;
    }
}