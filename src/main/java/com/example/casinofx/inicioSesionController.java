package com.example.casinofx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import static com.example.casinofx.utilidades.Constantes.*;

/**
 * Clase que controla la pestaña de inicio sesión
 */
public class inicioSesionController {

    @FXML
    private TextField contraseña;
    @FXML
    public TextField nomUsuario;

    /**
     * Método para comprobar si un usuario está conectado a internet
     * @return conectado
     */
    private boolean comprobarConexionInternet() throws IOException, InterruptedException {
        //Se crea un proceso con la clase Process y se hace ping a Google
        //Esta clase contiene un método waitFor(), que devuelve 0 si el proceso ha sido exitoso y 1 si ha sido interrumpido
        //Por lo tanto se interpreta el 0 ó el 1 para saber si un usuario está o no conectado a internet

        boolean conectado;

        Process process = java.lang.Runtime.getRuntime().exec("ping www.google.es");
        int estadoProceso = process.waitFor();

        if (estadoProceso == 0) {
            conectado = true;
        }
        else {
            conectado = false;
        }
        return conectado;
    }

    /**
     * Método para conectarse a la base de datos
     * @return con;
     */
    private Connection establecerConexion() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://iescristobaldemonroy.duckdns.org:" + PUERTO + "/" + NOMBD + "?useSSL=false", USUARIO, PASSWORD);
        return con;
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
     * Método para loguear al usuario y enseñar el menú principal
     * @param event;
     */
    @FXML
    protected void login(ActionEvent event) throws SQLException, IOException, InterruptedException {
        //Se declaran las variables necesarias y se establece la conexión con la BDAT
        //Se realiza una consulta para comprobar si un user está o no excluido del sistema

        int saldo;

        if(comprobarConexionInternet() == false){
            mostrarAlertas(Alert.AlertType.WARNING,"Error de conectividad","Por favor, compruebe su conexión a Internet");
        }else{
            Connection con = establecerConexion();
            PreparedStatement psConsultaExclusion = comprobarUserExcluido(con);

            //Se comprueba que la consulta devuelve datos. En caso afirmativo, se muestra una alerta avisando al usuario y se cierra la aplicación
            if(psConsultaExclusion.executeQuery().next()){
                mostrarAlertas(Alert.AlertType.ERROR,"Inicio de sesión","Usted está excluído del sistema. No podrá acceder al sistema");
                nomUsuario.setText("");
                contraseña.setText("");

            }else{
                //En caso negativo, se comprueba que los campos no estén vacíos. Se notificará al usuario para que los rellene mediante una alerta
                if (nomUsuario.getText().isEmpty() || contraseña.getText().isEmpty()) {
                    mostrarAlertas(Alert.AlertType.WARNING,"Inicio de sesión","Por favor, rellene los campos");

                } else {
                    //Si el usuario los rellena, se comprueba con una consulta que está registrado en la base de datos
                    try {
                        PreparedStatement psCompruebaUser = compruebaUsuario(con);

                        //Si está registrado en el sistema, se le da la bienvenida al user
                        if (psCompruebaUser.executeQuery().next()) {
                            mostrarAlertas(Alert.AlertType.CONFIRMATION,"Bienvenido al CasinoFX","Hola "+nomUsuario.getText()+", bienvenido al CasinoFX");

                            //Se almacena el nombre del usuario loguedado en un fichero para usarlo en otras ventanas
                            escribirFichero(nomUsuario);

                            //Se carga la ventana principal
                            cargarMenuPrincipal();
                        } else {
                            //En caso de que el usuario falle en el login se le avisa mediante una alerta y se vacían los campos de nuevo
                            mostrarAlertas(Alert.AlertType.ERROR,"Error en el inicio de sesión","Credenciales incorrectas. Inténtalo de nuevo o cree una cuenta si no la tiene");
                            nomUsuario.setText("");
                            contraseña.setText("");
                        }
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    } finally {
                        //Se cierra la conexión
                        if (con != null) {
                            try {
                                con.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Método para comprobar si un usuario está excluido del sistema o no
     * @return conectado
     */
    private PreparedStatement comprobarUserExcluido(Connection con) throws SQLException {
        PreparedStatement psConsultaExclusion = con.prepareStatement("SELECT nombre FROM Usuario WHERE nomUsuario = ? AND f_autoexclusion IS NOT NULL");
        psConsultaExclusion.setString(1, nomUsuario.getText());

        return psConsultaExclusion;
    }

    /**
     * Método que se encarga de comprobar que existe un usuario en la base de datos
     */
    private PreparedStatement compruebaUsuario(Connection con) throws SQLException {
        PreparedStatement psCompruebaUser = con.prepareStatement("SELECT * FROM Usuario WHERE nomUsuario = ? AND contraseña = ?");

        psCompruebaUser.setString(1, nomUsuario.getText());
        psCompruebaUser.setString(2, contraseña.getText());

        return psCompruebaUser;
    }

    /**
     * Método que se encarga de cargar la escena principal de la app
     */
    private void cargarMenuPrincipal() throws IOException {
        Stage stage2 = (Stage) this.nomUsuario.getScene().getWindow();
        stage2.close();

        Parent root = FXMLLoader.load(getClass().getResource("inicioView.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setScene(scene);
        stage.setResizable(false);
        Image ico = new Image(RUTA_LOGO);// No funciona con ruta relativa
        stage.getIcons().add(ico);
        stage.setTitle("Inicio | CasinoFX");
        stage.show();
    }

    /**
     * Método que deja escrito en un fichero el usuario que está usando la aplicación
     * @param nomUsuario,saldo;
     */
    public static void escribirFichero(TextField nomUsuario) throws IOException {
        //Se crea un fichero en caso de que no exista (primera vez que se ejecuta la aplicación)
        //Se almacena el nombre del usuario que está usando la app
        //Se cierra el flujo una vez se termine el proceso

        File fichero = new File("controlSesiones.txt");
        FileWriter flujo = null;
        try {
            if(!fichero.exists()){
                fichero.createNewFile();
            }
            flujo = new FileWriter(fichero);
            PrintWriter escribirFichero = new PrintWriter(flujo);
            escribirFichero.print(nomUsuario.getText());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (flujo != null) {
                    flujo.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Método que redirige a la ventana de registro
     * @param event
     */
    @FXML
    protected void registrarUsuario(ActionEvent event) {
        //Se carga la ventana de registrar usuario al clickar en su respectivo link
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("registroView.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("Registro | CryptoFX");
        stage.setScene(scene);
        stage.setResizable(false);
        Image ico = new Image(RUTA_LOGO); // No funciona con ruta relativa
        stage.getIcons().add(ico);
        stage.show();
    }
}
