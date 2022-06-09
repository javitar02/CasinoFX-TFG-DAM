package com.example.casinofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.casinofx.utilidades.Constantes.*;
import static com.example.casinofx.utilidades.Constantes.PASSWORD;

/**
 * Clase que gestiona la parte de la ruleta
 */
public class ruletaController {
    @FXML
    private TextField mostrarSaldo;

    @FXML
    private TextField apuestaEscogida;

    @FXML
    private TextField numeroApostado;

    @FXML
    private TextField cantidadApostada;

    @FXML
    private TextField mostrarNumGanador;

    /**
     * Método que se encarga de cerrar la pestaña y regresar al inicio
     */
    @FXML
    private void volverInicio(ActionEvent evento){
        Stage stage = (Stage) this.mostrarSaldo.getScene().getWindow();
        stage.close();
    }

    /**
     * Método encargado de gestionar la conexión con la base de datos
     **/
    private Connection establecerConexion() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://iescristobaldemonroy.duckdns.org:" + PUERTO + "/" + NOMBD + "?useSSL=false", USUARIO, PASSWORD);
        return con;
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
     * Mismo método que el anterior, pero actualiza el saldo en la etiqueta que lo muestra
     * Llama al método anterior y recoge la variable que éste devuelve
     * @param actionEvent;
     */
    @FXML
    public void actualizarSaldo(ActionEvent actionEvent) throws SQLException, IOException {
        Double saldoActual = consultarSaldo(actionEvent);
        mostrarSaldo.setText("Saldo: "+saldoActual+"€");
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
     * Método encargado de comprobar que se introduce un número
     * a la hora de pronosticar una jugada
     * @param cadena;
     * @return esNumero;
     **/
    private boolean comprobarNumeroEntero(String cadena){
        boolean esNumero;

        if (cadena.matches("-?([0-9]*)?")){
            esNumero = true;
        }else{
            esNumero = false;
        }

        return esNumero;
    }

    /**
     * Método encargado de comprobar que se introduce un número
     * decimal
     * @param cadena;
     * @return esDecimal;
     **/
    private boolean comprobarNumeroDecimal(String cadena){
        boolean esDecimal;

        if (cadena.matches("^\\d{1,3}(.?\\d{3})*(\\.\\d{1,2})?$")){
            esDecimal = true;
        }else {
            esDecimal = false;
        }

        return esDecimal;
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
     * Método encargado de gestionar toda la parte de jugar
     * @param evento;
     **/

    @FXML
    private void jugar(ActionEvent evento) throws SQLException, IOException {
        int numeroGanador;
        int numeroJugado;
        int apuestaRealizada;
        Double saldoActual;
        Double importeJugado;

        String nomUsu;
        Connection con;

        //Se controla que los campos no estén vacíos. Se mostrará alerta en caso de que los estén
        if (apuestaEscogida.getText().isEmpty() || (cantidadApostada.getText().isEmpty())){
            mostrarAlertas(Alert.AlertType.WARNING,"¡Cuidado!","No deje los campos vacíos por favor. Rellénelos");

            //Si son rellenados, se controla que se introduzcan correctamente caracteres numéricos
        }else{
            if (comprobarNumeroEntero(apuestaEscogida.getText()) == false || comprobarNumeroDecimal(cantidadApostada.getText())== false
                    || Integer.parseInt(apuestaEscogida.getText()) < 1 ||  Integer.parseInt(apuestaEscogida.getText()) >13){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Cuidado!","Introduzca un importe válido a jugar" +
                        "\nO un número de 1 a 13 para la jugada o de 0 a 36 si juega a número único");
            }else{
                //Si se hace correctamente, se controla que se introduzca una cantidad válida para jugar
                if(Double.parseDouble(cantidadApostada.getText()) <=0){
                    mostrarAlertas(Alert.AlertType.WARNING,"¡Atención!","Introduczca un cantidad postiva para jugar");
                }else{
                    //Al introducir los datos correctamente, se comprueba que el usuario dispone de saldo disponible
                    saldoActual = consultarSaldo(evento);

                    //Si no tiene, se le avisará para que deposite dinero
                    if(saldoActual <=0 || saldoActual < Double.parseDouble(cantidadApostada.getText())){
                        mostrarAlertas(Alert.AlertType.WARNING,"¡Saldo insuficiente!","No tiene saldo disponible. Por favor vaya a la pestaña de depósito");
                    }else{
                        //En caso de tener saldo y no jugar al número único, se inicializa el
                        //número jugado a -1, ya que al no usarse pero ser parámetro del
                        //método "logicaRuleta" da error por valer nulo
                        if(!apuestaEscogida.getText().contains("1")){
                            numeroJugado = -1;
                        }else{
                            numeroJugado=Integer.parseInt(numeroApostado.getText());
                        }
                        //Se recogen los parámetros necesarios, se establece la conexión,
                        //se sortea el número entre 0 y 36 y se llama al método "logicaRuleta"
                        apuestaRealizada = Integer.parseInt(apuestaEscogida.getText());
                        importeJugado = Double.parseDouble(cantidadApostada.getText());

                        nomUsu = consultarUsuario();
                        con=establecerConexion();
                        descontarSaldo(evento,nomUsu,con,importeJugado);

                        numeroGanador = (int) (Math.random() * 36 + 0);
                        logicaRuleta(apuestaRealizada,numeroJugado,numeroGanador, importeJugado, evento);
                    }
                }
            }
        }
    }

    /**
     * Método encargado de actualizar el saldo
     * del usuario cuando juega (participación)
     * @param evento,nomUsu,con,cantidadApostada;
     **/
    private void descontarSaldo(ActionEvent evento, String nomUsu, Connection con, Double cantidadApostada) throws SQLException, IOException {
        Double saldo = consultarSaldo(evento);
        Double totalADescontar = saldo-cantidadApostada;

        PreparedStatement psRestaSaldo = con.prepareStatement("UPDATE Usuario SET SALDO = " +redondearDecimales(totalADescontar)+ " WHERE nomUsuario = ?");
        psRestaSaldo.setString(1, nomUsu);
        psRestaSaldo.executeUpdate();
    }

    /**
     * Método encargado de gestionar todas las jugadas
     * disponibles mediante un switch
     * @param apuestaRealizada,numeroJugado,numeroGanador,importeJugado,evento;
     **/
    private void logicaRuleta(int apuestaRealizada, int numeroJugado, int numeroGanador, Double importeJugado, ActionEvent evento) throws SQLException, IOException {
        //Se realiza la conexión con la BDAT
        Connection con=establecerConexion();

        //Según la jugada realizada, se llama al método correspondiente
        switch (apuestaRealizada){
            case 1:
                apuestaNumeroUnico(numeroJugado,numeroGanador,importeJugado,con,evento);
                break;
            case 2:
                apuestaNumeroRojo(numeroGanador,importeJugado,con,evento);
                break;
            case 3:
                apuestaNumeroNegro(numeroGanador,importeJugado,con,evento);
                break;
            case 4:
                apuestaNumeroPar(numeroGanador,importeJugado,con,evento);
                break;
            case 5:
                apuestaNumeroImpar(numeroGanador,importeJugado,con,evento);
                break;
            case 6:
                apuesta1_A_18(numeroGanador,importeJugado,con,evento);
                break;
            case 7:
                apuesta19_A_36(numeroGanador,importeJugado,con,evento);
                break;
            case 8:
                apuestaPrimeraDocena(numeroGanador,importeJugado,con,evento);
                break;
            case 9:
                apuestaSegundaDocena(numeroGanador,importeJugado,con,evento);
                break;
            case 10:
                apuestaTerceraDocena(numeroGanador,importeJugado,con,evento);
                break;
            case 11:
                apuestaPrimeraColumna(numeroGanador,importeJugado,con,evento);
                break;
            case 12:
                apuestaSegundaColumna(numeroGanador,importeJugado,con,evento);
                break;
            case 13:
                apuestaTerceraColumna(numeroGanador,importeJugado,con,evento);
                break;
        }

        //Al terminar una apuesta, se actualiza el número ganador, el mensaje
        //con el saldo actualizado, y se inserta la información en la BDAT
        mostrarNumGanador.setText("NÚMERO GANADOR: "+numeroGanador);
        mostrarSaldo.setText("Saldo: " +consultarSaldo(evento)+ "€");
        insertarApuestaBDAT(con,importeJugado,apuestaRealizada,numeroGanador);
        insertarHistorico(con,apuestaRealizada);
    }

    /**
     * Método encargado de gestionar la jugada del número único
     * Tiene multiplicador 36 y compara el número pronosticado
     * con el ganador. Tanto si gana como pierde se muestran
     * las respectivas alertas y se actualiza el sueldo
     * @param numeroJugado,numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaNumeroUnico(int numeroJugado, int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        int multiplicadorPremio=36;

        if(numeroApostado.getText().isEmpty()){
            mostrarAlertas(Alert.AlertType.WARNING,"¡Cuidado!","Indique el número a jugar");
        }else{
            if(numeroJugado <0 || numeroJugado > 36){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Cuidado!","Introduzca un número entre 0 y 36");
            }else{
                if(numeroGanador == numeroJugado){
                    sumarPremio(importeJugado,multiplicadorPremio,con,evento);
                    mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","El número ganador ha sido el "+numeroGanador +
                            "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(consultarSaldo(evento))+"€");
                    mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");

                }else{
                    mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue el " +numeroJugado+ " y ha salido el "+numeroGanador +
                            "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
                }
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta al rojo.
     * Tiene multiplicador 2 y busca si el número ganador
     * se encuentra en la lista de rojos. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaNumeroRojo(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>rojos=new ArrayList<>(Arrays.asList(
                1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36
        ));
        int multiplicadorPremio=2;
        int posicion = rojos.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue rojo y ha salido el "+numeroGanador+" rojo" +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");

        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue rojo y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue rojo y ha salido el "+numeroGanador+" negro" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta al negro.
     * Tiene multiplicador 2 y busca si el número ganador
     * se encuentra en la lista de negros. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaNumeroNegro(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>negros=new ArrayList<>(Arrays.asList(
                2,4,6,8,10,11,13,15,17,20,22,24,26,28,31,33,35
        ));
        int multiplicadorPremio=2;
        int posicion = negros.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue negro y ha salido el "+numeroGanador+" negro" +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue negro y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue negro y ha salido el "+numeroGanador+" rojo" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta a par.
     * Tiene multiplicador 2 y busca si el número ganador
     * se encuentra en la lista de pares. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaNumeroPar(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>pares=new ArrayList<>(Arrays.asList(
                2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36
        ));
        int multiplicadorPremio=2;
        int posicion = pares.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue par y ha salido el "+numeroGanador+" par" +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue par y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue par y ha salido el "+numeroGanador+" impar" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta al impar.
     * Tiene multiplicador 2 y busca si el número ganador
     * se encuentra en la lista de impares. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaNumeroImpar(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>impares=new ArrayList<>(Arrays.asList(
                1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35
        ));
        int multiplicadorPremio=2;
        int posicion = impares.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue impar y ha salido el "+numeroGanador+" impar" +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue impar y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue impar y ha salido el "+numeroGanador+" par" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta de 1 a 18.
     * Tiene multiplicador 2 y busca si el número ganador
     * se encuentra en la lista de 1 a 18. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuesta1_A_18(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>numerosDe1a18=new ArrayList<>(Arrays.asList(
                1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18
        ));
        int multiplicadorPremio=2;
        int posicion = numerosDe1a18.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue número entre 1 y 18 y ha salido el "+numeroGanador +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue número entre 1 y 18 y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue número entre 1 y 18 y ha salido el "+numeroGanador +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta de 19 a 36.
     * Tiene multiplicador 2 y busca si el número ganador
     * se encuentra en la lista de 19 a 36. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuesta19_A_36(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>numerosDe19a36=new ArrayList<>(Arrays.asList(
                19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36
        ));
        int multiplicadorPremio=2;
        int posicion = numerosDe19a36.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue número entre 19 y 36 y ha salido el "+numeroGanador +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue número entre 19 y 36 y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue número entre 19 y 36 y ha salido el "+numeroGanador +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta a primera docena.
     * Tiene multiplicador 3 y busca si el número ganador
     * se encuentra en la primera docena. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaPrimeraDocena(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>primeraDocena=new ArrayList<>(Arrays.asList(
                1,2,3,4,5,6,7,8,9,10,11,12
        ));
        int multiplicadorPremio=3;
        int posicion = primeraDocena.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue primera docena y ha salido el "+numeroGanador +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue primera docena y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue primera docena y ha salido el "+numeroGanador +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta a segunda docena.
     * Tiene multiplicador 3 y busca si el número ganador
     * se encuentra en la segunda docena. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaSegundaDocena(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>segundaDocena=new ArrayList<>(Arrays.asList(
                13,14,15,16,17,18,19,20,21,22,23,24
        ));
        int multiplicadorPremio=3;
        int posicion = segundaDocena.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue segunda docena y ha salido el "+numeroGanador +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue segunda docena y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue segunda docena y ha salido el "+numeroGanador +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta a tercera docena.
     * Tiene multiplicador 3 y busca si el número ganador
     * se encuentra en la tercera docena. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaTerceraDocena(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>terceraDocena=new ArrayList<>(Arrays.asList(
                25,26,27,28,29,30,31,32,33,34,35,36
        ));
        int multiplicadorPremio=3;
        int posicion = terceraDocena.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue tercera docena y ha salido el "+numeroGanador +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue tercera docena y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue tercera docena y ha salido el "+numeroGanador +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta a primera columna.
     * Tiene multiplicador 3 y busca si el número ganador
     * se encuentra en la primera columna. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaPrimeraColumna(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>primeraColumna=new ArrayList<>(Arrays.asList(
                1,4,7,10,13,16,19,22,25,28,31,34
        ));
        int multiplicadorPremio=3;
        int posicion = primeraColumna.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue primera columna y ha salido el "+numeroGanador +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue primera columna y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue primera columna y ha salido el "+numeroGanador +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta a segunda columna.
     * Tiene multiplicador 3 y busca si el número ganador
     * se encuentra en la primera docena. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaSegundaColumna(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>segundaColumna=new ArrayList<>(Arrays.asList(
                1,4,7,10,13,16,19,22,25,28,31,34
        ));
        int multiplicadorPremio=3;
        int posicion = segundaColumna.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue segunda columna y ha salido el "+numeroGanador +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue segunda columna y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue segunda columna y ha salido el "+numeroGanador +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de gestionar la apuesta a tercera columna.
     * Tiene multiplicador 3 y busca si el número ganador
     * se encuentra en la tercera columna. Tanto si gana
     * como pierde se muestran las respectivas alertas y
     * se actualiza el sueldo.
     * @param numeroGanador,importeJugado,con,evento;
     **/
    private void apuestaTerceraColumna(int numeroGanador, Double importeJugado, Connection con, ActionEvent evento) throws SQLException, IOException {
        ArrayList<Integer>terceraColumna=new ArrayList<>(Arrays.asList(
                3,6,9,12,15,18,21,24,27,30,33,36
        ));
        int multiplicadorPremio=3;
        int posicion = terceraColumna.indexOf(numeroGanador);

        if(posicion != -1){
            sumarPremio(importeJugado,multiplicadorPremio,con,evento);
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su pronóstico fue tercera columna y ha salido el "+numeroGanador +
                    "\nUsted ha jugado "+importeJugado+"€ y ha ganado "+redondearDecimales(multiplicadorPremio*importeJugado)+"€");
            mostrarAlertas(Alert.AlertType.CONFIRMATION,"¡Enhorabuena!","Su nuevo saldo es de "+consultarSaldo(evento)+"€");
        }else{
            if(numeroGanador==0){
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue tercera columna y ha salido el 0 verde" +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }else{
                mostrarAlertas(Alert.AlertType.WARNING,"¡Lo sentimos!","Su pronóstico fue tercera columna y ha salido el "+numeroGanador +
                        "\nLe quedan " +consultarSaldo(evento)+ "€ restantes.¡Suerte para la próxima!");
            }
        }
    }

