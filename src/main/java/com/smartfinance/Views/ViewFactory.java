package com.smartfinance.Views;

import com.smartfinance.Controller.Admin.AdminController;
import com.smartfinance.Controller.Client.ClientController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;



public class ViewFactory {
    private AccountType loginAccountType;
    //client views
    private final ObjectProperty<ClientMenuOptions> clientSelectedMenuItem ;
    private AnchorPane dashboardView;
    private AnchorPane transactionView;
    private AnchorPane investmentView;
    private AnchorPane budgetView;
    private AnchorPane accountView;
    private AnchorPane profileView;
    private AnchorPane reportView;
    private AnchorPane logoutView;

    //admin views
    private final ObjectProperty<AdminMenuOption> adminSelectedMenuItem ;
    private AnchorPane createClientView;
    private AnchorPane clientsView;
    private AnchorPane reportsView;
    private AnchorPane LogoutAdminView;



    public ViewFactory(){
        this.loginAccountType=AccountType.CLIENT;
        this.clientSelectedMenuItem =new SimpleObjectProperty<>();
        this.adminSelectedMenuItem=new SimpleObjectProperty<>();

    }
    public AccountType getLoginAccountType(){
        return loginAccountType;
    }

    public void setLoginAccountType(AccountType loginAccountType)
    {
    this.loginAccountType=loginAccountType;
    }

    public ObjectProperty<ClientMenuOptions> getClientSelectedMenuItem(){
        return clientSelectedMenuItem;
    }
    // src/main/java/com/example/wealthwise/Views/ViewFactory.java

    public AnchorPane getDashboardView(){
        if (dashboardView == null) {
            try {
                dashboardView = new FXMLLoader(
                        getClass().getResource("/Fxml/Client/Dashboard.fxml")
                ).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dashboardView;
    }

    public AnchorPane getTransactionView() {
        if (transactionView == null) {
            try {
                transactionView = new FXMLLoader(
                        getClass().getResource("/Fxml/Client/Transactions.fxml")
                ).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return transactionView;
    }

    public AnchorPane getInvestmentView() {
        if (investmentView == null) {
            try {
                investmentView = new FXMLLoader(
                        getClass().getResource("/Fxml/Client/Investment.fxml")
                ).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return investmentView;
    }
    public AnchorPane getBudgetView(){
        if (budgetView == null) {
            try {
                budgetView = new FXMLLoader(
                        getClass().getResource("/Fxml/Client/Budget.fxml")
                ).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return budgetView;

    }
    public AnchorPane getAccountView(){
        if (accountView == null) {
            try {
                accountView = new FXMLLoader(
                        getClass().getResource("/Fxml/Client/accounts.fxml")
                ).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return accountView;
    }
    public AnchorPane getProfileView(){
        if(profileView==null){
            try{
                profileView=new FXMLLoader(
                        getClass().getResource("/Fxml/Client/profile.fxml")
                ).load();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return profileView;
    }
    public AnchorPane getReportView(){
        if(reportView==null){
            try{
                reportView=new FXMLLoader(
                        getClass().getResource("/Fxml/Client/report.fxml")
                ).load();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return reportView;
    }
    public AnchorPane getlogoutView(){
        if(logoutView==null){
            try{
                logoutView=new FXMLLoader(
                        getClass().getResource("/Fxml/Login.fxml")
                ).load();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return logoutView;
    }




    public void showClientWindow(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Client/Client.fxml"));
        ClientController clientController = new ClientController();
        loader.setController(clientController);
        createStage(loader);
    }

// Admin Views Sections

    public ObjectProperty<AdminMenuOption> getAdminSelectedMenuItem()
    {
        return adminSelectedMenuItem;
    }
    public AnchorPane getCreateClientView() {
        if(createClientView==null){
            try{
                createClientView= new FXMLLoader(getClass().getResource("/Fxml/Admin/createclient.fxml")).load();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return createClientView;
    }

    public AnchorPane getClientsView(){
        if(clientsView==null){
            try{
                clientsView=new FXMLLoader(getClass().getResource("/Fxml/Admin/clients.fxml")).load();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return clientsView;
    }
    
    public AnchorPane getReportsView(){
        if(reportsView==null){
            try{
                reportsView=new FXMLLoader(getClass().getResource("/Fxml/Admin/reports.fxml")).load();
            }catch(Exception e){
                System.err.println("Error loading reports.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return reportsView;
    }
    
     public AnchorPane getLogoutAdminView(){
         if(LogoutAdminView==null){
             try{
                 LogoutAdminView=new FXMLLoader(getClass().getResource("/Fxml/Login.fxml")).load();
             }catch(Exception e){
                 e.printStackTrace();
             }
         }
         return LogoutAdminView;
     }
    public void showAdminWindow(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/Admin.fxml"));
        AdminController adminController = new AdminController();
        loader.setController(adminController);
        createStage(loader);


    }

    public void showloginWindow(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login.fxml"));
        createStage(loader);
    }


    private void createStage(FXMLLoader loader){
        Scene scene = null ;
        try{
            scene = new Scene(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/Images/logo_wealthwise-modified.png"))));
        stage.setTitle("WealthWise");
        stage.setResizable(false);

        stage.show();
    }

    public void closeStage(Stage stage){
        stage.close();

    }

}
