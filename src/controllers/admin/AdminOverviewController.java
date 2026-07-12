package controllers.admin;

import controllers.MainLayoutController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import models.ShowDAO;
import models.ShowTableItem;
import utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AdminOverviewController {

    @FXML private Label activeMoviesLabel;
    @FXML private Label totalStaffLabel;
    @FXML private Label todaysBookingsLabel;
    @FXML private Label todaysRevenueLabel;

    @FXML private TableView<ShowTableItem> activeShowsTable;
    @FXML private TableColumn<ShowTableItem, String> colShowId;
    @FXML private TableColumn<ShowTableItem, String> colMovieTitle;
    @FXML private TableColumn<ShowTableItem, String> colHall;
    @FXML private TableColumn<ShowTableItem, String> colTime;
    @FXML private TableColumn<ShowTableItem, String> colSeats;
    @FXML private TableColumn<ShowTableItem, String> colStatus;

    private ShowDAO showDAO = new ShowDAO();

    @FXML
    public void initialize() {
        setupTable();
        loadDashboardData();
    }

    private void setupTable() {
        colShowId.setCellValueFactory(cellData -> cellData.getValue().showIdProperty());
        colMovieTitle.setCellValueFactory(cellData -> cellData.getValue().movieTitleProperty());
        colHall.setCellValueFactory(cellData -> cellData.getValue().hallProperty());
        colTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        colSeats.setCellValueFactory(cellData -> cellData.getValue().seatsProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Optional row styling for status
        activeShowsTable.setRowFactory(tv -> new TableRow<ShowTableItem>() {
            @Override
            protected void updateItem(ShowTableItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("table-row-available", "table-row-booked");
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if ("Available".equalsIgnoreCase(item.getStatus())) {
                        getStyleClass().add("table-row-available");
                    } else if ("Fully Booked".equalsIgnoreCase(item.getStatus())) {
                        getStyleClass().add("table-row-booked");
                    }
                }
            }
        });
    }

    private void loadDashboardData() {
        // Load table data
        List<ShowTableItem> shows = showDAO.getUpcomingShows();
        ObservableList<ShowTableItem> observableShows = FXCollections.observableArrayList(shows);
        activeShowsTable.setItems(observableShows);

        // Load stat counts
        try (Connection conn = DBUtils.getConnection()) {
            // Active Movies
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM movies WHERE status = 'ACTIVE'")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) activeMoviesLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Total Staff
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) totalStaffLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Today's Bookings
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM bookings WHERE DATE(booking_time) = CURDATE()")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) todaysBookingsLabel.setText(String.valueOf(rs.getInt(1)));
            }

            // Today's Revenue
            try (PreparedStatement stmt = conn.prepareStatement("SELECT SUM(total_amount) FROM bookings WHERE DATE(booking_time) = CURDATE() AND status != 'CANCELLED'")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double rev = rs.getDouble(1);
                    todaysRevenueLabel.setText(String.format("$%.2f", rev));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            activeMoviesLabel.setText("0");
            totalStaffLabel.setText("0");
            todaysBookingsLabel.setText("0");
            todaysRevenueLabel.setText("$0.00");
        }
    }

    @FXML
    public void handleAddNewMovie() {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().navigateByTitle("Movie Management");
        }
    }
}
