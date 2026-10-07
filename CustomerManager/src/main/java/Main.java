import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    // ---- Validation rules (kept in one place so they are easy to change) ----
    // Letters, with single spaces, hyphens or apostrophes between them
    private static final String NAME_PATTERN = "\\p{L}+([ '\u2019\\-]\\p{L}+)*";
    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 50;
    private static final String PROVINCE_PROMPT = "Choose a province";

    // ---- Data store: the table is bound directly to this list ----
    private final ObservableList<Customer> customers = FXCollections.observableArrayList();

    // ---- Controls ----
    private final TextField nameField = new TextField();
    private final ComboBox<String> provinceBox = new ComboBox<>();
    private final Button saveButton = new Button("_Save customer");
    private final Button deleteButton = new Button("_Delete selected");
    private final Label statusLabel = new Label();
    private final TableView<Customer> table = new TableView<>();

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        // ---------- Menu bar: File -> Close ----------
        MenuItem closeItem = new MenuItem("_Close");
        closeItem.setMnemonicParsing(true);
        closeItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Q"));
        closeItem.setOnAction(e -> stage.close());

        Menu fileMenu = new Menu("_File");
        fileMenu.setMnemonicParsing(true);
        fileMenu.getItems().add(closeItem);

        MenuBar menuBar = new MenuBar(fileMenu);

        // ---------- Name field ----------
        Label nameLabel = new Label("Customer _name:");
        nameLabel.setMnemonicParsing(true);
        nameLabel.setLabelFor(nameField);          // Alt+N jumps to the field
        nameField.setPromptText("e.g., Mary Banda");

        // ---------- Province list ----------
        Label provinceLabel = new Label("_Province:");
        provinceLabel.setMnemonicParsing(true);
        provinceLabel.setLabelFor(provinceBox);    // Alt+P jumps to the list
        provinceBox.getItems().addAll(
                "Central", "Copperbelt", "Eastern", "Luapula", "Lusaka",
                "Muchinga", "Northern", "North-Western", "Southern", "Western");
        provinceBox.setPromptText(PROVINCE_PROMPT);
        provinceBox.setMaxWidth(Double.MAX_VALUE);

        // Keep the hint visible again after the box is reset to "nothing selected"
        provinceBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? PROVINCE_PROMPT : item);
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(provinceLabel, 0, 1);
        form.add(provinceBox, 1, 1);

        ColumnConstraints labelCol = new ColumnConstraints();
        ColumnConstraints inputCol = new ColumnConstraints();
        inputCol.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelCol, inputCol);

        // ---------- Buttons ----------
        saveButton.setMnemonicParsing(true);
        saveButton.setDefaultButton(true);         // Enter triggers Save
        saveButton.setOnAction(e -> saveCustomer());

        deleteButton.setMnemonicParsing(true);
        deleteButton.setOnAction(e -> deleteSelected());

        HBox buttons = new HBox(10, saveButton, deleteButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        // ---------- Status feedback ----------
        statusLabel.setAccessibleText("Status message");
        statusLabel.setWrapText(true);

        // ---------- Table ----------
        TableColumn<Customer, String> nameCol = new TableColumn<>("Customer name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));         // calls getName()

        TableColumn<Customer, String> provinceCol = new TableColumn<>("Province");
        provinceCol.setCellValueFactory(new PropertyValueFactory<>("province")); // calls getProvince()

        table.getColumns().add(nameCol);
        table.getColumns().add(provinceCol);
        table.setItems(customers);                 // bound directly to the ObservableList
        table.setPlaceholder(new Label("No customers yet."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ---------- Layout ----------
        // Child order = tab order: name -> province -> save -> delete -> table
        VBox content = new VBox(12, form, buttons, statusLabel, table);
        content.setPadding(new Insets(16));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(content);

        Scene scene = new Scene(root, 520, 480);

        // Deselect the row when focus leaves the table, except when it goes to
        // the Delete button (that button needs the selection to do its job)
        scene.focusOwnerProperty().addListener((obs, oldOwner, newOwner) -> {
            if (newOwner != null && newOwner != table && newOwner != deleteButton) {
                table.getSelectionModel().clearSelection();
            }
        });

        stage.setTitle("Customer Manager");
        stage.setScene(scene);
        stage.show();

        // Logical initial focus: start in the name field
        Platform.runLater(nameField::requestFocus);
    }

    // ---------- Validate input, then add the customer ----------
    private void saveCustomer() {
        // Clean the name: trim the ends and turn runs of spaces into one space
        String raw = nameField.getText() == null ? "" : nameField.getText();
        String name = raw.trim().replaceAll("\\s+", " ");

        String nameError = validateName(name);
        if (nameError != null) {
            statusLabel.setText(nameError);
            nameField.requestFocus();
            return;
        }

        String province = provinceBox.getValue();
        if (province == null) {
            statusLabel.setText("Choose a province.");
            provinceBox.requestFocus();
            return;
        }

        // All checks passed: add the customer, then clear the form
        customers.add(new Customer(name, province));
        statusLabel.setText("Customer saved.");
        nameField.clear();
        provinceBox.setValue(null);
        nameField.requestFocus();
    }

    /** Returns an error message for an invalid name, or null if the name is fine. */
    private String validateName(String name) {
        if (name.isEmpty()) {
            return "Enter the customer name.";
        }
        if (name.length() < NAME_MIN_LENGTH || name.length() > NAME_MAX_LENGTH) {
            return "The name must be " + NAME_MIN_LENGTH + " to "
                    + NAME_MAX_LENGTH + " characters long.";
        }
        if (!name.matches(NAME_PATTERN)) {
            return "Use letters only. Spaces, hyphens and apostrophes are allowed.";
        }
        return null;
    }

    // ---------- Confirm deletion of the selected customer ----------
    private void deleteSelected() {
        Customer selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a customer to delete.");
            table.requestFocus();
            return;
        }

        ButtonType delete = new ButtonType("Delete");
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + selected.getName() + " (" + selected.getProvince() + ")?",
                delete, ButtonType.CANCEL);
        confirm.setTitle("Confirm deletion");
        confirm.setHeaderText("Delete the selected customer?");
        confirm.initOwner(stage);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == delete) {
            customers.remove(selected);
            table.getSelectionModel().clearSelection();
            statusLabel.setText("Customer deleted.");
        } else {
            statusLabel.setText("Deletion cancelled.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}