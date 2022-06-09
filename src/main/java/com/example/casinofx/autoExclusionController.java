package com.example.casinofx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.sql.*;

import static com.example.casinofx.utilidades.Constantes.*;

/**
 * Clase que controla la pestaña de autoexclusión
 */
public class autoExclusionController {
    @FXML
    private Button btnBorrarUsuario;

    /**
     * Método que se encarga de gestionar las alertas
     * @param warning,tituloAlerta,mensaje;
     */
    private void mostrarAlertas(Alert.AlertType warning, String tituloAlerta, String mensaje) {
        Parent root = null;

        Alert alert = new Alert(warning);
        alert.setHeaderText(null);
        alert.setTitle(tituloAlerta);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Método que se encarga de gestionar si un usuario decide
     * no borrarse del sistema y regresa al menú principal
     */
    @FXML
    private void noBorrar(ActionEvent evento){
        mostrarAlertas(Alert.AlertType.WARNING,"Autoexclusión","Has decidido mantenerse en el sistema. Volverá al menú principal en breves instantes");
        Stage stage = (Stage) this.btnBorrarUsuario.getScene().getWindow();
        stage.close();
    }

    /**
     * Método que se encarga de gestionar si un usuario decide borrarse del sistema
     */
    @FXML
    private void borrar(ActionEvent evento) throws SQLException, IOException {
        Parent root = null;
        String linea;

        mostrarAlertas(Alert.AlertType.CONFIRMATION,"Autoexclusión","Ha decidido borrarse del sistema. Desde CasinoFX apoyamos el juego responsable y respetamos su decisión");

        BufferedReader br = new BufferedReader(new FileReader(new File("controlSesiones.txt")));
        while ((linea = br.readLine()) != null) {
            borrarUserDelSistema(linea);
        }

        Platform.exit();
        System.exit(0);
    }

    /**
     * Método que gestiona el borrado del sistema del usuario
     * Inserta en la tabla correspondiente del usuario la
     * fecha del sistema actual, que será el filtro que detecta
     * si este esta o no borrado del sistema
     */
    private void borrarUserDelSistema(String linea) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:mysql://iescristobaldemonroy.duckdns.org:" + PUERTO + "/" + NOMBD + "?useSSL=false", USUARIO, PASSWORD);

        PreparedStatement psAutoExclusion = con.prepareStatement("UPDATE Usuario SET f_autoexclusion = CURTIME() WHERE nomUsuario = ?");
        psAutoExclusion.setString(1, linea);

        psAutoExclusion.executeUpdate();
    }
}
