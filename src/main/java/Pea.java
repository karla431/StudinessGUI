import javafx.application.Application;
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
    private static ListView<String> tunnid = new ListView<>();
    private static ListView<String> asjad = new ListView<>();
    private int päevaNr;

    @Override
    public void start(Stage peaLava) {
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

        Label tunnidSilt = new Label(tunnidPäis);
        Label vajaminevadAsjadSilt = new Label("Vajaminevad asjad");
        grid.add(tunnidSilt,0,0);
        grid.add(vajaminevadAsjadSilt,1,0);
        grid.add(tunnid,0,1);
        grid.add(asjad,1,1);
        Button lisa = new Button("Lisa tund");
        Button eemalda = new Button("Eemalda tund");

        lisa.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                lisaTund();
            }
        });
        eemalda.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //TODO
            }
        });

        jalus.setSpacing(20);
        jalus.setPadding(new Insets(10, 10, 10, 10));
        jalus.getChildren().add(lisa);
        jalus.getChildren().add(eemalda);
        piiriPaan.setBottom(jalus);
        piiriPaan.setCenter(grid);
        Scene stseen1 = new Scene(piiriPaan, 400, 400, Color.SNOW);

        peaLava.setTitle("Studiness");
        peaLava.setScene(stseen1);
        peaLava.show();
    }

    public void lisaTund() {
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
        Label asiLisatud = new Label();
        grid.add(asiLisatud,1,2);
        asi.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode().equals(KeyCode.ENTER)) {
                    if (asi.getText().contains(","))
                        JOptionPane.showMessageDialog(new Frame(),
                                "Asja nimetus ei tohi sisaldada koma!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                    else if (asi.getText().equals(""))
                        JOptionPane.showMessageDialog(new Frame(),
                                "Asja nimetus ei tohi olla tühi!",
                                "Hoiatus!",
                                JOptionPane.WARNING_MESSAGE);
                    else{
                        asjadeNimetused.add(asi.getText());
                        asiLisatud.setText(asi.getText() + " lisatud!");
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
            public void handle(ActionEvent actionEvent) {
                if(tunniNimi.getText().equals("")){
                    JOptionPane.showMessageDialog(new Frame(),
                            "Tunni nimi ei tohi olla tühi!",
                            "Hoiatus!",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                } else if(tunniNimi.getText().contains(",")){
                    JOptionPane.showMessageDialog(new Frame(),
                            "Tunni nimi ei tohi sisaldada koma!",
                            "Hoiatus!",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                } else{
                    HashSet<String> päevad = new HashSet<>();
                    int valituteArv = 0;
                    for (CheckBox nupp:kastikesed) {
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
                    boolean õnnestus = Tund.lisaTund(tunniNimi.getText(),päevad,asjadeNimetused);
                    if(õnnestus){
                        JOptionPane.showMessageDialog(new Frame(),
                                "Tund on lisatud!",
                                "",
                                JOptionPane.INFORMATION_MESSAGE);
                        tunnid = new ListView<>();
                        asjad = new ListView<>();
                        ehitaListView();
                        try {
                            kirjutaFaili();
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(new Frame(),
                                    "Faili kirjutamine ebaõnnestus!\nProgramm sulgub!",
                                    "ERROR",
                                    JOptionPane.ERROR_MESSAGE);
                            System.exit(-1);
                        }
                        lisamiseAken.close();
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
        lisamiseAken.setTitle("Lisamine");
        lisamiseAken.setScene(stseen);
        lisamiseAken.show();
    }

    public void ehitaListView() {
        HashSet<String> vajalikudAsjad = new HashSet();
        for (Tund tund:Tund.tunniplaan.get(päevaNr)) {
            tunnid.getItems().add(tund.getNimi());
            for (String asi:tund.getVajalikudAsjad()) {
                    vajalikudAsjad.add(asi);
            }
        }
        for (String asi:vajalikudAsjad) {
            asjad.getItems().add(asi);
        }
        //TODO ListView ei update ennast millegipärast...
    }


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
        } catch (FileNotFoundException e) {
            return;
        }
    }

    static void kirjutaFaili() throws IOException {
        File fail = new File("db.txt");
        fail.createNewFile();
        FileWriter kirjutaja = new FileWriter("db.txt");
        for (Tund tund: Tund.olemasolevadTunnid) {
            kirjutaja.write(tund.getNimi() + "; ");
            int lugeja = 0;
            for (String päev: tund.getPäevad()) {
                kirjutaja.write(päev);
                if (lugeja != tund.getPäevad().size()-1){
                    kirjutaja.write(", ");
                }
                lugeja++;
            }
            lugeja = 0;
            kirjutaja.write("; ");
            for (String asi: tund.getVajalikudAsjad()) {
                kirjutaja.write(asi);
                if (lugeja != tund.getVajalikudAsjad().size()-1){
                    kirjutaja.write(", ");
                }
                lugeja++;
            }
            kirjutaja.write("\n");
        }
        kirjutaja.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

}