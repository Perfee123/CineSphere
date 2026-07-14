package controllers.ticket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.ShowTableItem;

import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;

public class TicketOverviewController {

    @FXML private TextField searchField;
    @FXML private TableView<ShowTableItem> showsTable;
    @FXML private TableColumn<ShowTableItem, String> colShowId;
    @FXML private TableColumn<ShowTableItem, String> colMovieTitle;
    @FXML private TableColumn<ShowTableItem, String> colHall;
    @FXML private TableColumn<ShowTableItem, String> colTime;
    @FXML private TableColumn<ShowTableItem, String> colSeats;
    @FXML private TableColumn<ShowTableItem, String> colStatus;
    
    @FXML private Label statActiveMovies;
    @FXML private Label statTodayShows;
    @FXML private Label statTotalBookings;
    @FXML private Label statTicketsSold;

    @FXML
    public void initialize() {
        // Setup columns
        colShowId.setCellValueFactory(new PropertyValueFactory<>("showId"));
        colMovieTitle.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        colHall.setCellValueFactory(new PropertyValueFactory<>("hall"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("seats"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup custom row coloring based on Status
        showsTable.setRowFactory(tv -> new TableRow<ShowTableItem>() {
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

        // Load dummy data
        loadDummyData();
    }

    private void loadDummyData() {
        models.ShowDAO showDao = new models.ShowDAO();
        ObservableList<ShowTableItem> data = FXCollections.observableArrayList(showDao.getTodayShows());
        
        // Compute Dashboard Stats
        int activeMovies = new models.MovieDAO().getActiveMovies().size();
        statActiveMovies.setText(String.valueOf(activeMovies));
        
        statTodayShows.setText(String.valueOf(data.size()));
        
        int bookingsToday = 0;
        int ticketsToday = 0;
        String todayDate = java.time.LocalDate.now().toString(); // format: yyyy-MM-dd
        for (models.BookingTableItem b : new models.BookingDAO().getAllBookings()) {
            if (b.getDate().startsWith(todayDate) && ("CONFIRMED".equals(b.getStatus()) || "CHECKED IN".equals(b.getStatus()))) {
                bookingsToday++;
                ticketsToday += b.getTickets();
            }
        }
        statTotalBookings.setText(String.valueOf(bookingsToday));
        statTicketsSold.setText(String.valueOf(ticketsToday));
        
        javafx.collections.transformation.FilteredList<ShowTableItem> filteredData = new javafx.collections.transformation.FilteredList<>(data, p -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(show -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String lowerCaseFilter = newValue.toLowerCase();
                    return show.getMovieTitle().toLowerCase().contains(lowerCaseFilter) ||
                           show.getHall().toLowerCase().contains(lowerCaseFilter) ||
                           show.getShowId().toLowerCase().contains(lowerCaseFilter);
                });
            });
        }
        showsTable.setItems(filteredData);
    }



    @FXML
    public void handleNewBooking() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ticket/BookingTicket.fxml"));
            Parent root = loader.load();
            StackPane contentArea = (StackPane) showsTable.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
