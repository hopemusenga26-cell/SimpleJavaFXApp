package customermanager;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CustomerManagerApp extends Application {

    // Letters, with single spaces, hyphens or apostrophes between them
    private static final String NAME_PATTERN = "\\p{L}+([ '’\\-]\\p{L}+)*";
    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 50;

    // Colours for the status message
    private static final String ERROR_COLOUR = "#b3261e";
    private static final String SUCCESS_COLOUR = "#198a00";
    private static final String INFO_COLOUR = "#555555";

    private final TextField nameField = new TextField();
    private final ComboBox<String> provinceBox = new ComboBox<>();
    private final Button saveButton = new Button("Save customer");
    private final Button deleteButton = new Button("Delete selected");
    private final Label status = new Label();
    private final Label countLabel = new Label();

    private final ObservableList<Customer> customers =
            FXCollections.observableArrayList();
    private final TableView<Customer> table = new TableView<>();

    @Override
    public void start(Stage stage) {
        // Header banner
        Label title = new Label("Customer Manager");
        title.getStyleClass().add("title");
        Label subtitle = new Label("ICT261 lab  |  Musenga Hope Sichula");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(4, title, subtitle);
        header.getStyleClass().add("header");

        // Name field
        Label nameLabel = new Label("Customer name");
        nameLabel.getStyleClass().add("form-label");
        nameLabel.setLabelFor(nameField);
        nameField.setPromptText("e.g., Mary Banda");
        nameField.setMaxWidth(Double.MAX_VALUE);

        // Province list
        Label provinceLabel = new Label("Province");
        provinceLabel.getStyleClass().add("form-label");
        provinceLabel.setLabelFor(provinceBox);
        provinceBox.getItems().addAll(
                "Central", "Copperbelt", "Eastern", "Luapula", "Lusaka",
                "Muchinga", "Northern", "North-Western", "Southern", "Western");
        provinceBox.setPromptText("Choose a province");
        provinceBox.setMaxWidth(Double.MAX_VALUE);

        // Keep the hint visible again after the box is reset to "nothing selected"
        provinceBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Choose a province" : item);
            }
        });

        // Save button with name and province validation
        saveButton.getStyleClass().add("save-button");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(event -> {
            // Clean the name: trim the ends and turn runs of spaces into one space
            String name = nameField.getText().trim().replaceAll("\\s+", " ");

            // Check 1: the name must contain some text
            if (name.isEmpty()) {
                showMessage("Enter the customer name.", ERROR_COLOUR);
                nameField.requestFocus();
                return;
            }

            // Check 2: the name must be a sensible length
            if (name.length() < NAME_MIN_LENGTH || name.length() > NAME_MAX_LENGTH) {
                showMessage("The name must be " + NAME_MIN_LENGTH + " to "
                        + NAME_MAX_LENGTH + " characters long.", ERROR_COLOUR);
                nameField.requestFocus();
                return;
            }

            // Check 3: the name may contain only letters, spaces, hyphens and apostrophes
            if (!name.matches(NAME_PATTERN)) {
                showMessage("Use letters only. Spaces, hyphens and apostrophes are allowed.",
                        ERROR_COLOUR);
                nameField.requestFocus();
                return;
            }

            // Check 4: a province must be selected
            String province = provinceBox.getValue();
            if (province == null) {
                showMessage("Choose a province.", ERROR_COLOUR);
                provinceBox.requestFocus();
                return;
            }

            // All checks passed: add the customer, then clear the form
            customers.add(new Customer(name, province));
            showMessage("Customer saved.", SUCCESS_COLOUR);
            updateCount();
            nameField.clear();
            provinceBox.setValue(null);
            nameField.requestFocus();
        });

        // Delete button: a customer must be selected, then the user must confirm
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showMessage("Select a customer to delete.", ERROR_COLOUR);
                table.requestFocus();
                return;
            }

            ButtonType delete = new ButtonType("Delete");
            Alert ask = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + selected.getName() + " (" + selected.getProvince() + ")?",
                    delete, ButtonType.CANCEL);
            ask.setTitle("Confirm deletion");
            ask.setHeaderText("Confirm deletion");
            ask.initOwner(stage);

            if (ask.showAndWait().orElse(ButtonType.CANCEL) == delete) {
                customers.remove(selected);
                table.getSelectionModel().clearSelection();
                showMessage("Customer deleted.", SUCCESS_COLOUR);
                updateCount();
            } else {
                showMessage("Deletion cancelled.", INFO_COLOUR);
            }
        });

        // Table connected to the list, with name and province columns
        table.setItems(customers);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No customers yet. Add one above."));

        TableColumn<Customer, String> nameCol = new TableColumn<>("Customer name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        table.getColumns().add(nameCol);

        TableColumn<Customer, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setCellValueFactory(new PropertyValueFactory<>("province"));
        table.getColumns().add(provinceCol);

        // Form layout inside a white card
        HBox buttons = new HBox(10, saveButton, deleteButton);
        status.setWrapText(true);

        GridPane form = new GridPane();
        form.getStyleClass().add("card");
        form.setHgap(12);
        form.setVgap(12);
        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(provinceLabel, 0, 1);
        form.add(provinceBox, 1, 1);
        form.add(buttons, 1, 2);
        form.add(status, 1, 3);

        // The second column (the fields) stretches when the window gets wider
        ColumnConstraints labelColumn = new ColumnConstraints();
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelColumn, fieldColumn);

        // Form on top, table in the middle, customer count at the bottom
        countLabel.getStyleClass().add("count-label");
        VBox content = new VBox(15, form, table, countLabel);
        content.setPadding(new Insets(20));

        // The table takes all the extra height when the window gets taller
        VBox.setVgrow(table, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(content);

        Scene scene = new Scene(root, 560, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Deselect the row when focus leaves the table, except when it goes to
        // the Delete button (that button needs the selection to do its job)
        scene.focusOwnerProperty().addListener((obs, oldOwner, newOwner) -> {
            if (newOwner != table && newOwner != deleteButton) {
                table.getSelectionModel().clearSelection();
            }
        });

        updateCount();

        stage.setScene(scene);
        stage.setTitle("Customer Manager");
        stage.setMinWidth(440);
        stage.setMinHeight(480);
        stage.show();
    }

    // Shows a message under the buttons in the given colour
    private void showMessage(String text, String colour) {
        status.setText(text);
        status.setStyle("-fx-text-fill: " + colour + "; -fx-font-weight: bold;");
    }

    // Shows how many customers are in the list
    private void updateCount() {
        countLabel.setText("Total customers: " + customers.size());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
