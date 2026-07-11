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
    @FXML private TableColumn<ShowTableItem, String> colInfo;
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

        // Setup Info button cell
        colInfo.setCellFactory(tc -> new TableCell<ShowTableItem, String>() {
            private final Button btn = new Button();

            {
                SVGPath infoIcon = new SVGPath();
                infoIcon.setContent("M12,2A10,10,0,1,0,22,12,10,10,0,0,0,12,2Zm1,15H11V11h2Zm0-8H11V7h2Z");
                infoIcon.setFill(Color.web("#0d6efd"));
                
                btn.setGraphic(infoIcon);
                btn.getStyleClass().add("info-btn");
                btn.setOnAction(e -> {
                    ShowTableItem item = getTableView().getItems().get(getIndex());
                    showMovieInfoDialog(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(btn);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(box);
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

    private void showMovieInfoDialog(ShowTableItem item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Movie Info");
        dialog.setHeaderText("Details for: " + item.getMovieTitle());
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK);
        
        HBox content = new HBox(20);
        
        String posterUrl = null;
        try (java.sql.Connection conn = utils.DBUtils.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT poster_path, tmdb_id FROM movies WHERE title = ? LIMIT 1")) {
             stmt.setString(1, item.getMovieTitle());
             try (java.sql.ResultSet rs = stmt.executeQuery()) {
                 if (rs.next()) {
                     int tmdbId = rs.getInt("tmdb_id");
                     if (!rs.wasNull() && tmdbId > 0) {
                         models.MovieDTO dto = utils.TMDBUtils.getMovieDetails(tmdbId);
                         if (dto != null && dto.poster_path != null) {
                             posterUrl = utils.TMDBUtils.getImageUrl(dto.poster_path, "w500");
                         }
                     } else {
                         posterUrl = rs.getString("poster_path");
                     }
                 }
             }
        } catch(Exception ex) {}
        
        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView();
        if (posterUrl != null && !posterUrl.isEmpty()) {
            try {
                if(!posterUrl.startsWith("http")) posterUrl = "file:" + posterUrl; // handle local
                imgView.setImage(new javafx.scene.image.Image(posterUrl, 150, 225, true, true));
            } catch (Exception e) {}
        }
        
        javafx.scene.layout.VBox textContent = new javafx.scene.layout.VBox(10);
        textContent.getChildren().addAll(
            new Label("Show ID: " + item.getShowId()),
            new Label("Hall: " + item.getHall()),
            new Label("Time: " + item.getTime()),
            new Label("Seats Booked: " + item.getSeats()),
            new Label("Status: " + item.getStatus())
        );
        
        content.getChildren().addAll(imgView, textContent);
        dialogPane.setContent(content);
        dialog.showAndWait();
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
