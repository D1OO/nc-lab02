package org.shvdy.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.shvdy.model.types.Employee;

import java.util.regex.Pattern;

public class NewEmployeePopUpController implements IPopUpWithReturnController<Employee> {
    @FXML
    private TextField numberTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField jobTextField;
    @FXML
    private TextField managerTextField;
    @FXML
    private TextField salaryTextField;
    @FXML
    private TextField hiredateTextField;
    @FXML
    private TextField comissionTextField;
    @FXML
    private TextField departmentNumTextField;
    @FXML
    private Button commitButton;
    @FXML
    private Button cancelButton;

    private Employee newEmployee;
    private Pattern validNumberPattern = Pattern.compile("\\d*");
    private boolean cancelled = false;
    private boolean isNumberValid = false;
    private boolean isNameValid = false;
    private boolean isJobValid = false;
    private boolean isManagerValid = false;
    private boolean isSalaryValid = false;
    private boolean isHiredateValid = false;
    private boolean isComissionValid = false;
    private boolean isDepartmentnumValid = false;

    @FXML
    private void initialize() {
        numberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isNumberValid = !newValue.trim().isEmpty() && validNumberPattern.matcher(newValue).matches();
            commitButton.setDisable(!(isNumberValid && isNameValid && isJobValid && isManagerValid
                    && isSalaryValid && isHiredateValid && isComissionValid && isDepartmentnumValid));
        });
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isNameValid = !newValue.trim().isEmpty();
            commitButton.setDisable(!(isNumberValid && isNameValid && isJobValid && isManagerValid
                    && isSalaryValid && isHiredateValid && isComissionValid && isDepartmentnumValid));
        });
        jobTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isJobValid = !newValue.trim().isEmpty();
            commitButton.setDisable(!(isNumberValid && isNameValid && isJobValid && isManagerValid
                    && isSalaryValid && isHiredateValid && isComissionValid && isDepartmentnumValid));
        });
        managerTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isManagerValid = !newValue.trim().isEmpty() && validNumberPattern.matcher(newValue).matches();
            commitButton.setDisable(!(isNumberValid && isNameValid && isJobValid && isManagerValid
                    && isSalaryValid && isHiredateValid && isComissionValid && isDepartmentnumValid));
        });
        salaryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isSalaryValid = !newValue.trim().isEmpty() && validNumberPattern.matcher(newValue).matches();
            commitButton.setDisable(!(isNumberValid && isNameValid && isJobValid && isManagerValid
                    && isSalaryValid && isHiredateValid && isComissionValid && isDepartmentnumValid));
        });
        hiredateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isHiredateValid = !newValue.trim().isEmpty();
            commitButton.setDisable(!(isNumberValid && isNameValid && isJobValid && isManagerValid
                    && isSalaryValid && isHiredateValid && isComissionValid && isDepartmentnumValid));
        });
        comissionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isComissionValid = !newValue.trim().isEmpty() && validNumberPattern.matcher(newValue).matches();
            commitButton.setDisable(!(isNumberValid && isNameValid && isJobValid && isManagerValid
                    && isSalaryValid && isHiredateValid && isComissionValid && isDepartmentnumValid));
        });
        departmentNumTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isDepartmentnumValid = !newValue.trim().isEmpty() && validNumberPattern.matcher(newValue).matches();
            commitButton.setDisable(!(isNumberValid && isNameValid && isJobValid && isManagerValid
                    && isSalaryValid && isHiredateValid && isComissionValid && isDepartmentnumValid));
        });

        commitButton.setDisable(true);
    }

    @FXML
    private void validateInputData(ActionEvent actionEvent) {
        cancelled = false;
        newEmployee = new Employee();
        newEmployee.setEmpno(Integer.parseInt(numberTextField.getText()));
        newEmployee.setEname(nameTextField.getText());
        newEmployee.setJob(jobTextField.getText());
        newEmployee.setMgr(Integer.parseInt(managerTextField.getText()));
        newEmployee.setSal(Integer.parseInt(salaryTextField.getText()));
        newEmployee.setHiredate(hiredateTextField.getText());
        newEmployee.setComm(Integer.parseInt(comissionTextField.getText()));
        newEmployee.setDeptno(Integer.parseInt(departmentNumTextField.getText()));

        Stage parentStage = ((Stage) ((Button) actionEvent.getSource()).getScene().getWindow());
        parentStage.fireEvent(new WindowEvent(parentStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void cancel(ActionEvent actionEvent) {
        cancelled = true;
        Stage parentStage = ((Stage) ((Button) actionEvent.getSource()).getScene().getWindow());
        parentStage.fireEvent(new WindowEvent(parentStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public Employee getDialogResult() {
        return cancelled ? null : newEmployee;
    }
}

