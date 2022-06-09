package com.example.casinofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.example.casinofx.utilidades.Constantes.*;

/**
 * Clase que controla el registro
 */
public class registroController {
    @FXML
    private TextField nombre;

    @FXML
    private TextField apellidos;

    @FXML
    private TextField nomUsuARegistrar;

    @FXML
    private PasswordField passwordARegistrar;

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
     * Método para conectarse a la base de datos
     * @return con;
     */
    private Connection establecerConexion() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://iescristobaldemonroy.duckdns.org:" + PUERTO + "/" + NOMBD + "?useSSL=false", USUARIO, PASSWORD);
        return con;
    }

    /**
     * Método que se encarga del registro
     * @param event
     */
    @FXML
    private void registroUsuario(ActionEvent event) throws IOException, InterruptedException {
        //Se comprueba previamente que hay conexión a internet
        if (comprobarConexionInternet() == false) {
            mostrarAlertas(Alert.AlertType.WARNING, "Error de conectividad", "Por favor, compruebe su conexión a Internet");
        } else {
            //Se comprueba si los campos están vacíos. En caso de que lo estén, se muestra una alerta avisando al usuario
            if (nombre.getText().isEmpty() || apellidos.getText().isEmpty() || nomUsuARegistrar.getText().isEmpty() || passwordARegistrar.getText().isEmpty()) {
                mostrarAlertas(Alert.AlertType.WARNING, "Inicio de sesión", "Por favor, rellene los campos");
            } else {
                Connection conexion = null;
                try {
                    //En caso que los rellene, se comprueba que no está logueado en la base de datos
                    conexion = establecerConexion();
                    PreparedStatement psCompruebaUser = conexion.prepareStatement("SELECT * FROM Usuario WHERE nomUsuario = ?");
                    psCompruebaUser.setString(1, nomUsuARegistrar.getText());

                    if (!psCompruebaUser.executeQuery().next()) {
                        //Si no tiene cuenta, se crea una nueva en la base de datos con los datos introducidos en el formulario
                        psCompruebaUser = insertarUserBDAT(conexion);

                        //Si el proceso resulta exitoso, se enseña al usuario el mensaje correspondiente
                        //y se cierra esta pestaña para volver al login. En caso contrario, se informa del error
                        if (psCompruebaUser.executeUpdate() == 1) {
                            mostrarAlertas(Alert.AlertType.INFORMATION, "Registro", "Usuario registrado correctamente");
                            inicioSesion(event);
                        } else {
                            mostrarAlertas(Alert.AlertType.ERROR,"Error en el registro","Algo no ha salido bien, intente de nuevo");
                        }
                    } else {
                        //Si el usuario estuviese logueado será informado ya que no puede tener dos cuentas
                        //Lo mismo ocurrirá si estuviese excluido del sistema(su cuenta aparece en la base de datos)
                        //pero no tiene acceso a la aplicación
                        mostrarAlertas(Alert.AlertType.INFORMATION, "Error en el registro", "Ya existe un usuario con ese nick y/o está excluido del sistema");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    //Se cierra la conexión
                    if (conexion != null) {
                        try {
                            conexion.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Método que se encarga de insertar al usuario en la base de datos
     */
    private PreparedStatement insertarUserBDAT(Connection conexion) throws SQLException {
        PreparedStatement ps;
        ps = conexion.prepareStatement("INSERT INTO Usuario(nombre, apellidos, nomUsuario, contraseña, saldo) VALUES (?, ?, ?, ?, 0)");

        ps.setString(1, nombre.getText());
        ps.setString(2, apellidos.getText());
        ps.setString(3, nomUsuARegistrar.getText());
        ps.setString(4, passwordARegistrar.getText());
        return ps;
    }

    /**
     * Método que se encarga de regresar al inicio
     * @param event
     */
    @FXML
    public void inicioSesion(ActionEvent event) {
        Stage stage2 = (Stage) this.nombre.getScene().getWindow();
        stage2.close();
    }
}
