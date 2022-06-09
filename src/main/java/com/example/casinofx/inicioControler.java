package com.example.casinofx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import static com.example.casinofx.utilidades.Constantes.RUTA_LOGO;

/**
 * Clase que contiene el método que carga la primera escena/ventana mostrada al ejecutar la aplicación
 */
public class inicioControler extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(inicioControler.class.getResource("inicioSesionView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 335, 335);
        stage.setTitle("Inicio Sesión | CasinoMonroyFX");
        Image ico = new Image(RUTA_LOGO);
        stage.getIcons().add(ico);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}