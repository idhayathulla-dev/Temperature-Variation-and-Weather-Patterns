package ex;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class WeatherAnalyzerApp extends Application {

    private TableView<WeatherData> table;
    private LineChart<String, Number> chart;
    private Label avgTempLabel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("🌦 Temperature Variation and Weather Patterns");

        // Table setup
        table = new TableView<>();
        TableColumn<WeatherData, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> cell.getValue().dateProperty());

        TableColumn<WeatherData, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(cell -> cell.getValue().cityProperty());

        TableColumn<WeatherData, Number> minTempCol = new TableColumn<>("Min Temp (°C)");
        minTempCol.setCellValueFactory(cell -> cell.getValue().minTempProperty());

        TableColumn<WeatherData, Number> maxTempCol = new TableColumn<>("Max Temp (°C)");
        maxTempCol.setCellValueFactory(cell -> cell.getValue().maxTempProperty());

        TableColumn<WeatherData, Number> humidityCol = new TableColumn<>("Humidity (%)");
        humidityCol.setCellValueFactory(cell -> cell.getValue().humidityProperty());

        TableColumn<WeatherData, Number> windCol = new TableColumn<>("Wind Speed (km/h)");
        windCol.setCellValueFactory(cell -> cell.getValue().windSpeedProperty());

        TableColumn<WeatherData, String> condCol = new TableColumn<>("Condition");
        condCol.setCellValueFactory(cell -> cell.getValue().conditionProperty());

        table.getColumns().addAll(dateCol, cityCol, minTempCol, maxTempCol, humidityCol, windCol, condCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Chart setup
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Temperature Trends");
        chart.setCreateSymbols(true);
        chart.setLegendVisible(true);

        avgTempLabel = new Label("Average Temperature: -- °C");

        Button loadButton = new Button("📂 Load Weather CSV");
        loadButton.setOnAction(e -> loadCSV(stage));

        VBox layout = new VBox(15, loadButton, table, chart, avgTempLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void loadCSV(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Weather CSV File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        List<WeatherData> dataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {

                // skip header
                if (line.startsWith("Date")) continue;

                String[] values = line.split(",");

                // ensure proper format
                if (values.length < 7) continue;

                String date = values[0];
                String city = values[1];
                double minTemp = Double.parseDouble(values[2]);
                double maxTemp = Double.parseDouble(values[3]);
                double humidity = Double.parseDouble(values[4]);
                double windSpeed = Double.parseDouble(values[5]);
                String condition = values[6];

                dataList.add(new WeatherData(date, city, minTemp, maxTemp, humidity, windSpeed, condition));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error loading CSV", "Make sure your CSV has correct columns:\nDate,City,MinTemp,MaxTemp,Humidity,WindSpeed,Condition");
            return;
        }

        table.getItems().setAll(dataList);

        updateChart(dataList);
        updateStats(dataList);
    }

    private void updateChart(List<WeatherData> data) {
        chart.getData().clear();

        XYChart.Series<String, Number> minSeries = new XYChart.Series<>();
        minSeries.setName("Min Temp (°C)");

        XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Max Temp (°C)");

        for (WeatherData d : data) {
            minSeries.getData().add(new XYChart.Data<>(d.getDate(), d.getMinTemp()));
            maxSeries.getData().add(new XYChart.Data<>(d.getDate(), d.getMaxTemp()));
        }

        chart.getData().addAll(minSeries, maxSeries);
    }

    private void updateStats(List<WeatherData> data) {
        double avg = data.stream().mapToDouble(d -> (d.getMinTemp() + d.getMaxTemp()) / 2.0).average().orElse(0);
        avgTempLabel.setText(String.format("Average Temperature: %.2f °C", avg));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