    /**
     * Método encargado de buscar en el fichero el usuario que está usando la app en un momento determinado
     **/
    private String consultarUsuario() throws IOException {
        BufferedReader br;
        String linea;

        br = new BufferedReader(new FileReader(("controlSesiones.txt")));
        linea = br.readLine();

        return linea;
    }

    /**
     * Método encargado de insertar la apuesta en la base de datos
     **/
    private void insertarApuestaBDAT(Connection con, Double importeJugado, int apuestaRealizada, int numeroGanador) throws SQLException {
        PreparedStatement psRegistraApuesta = con.prepareStatement("INSERT INTO Ruleta (importeApostado,tipoApuesta,numGanador) VALUES(?,?,?)");

        psRegistraApuesta.setDouble(1, importeJugado);
        psRegistraApuesta.setInt(2, apuestaRealizada);
        psRegistraApuesta.setInt(3, numeroGanador);

        psRegistraApuesta.executeUpdate();
    }

    /**
     * Método encargado de actualizar el histórico del jugador
     **/
    private void insertarHistorico(Connection con, int apuestaRealizada) throws IOException, SQLException {
        String nomUsuario = consultarUsuario();

        PreparedStatement psRegistraHistorico = con.prepareStatement("INSERT INTO UsuarioRuleta (idApuestaRuleta,nomUsuario) VALUES(?,?)");

        psRegistraHistorico.setInt(1, apuestaRealizada);
        psRegistraHistorico.setString(2, nomUsuario);

        psRegistraHistorico.executeUpdate();
    }



    private void sumarPremio(Double importeJugado, int multiplicadorPremio, Connection con, ActionEvent evento) throws SQLException, IOException {
        String nomUsuario = consultarUsuario();
        PreparedStatement psSumaSaldo = null;

        Double saldo = consultarSaldo(evento);
        Double dineroGanado = multiplicadorPremio*importeJugado;
        Double totalGanado = saldo + dineroGanado;

        psSumaSaldo = con.prepareStatement("UPDATE Usuario SET SALDO = " +redondearDecimales(totalGanado)+ "WHERE nomUsuario = ?");
        psSumaSaldo.setString(1, nomUsuario);

        psSumaSaldo.executeUpdate();
    }

    private Double redondearDecimales(Double cantidad) {
        return  Math.round(cantidad*100.0)/100.0;
    }

}
