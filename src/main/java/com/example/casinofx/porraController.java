package com.example.casinofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.casinofx.utilidades.Constantes.*;
import static com.example.casinofx.utilidades.Constantes.PASSWORD;
import static java.lang.Thread.sleep;

/**
 * Clase que gestiona la pestaña de porra
 */
public class porraController {
    @FXML
    private TextField minutosPartido;
    @FXML
    private TextField saldoUsuario;
    @FXML
    private TextField equipoLocal;
    @FXML
    private TextField equipoVisitante;
    @FXML
    private TextField golesLocales;
    @FXML
    private TextField golesVisitantes;
    @FXML
    private Button btnInicio;
    @FXML
    private Button btnJugarPorra;
    @FXML
    private Button btnVerPartido;

    /**
     * Método que se encarga de cerrar la pestaña y regresar al inicio
     */
    @FXML
    private void volverInicio(ActionEvent evento){
        Stage stage = (Stage) this.btnInicio.getScene().getWindow();
        stage.close();
    }

    /**
     * Método para consultar el saldo del usuario
     * @param actionEvent;
     * @return saldoActual;
     */
    @FXML
    public Double consultarSaldo(ActionEvent actionEvent) throws SQLException, IOException {
        BufferedReader br = null;
        String linea;
        Double saldoActual=0.0;

        Connection con = establecerConexion();
        br = new BufferedReader(new FileReader(new File("controlSesiones.txt")));

        while ((linea = br.readLine()) != null) {
            PreparedStatement psConsultaSaldo = con.prepareStatement("SELECT saldo from Usuario WHERE nomUsuario = ?");
            psConsultaSaldo.setString(1, linea);
            ResultSet rsConsultaSaldo = psConsultaSaldo.executeQuery();
            if (rsConsultaSaldo.next()) {
                saldoActual = rsConsultaSaldo.getDouble(1);
            }
        }

        return saldoActual;
    }

    /**
     * Mismo método que el anterior, pero actualiza el saldo en la etiqueta que lo muestra
     * Llama al método anterior y recoge la variable que éste devuelve
     * @param actionEvent;
     * @return saldoActual;
     */
    @FXML
    public void actualizarSaldo(ActionEvent actionEvent) throws SQLException, IOException {
        Double saldoActual = consultarSaldo(actionEvent);
        if(!saldoUsuario.getText().contains(String.valueOf(saldoActual))){
            saldoUsuario.setText("Saldo: "+saldoActual+"€");
        }
    }

