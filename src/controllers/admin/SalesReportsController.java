package controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.TransactionTableItem;
import utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SalesReportsController {

    @FXML private Label totalRevenueLabel;
    @FXML private Label ticketsSoldLabel;
    @FXML private Label avgTicketPriceLabel;
    @FXML private Label activeShowsLabel;

    @FXML private BarChart<String, Number> revenueChart;
    @FXML private PieChart audiencePieChart;
    @FXML private ComboBox<String> timeFilterCombo;

    @FXML private TableView<TransactionTableItem> transactionTable;
    @FXML private TableColumn<TransactionTableItem, String> colBookingId;
    @FXML private TableColumn<TransactionTableItem, String> colDateTime;
    @FXML private TableColumn<TransactionTableItem, String> colMovieTitle;
    @FXML private TableColumn<TransactionTableItem, Integer> colAdults;
    @FXML private TableColumn<TransactionTableItem, Integer> colKids;
    @FXML private TableColumn<TransactionTableItem, String> colSoldBy;
    @FXML private TableColumn<TransactionTableItem, String> colStatus;
    @FXML private TableColumn<TransactionTableItem, String> colTotalAmount;

    @FXML
    public void initialize() {
        if (timeFilterCombo != null) {
            timeFilterCombo.setItems(FXCollections.observableArrayList("Today", "This Week", "This Month", "All Time"));
            timeFilterCombo.getSelectionModel().selectFirst();
        }
        setupTable();
        loadAnalyticsData();
    }

    private void setupTable() {
        colBookingId.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty());
        colDateTime.setCellValueFactory(cellData -> cellData.getValue().dateTimeProperty());
        colMovieTitle.setCellValueFactory(cellData -> cellData.getValue().movieTitleProperty());
        colAdults.setCellValueFactory(cellData -> cellData.getValue().adultsProperty().asObject());
        colKids.setCellValueFactory(cellData -> cellData.getValue().kidsProperty().asObject());
        colSoldBy.setCellValueFactory(cellData -> cellData.getValue().soldByProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        colTotalAmount.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty());

        transactionTable.setRowFactory(tv -> new javafx.scene.control.TableRow<TransactionTableItem>() {
            @Override
            protected void updateItem(TransactionTableItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("table-row-checkedin", "table-row-cancelled");
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if ("CHECKED_IN".equals(item.getStatus())) {
                        getStyleClass().add("table-row-checkedin");
                    } else if ("CANCELLED".equals(item.getStatus())) {
                        getStyleClass().add("table-row-cancelled");
                    }
                }
            }
        });
    }

    private void loadAnalyticsData() {
        try (Connection conn = DBUtils.getConnection()) {
            loadStats(conn);
            loadChartData(conn);
            loadTransactionData(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStats(Connection conn) throws SQLException {
        double totalRev = 0;
        int totalTickets = 0;
        
        String sqlRev = "SELECT SUM(total_amount), SUM(adult_count + kids_count) FROM bookings WHERE status != 'CANCELLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sqlRev);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                totalRev = rs.getDouble(1);
                totalTickets = rs.getInt(2);
            }
        }
        
        totalRevenueLabel.setText(String.format("$%.2f", totalRev));
        ticketsSoldLabel.setText(String.valueOf(totalTickets));
        
        double avg = totalTickets > 0 ? (totalRev / totalTickets) : 0;
        avgTicketPriceLabel.setText(String.format("$%.2f", avg));

        String sqlShows = "SELECT COUNT(*) FROM shows WHERE status = 'SCHEDULED' OR status = 'RUNNING'";
        try (PreparedStatement stmt = conn.prepareStatement(sqlShows);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                activeShowsLabel.setText(String.valueOf(rs.getInt(1)));
            }
        }
    }

    private void loadChartData(Connection conn) throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue ($)");

        String sql = "SELECT m.title, SUM(b.total_amount) as revenue " +
                     "FROM bookings b " +
                     "JOIN shows s ON b.show_id = s.id " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "WHERE b.status != 'CANCELLED' " +
                     "GROUP BY m.title";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("title"), rs.getDouble("revenue")));
            }
        }

        revenueChart.getData().clear();
        revenueChart.getData().add(series);

        // Populate PieChart
        String sqlDemographics = "SELECT SUM(adult_count) as adults, SUM(kids_count) as kids FROM bookings WHERE status != 'CANCELLED'";
        try (PreparedStatement stmtDem = conn.prepareStatement(sqlDemographics);
             ResultSet rsDem = stmtDem.executeQuery()) {
            if (rsDem.next()) {
                int adults = rsDem.getInt("adults");
                int kids = rsDem.getInt("kids");
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Adults (" + adults + ")", adults),
                    new PieChart.Data("Kids (" + kids + ")", kids)
                );
                if (audiencePieChart != null) {
                    audiencePieChart.setData(pieChartData);
                }
            }
        }
    }

    private void loadTransactionData(Connection conn) throws SQLException {
        ObservableList<TransactionTableItem> transactions = FXCollections.observableArrayList();

        String sql = "SELECT b.id, b.booking_time, m.title as movie_title, b.adult_count, b.kids_count, " +
                     "u.full_name as sold_by, b.status, b.total_amount " +
                     "FROM bookings b " +
                     "JOIN shows s ON b.show_id = s.id " +
                     "JOIN movies m ON s.movie_id = m.id " +
                     "JOIN users u ON b.booked_by = u.id " +
                     "ORDER BY b.booking_time DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String bookingId = "BK-" + rs.getInt("id");
                String dateTime = rs.getString("booking_time");
                String movieTitle = rs.getString("movie_title");
                int adults = rs.getInt("adult_count");
                int kids = rs.getInt("kids_count");
                String soldBy = rs.getString("sold_by");
                String status = rs.getString("status");
                double totalAmount = rs.getDouble("total_amount");

                transactions.add(new TransactionTableItem(bookingId, dateTime, movieTitle, adults, kids, soldBy, status, totalAmount));
            }
        }

        transactionTable.setItems(transactions);
    }

    @FXML
    public void handleGenerateReport() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Sales Report");
        fileChooser.setInitialFileName("cinesphere_sales_report.csv");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        java.io.File file = fileChooser.showSaveDialog(totalRevenueLabel.getScene().getWindow());
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("Booking ID,Date & Time,Movie Title,Adults,Kids,Sold By,Status,Total Amount");
                for (TransactionTableItem item : transactionTable.getItems()) {
                    writer.printf("%s,%s,%s,%d,%d,%s,%s,%.2f%n",
                        item.getBookingId(), item.getDateTime(), item.getMovieTitle().replace(",", " "), 
                        item.getAdults(), item.getKids(), item.getSoldBy(), item.getStatus(), item.getTotalAmount());
                }
                System.out.println("Report generated at " + file.getAbsolutePath());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}
