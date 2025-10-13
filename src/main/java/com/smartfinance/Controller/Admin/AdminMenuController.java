package com.smartfinance.Controller.Admin;

import com.smartfinance.Models.Model;
import com.smartfinance.Views.AdminMenuOption;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminMenuController implements Initializable{
    @FXML
    public Button create_client;
    
    @FXML
    public Button client_btn;
    
    @FXML
    public Button report_btn;
    
    @FXML
    public Button logout_btn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
    }

    private void addListeners(){
        create_client.setOnAction(event -> onCreateClient());
        client_btn.setOnAction(event -> onClients());
        report_btn.setOnAction(event -> onReports());
        logout_btn.setOnAction(event -> onLogout());
    }

    private void onCreateClient(){
        Model.getInstance().getViewFactory().getAdminSelectedMenuItem().set(AdminMenuOption.CREATE_CLIENT);
    }

    private void onClients(){
        Model.getInstance().getViewFactory().getAdminSelectedMenuItem().set(AdminMenuOption.CLIENTS);
    }

    private void onReports(){
        Model.getInstance().getViewFactory().getAdminSelectedMenuItem().set(AdminMenuOption.REPORTS);
    }

    private void onLogout(){
        Model.getInstance().getViewFactory().getAdminSelectedMenuItem().set(AdminMenuOption.LOGOUT);
    }
}
