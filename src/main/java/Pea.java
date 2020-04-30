import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Pea extends Application {
    private static ListView<String> tunnid = new ListView<>(); //Listview koos ArrayListiga tänaste/homsete tundide salvestamiseks.
    private static ObservableList<String> tunnidList = FXCollections.observableArrayList();
    private static ListView<String> asjad = new ListView<>(); //Listview koos ArrayListiga tänaste/homsete asjade salvestamiseks.
    private static ObservableList<String> asjadList = FXCollections.observableArrayList();
    private int päevaNr; //Arv säilitamaks seda, et mis päev täna on. ISO-8601 standardi alusel.

    @Override
    public void start(Stage peaLava) {
        tunnid.setItems(tunnidList); //Ühendatakse ObservableListid ListViewdega
        asjad.setItems(asjadList);
        for (int i = 0; i < 7; i++) { //Luuakse 7 ArrayListi tunniplaani sisse, mis tähistavad erinevaid päevasid.
            Tund.tunniplaan.add(new ArrayList<>());
        }
        loeFailist();
        BorderPane piiriPaan = new BorderPane();
        GridPane grid = new GridPane();
        HBox jalus = new HBox();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        LocalTime õhtu = LocalTime.parse("20:00:00");
        päevaNr = DayOfWeek.from(LocalDate.now()).getValue();
        String tunnidPäis;
        if(LocalTime.now().isAfter(õhtu)) { //Kui kell on hiljem kui 20:00, näidatakse sulle juba homset tunniplaani tänase asemel.
            tunnidPäis = "Homsed tunnid on";
        }
        else{
            tunnidPäis = "Tänased tunnid on";
            päevaNr--;
        }
        ehitaListView();
        Label tunnidSilt = new Label(tunnidPäis); //Kiri tundide ListView kohal.
        Label vajaminevadAsjadSilt = new Label("Vajaminevad asjad"); //Kiri asjade ListView kohal.
        grid.add(tunnidSilt,0,0);
        grid.add(vajaminevadAsjadSilt,1,0);
        grid.add(tunnid,0,1);
        grid.add(asjad,1,1);
        Button lisa = new Button("Lisa");
        Button eemalda = new Button("Eemalda");

        lisa.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                lisaTund(peaLava.getX(),peaLava.getY());
            }
        });
        eemalda.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                kustuta(peaLava.getX(),peaLava.getY());
            }
        });
        Button muudaNupp = new Button("Muuda");
        muudaNupp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                muuda(peaLava.getX(),peaLava.getY());
            }
        });

        jalus.setSpacing(20); //Jalus on kustuta, muuda ja lisa nuppude kast.
        jalus.setPadding(new Insets(10, 10, 10, 10));
        jalus.getChildren().add(lisa);
        jalus.getChildren().add(muudaNupp);
        jalus.getChildren().add(eemalda);
        piiriPaan.setBottom(jalus);
        piiriPaan.setCenter(grid);
        Scene stseen1 = new Scene(piiriPaan, 400, 400, Color.SNOW);

        peaLava.setTitle("Studiness");
        peaLava.setScene(stseen1);
        peaLava.show();
    }

    /**
     * Meetod käivitab akna, kus saab olemasoelvaid tunde muuta.
     * @param x Eelmise akna x koordinaat.
     * @param y Eelmise akna y koordinaat.
     */
    private void muuda(double x, double y) {
        BorderPane border = new BorderPane();
        border.setPadding(new Insets(10,10,10,10));
        Label silt = new Label("Muutmiseks topeltkliki mõne tunni peal.");
        border.setTop(silt);
        Stage muutmisAken = new Stage();
        ListView tunnidMuutmiseks = new ListView(); //List kõikidest olemasolevatest tundidest.
        ObservableList<Tund> tunnidMuutmiseksList = FXCollections.observableArrayList();
        tunnidMuutmiseks.setItems(tunnidMuutmiseksList);
        for (Tund tund:Tund.olemasolevadTunnid) { //Kõik tunnid lisatakse listview-sse.
            tunnidMuutmiseksList.add(tund); //Kuna tundide toString on lihtsalt tunni nimi, siis piisab siin objekti tund lisamiseks.
        }
        border.setCenter(tunnidMuutmiseks);
        Scene stseen = new Scene(border,400,400,Color.SNOW);
        muutmisAken.setTitle("Muuda");
        muutmisAken.setScene(stseen);
        muutmisAken.setY(y+100);
        muutmisAken.setX(x+100);

        tunnidMuutmiseks.setOnMouseClicked(new EventHandler<MouseEvent>() { //Kuulatakse klikke listview peal.
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2){ //Kui tehakse topeltklõps, siis:
                    Tund selekteeritud = (Tund) tunnidMuutmiseks.getSelectionModel().getSelectedItem(); //Võetakse selekteeritud tund,
                    muudaTundi(selekteeritud,muutmisAken.getX(),muutmisAken.getY()); //käivatatakse temaga muudaTundi meetod,
                    tunnidMuutmiseksList.clear(); //ning lõpetuseks korraldatakse listview uuesti ümber, et seal oleks tund muudetud andmetega.
                    for (Tund tund:Tund.olemasolevadTunnid) {
                        tunnidMuutmiseksList.add(tund);
                    }
                    ehitaListView();
                    kirjutaFaili(); //kõik salvestatakse faili.
                    muutmisAken.close();
                }
            }
        });

        muutmisAken.show();
    }

    /**
     * Meetod muudab ühte konkreetset tundi.
     * @param selekteeritud Antud tund, mida muudetakse.
     * @param x Eelneva akna x koordinaat.
     * @param y Eelneva akna y koordinaat.
     */
    private void muudaTundi(Tund selekteeritud, double x, double y) {
        HashSet<String> asjadeNimetused = new HashSet<>();
        Stage muutmiseAken = new Stage();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        VBox nimetuseKast = new VBox(); //Igale küsimusele on loodud oma kast.
        nimetuseKast.setPadding(new Insets(10,10,10,10));
        VBox päevadeKast = new VBox();
        päevadeKast.setPadding(new Insets(10,10,10,10));
        VBox asjadeKast = new VBox();
        asjadeKast.setPadding(new Insets(10,10,10,10));
        Label nimi = new Label("Tunni nimi");
        nimetuseKast.getChildren().add(nimi);
        Label päevad = new Label("Mis päevadel tund toimub?");
        päevadeKast.getChildren().add(päevad);
        Label asjadLabel = new Label("Lisa vajalikke asju \n(asjade vahel vajuta enter)");
        asjadeKast.getChildren().add(asjadLabel);
        TextField tunniNimi = new TextField(); //Tunni nime sisestamiseks lahter..
        tunniNimi.setText(selekteeritud.getNimi()); //Tunni lahtrisse kirjutatakse hetkene tunni nimi.
        nimetuseKast.getChildren().add(tunniNimi);
        TextField asi = new TextField(); //Asja sisestamiseks lahter.
        VBox asjadeListiKast = new VBox();
        asjadeListiKast.setMaxSize(100,150);
        ListView asjadListView = new ListView(); //ListView olemasolevate asjade jaoks.
        ObservableList<String> asjadList = FXCollections.observableArrayList();
        asjadeListiKast.getChildren().add(asjadListView);
        asjadListView.setItems(asjadList);
        asjadeNimetused.addAll(selekteeritud.getVajalikudAsjad()); //Kõik juba olemasolevad asjad tõstetakse ka listiview-sse.
        asjadList.addAll(asjadeNimetused);
        grid.add(asjadeListiKast,1,1);
        Button kustutaAsiNupp = new Button("Kustuta asi"); //Nupp asja kustutamiseks, listview-st.
        asjadeListiKast.getChildren().add(kustutaAsiNupp);
        kustutaAsiNupp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Object selekteeritudAsi = asjadListView.getSelectionModel().getSelectedItem();
                if(selekteeritudAsi == null){ //Kui listview-st midagi selekteeritud pole, siis kustutada ei saa.
                    JOptionPane.showMessageDialog(new Frame(),
                            "Sul pole ükski asi selekteeritud!",
                            "Hoiatus!",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                asjadeNimetused.remove(selekteeritudAsi); //Kustutamisel eemaldatakse asi ja listView resetitakse.
                asjadList.clear();
                asjadList.addAll(asjadeNimetused);
            }
        });
        asi.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode().equals(KeyCode.ENTER)) { //Kui vajutatakse mõnda enterit, siis:
                    if (asi.getText().contains(",")) //kontrollitakse, et ega see asi ei sisalda koma,
                        JOptionPane.showMessageDialog(new Frame(),
                                "Asja nimetus ei tohi sisaldada koma!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                    else if (asi.getText().equals("")) //Kontrollitakse, et ega see asi pole lihtsalt tühi lahter.
                        JOptionPane.showMessageDialog(new Frame(),
                                "Asja nimetus ei tohi olla tühi!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                    else if (asi.getText().equals(";")) //Kontrollitakse, et ega see ei sisalda semikoolonit.
                        JOptionPane.showMessageDialog(new Frame(),
                                "Asja nimetus ei tohi sisaldada semikoolonit!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                    else{ //Kui eelnevad tingimused on kõik korras, siis lisatakse antud asi ListView-sse.
                        asjadeNimetused.add(asi.getText());
                        asjadList.clear();
                        asjadList.addAll(asjadeNimetused);
                    }
                }
            }
        });
        asjadeKast.getChildren().add(asi);
        Button salvesta = new Button("Salvesta");
        VBox nuppudeKast = new VBox();
        nuppudeKast.setSpacing(5);
        nuppudeKast.setPadding(new Insets(5, 0, 5, 0));
        String[] päevadeNimetused = {"Esmaspäev","Teispäev","Kolmapäev","Neljapäev","Reede","Laupäev","Pühapäev"};
        ArrayList<CheckBox> kastikesed = new ArrayList<>();
        for (String päev:päevadeNimetused) { //Tehakse 7 checkboxi, mis kõik on erinevate päevadega.
            CheckBox uus = new CheckBox(päev);
            if(selekteeritud.getPäevad().contains(päev.substring(0,1))) //Kui antud päev peaks tund toimuma, siis tehakse linnuke.
                uus.fire();
            kastikesed.add(uus);
            nuppudeKast.getChildren().add(uus);
        }
        päevadeKast.getChildren().add(nuppudeKast);
        grid.add(salvesta,0,3);
        grid.add(nimetuseKast,0,0);
        grid.add(asjadeKast,0,2);
        grid.add(päevadeKast,0,1);

        salvesta.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) { //Toimub siis, kui salvesta nuppu vajutatakse.
                if(tunniNimi.getText().equals("")){ //Kontrollitakse, ega tunni nimi pole tühi.
                    JOptionPane.showMessageDialog(new Frame(),
                            "Tunni nimi ei tohi olla tühi!",
                            "Hoiatus!",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                } else if(tunniNimi.getText().contains(",")){ //Kontrollitakse, et ega tunni nimi koma ei sisalda.
                    JOptionPane.showMessageDialog(new Frame(),
                            "Tunni nimi ei tohi sisaldada koma!",
                            "Hoiatus!",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                else if(tunniNimi.getText().contains(";")){ //Kontrollitakse, et ega tunni nimi semikoolonit ei sisalda.
                    JOptionPane.showMessageDialog(new Frame(),
                            "Tunni nimi ei tohi sisaldada semikoolonit!",
                            "Hoiatus!",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                } else{
                    HashSet<String> päevad = new HashSet<>();
                    int valituteArv = 0;
                    for (CheckBox nupp:kastikesed) { //Kontrollitakse, et tund toimuks vähemalt ühel päeval.
                        if(nupp.isSelected()){
                            päevad.add(nupp.getText().substring(0,1));
                            valituteArv++;
                        }
                    }
                    if(valituteArv == 0){
                        JOptionPane.showMessageDialog(new Frame(),
                                "Tund peab toimuma vähemalt ühel päeval!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    boolean õnnestus = true; //Kontrollitakse, et sama nimega tundi juba ei oleks.
                    selekteeritud.setNimi(""); //Selleks pannakse tunni nimeks ajutiselt "", siis on kindel, et kontroll tunni iseenda nime peale errorit ei anna.
                    for (Tund tund:Tund.olemasolevadTunnid) {
                        if(tund.getNimi().equals(tunniNimi.getText())){
                            õnnestus = false;
                        }
                    }
                    if(õnnestus){ //Kui kattuvat nime ei ole, siis kirjutatakse kõik soovitud andmed üle.
                        selekteeritud.setNimi(tunniNimi.getText());
                        selekteeritud.setPäevad(päevad);
                        selekteeritud.setVajalikudAsjad(asjadeNimetused);
                        JOptionPane.showMessageDialog(new Frame(),
                                "Tund on muudetud!",
                                "",
                                JOptionPane.INFORMATION_MESSAGE);
                        ehitaListView(); //Peaakna lahtrid uuendatakse.
                        kirjutaFaili(); //Kõik salvestatakse faili.
                        muutmiseAken.close();
                    } else{
                        JOptionPane.showMessageDialog(new Frame(),
                                "Antud nimega tund juba eksisteerib!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }
        });

        Scene stseen = new Scene(grid,400,400,Color.SNOW);
        muutmiseAken.setTitle("Muutmine");
        muutmiseAken.setX(x+100);
        muutmiseAken.setY(y+100);
        muutmiseAken.setScene(stseen);
        muutmiseAken.show();
    }


    /**
     * Antud meetodiga saab olemasolevaid tunde kustutada.
     * @param x Eelneva akna x koordinaat.
     * @param y Eelneva akna y koordinaat.
     */
    private void kustuta(double x, double y) {
        BorderPane border = new BorderPane();
        border.setPadding(new Insets(10,10,10,10));
        Label silt = new Label("Kustutamiseks topeltkliki mõne tunni peal.");
        Button kustutaKõik = new Button("Kustuta kõik tunnid");
        kustutaKõik.setStyle("-fx-background-color: #FF0000");
        border.setBottom(kustutaKõik);
        border.setTop(silt);
        Stage kustutamisAken = new Stage();
        ListView tunnidKustutamiseks = new ListView(); //Tehakse list kõikidest tundidest tundide kustutamiseks.
        ObservableList<Tund> tunnidKustutamiseksList = FXCollections.observableArrayList();
        tunnidKustutamiseks.setItems(tunnidKustutamiseksList);
        for (Tund tund:Tund.olemasolevadTunnid) {
            tunnidKustutamiseksList.add(tund);
        }
        border.setCenter(tunnidKustutamiseks);
        Scene stseen = new Scene(border,400,400,Color.SNOW);
        kustutamisAken.setTitle("Kustuta");
        kustutamisAken.setScene(stseen);

        tunnidKustutamiseks.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2){ //Kui tehakse topeltklõps mõne eseme peal, siis:
                    Tund selekteeritud = (Tund) tunnidKustutamiseks.getSelectionModel().getSelectedItem(); //võetakse selekteeritud tund,
                    int n = JOptionPane.showConfirmDialog(new Frame(), //küsitakse inimeselt üle, kas ta ikka soovi seda tundi kustutada,
                            "Oled kindel, et soovid kustutada tundi " + selekteeritud + "?",
                            "Hoiatus!",
                            JOptionPane.YES_NO_OPTION);
                    if(n == 0){ //kui nõusolek on olemas, siis antud tund eemaldatakse ja listview uuendatakse.
                        selekteeritud.eemaldaTund();
                        tunnidKustutamiseksList.clear();
                        for (Tund tund:Tund.olemasolevadTunnid) {
                            tunnidKustutamiseksList.add(tund);
                        }
                        ehitaListView(); //Uuendatakse peaakent.
                        kirjutaFaili(); //Uuendused kirjutatakse faili.
                    }
                }
            }
        });

        kustutaKõik.setOnAction(new EventHandler<ActionEvent>() { //Kui vajutatakse nuppu, et kustuta kõik, siis:
            @Override
            public void handle(ActionEvent actionEvent) {
                Tund selekteeritud = (Tund) tunnidKustutamiseks.getSelectionModel().getSelectedItem();
                int n = JOptionPane.showConfirmDialog(new Frame(), //küsitakse kõik kasutajalt kõigepealt üle.
                        "Oled kindel, et soovid kustutada KÕIK TUNNID?\n" +
                                "Antud tegevus ei ole tagasikeritav!!" ,
                        "Hoiatus!",
                        JOptionPane.YES_NO_OPTION);
                if(n == 0){ //kui on nõusolek olemas, siis
                    for (Tund tund:tunnidKustutamiseksList) { //kõik tunnid käiakse ükshaaval läbi ning eemaldatakse.
                        tund.eemaldaTund();
                    }
                    ehitaListView(); //Peaakent uuendatakse.
                    tunnidKustutamiseksList.clear(); //Kustutamise akent uuendatakse.
                    for (Tund tund:Tund.olemasolevadTunnid) {
                        tunnidKustutamiseksList.add(tund);
                    }
                    kirjutaFaili(); //Uuendused kantakse faili.
                }
            }
        });
        kustutamisAken.setY(y+100);
        kustutamisAken.setX(x+100);
        kustutamisAken.show();
    }

    /**
     * Antud meetodiga muudetakse ühte konkreetset tundi.
     * @param x Eelneva akna x koordinaat.
     * @param y Eelneva akna y koordinaat.
     */
    public void lisaTund(double x, double y) {
        HashSet<String> asjadeNimetused = new HashSet<>();
        Stage lisamiseAken = new Stage();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10,10,10,10));
        VBox nimetuseKast = new VBox();
        nimetuseKast.setPadding(new Insets(10,10,10,10));
        VBox päevadeKast = new VBox();
        päevadeKast.setPadding(new Insets(10,10,10,10));
        VBox asjadeKast = new VBox();
        asjadeKast.setPadding(new Insets(10,10,10,10));
        Label nimi = new Label("Sisesta tunni nimi");
        nimetuseKast.getChildren().add(nimi);
        Label päevad = new Label("Mis päevadel tund toimub?");
        päevadeKast.getChildren().add(päevad);
        Label asjadLabel = new Label("Sisesta vajalikud asjad \n(asjade vahel vajuta enter)");
        asjadeKast.getChildren().add(asjadLabel);
        TextField tunniNimi = new TextField();
        nimetuseKast.getChildren().add(tunniNimi);
        TextField asi = new TextField();
        ListView asiLisatud = new ListView();
        ObservableList lisatudAsjad = FXCollections.observableArrayList();
        asiLisatud.setItems(lisatudAsjad);
        grid.add(asiLisatud,1,1);
        Label tühi = new Label();
        grid.add(tühi,1,2);
        asi.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode().equals(KeyCode.ENTER)) { //Asja sisestades enter vajutades,
                    if (asi.getText().contains(","))
                        JOptionPane.showMessageDialog(new Frame(),//Kontrollitakse, et asi ei sisaldaks koma,
                                "Asja nimetus ei tohi sisaldada koma!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                    else if (asi.getText().equals("")) //oleks tühi,
                        JOptionPane.showMessageDialog(new Frame(),
                                "Asja nimetus ei tohi olla tühi!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                    else if (asi.getText().equals(";")) //ega sisaldaks semikoolonit.
                        JOptionPane.showMessageDialog(new Frame(),
                                "Asja nimetus ei tohi sisaldada semikoolonit!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                    else{
                        asjadeNimetused.add(asi.getText()); //Asjade listviewd uuendatakse.
                        lisatudAsjad.add(asi.getText());
                        asi.setText(""); //Lahter tühjendatakse.
                    }
                }
            }
        });
        asjadeKast.getChildren().add(asi);
        Button salvesta = new Button("Salvesta");
        VBox nuppudeKast = new VBox();
        nuppudeKast.setSpacing(5);
        nuppudeKast.setPadding(new Insets(5, 0, 5, 0));
        String[] päevadeNimetused = {"Esmaspäev","Teispäev","Kolmapäev","Neljapäev","Reede","Laupäev","Pühapäev"};
        ArrayList<CheckBox> kastikesed = new ArrayList<>();
        for (String päev:päevadeNimetused) {
            CheckBox uus = new CheckBox(päev);
            kastikesed.add(uus);
            nuppudeKast.getChildren().add(uus);
        }
        päevadeKast.getChildren().add(nuppudeKast);
        grid.add(salvesta,0,3);
        grid.add(nimetuseKast,0,0);
        grid.add(asjadeKast,0,2);
        grid.add(päevadeKast,0,1);

        salvesta.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) { //Kui vajutatakse nuppu salvesta, siis:
                if(tunniNimi.getText().equals("")){ //kontrollitakse, et ega tunni nimi ei ole tühi,
                    JOptionPane.showMessageDialog(new Frame(),
                            "Tunni nimi ei tohi olla tühi!",
                            "Hoiatus!",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                } else if(tunniNimi.getText().contains(",")){ //et tunni nimi ei sisalda koma,
                    JOptionPane.showMessageDialog(new Frame(),
                            "Tunni nimi ei tohi sisaldada koma!",
                            "Hoiatus!",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }  else if(tunniNimi.getText().contains(";")){ //et tunni nimi ei sisalda semikoolonit.
                    JOptionPane.showMessageDialog(new Frame(),
                            "Tunni nimi ei tohi sisaldada semikoolonit!",
                            "Hoiatus!",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                } else{
                    HashSet<String> päevad = new HashSet<>();
                    int valituteArv = 0;
                    for (CheckBox nupp:kastikesed) { //Loetakse kokku selekteeritud nuppude koguse.
                        if(nupp.isSelected()){
                            päevad.add(nupp.getText().substring(0,1));
                            valituteArv++;
                        }
                    }
                    if(valituteArv == 0){
                        JOptionPane.showMessageDialog(new Frame(), //Kui ühtegi pole selekteeritud, siis antakse vastav teade.
                                "Tund peab toimuma vähemalt ühel päeval!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    boolean õnnestus = Tund.lisaTund(tunniNimi.getText(),päevad,asjadeNimetused); //Tundi proovitakse lisada.
                    if(õnnestus){
                        JOptionPane.showMessageDialog(new Frame(),
                                "Tund on lisatud!",
                                "",
                                JOptionPane.INFORMATION_MESSAGE);
                        ehitaListView();
                        kirjutaFaili();
                        lisamiseAken.close();
                    } else{ //Kui ei õnnestu, siis antakse teade.
                        JOptionPane.showMessageDialog(new Frame(),
                                "Antud nimega tund juba eksisteerib!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }
        });

        Scene stseen = new Scene(grid,600,400,Color.SNOW);
        lisamiseAken.setTitle("Lisamine");
        lisamiseAken.setX(x+100);
        lisamiseAken.setY(y+100);
        lisamiseAken.setScene(stseen);
        lisamiseAken.show();
    }

    /**
     * Meetod peaakna listviewde uuendamiseks.
     */
    public void ehitaListView() {
        asjadList.clear(); //Mõlemad listviewd tehakse tühjaks.
        tunnidList.clear();
        HashSet<String> vajalikudAsjad = new HashSet();
        for (Tund tund:Tund.tunniplaan.get(päevaNr)) { //Listviewd täidetakse taaskord.
            tunnidList.add(tund.getNimi());
            for (String asi:tund.getVajalikudAsjad()) {
                    vajalikudAsjad.add(asi);
            }
        }
        asjadList.addAll(vajalikudAsjad);
    }

    /**
     * Failist andmete lugemine.
     */
    static void loeFailist() {
        try (java.util.Scanner sc = new java.util.Scanner(new File("db.txt"), "UTF-8")){
            while (sc.hasNextLine()) {
                String rida = sc.nextLine();
                String[] tükid = rida.split("; ");
                String[] päevad = tükid[1].split(", ");
                String[] asjad = tükid[2].split(", ");
                Set<String> asjadSet = new HashSet<>(Arrays.asList(asjad));
                Set<String> päevadSet = new HashSet<>(Arrays.asList(päevad));
                Tund.lisaTund(tükid[0],päevadSet,asjadSet);
            }
        } catch (FileNotFoundException e) { //Kui faili pole, siis failist lihtsalt ei loeta midagi.
            return;
        }
    }

    /**
     * Faili kirjutamine.
     */
    static void kirjutaFaili(){
        try {
            File fail = new File("db.txt");
            fail.createNewFile();
            FileWriter kirjutaja = new FileWriter("db.txt");
            for (Tund tund : Tund.olemasolevadTunnid) { //Käiakse läbi kõik olemasolevad tunnid.
                kirjutaja.write(tund.getNimi() + "; ");
                int lugeja = 0;
                for (String päev : tund.getPäevad()) {
                    kirjutaja.write(päev);
                    if (lugeja != tund.getPäevad().size() - 1) {
                        kirjutaja.write(", ");
                    }
                    lugeja++;
                }
                lugeja = 0;
                kirjutaja.write("; ");
                for (String asi : tund.getVajalikudAsjad()) {
                    kirjutaja.write(asi);
                    if (lugeja != tund.getVajalikudAsjad().size() - 1) {
                        kirjutaja.write(", ");
                    }
                    lugeja++;
                }
                kirjutaja.write("\n");
            }
            kirjutaja.close();
        }catch(IOException e){ //Kui tekib error, siis programm sulgetakse.
            JOptionPane.showMessageDialog(new Frame(),
                    "Faili kirjutamine ebaõnnestus!\nProgramm sulgub!",
                    "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}