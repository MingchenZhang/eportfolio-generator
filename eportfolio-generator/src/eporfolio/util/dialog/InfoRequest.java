package eporfolio.util.dialog;

import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;

/**
 * This class provide function to prompt dialog for user input
 *
 * @author Mingchen Zhang
 */
public class InfoRequest {

	/**
	 * this method will prompt multiple buttons for user to choose. user could
	 * cancel the dialog, which will result in the return null.
	 *
	 * @param title title of the prompt
	 * @param header header of the prompt
	 * @param body body of the prompt
	 * @param selections an String array contain all the possible selection
	 * @param cancel string for cancel button
	 * @return user's selection
	 */
	public static String ButtonSelection(String title, String header, String body, String[] selections, String cancel) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(body);

		ButtonType[] selectButtons = new ButtonType[selections.length + 1];

		for (int i = 0; i < selections.length; i++) {
			selectButtons[i] = new ButtonType(selections[i]);
		}
		selectButtons[selectButtons.length - 1] = new ButtonType(cancel, ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(selectButtons);
		
		//set style //todo:set style
		//alert.getDialogPane().getStylesheets().add(STYLE_SHEET_DEFAULT);
		//alert.getDialogPane().getStyleClass().add(CSS_CLASS_DEFAULT);

		Optional<ButtonType> result = alert.showAndWait();
		for (int i = 0; i < selectButtons.length - 1; i++) {
			if (result.get() == selectButtons[i]) {
				return selections[i];
			}
		}
		return null;
	}

	/**
	 * prompt a dialog to acquire info from user
	 *
	 * @param title the title of the dialog
	 * @param header the header of the header
	 * @param questions a String array that store all the questions that will be
	 * presented to user
	 * @param ok ok string
	 * @param cancel cancel string
	 * @return a String array that store all the answers to the questions given
	 * respectively. return null if user cancel the dialog.
	 */
	public static String[] questionDialog(String title, String header, String[] questions, String ok, String cancel) {
		Dialog<String[]> dialog = new Dialog<>();
		dialog.setTitle(title);
		dialog.setHeaderText(header);

		ButtonType okButtonType = new ButtonType(ok, ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType(cancel, ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes()
				.addAll(okButtonType, cancelButtonType);

		GridPane grid = new GridPane();
		grid.setVgap(10);
		grid.setHgap(10);

		TextField questionTextFields[] = new TextField[questions.length];
		for (int i = 0; i < questions.length; i++) {
			questionTextFields[i] = new TextField();
			questionTextFields[i].setPromptText(questions[i]);
			
			grid.add(new Label(questions[i] + ":"), 0, i);
			grid.add(questionTextFields[i], 1, i);
		}

		dialog.getDialogPane().setContent(grid);
		Platform.runLater(() -> questionTextFields[0].requestFocus());

		String answer[] = new String[questions.length];
		dialog.setResultConverter(pressedButtonType -> {
			if (pressedButtonType == okButtonType) {
				for (int i = 0; i < questions.length; i++) {
					answer[i] = questionTextFields[i].getText();
				}
				return answer;
			} else {
				return null;
			}
		});
		
		//set style //todo:set style
		//dialog.getDialogPane().getStylesheets().add(STYLE_SHEET_DEFAULT);
		//dialog.getDialogPane().getStyleClass().add(CSS_CLASS_DEFAULT);
		
		Optional<String[]> optionalResult = dialog.showAndWait();
		if (optionalResult.isPresent()) {
			return optionalResult.get();
		} else {
			return null;
		}
	}

	/**
	 * prompt a dialog asking for a selection
	 *
	 * @param title dialog title
	 * @param header dialog header
	 * @param question question
	 * @param selection an String array that contain all the possible selection
	 * @return the user's selection
	 */
	public static String selectionDialog(String title, String header,
			String question, String[] selection) {
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle(title);
		dialog.setHeaderText(header);

		//ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes()
				.addAll(ButtonType.OK, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setVgap(10);
		grid.setHgap(10);

		ComboBox selector = new ComboBox();
		selector.setEditable(false);
		ObservableList<String> nameList = FXCollections.observableArrayList();
		for (int i = 0; i < selection.length; i++) {
			nameList.add(selection[i]);
		}
		selector.setItems(nameList);
		selector.setVisibleRowCount(selector.getItems().size());
		selector.getSelectionModel().select(0);

		grid.add(new Label(question + ":"), 0, 0);
		grid.add(selector, 1, 0);

		dialog.getDialogPane().setContent(grid);
		Platform.runLater(() -> selector.requestFocus());

		dialog.setResultConverter(pressedButtonType -> {
			if (pressedButtonType == ButtonType.OK) {
				return selector.getValue().toString();
			} else {
				return null;
			}
		});
		
		//set style //todo:set style
		//dialog.getDialogPane().getStylesheets().add(STYLE_SHEET_DEFAULT);
		//dialog.getDialogPane().getStyleClass().add(CSS_CLASS_DEFAULT);
		
		Optional<String> optionalResult = dialog.showAndWait();
		if (optionalResult.isPresent()) {
			return optionalResult.get();
		} else {
			return null;
		}
	}
}
