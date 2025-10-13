package com.smartfinance.Controller.Admin;
import com.smartfinance.Models.Model;
import com.smartfinance.Views.AdminMenuOption;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {
    public BorderPane admin_parent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Model.getInstance().getViewFactory().getAdminSelectedMenuItem().addListener((observable,oldval,newval) ->{
            if (newval == AdminMenuOption.LOGOUT) {
                Stage stage = (Stage) admin_parent.getScene().getWindow();
                Model.getInstance().getViewFactory().showloginWindow();
                stage.close();
            } else {
                switch (newval){
                    case CLIENTS -> admin_parent.setCenter(Model.getInstance().getViewFactory().getClientsView());
                    case CREATE_CLIENT -> admin_parent.setCenter(Model.getInstance().getViewFactory().getCreateClientView());
                    case REPORTS -> admin_parent.setCenter(Model.getInstance().getViewFactory().getReportsView());

                    default -> admin_parent.setCenter(Model.getInstance().getViewFactory().getCreateClientView());

                }
            }

        } );
    }


}
