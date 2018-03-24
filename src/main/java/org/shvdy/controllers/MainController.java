package org.shvdy.controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.shvdy.model.DaoFactory;
import org.shvdy.model.Dao;
import org.shvdy.model.OutdatedObjectException;
import org.shvdy.model.types.Department;
import org.shvdy.model.types.Employee;
import org.shvdy.model.types.ModelObject;
import org.shvdy.view.PopUpWithReturnStage;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class MainController {
    @FXML
    private AnchorPane rootPane;
    @FXML
    private BorderPane loadingScreenPane;
    @FXML
    private VBox loadingScreenItemsContainer;
    @FXML
    private BorderPane mainScreenPane;
    @FXML
    private Button createButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private TabPane tablesTabPane;
    @FXML
    private Tab employeesTab;
    @FXML
    private Tab departmentsTab;
    @FXML
    private TableView<Employee> empTable;
    @FXML
    private TableColumn<Employee, BigInteger> empColumnId;
    @FXML
    private TableColumn<Employee, Integer> empColumnNumber;
    @FXML
    private TableColumn<Employee, String> empColumnName;
    @FXML
    private TableColumn<Employee, String> empColumnJob;
    @FXML
    private TableColumn<Employee, Integer> empColumnManager;
    @FXML
    private TableColumn<Employee, String> empColumnHiredate;
    @FXML
    private TableColumn<Employee, Integer> empColumnSalary;
    @FXML
    private TableColumn<Employee, Integer> empColumnComission;
    @FXML
    private TableColumn<Employee, Integer> empColumnDepartmentNum;
    @FXML
    private TableColumn<Employee, BigInteger> empColumnLastModified;
    @FXML
    private TableView<Department> depTable;
    @FXML
    private TableColumn<Department, BigInteger> depColumnId;
    @FXML
    private TableColumn<Department, Integer> depColumnNumber;
    @FXML
    private TableColumn<Department, String> depColumnName;
    @FXML
    private TableColumn<Department, String> depColumnLocation;
    @FXML
    private TableColumn<Department, BigInteger> depColumnLastModified;
    @FXML
    private Label statusBarText;

    private Dao<Department> daoDep;
    private Dao<Employee> daoEmp;
    private ExecutorService taskExecutor;
    private Pattern validNumberPattern = Pattern.compile("\\d*");

    @FXML
    private void initialize() {
        loadingScreenPane.setVisible(true);
        bigIntColumnSetUp(empColumnId, "id");
        intColumnSetUp(empColumnNumber, "empno", true);
        textColumnSetUp(empColumnName, "ename", true);
        textColumnSetUp(empColumnJob, "job", true);
        intColumnSetUp(empColumnManager, "mgr", true);
        textColumnSetUp(empColumnHiredate, "hiredate", true);
        intColumnSetUp(empColumnSalary, "sal", true);
        intColumnSetUp(empColumnComission, "comm", true);
        intColumnSetUp(empColumnDepartmentNum, "deptno", true);
        bigIntColumnSetUp(empColumnLastModified, "lastModifiedDate");
        bigIntColumnSetUp(depColumnId, "id");
        intColumnSetUp(depColumnNumber, "deptno", true);
        textColumnSetUp(depColumnName, "dname", true);
        textColumnSetUp(depColumnLocation, "loc", true);
        bigIntColumnSetUp(depColumnLastModified, "lastModifiedDate");
        deleteButton.setDisable(true);
        searchButton.setDisable(true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (validNumberPattern.matcher(newValue).matches()) {
                if (tablesTabPane.getSelectionModel().getSelectedItem().equals(departmentsTab)) {
                    setMatchingItemsInTable(depTable, daoDep, newValue);
                } else if (tablesTabPane.getSelectionModel().getSelectedItem().equals(employeesTab)) {
                    setMatchingItemsInTable(empTable, daoEmp, newValue);
                }
                if (!newValue.isEmpty())
                    searchButton.setDisable(false);
                else
                    searchButton.setDisable(true);
            } else
                searchButton.setDisable(true);
        });
        taskExecutor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
    }

    private <T extends ModelObject, O> void columnSetUp(TableColumn<T, O> column, String parameterName) {
        column.setCellValueFactory(new PropertyValueFactory<>(parameterName));

        column.setOnEditCommit(t -> {
            T editedObject = t.getTableView().getItems().get(t.getTablePosition().getRow());
            editedObject.setParameterByName(parameterName, t.getNewValue());
            updateObject(editedObject);
        });
    }

    private <T extends ModelObject> void textColumnSetUp(TableColumn<T, String> column, String parameterName, boolean editable) {
        columnSetUp(column, parameterName);
        if (editable) {
            column.setCellFactory(TextFieldTableCell.forTableColumn());
        }
        column.setStyle("-fx-alignment: CENTER-LEFT;");
    }

    private <T extends ModelObject> void intColumnSetUp(TableColumn<T, Integer> column, String parameterName, boolean editable) {
        columnSetUp(column, parameterName);
        if (editable) {
            column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        }
        column.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    private <T extends ModelObject> void bigIntColumnSetUp(TableColumn<T, BigInteger> column, String parameterName) {
        columnSetUp(column, parameterName);
        column.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    public void initAfterSceneLoaded(DaoFactory.Implementations defaultDaoImpl) {
        ((Stage) rootPane.getScene().getWindow()).getIcons().add(new Image("/org/shvdy/icon.png"));
        DaoFactory daoFactory = DaoFactory.getDaoFactory(defaultDaoImpl);
        if (daoFactory == null) {
            showErrorAlert("Failed to access default data source: " + defaultDaoImpl);
            System.exit(1);
        }
        daoDep = daoFactory.getDao(Department::new);
        daoEmp = daoFactory.getDao(Employee::new);
        populateTables();

        rootPane.getScene().focusOwnerProperty().addListener((observable, oldValue, newValue) ->
                deleteButton.setDisable(!(newValue instanceof TableView) && !(newValue == deleteButton)));
    }

    private void populateTables() {
        Task<Void> setAll = new Task<Void>() {
            @Override
            public Void call() throws ExecutionException, InterruptedException {
                ObservableList<Employee> employees = FXCollections.observableArrayList();
                Future<List<Employee>> findEmployees = taskExecutor.submit(daoEmp::findByType);
                employees.setAll(findEmployees.get());
                empTable.setItems(employees);
                ObservableList<Department> departments = FXCollections.observableArrayList();
                Future<List<Department>> findDepartments = taskExecutor.submit(daoDep::findByType);
                departments.setAll(findDepartments.get());
                depTable.setItems(departments);
                return null;
            }
        };

        final long loadingStart = System.nanoTime();
        setAll.setOnSucceeded(e -> {
            FadeTransition loadingFadeOutEffect = new FadeTransition(Duration.millis(1000), loadingScreenPane);
            loadingFadeOutEffect.setFromValue(1.0);
            loadingFadeOutEffect.setToValue(0.1);
            loadingFadeOutEffect.setOnFinished(event -> loadingScreenPane.setVisible(false));
            FadeTransition progressFadeOutEffect = new FadeTransition(Duration.millis(400), loadingScreenItemsContainer);
            progressFadeOutEffect.setFromValue(1.0);
            progressFadeOutEffect.setToValue(0.1);

            loadingFadeOutEffect.play();
            progressFadeOutEffect.play();
            setStatusBarMessage("Loading time: " + ((System.nanoTime() - loadingStart) / 1000000000) + "s "
                    + (((System.nanoTime() - loadingStart) % 1000000000) / 1000000) + "ms");

        });
        setAll.setOnFailed(e -> {
            showErrorAlert(setAll.getException().toString());
            System.exit(1);
        });
        new Thread(setAll).start();
    }

    private <T extends ModelObject> void setMatchingItemsInTable(TableView<T> table, Dao<T> dao, String value) {
        ObservableList<T> matchingObjects = FXCollections.observableArrayList();
        Set<BigInteger> daoCacheEntries = new HashSet<>(dao.getCacheEntries());
        for (BigInteger id : daoCacheEntries) {
            if (id.toString().substring(0, value.length()).equals(value))
                matchingObjects.add(dao.getFromCache(id));
        }
        matchingObjects.sorted(Comparator.comparing(ModelObject::getId));
        table.setItems(matchingObjects);
    }

    private void setStatusBarMessage(String msg) {
        statusBarText.setText(msg);
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }

    private Class<? extends ModelObject> getActiveTab() {
        if (tablesTabPane.getSelectionModel().getSelectedItem().equals(departmentsTab)) {
            return Department.class;
        } else {
            return Employee.class;
        }
    }

    @FXML
    private void searchById(ActionEvent event) {
        try {
            switch (getActiveTab().getSimpleName()) {
                case "Employee": {
                    Employee e = daoEmp.findById(new BigInteger(searchField.getText()));
                    if (e != null) {
                        empTable.getItems().setAll(e);
                    }
                    break;
                }
                case "Department": {
                    Department d = daoDep.findById(new BigInteger(searchField.getText()));
                    if (d != null)
                        depTable.getItems().setAll(d);
                    break;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            showErrorAlert(e.toString());
        }
    }

    @FXML
    private void updateObject(ModelObject updatedObject) {
        try {
            long st = System.nanoTime();
            if (Employee.class.isInstance(updatedObject)) {
                daoEmp.update(((Employee) updatedObject));
                empTable.getItems().set(empTable.getItems().indexOf(updatedObject), (Employee) updatedObject);
            } else if (Department.class.isInstance(updatedObject)) {
                daoDep.update((Department) updatedObject);
                depTable.getItems().set(depTable.getItems().indexOf(updatedObject), (Department) updatedObject);
            }
            setStatusBarMessage("Updated record in " + ((System.nanoTime() - st) / 1000000) + "ms");
        } catch (SQLException | ClassNotFoundException | OutdatedObjectException e) {
            showErrorAlert(e.toString());
        }
    }

    private <T extends ModelObject> T getCreatedObject(ActionEvent e, FXMLLoader loader) {
        PopUpWithReturnStage<T, IPopUpWithReturnController<T>> newObjectDialog =
                new PopUpWithReturnStage<>(loader, e);
        return newObjectDialog.getDialogResult();
    }

    @FXML
    private void createObject(ActionEvent actionEvent) {
        FXMLLoader dialogLoader;
        try {
            switch (getActiveTab().getSimpleName()) {
                case "Employee": {
                    dialogLoader = new FXMLLoader(getClass().getResource("NewEmployeeDialog.fxml"));
                    Employee newEmployee = getCreatedObject(actionEvent, dialogLoader);
                    if (newEmployee != null) {
                        daoEmp.insert(newEmployee);
                        empTable.getItems().add(newEmployee);
                        empTable.getSelectionModel().select(newEmployee);

                    }
                    break;
                }
                case "Department": {
                    dialogLoader = new FXMLLoader(getClass().getResource("NewDepartmentDialog.fxml"));
                    Department newDepartment = getCreatedObject(actionEvent, dialogLoader);
                    if (newDepartment != null) {
                        daoDep.insert(newDepartment);
                        depTable.getItems().add(newDepartment);
                        depTable.getSelectionModel().select(newDepartment);
                    }
                    break;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            showErrorAlert(e.toString());
        }
    }

    @FXML
    private void deleteObject(ActionEvent actionEvent) {
        try {
            switch (getActiveTab().getSimpleName()) {
                case "Employee": {
                    Employee selectedEmp = empTable.getSelectionModel().getSelectedItem();
                    empTable.getItems().remove(selectedEmp);
                    daoEmp.delete(selectedEmp);
                    break;
                }
                case "Department": {
                    Department selectedDep = depTable.getSelectionModel().getSelectedItem();
                    depTable.getItems().remove(selectedDep);
                    daoDep.delete(selectedDep);
                    break;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            showErrorAlert(e.toString());
        }
    }
}
