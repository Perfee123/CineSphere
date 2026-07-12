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

import javafx.scene.control.ComboBox;

public class AddMovieManuallyController {

    @FXML private TextField titleField;
    @FXML private TextField taglineField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private TextField durationField;
    @FXML private TextField ratingField;
    @FXML private TextField popularityField;
    @FXML private TextField releaseDateField;
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
    public void initialize() {
        genreComboBox.getItems().addAll(
            "Action", "Comedy", "Drama", "Sci-Fi", "Horror", "Romance", "Thriller", "Documentary", "Animation", "Family"
        );
        genreComboBox.getSelectionModel().selectFirst();
    }

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
        String tagline = taglineField.getText().trim();
        String genre = genreComboBox.getValue();
        String duration = durationField.getText().trim();
        String synopsis = synopsisArea.getText().trim();
        String ratingStr = ratingField.getText().trim();
        String popularityStr = popularityField.getText().trim();
        String releaseDate = releaseDateField.getText().trim();
        String adultPriceStr = adultPriceField.getText().trim();
        String kidsPriceStr = kidsPriceField.getText().trim();

        if (title.isEmpty() || genre == null || duration.isEmpty() || synopsis.isEmpty() ||
            ratingStr.isEmpty() || popularityStr.isEmpty() || releaseDate.isEmpty() ||
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
            double rating = Double.parseDouble(ratingStr);
            double popularity = Double.parseDouble(popularityStr);
            
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String showingFrom = showingFromPicker.getValue().format(dtf);
            String showingUntil = showingUntilPicker.getValue().format(dtf);

            Movie movie = new Movie("-1", title, genre, durationInt + " mins", synopsis, null);
            movie.setPosterPath(posterPath);
            movie.setBannerPath(bannerPath);
            movie.setRating(rating);
            movie.setPopularity(popularity);
            movie.setReleaseDate(releaseDate);
            movie.setTagline(tagline);
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
