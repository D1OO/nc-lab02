package org.shvdy.view;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class PopUpStage extends Stage {
    private Stage parentsWindow;
    private Parent parentsRootPane;

    private GaussianBlur blur = new GaussianBlur(0);
    private Light.Point light = new Light.Point(400, 230, 1000, Color.WHITE);
    private Lighting lighting = new Lighting();
    private Timeline fadeInTimeline = new Timeline();
    private Timeline fadeOutTimeline = new Timeline();
    private FadeTransition dialogFadeOutEffect;
    private FadeTransition dialogFadeInEffect;
    private static final int ANIMATION_TIME = 150;

    public PopUpStage(FXMLLoader loader, ActionEvent actionEvent) {
        try {
            this.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.toString());
            alert.setHeaderText("Critical error");
            alert.showAndWait();
            System.exit(1);
        }

        parentsWindow = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        parentsRootPane = ((Button) (actionEvent.getSource())).getScene().getRoot();
        this.initOwner(parentsWindow);
        this.initStyle(StageStyle.UNDECORATED);
        this.setResizable(false);
        this.sizeToScene();

        parentsWindow.xProperty().addListener((observable, oldValue, newValue) ->
                this.setX(newValue.doubleValue() + parentsWindow.getWidth() / 2 - this.getWidth() / 2));

        parentsWindow.yProperty().addListener((observable, oldValue, newValue) ->
                this.setY(newValue.doubleValue() + parentsWindow.getHeight() / 2 - this.getHeight() / 2));

        parentsWindow.widthProperty().addListener((observable, oldValue, newValue) ->
                this.setX(parentsWindow.getX() + parentsWindow.getWidth() / 2 - this.getWidth() / 2));

        parentsWindow.heightProperty().addListener((observable, oldValue, newValue) ->
                this.setY(parentsWindow.getY() + parentsWindow.getHeight() / 2 - this.getHeight() / 2));


        this.onShownProperty().set(event -> {
            this.setX(parentsWindow.getX() + parentsWindow.getWidth() / 2 - this.getWidth() / 2);
            this.setY(parentsWindow.getY() + parentsWindow.getHeight() / 2 - this.getHeight() / 2);
            parentsRootPane.setEffect(lighting);
            dialogFadeInEffect.play();
            fadeInTimeline.play();
        });
        this.setOnCloseRequest(event -> {
            event.consume();
            dialogFadeOutEffect.setOnFinished(e -> {
                this.close();
                parentsRootPane.setEffect(null);
            });
            dialogFadeOutEffect.play();
            fadeOutTimeline.play();
        });

        setUpEffects();
    }

    private void setUpEffects() {
        lighting.setLight(light);
        lighting.setContentInput(blur);
        blur.radiusProperty().addListener((observable, oldV, newV) ->
                blur.setRadius((double) newV));

        dialogFadeInEffect = new FadeTransition(Duration.millis(ANIMATION_TIME), this.getScene().getRoot());
        dialogFadeInEffect.setFromValue(0.0);
        dialogFadeInEffect.setToValue(1.0);

        dialogFadeOutEffect = new FadeTransition(Duration.millis(ANIMATION_TIME), this.getScene().getRoot());
        dialogFadeOutEffect.setFromValue(1.0);
        dialogFadeOutEffect.setToValue(0.0);

        final KeyValue blurGrowValue = new KeyValue(blur.radiusProperty(), 10);
        final KeyFrame blurGrowFrame = new KeyFrame(Duration.millis(ANIMATION_TIME), blurGrowValue);
        final KeyValue lightGrowValue = new KeyValue(light.zProperty(), 160);
        final KeyFrame lightGrowFrame = new KeyFrame(Duration.millis(ANIMATION_TIME), lightGrowValue);
        fadeInTimeline.getKeyFrames().addAll(blurGrowFrame, lightGrowFrame);

        final KeyValue blurOffValue = new KeyValue(blur.radiusProperty(), 0);
        final KeyFrame blurOffFrame = new KeyFrame(Duration.millis(ANIMATION_TIME), blurOffValue);
        final KeyValue lightOffValue = new KeyValue(light.zProperty(), 1000);
        final KeyFrame lightOffFrame = new KeyFrame(Duration.millis(ANIMATION_TIME), lightOffValue);
        fadeOutTimeline.getKeyFrames().addAll(blurOffFrame, lightOffFrame);

        fadeInTimeline.setOnFinished(event -> {
            parentsRootPane.getStylesheets().add(
                    getClass().getResource("disableSetDisableGreyOut.css").toExternalForm());
            parentsRootPane.setDisable(true);
        });

        fadeOutTimeline.setOnFinished(event -> {
            parentsRootPane.getStylesheets().remove(
                    getClass().getResource("disableSetDisableGreyOut.css").toExternalForm());
            parentsRootPane.setDisable(false);
        });
    }
}
