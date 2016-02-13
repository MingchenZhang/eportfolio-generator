/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.main;

import eporfolio.util.ErrorHandler;
import eporfolio.util.dialog.InfoRequest;
import static eportfolio.main.Constant.*;
import eportfolio.ui.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;
import properties_manager.PropertiesManager;
import xml_utilities.InvalidXMLFileFormatException;

/**
 *
 * @author zmc94
 */
public class ePortfolioGenerator extends Application {
	private MainView ui = new MainView();

	@Override
	public void start(Stage primaryStage) throws Exception {
		boolean success = loadProperties();
		if (success) {
			// NOW START THE UI IN EVENT HANDLING MODE
			ui.startUI(primaryStage);
		} // THERE WAS A PROBLEM LOADING THE PROPERTIES FILE
		else {
			// LET THE ERROR HANDLER PROVIDE THE RESPONSE
			ErrorHandler errorHandler = ui.getErrorHandler();
			errorHandler.processError("Error", "fail to read essential data", null);
			System.exit(0);
		}
	}

	/**
	 * Loads this application's properties file, which has a number of settings
	 * for initializing the user interface.
	 *
	 * @return true if the properties file was loaded successfully, false
	 * otherwise.
	 */
	public boolean loadProperties() {
		// ASK USER FOR THE LANGUAGE PROPERTY FILE
		String loadPropertiesFilename = chooseProperties();

		// QUIT IF USER CANCELLED
		if (loadPropertiesFilename == null) {
			System.exit(1);
		}
		try {
			// LOAD THE SETTINGS FOR STARTING THE APP
			PropertiesManager props = PropertiesManager.getPropertiesManager();
			props.addProperty(PropertiesManager.DATA_PATH_PROPERTY, PATH_DATA);
			props.loadProperties(loadPropertiesFilename, PROPERTIES_SCHEMA_FILE_NAME);
			return true;
		} catch (InvalidXMLFileFormatException ixmlffe) {
			// SOMETHING WENT WRONG INITIALIZING THE XML FILE
			ErrorHandler eH = ui.getErrorHandler();
			eH.processError("Error", "fail to read properties file", ixmlffe);
			return false;
		}
	}

	public String chooseProperties() {
		String selections[] = UI_PROPERTIES_TYPES;
		String answer = InfoRequest.selectionDialog(
				LANGUAGE_WINDOW_TITLE,
				LANGUAGE_WINDOW_TITLE,
				LANGUAGE_WINDOW_LANGUAGE,
				selections);
		int index = -1;
		for (int i = 0; i < UI_PROPERTIES_TYPES.length; i++) {
			if (UI_PROPERTIES_TYPES[i].equals(answer)) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			return UI_PROPERTIES_FILE_NAME + UI_PROPERTIES_TYPES_NAMES[index];
		}
		return null;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
