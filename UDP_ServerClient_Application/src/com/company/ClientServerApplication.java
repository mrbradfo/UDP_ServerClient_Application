package company;

import company.UDP_Client;
import company.UDP_Server;

import java.io.IOException;


import javafx.scene.layout.VBox;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
* Client Server Application
* This Java program implements UDP Server
* 
* This program allows the client to send and receive messages from the server. 
*
* @author  Matthew Bradford
* @version 1.0
* @since   12/11/2019 
*/

public class ClientServerApplication  extends Application {

	private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Client Server App");

        initRootLayout();

        ClientServerOverview();
    }
    
    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ClientServerApplication.class.getResource("RootBorderPane.fxml"));
            rootLayout = (BorderPane) loader.load();
            
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the person overview inside the root layout.
     */
    public void ClientServerOverview() {
        try {
            // Load Client Server overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ClientServerApplication.class.getResource("ClientServerOverview.fxml"));
            VBox ClientServerOverview = (VBox) loader.load();
            
            // Set person overview into the center of root layout.
            rootLayout.setCenter(ClientServerOverview);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }


}
