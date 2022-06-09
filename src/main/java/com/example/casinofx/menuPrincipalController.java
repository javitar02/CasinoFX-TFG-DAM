package com.example.casinofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import static com.example.casinofx.utilidades.Constantes.*;

/**
 * Clase que controla el menú principal
 */
public class menuPrincipalController {
    @FXML
    private TextField saldoUsuario;
    @FXML
    private TextField nomUsuario;

    /**
     * Método que carga la ventana cuyo título y nombre de FXML sean indicados por parámetro
     *
     * @param nombreFXML,tituloVentana;
     */
    private void cargarVentana(String nombreFXML, String tituloVentana) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource(nombreFXML));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle(tituloVentana);
        stage.setResizable(false);
        stage.setScene(scene);
        Image ico = new Image(RUTA_LOGO); // No funciona con ruta relativa
        stage.getIcons().add(ico);
        stage.show();
    }

    /**
     * Método para abrir la pestaña de depósito
     * Se rescata la escena padre y a partir de ella se carga la nueva escena
     *
     * @param evento;
     */
    @FXML
    private void navegacionDeposito(ActionEvent evento) {
        cargarVentana("depositoView.fxml", "Deposito | CasinoFX");
    }

    /**
     * Método para abrir la pestaña de ruleta
     * Se rescata la escena padre y a partir de ella se carga la nueva escena
     *
     * @param evento;
     */
    @FXML
    private void navegacionRuleta(ActionEvent evento) throws SQLException, IOException {
        //Se comprueba el saldo del usuario. Si es 0, se mostrará una alerta para que deposite saldo
        //En caso contrario, se cargará la escena sin problemas

        Double saldo = consultarSaldo(evento);

        if (saldo <= 0) {
            mostrarAlertas(Alert.AlertType.WARNING, "¡Atención!", "Su saldo es de 0€. Por favor dirígase a la ventana de depósito e ingrese saldo");
        } else {
            cargarVentana("ruletaView.fxml", "Ruleta | CasinoFX");
        }
    }

    /**
     * Método generador del historial de jugadas de porra de un jugador
     * Crea un fichero csv, recoge la información de la base de datos
     * y la pinta en un fichero .csv que será descargado en la carpeta
     * ráiz del proyecto
     */
    @FXML
    public void generarCsvHistorial() throws IOException, SQLException {
        String nomUsu = consultarNick();

        Connection con = establecerConexion();
        PreparedStatement psConsultaHistorialPorra = con.prepareStatement("select up.nomUsuario,p.idPorra,p.equipoLocal,p.equipoVisitante,p." +
                "pronosticoGolesLocales,p.pronosticoGolesVisitantes,p.golesLocal,p.golesVisitante " +
                "from UsuarioPorra up INNER JOIN Porra p " +
                "ON up.idPorra = p.idPorra where up.nomUsuario = ?");
        psConsultaHistorialPorra.setString(1, nomUsu);

        ResultSet rsConsultaHistorialPorra = psConsultaHistorialPorra.executeQuery();

        if (rsConsultaHistorialPorra.next()) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Historico_Porra_" + nomUsu + ".csv"), StandardCharsets.ISO_8859_1));
            bw.write("nom Usu;".toUpperCase());
            bw.write("id Porra;".toUpperCase());
            bw.write("Local;".toUpperCase());
            bw.write("Visitante;".toUpperCase());
            bw.write("ap Local;".toUpperCase());
            bw.write("ap Visit;".toUpperCase());
            bw.write("goles Loc;".toUpperCase());
            bw.write("goles Vis;".toUpperCase());
            bw.newLine();

            while (rsConsultaHistorialPorra.next()) {
                bw.write(rsConsultaHistorialPorra.getString(1) + ";");
                bw.write(rsConsultaHistorialPorra.getInt(2) + ";");
                bw.write(rsConsultaHistorialPorra.getString(3) + ";");
                bw.write(rsConsultaHistorialPorra.getString(4) + ";");
                bw.write(rsConsultaHistorialPorra.getInt(5) + ";");
                bw.write(rsConsultaHistorialPorra.getInt(6) + ";");
                bw.write(rsConsultaHistorialPorra.getInt(7) + ";");
                bw.write(rsConsultaHistorialPorra.getInt(8) + ";");
                bw.newLine();
            }
            bw.close();
            mostrarAlertas(Alert.AlertType.CONFIRMATION, "Historial generado correctamente", "Puede consultarlo cuando desee");
        }

    }


    /**
     * Método para abrir la pestaña de porra
     * Se rescata la escena padre y a partir de ella se carga la nueva escena
     * @param evento;
     */
    @FXML
    private void navegacionPorra(ActionEvent evento) throws SQLException, IOException {
        //Se comprueba el saldo del usuario. Si es 0, se mostrará una alerta para que deposite saldo
        //En caso contrario, se cargará la escena sin problemas

        Double saldo = consultarSaldo(evento);

        if(saldo <= 0){
            mostrarAlertas(Alert.AlertType.WARNING,"¡Atención!","Su saldo es de 0€. Por favor dirígase a la ventana de depósito e ingrese saldo");
        }else{
            cargarVentana("porraView.fxml","Porra | CasinoFX");
        }
    }

    /**
     * Método para abrir la pestaña de autoexclusión
     * Se rescata la escena padre y a partir de ella se carga la nueva escena
     * @param evento;
     */
    @FXML
    private void navegacionExclusion(ActionEvent evento) {
        cargarVentana("autoExclusionView.fxml","Exclusión | CasinoFX");
    }

    /**
     * Método para establecer la conexión con la base de datos
     * Devuelve un objeto Connection que será reutilizado posteriormente
     * @return con;
     */
    private Connection establecerConexion() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://iescristobaldemonroy.duckdns.org:" + PUERTO + "/" + NOMBD + "?useSSL=false", USUARIO, PASSWORD);
        return con;
    }

    /**
     * Método para consultar el nick del usuario
     * Se abre el fichero creado previamente y se lee para actualizar la etiqueta que lo muestra
     */
    @FXML
    public String consultarNick() throws SQLException, IOException {
        BufferedReader br = null;
        String linea = "";
        String nombre = "";
        br = new BufferedReader(new FileReader("controlSesiones.txt"));

        while ((linea = br.readLine()) != null) {
            if(!nomUsuario.getText().contains(linea)){
                nomUsuario.setText(nomUsuario.getText()+" "+linea);
                nombre = linea;
            }
        }
        return nombre;
    }

    /**
     * Método encargado de controlar y gestionar todas las alertas
     * @param warning, mensaje, tituloAlerta;
     */

    private void mostrarAlertas(Alert.AlertType warning, String tituloAlerta, String mensaje) {
        Alert alert = new Alert(warning);
        alert.setHeaderText(null);
        alert.setTitle(tituloAlerta);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Método para consultar el saldo del usuario
     * @param actionEvent;
     * @return saldoActual;
     */
    @FXML
    public Double consultarSaldo(ActionEvent actionEvent) throws SQLException, IOException {
        //Se abre y lee el fichero creado anteriormente que contiene al usuario logueado
        //Se reutiliza el método creado anteriormente para realizar la conexión con la base de datos
        //Se recoge el saldo del usuario y se devuelve para ser usado en otros métodos

        BufferedReader br = null;
        String linea;
        Double saldoActual=0.0;

        Connection con = establecerConexion();
        br = new BufferedReader(new FileReader(("controlSesiones.txt")));

        while ((linea = br.readLine()) != null) {
            saldoActual = recuperarSaldo(linea, saldoActual, con);
        }
        return saldoActual;
    }

    /**
     * Método que recupera de la BDAT el saldo del usuario y lo devuelve para que sea usado en otros métodos
     * @param linea,saldoActual,con;
     * @return saldoActual;
     */
    private Double recuperarSaldo(String linea, Double saldoActual, Connection con) throws SQLException {
        PreparedStatement psConsultaSaldo = con.prepareStatement("SELECT saldo from Usuario WHERE nomUsuario = ?");
        psConsultaSaldo.setString(1, linea);
        ResultSet rsConsultaSaldo = psConsultaSaldo.executeQuery();

        if (rsConsultaSaldo.next()) {
            saldoActual = rsConsultaSaldo.getDouble(1);
        }
        return saldoActual;
    }

    /**
     * Mismo método que el anterior, pero actualiza el saldo en la etiqueta que lo muestra
     * Llama al método anterior y recoge la variable que éste devuelve
     * @param actionEvent;
     */
    @FXML
    public void actualizarSaldo(ActionEvent actionEvent) throws SQLException, IOException {
        Double saldoActual = consultarSaldo(actionEvent);
        saldoUsuario.setText("Saldo: "+saldoActual+"€");
    }
}
