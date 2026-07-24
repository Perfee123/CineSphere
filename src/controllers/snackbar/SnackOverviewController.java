package controllers.snackbar;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Snack;
import models.SnackDAO;
import models.SnackSale;
import models.SnackSaleDAO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class SnackOverviewController {

    @FXML private Label totalSnacksLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label totalSalesTodayLabel;
    @FXML private Label revenueTodayLabel;

    @FXML private TableView<Snack> lowStockTable;
    @FXML private TableColumn<Snack, Integer> colId;
    @FXML private TableColumn<Snack, String> colName;
    @FXML private TableColumn<Snack, String> colCategory;
    @FXML private TableColumn<Snack, BigDecimal> colPrice;
    @FXML private TableColumn<Snack, Integer> colQuantity;

    private SnackDAO snackDAO = new SnackDAO();
    private SnackSaleDAO snackSaleDAO = new SnackSaleDAO();

    @FXML
    public void initialize() {
        setupTable();
        loadDashboardData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    private void loadDashboardData() {
        List<Snack> allSnacks = snackDAO.getAllSnacks();
        
        int activeCount = 0;
        ObservableList<Snack> lowStockList = FXCollections.observableArrayList();
        
        for (Snack s : allSnacks) {
            if ("ACTIVE".equals(s.getStatus())) {
                activeCount++;
                if (s.getQuantity() < s.getMinStock()) {
                    lowStockList.add(s);
                }
            }
        }
        
        totalSnacksLabel.setText(String.valueOf(activeCount));
        lowStockLabel.setText(String.valueOf(lowStockList.size()));
        lowStockTable.setItems(lowStockList);
        
        // Load sales for today
        List<SnackSale> allSales = snackSaleDAO.getAllSales();
        int salesToday = 0;
        BigDecimal revenueToday = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();
        
        for (SnackSale sale : allSales) {
            if (sale.getSaleTime() != null) {
                LocalDate saleDate = sale.getSaleTime().toLocalDateTime().toLocalDate();
                if (saleDate.equals(today)) {
                    salesToday++;
                    if (sale.getTotalAmount() != null) {
                        revenueToday = revenueToday.add(sale.getTotalAmount());
                    }
                }
            }
        }
        
        totalSalesTodayLabel.setText(String.valueOf(salesToday));
        revenueTodayLabel.setText(String.format("%.2f", revenueToday));
    }
}
