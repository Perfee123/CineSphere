package controllers.scheduler;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import models.ShowDAO;
import models.ShowTableItem;
import models.HallDAO;
import models.DiscountDAO;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import models.Movie;
import models.MovieDAO;

public class SchedulerOverviewController implements Initializable {

    @FXML private Label totalHallsLabel;
    @FXML private Label totalShowsLabel;
    @FXML private Label activeDiscountsLabel;
    @FXML private Label pendingMoviesLabel;
    @FXML private Label popularMovieLabel;
    
    @FXML private TableView<ShowTableItem> recentShowsTable;
    @FXML private TableColumn<ShowTableItem, String> movieColumn;
    @FXML private TableColumn<ShowTableItem, String> hallColumn;
    @FXML private TableColumn<ShowTableItem, String> timeColumn;
    @FXML private TableColumn<ShowTableItem, String> periodColumn;
    @FXML private TableColumn<ShowTableItem, String> seatsColumn;
    @FXML private TableColumn<ShowTableItem, String> statusColumn;

    private HallDAO hallDAO = new HallDAO();
    private ShowDAO showDAO = new ShowDAO();
    private DiscountDAO discountDAO = new DiscountDAO();
    private MovieDAO movieDAO = new MovieDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        movieColumn.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        hallColumn.setCellValueFactory(new PropertyValueFactory<>("hall"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("period"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("seats"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadData() {
        // Stats
        totalHallsLabel.setText(String.valueOf(hallDAO.getAllHalls().size()));
        
        List<ShowTableItem> todayShowsList = showDAO.getTodayShows();
        totalShowsLabel.setText(String.valueOf(todayShowsList.size()));
        
        long activeDiscounts = discountDAO.getAllDiscounts().stream().filter(d -> "ACTIVE".equals(d.getStatus())).count();
        activeDiscountsLabel.setText(String.valueOf(activeDiscounts));

        // Table (Currently Showing)
        List<ShowTableItem> shows = showDAO.getTodayShows();
        ObservableList<ShowTableItem> observableList = FXCollections.observableArrayList(shows);
        recentShowsTable.setItems(observableList);
        
        loadPendingMovies();
    }

    private void loadPendingMovies() {
        List<Movie> pendingMovies = movieDAO.getPendingMovies();
        pendingMoviesLabel.setText(String.valueOf(pendingMovies.size()));
        
        String popularMovie = movieDAO.getMostPopularMovieTitle();
        popularMovieLabel.setText(popularMovie);
    }
}
