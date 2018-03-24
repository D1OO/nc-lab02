package org.shvdy.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import org.shvdy.controllers.IPopUpWithReturnController;
import org.shvdy.view.PopUpStage;

public class PopUpWithReturnStage<T, C extends IPopUpWithReturnController<T>> extends PopUpStage {
    private C newObjectDialogController;

    public PopUpWithReturnStage(FXMLLoader loader, ActionEvent actionEvent) {
        super(loader, actionEvent);
        newObjectDialogController = loader.getController();
    }

    public T getDialogResult() {
        this.showAndWait();
        return newObjectDialogController.getDialogResult();
    }
}
