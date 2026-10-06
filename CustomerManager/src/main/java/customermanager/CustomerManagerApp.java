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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CustomerManagerApp extends Application {

    // Letters, with single spaces, hyphens or apostrophes between them
    private static final String NAME_PATTERN = "\\p{L}+([ '\u2019\\-]\\p{L}+)*";
    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 50;

    private final TextField nameField = new TextField();
    private final ComboBox<String> provinceBox = new ComboBox<>();
    private final Button saveButton = new Button("Save customer");
    private final Button deleteButton = new Button("Delete selected");
    private final Label status = new Label();

    private final ObservableList<Customer> customers =
            FXCollections.observableArrayList();
    private final TableView<Customer> table = new TableView<>();

    @Override
    public void start(Stage stage) {
        // Name field
        Label nameLabel = new Label("Customer name");
        nameLabel.setLabelFor(nameField);
        nameField.setPromptText("e.g., Mary Banda");

        // Province list
        Label provinceLabel = new Label("Province");
        provinceLabel.setLabelFor(provinceBox);
        provinceBox.getItems().addAll(
                "Central", "Copperbelt", "Eastern", "Luapula", "Lusaka",
                "Muchinga", "Northern", "North-Western", "Southern", "Western");
        provinceBox.setPromptText("Choose a province");

        // Keep the hint visible again after the box is reset to "nothing selected"
        provinceBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Choose a province" : item);
            }
        });

        // Save button with name and province validation
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(event -> {
            // Clean the name: trim the ends and turn runs of spaces into one space
            String name = nameField.getText().trim().replaceAll("\\s+", " ");

            // Check 1: the name must contain some text
            if (name.isEmpty()) {
                status.setText("Enter the customer name.");
                nameField.requestFocus();
                return;
            }

            // Check 2: the name must be a sensible length
            if (name.length() < NAME_MIN_LENGTH || name.length() > NAME_MAX_LENGTH) {
                status.setText("The name must be " + NAME_MIN_LENGTH + " to "
                        + NAME_MAX_LENGTH + " characters long.");
                nameField.requestFocus();
                return;
            }

            // Check 3: the name may contain only letters, spaces, hyphens and apostrophes
            if (!name.matches(NAME_PATTERN)) {
                status.setText("Use letters only. Spaces, hyphens and apostrophes are allowed.");
                nameField.requestFocus();
                return;
            }

            // Check 4: a province must be selected
            String province = provinceBox.getValue();
            if (province == null) {
                status.setText("Choose a province.");
                provinceBox.requestFocus();
                return;
            }

            // All checks passed: add the customer, then clear the form
            customers.add(new Customer(name, province));
            status.setText("Customer saved.");
            nameField.clear();
            provinceBox.setValue(null);
            nameField.requestFocus();
        });

        // Delete button: a customer must be selected, then the user must confirm
        deleteButton.setOnAction(event -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                status.setText("Select a customer to delete.");
                table.requestFocus();
                return;
            }

            ButtonType delete = new ButtonType("Delete");
            Alert ask = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + selected.getName() + " (" + selected.getProvince() + ")?",
                    delete, ButtonType.CANCEL);
            ask.setTitle("Confirm deletion");
            ask.setHeaderText("Confirm deletion");

            if (ask.showAndWait().orElse(ButtonType.CANCEL) == delete) {
                customers.remove(selected);
                table.getSelectionModel().clearSelection();
                status.setText("Customer deleted.");
            } else {
                status.setText("Deletion cancelled.");
            }
        });

        // Table connected to the list, with name and province columns
        table.setItems(customers);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Customer name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        table.getColumns().add(nameCol);

        TableColumn<Customer, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setCellValueFactory(new PropertyValueFactory<>("province"));
        table.getColumns().add(provinceCol);

        // Layout
        HBox buttons = new HBox(10, saveButton, deleteButton);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(20));
        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(provinceLabel, 0, 1);
        form.add(provinceBox, 1, 1);
        form.add(buttons, 1, 2);
        form.add(status, 1, 3);
        form.add(table, 0, 4, 2, 1);

        Scene scene = new Scene(form, 480, 480);

        // Deselect the row when focus leaves the table, except when it goes to
        // the Delete button (that button needs the selection to do its job)
        scene.focusOwnerProperty().addListener((obs, oldOwner, newOwner) -> {
            if (newOwner != table && newOwner != deleteButton) {
                table.getSelectionModel().clearSelection();
            }
        });

        stage.setScene(scene);
        stage.setTitle("Customer Manager");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}