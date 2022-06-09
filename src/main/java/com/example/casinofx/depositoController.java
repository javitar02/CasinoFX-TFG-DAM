package com.example.casinofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;

import static com.example.casinofx.utilidades.Constantes.*;
import static com.example.casinofx.utilidades.Constantes.PASSWORD;

/**
 * Clase que gestiona el depósito
 */
public class depositoController {
    @FXML
    public TextField cantidadAIngresar;
    @FXML
    public Button btnDepositarSaldo;

    /**
     * Método encargado de comprobar que se introduce un número
     * a la hora de indicar la cantidad a depositar
     * @param cadena;
     **/
    private boolean comprobarNumero(String cadena){
        boolean esNumero;

        if (cadena.matches("^\\d{1,3}(.?\\d{3})*(\\.\\d{1,2})?$")){
            esNumero = true;
        }else{
            esNumero = false;
        }

        return esNumero;
    }

    /**
     * Método encargado de gestionar las alertas
     * @param warning,tituloAlerta,mensaje;
     **/
    private void mostrarAlertas(Alert.AlertType warning, String tituloAlerta, String mensaje) {
        Alert alert = new Alert(warning);
        alert.setHeaderText(null);
        alert.setTitle(tituloAlerta);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Método encargado de gestionar el deposito de saldo
     * @param event;
     **/
    @FXML
    private void deposito(ActionEvent event) throws IOException, SQLException {
        BufferedReader br = null;
        String linea;
        Double saldoAntiguo;
        Double saldoNuevo;
        Double saldoActual;

        //Se realiza la conexión
        Connection con = DriverManager.getConnection("jdbc:mysql://iescristobaldemonroy.duckdns.org:" + PUERTO + "/" + NOMBD + "?useSSL=false", USUARIO, PASSWORD);

        //Se muestra mensaje si se deja el campo vacío
        if (cantidadAIngresar.getText().isEmpty()) {
            mostrarAlertas(Alert.AlertType.WARNING,"Depósito","Por favor, rellene el campo con una cantidad");
        } else {
            //En caso contrario, se comprueba que el usuario ha introducido un campo numérico
            //Si lo ha hecho, se comprueba que no ha introducido un 0/número negativo
            if (comprobarNumero(cantidadAIngresar.getText())) {
                saldoNuevo = Double.parseDouble(cantidadAIngresar.getText());
                if (saldoNuevo <= 0) {
                    mostrarAlertas(Alert.AlertType.WARNING,"Depósito","Por favor, indique una cantidad válida");
                } else {
                    //Si ha introducido un número positivo, se procede a actualizar su saldo
                    //Se lee el fichero que contiene su nick, se recupera su saldo actual,
                    //y se suma la cantidad que ha ingresado. Finalmente se muestra un mensaje
                    //para indicar que el proceso ha sido realizado correctamente
                    br = new BufferedReader(new FileReader(("controlSesiones.txt")));

                    while ((linea = br.readLine()) != null) {
                        ResultSet rsConsultaSaldo = consultarSaldo(linea, con);
                        if (rsConsultaSaldo.next()) {
                            saldoAntiguo = rsConsultaSaldo.getDouble(1);
                            actualizarSaldo(linea, saldoAntiguo, saldoNuevo, con);
                            saldoActual = saldoAntiguo + saldoNuevo;
                            mostrarAlertas(Alert.AlertType.CONFIRMATION,"Depósito","Se han ingresado correctamente los " + saldoNuevo + "€. Su saldo actual es de " + saldoActual + "€");

                            //Se cierra la pestaña y se regresa al menú principal
                            Stage stage = (Stage) this.cantidadAIngresar.getScene().getWindow();
                            stage.close();
                        }
                    }
                }
            }else {
                //Si se ha equivocado metiendo un campo no numérico una alerta será lanzada
                mostrarAlertas(Alert.AlertType.WARNING,"Cuidado","Por favor rellene el campo con un caracter númerico");
            }
        }
    }

    /**
     * Método encargado de actualizar el saldo al usuario
     * @param linea,saldoAntiguo,saldoNuevo,con;
     **/
    private void actualizarSaldo(String linea, Double saldoAntiguo, Double saldoNuevo, Connection con) throws SQLException {
        PreparedStatement psIncrementaSaldo = con.prepareStatement("UPDATE Usuario SET saldo = " + saldoAntiguo + "+" + saldoNuevo + " WHERE nomUsuario = ?");
        psIncrementaSaldo.setString(1, linea);
        psIncrementaSaldo.executeUpdate();
    }

    /**
     * Método encargado de consultar el saldo del usuario
     * @param linea,con;
     **/
    private ResultSet consultarSaldo(String linea, Connection con) throws SQLException {
        PreparedStatement psConsultaSaldo = con.prepareStatement("SELECT saldo from Usuario WHERE nomUsuario = ?");
        psConsultaSaldo.setString(1, linea);
        ResultSet rsConsultaSaldo = psConsultaSaldo.executeQuery();
        return rsConsultaSaldo;
    }
}

