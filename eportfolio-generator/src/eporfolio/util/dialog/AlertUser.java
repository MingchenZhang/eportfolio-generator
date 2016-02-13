package eporfolio.util.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

/**
 * This class provide a dialog for alerting user
 * 
 * @author Mingchen Zhang
 */
public class AlertUser {
    /**
	 * Create a error dialog to show a given String error info
	 *
	 * @param content the String that will be shown to the user
	 */
	public static void errorAlert(String title, String header, String content, String ok) {
		Alert alert = new Alert(AlertType.NONE);

		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		
		alert.getButtonTypes().add(new ButtonType(ok, ButtonData.OK_DONE));
		
		//set style//todo:set style
		//alert.getDialogPane().getStylesheets().add(STYLE_SHEET_DEFAULT);
		//alert.getDialogPane().getStyleClass().add(CSS_CLASS_DEFAULT);

		alert.showAndWait();
	}
	
	/**
	 * prompt a large dialog for a long String.
	 *
	 * @param title dialog title
	 * @param detail the message body
	 */
	public static void textDialog(String title, String content, String detail) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(content);

		TextArea textArea = new TextArea(detail);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);

		alert.getDialogPane().setContent(textArea);
		
		//set style//todo:set style
		//alert.getDialogPane().getStylesheets().add(STYLE_SHEET_DEFAULT);
		//alert.getDialogPane().getStyleClass().add(CSS_CLASS_DEFAULT);

		alert.showAndWait();
	}
}