    /**
     * Método encargado de mostrar información al usuario
     * @param evento;
     */
    @FXML
    private void verInfo(ActionEvent evento){
        mostrarAlertas(Alert.AlertType.INFORMATION,"¡Importante!","Bienvenido a este modo de juego. Usted deberá acertar el resultado y puede llevarse 100€ por partido");
        mostrarAlertas(Alert.AlertType.INFORMATION,"¡Importante!","Los equipos disponibles pertenecen a la Liga Santander 2021/2022. En futuras versiones serán actualizados");
        mostrarAlertas(Alert.AlertType.INFORMATION,"¡Importante!","El precio por participación es de 5€. A continuación verá el partido asignado. Podrá jugar los partidos que quiera. ¡Suerte!");
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
     * Método encargado de gestionar el emparejamiento de equipos en un partido
     */
    @FXML
    private void escogerPartido(){
        resetearEtiquetas();
        ArrayList<String> arrayEquipos = rellenarArray();
        sortearPartido(arrayEquipos);
    }

    /**
     * Método encargado de resetear el marcador al acabar un partido
     */
    private void resetearEtiquetas() {
        golesLocales.setText("0");
        golesVisitantes.setText("0");
        minutosPartido.setText("0'");
        golesLocales.setEditable(true);
        golesVisitantes.setEditable(true);
    }

    /**
     * Método encargado de insertar los equipos disponibles en un array para poder seleccionarlos aleatoriamente en un partido
     */
    private ArrayList<String> rellenarArray() {
        ArrayList<String>arrayEquipos=new ArrayList<>(Arrays.asList(
                "ALAVÉS","ATHLETIC CLUB","ATLÉTICO MADRID","FC BARCELONA","REAL BETIS","CÁDIZ","CELTA DE VIGO","ELCHE",
                "ESPANYOL","GETAFE","GRANADA","LEVANTE","MALLORCA","OSASUNA","REAL SOCIEDAD","RAYO VALLECANO","REAL MADRID",
                "SEVILLA","VALENCIA","VILLAREAL"
        ));
        return arrayEquipos;
    }

    /**
     * Método encargado de gestionar el sorteo de equipos
     */
    private void sortearPartido(ArrayList<String> arrayEquipos) {
        mostrarAlertas(Alert.AlertType.INFORMATION,"Cargando el partido...", "Bienvenido a un nuevo encuentro de la Liga Santander. Se mostrarán los equipos en breve");
        escogerEquipos(arrayEquipos);
        configurarEtiquetasBotones();
    }

    /**
     * Método encargado de elegir los equipos del array mediante sorteo
     * Se generan dos números aleatorios para ello entre 0 y la dimensión del array
     * Se evita que se repita el mismo equipo en un partido mediante el borrado de éste de la lista
     **/
    private void escogerEquipos(ArrayList<String> arrayEquipos) {
        int local;
        int visitante;

        local = (int) (Math.random()*19+0);
        equipoLocal.setText(arrayEquipos.get(local));
        arrayEquipos.remove(local);

        visitante = (int) (Math.random()*18+0);
        equipoVisitante.setText(arrayEquipos.get(visitante));
    }

    /**
     * Método encargado de activar y desactivar las etiquetas y/o botones pertinentes
     * al simular el partido
     **/
    private void configurarEtiquetasBotones() {
        btnVerPartido.setDisable(true);
        btnJugarPorra.setDisable(false);
        golesLocales.setDisable(false);
        golesVisitantes.setDisable(false);
        minutosPartido.setVisible(true);
    }

    /**
     * Método encargado de comprobar que se introduce un número
     * a la hora de pronosticar un marcador
     * @param cadena;
     * @return esNumero;
     **/
    private boolean comprobarNumero(String cadena){
        boolean esNumero;

        if (cadena.matches("-?([0-9]*)?")){
            esNumero = true;
        }else{
            esNumero = false;
        }

        return esNumero;
    }

    /**
     * Método encargado de gestionar la simulación de un partido
     **/
    @FXML
    private void simularPartido(ActionEvent evento) throws SQLException, IOException {
        int golesLocalesAleatorios;
        int golesVisitantesAleatorios;
        int idPorra;
        String linea;

        //Se comprueba que el usuario tiene 5€ o más de saldo para jugar. En caso contrario, se le avisará mediante alerta
        if (consultarSaldo(evento) >= 0 && consultarSaldo(evento) < 5) {
            mostrarAlertas(Alert.AlertType.WARNING, "¡Revise el saldo!", "Su saldo es inferior a 5€ por lo que no podrá jugar. Ingrese saldo en su cuenta por favor");
        } else {
            if (comprobarNumero(golesLocales.getText()) == false || comprobarNumero(golesVisitantes.getText()) == false) {
                mostrarAlertas(Alert.AlertType.WARNING, "¡Cuidado!", "Rellene el marcador con cararcteres numéricos");
            } else {
                //Si dispone de dinero para jugar, se procede a la simulación del partido
                btnJugarPorra.setDisable(true);
                btnVerPartido.setDisable(false);
                minutosPartido.setText("0'");

                //Se consulta el fichero para saber que usuario está jugando
                BufferedReader br = null;
                Connection con = establecerConexion();
                linea = consultarUsuario();

                //Se le actualiza el saldo por participación, se simulan los goles de ambos equipos y
                //se registra en la base de datos
                cobrarParticipacionPorra(evento, linea, con);
                golesLocalesAleatorios = (int) (Math.random() * 5 + 0);
                golesVisitantesAleatorios = (int) (Math.random() * 5 + 0);
                insertarPartidoBDAT(golesLocalesAleatorios, golesVisitantesAleatorios, con);

                ResultSet rsIdPorra = consultarIdPorra(con);
                if(rsIdPorra.next()){
                    idPorra = rsIdPorra.getInt(1);
                    insertaHistorico(idPorra, linea, con);

                    Alert alert = mostrarAlertaFinPartido();

                    //Se muestran los mensajes con el marcador final y nuestro pronóstico
                    //En caso de acertar, salta una alerta felicitando al usuario y se actualiza su sueldo
                    //En caso de no acertar, salta una alerta consolando al usuario pero no se actualiza el saldo,
                    //ya que su participación fue cobrada anteriormente
                    if (golesLocalesAleatorios == golesVisitantesAleatorios) {
                        alert.setContentText("Al final tenemos reparto de puntos entre " + equipoLocal.getText() + " y " + equipoVisitante.getText() + " (" + golesLocalesAleatorios + "-" + golesVisitantesAleatorios + "). \nSu pronóstico fue (" + Integer.parseInt(golesLocales.getText()) + "-" + Integer.parseInt(golesVisitantes.getText()) + ")");
                        alert.showAndWait();

                        if (golesLocalesAleatorios == Integer.parseInt(golesLocales.getText()) && golesVisitantesAleatorios == Integer.parseInt(golesVisitantes.getText())) {
                            annadirPremioUsuarioBDAT(evento, linea, con);
                            mostrarAlertas(Alert.AlertType.INFORMATION,"¡Enhorabuena!","¡Has acertado el resultado del partido. Los 100€ son tuyos! Tu nuevo saldo es de " + consultarSaldo(evento) + "€");
                        } else {
                            mostrarAlertas(Alert.AlertType.INFORMATION,"¡Lo sentimos!","Has fallado el resultado del partido. Te quedan " + consultarSaldo(evento) + "€ de saldo. ¡Suerte para la próxima!");
                        }
                    } else {
                        if (golesLocalesAleatorios > golesVisitantesAleatorios) {
                            alert.setContentText("Victoria local. " + equipoLocal.getText() + " se impone ante " + equipoVisitante.getText() + " (" + golesLocalesAleatorios + "-" + golesVisitantesAleatorios + "). \nSu pronóstico fue (" + Integer.parseInt(golesLocales.getText()) + "-" + Integer.parseInt(golesVisitantes.getText()) + ")");
                            alert.showAndWait();

                            if (golesLocalesAleatorios == Integer.parseInt(golesLocales.getText()) && golesVisitantesAleatorios == Integer.parseInt(golesVisitantes.getText())) {
                                annadirPremioUsuarioBDAT(evento, linea, con);
                                mostrarAlertas(Alert.AlertType.INFORMATION,"¡Enhorabuena!","¡Has acertado el resultado del partido. Los 100€ son tuyos! Tu nuevo saldo es de " + consultarSaldo(evento) + "€");
                            } else {
                                mostrarAlertas(Alert.AlertType.INFORMATION,"¡Lo sentimos!","Has fallado el resultado del partido. Te quedan " + consultarSaldo(evento) + "€ de saldo. ¡Suerte para la próxima!");
                            }
                        } else {
                            alert.setContentText("Victoria visitante. " + equipoLocal.getText() + " pierde en casa ante " + equipoVisitante.getText() + " (" + golesLocalesAleatorios + "-" + golesVisitantesAleatorios + "). \nSu pronóstico fue (" + Integer.parseInt(golesLocales.getText()) + "-" + Integer.parseInt(golesVisitantes.getText()) + ")");
                            alert.showAndWait();

                            if (golesLocalesAleatorios == Integer.parseInt(golesLocales.getText()) && golesVisitantesAleatorios == Integer.parseInt(golesVisitantes.getText())) {
                                annadirPremioUsuarioBDAT(evento, linea, con);
                                mostrarAlertas(Alert.AlertType.INFORMATION,"¡Enhorabuena!","¡Has acertado el resultado del partido. Los 100€ son tuyos! Tu nuevo saldo es de " + consultarSaldo(evento) + "€");
                            } else {
                                mostrarAlertas(Alert.AlertType.INFORMATION,"¡Lo sentimos!","Has fallado el resultado del partido. Te quedan " + consultarSaldo(evento) + "€ de saldo. ¡Suerte para la próxima!");
                            }
                        }
                        actualizarCamposFinPartido(golesLocalesAleatorios, golesVisitantesAleatorios);
                    }
                    saldoUsuario.setText("Saldo: "+consultarSaldo(evento)+"€");
                    //Se cierra la conexión al finalizar
                    con.close();
                }
            }
        }
    }

    /**
     * Método encargado de gestionar la conexión con la base de datos
     **/
    private Connection establecerConexion() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://iescristobaldemonroy.duckdns.org:" + PUERTO + "/" + NOMBD + "?useSSL=false", USUARIO, PASSWORD);
        return con;
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
     * Método encargado de restar 5€ al usuario por participar en una porra
     **/
    private void cobrarParticipacionPorra(ActionEvent evento, String linea, Connection con) throws SQLException, IOException {
        PreparedStatement psRestaSaldo = con.prepareStatement("UPDATE Usuario SET SALDO =" + consultarSaldo(evento) + " - 5 WHERE nomUsuario = ?");
        psRestaSaldo.setString(1, linea);
        psRestaSaldo.executeUpdate();
    }


    /**
     * Método encargado de insertar en la base de datos un partido disputado
     **/
    private void insertarPartidoBDAT(int golesLocalesAleatorios, int golesVisitantesAleatorios, Connection con) throws SQLException {
        PreparedStatement psRegistraPartido = con.prepareStatement("INSERT INTO Porra (bote,equipoLocal,equipoVisitante,pronosticoGolesLocales,pronosticoGolesVisitantes,golesLocal,golesVisitante) VALUES(?,?,?,?,?,?,?)");

        psRegistraPartido.setInt(1, 100);
        psRegistraPartido.setString(2, equipoLocal.getText());
        psRegistraPartido.setString(3, equipoVisitante.getText());
        psRegistraPartido.setInt(4, Integer.parseInt(golesLocales.getText()));
        psRegistraPartido.setInt(5, Integer.parseInt(golesVisitantes.getText()));
        psRegistraPartido.setInt(6, golesLocalesAleatorios);
        psRegistraPartido.setInt(7, golesVisitantesAleatorios);

        psRegistraPartido.executeUpdate();
    }


    /**
     * Método encargado de recuperar el id de la porra mas reciente en jugarse
     **/
    private ResultSet consultarIdPorra(Connection con) throws SQLException {
        PreparedStatement psIdPorra = con.prepareStatement("SELECT idPorra FROM `Porra` order by idPorra desc limit 1");
        ResultSet rsIdPorra = psIdPorra.executeQuery();
        return rsIdPorra;
    }


    /**
     * Método encargado de gestionar la simulación de un partido
     **/
    private void insertaHistorico(int idPorra, String linea, Connection con) throws SQLException {
        PreparedStatement psRegistraHistorico = con.prepareStatement("INSERT INTO UsuarioPorra (nomUsuario, idPorra) VALUES(?,?)");
        psRegistraHistorico.setString(1, linea);
        psRegistraHistorico.setInt(2, idPorra);
        psRegistraHistorico.executeUpdate();
    }

    /**
     * Método encargado de mostrar la alerta del fin del partido
     **/
    private Alert mostrarAlertaFinPartido() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("¡Final del partido!");
        return alert;
    }

    /**
     * Método encargado de añadir 100€ al usuario cuando acierte un partido
     **/
    private void annadirPremioUsuarioBDAT(ActionEvent evento, String linea, Connection con) throws SQLException, IOException {
        PreparedStatement psSumaSaldo = null;

        psSumaSaldo = con.prepareStatement("UPDATE Usuario SET SALDO =" + consultarSaldo(evento) + " + 100 WHERE nomUsuario = ?");
        psSumaSaldo.setString(1, linea);

        psSumaSaldo.executeUpdate();
    }

    /**
     * Método encargado de actualizar los marcadores al finalizar un partido
     **/
    private void actualizarCamposFinPartido(int golesLocalesAleatorios, int golesVisitantesAleatorios) {
        golesLocales.setText(String.valueOf(golesLocalesAleatorios));
        golesVisitantes.setText(String.valueOf(golesVisitantesAleatorios));
        golesLocales.setEditable(false);
        golesVisitantes.setEditable(false);
        minutosPartido.setText("90'");
    }
}
