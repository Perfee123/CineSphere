package controllers.admin;

import controllers.MainLayoutController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import models.Movie;
import models.MovieDAO;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class AddMovieManuallyController {

    @FXML private TextField titleField;
    @FXML private TextField genreField;
    @FXML private TextField durationField;
    @FXML private TextArea synopsisArea;
    
    @FXML private Label posterLabel;
    @FXML private Label bannerLabel;
    
    @FXML private TextField adultPriceField;
    @FXML private TextField kidsPriceField;
    @FXML private DatePicker showingFromPicker;
    @FXML private DatePicker showingUntilPicker;

    private String posterPath = "";
    private String bannerPath = "";

    private MovieDAO movieDAO = new MovieDAO();

    @FXML
    public void handleBack() {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().loadPageDirectly("/views/admin/MovieManagement.fxml");
        }
    }

    @FXML
    public void handleChoosePoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Poster Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(titleField.getScene().getWindow());
        if (selectedFile != null) {
            posterPath = selectedFile.toURI().toString();
            posterLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    public void handleChooseBanner() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cinematic Banner Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(titleField.getScene().getWindow());
        if (selectedFile != null) {
            bannerPath = selectedFile.toURI().toString();
            bannerLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    public void handleSave() {
        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();
        String duration = durationField.getText().trim();
        String synopsis = synopsisArea.getText().trim();
        String adultPriceStr = adultPriceField.getText().trim();
        String kidsPriceStr = kidsPriceField.getText().trim();

        if (title.isEmpty() || genre.isEmpty() || duration.isEmpty() || synopsis.isEmpty() ||
            adultPriceStr.isEmpty() || kidsPriceStr.isEmpty() || showingFromPicker.getValue() == null ||
            showingUntilPicker.getValue() == null || posterPath.isEmpty() || bannerPath.isEmpty()) {
            
            Alert alert = new Alert(Alert.AlertType.ERROR, "All fields are required. Please fill in all fields and select images.");
            alert.showAndWait();
            return;
        }

        try {
            int durationInt = Integer.parseInt(duration);
            double adultPrice = Double.parseDouble(adultPriceStr);
            double kidsPrice = Double.parseDouble(kidsPriceStr);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String showingFrom = showingFromPicker.getValue().format(formatter);
            String showingUntil = showingUntilPicker.getValue().format(formatter);
            
            Movie movie = new Movie("", title, genre, durationInt + " mins", synopsis, null);
            movie.setPosterPath(posterPath);
            movie.setBannerPath(bannerPath);
            movie.setAdultPrice(adultPrice);
            movie.setKidsPrice(kidsPrice);
            movie.setShowingFrom(showingFrom);
            movie.setShowingUntil(showingUntil);
            
            boolean success = movieDAO.addManualMovie(movie);
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Movie successfully added to the theater!");
                alert.showAndWait();
                handleBack(); // Navigate back on success
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save movie to database.");
                alert.showAndWait();
            }

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter valid numeric values for Duration and Prices.");
            alert.showAndWait();
        }
    }
}
