package org.shvdy.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.shvdy.model.types.Department;

import java.util.regex.Pattern;

public class NewDepartmentPopUpController  implements IPopUpWithReturnController<Department> {
    @FXML
    private Button commitButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField numberTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField locationTextField;

    private Department newDepartment;
    private Pattern validNumberPattern = Pattern.compile("\\d*");
    private boolean cancelled = false;
    private boolean isNumberValid = false;
    private boolean isNameValid = false;
    private boolean isLocationValid = false;

    @FXML
    private void initialize() {
        numberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isNumberValid = !newValue.trim().isEmpty() && validNumberPattern.matcher(newValue).matches();
            commitButton.setDisable(!(isNumberValid && isNameValid && isLocationValid));
        });

        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isNameValid = !newValue.trim().isEmpty();
            commitButton.setDisable(!(isNumberValid && isNameValid && isLocationValid));
        });

        locationTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isLocationValid = !newValue.trim().isEmpty();
            commitButton.setDisable(!(isNumberValid && isNameValid && isLocationValid));
        });

        commitButton.setDisable(true);
    }

    @FXML
    private void validateInputData(ActionEvent actionEvent) {
        cancelled = false;
        newDepartment = new Department();
        newDepartment.setDeptno(Integer.parseInt(numberTextField.getText()));
        newDepartment.setDname(nameTextField.getText());
        newDepartment.setLoc(locationTextField.getText());
        Stage parentStage = ((Stage) ((Button) actionEvent.getSource()).getScene().getWindow());
        parentStage.fireEvent(new WindowEvent(parentStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        cancelled = true;
        Stage parentStage = ((Stage) ((Button) actionEvent.getSource()).getScene().getWindow());
        parentStage.fireEvent(new WindowEvent(parentStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public Department getDialogResult() {
        return cancelled ? null : newDepartment;
    }
}
