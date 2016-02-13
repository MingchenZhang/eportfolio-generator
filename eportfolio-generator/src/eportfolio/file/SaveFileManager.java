/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.file;

import eporfolio.util.dialog.InfoRequest;
import eportfolio.exception.ContentCreationException;
import eportfolio.exception.PropertyCreationException;
import static eportfolio.main.Constant.PATH_LAYOUT_FOLDER;
import static eportfolio.main.LanguageEnum.*;
import eportfolio.ui.view.MainViewDataInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javafx.stage.FileChooser;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class SaveFileManager {

	private MainViewDataInterface data;

	private boolean saved = true;
	private boolean fileOpened;
	private File openedFile;

	public boolean isSaved() {
		return saved;
	}

	public SaveFileManager(MainViewDataInterface d) {
		data = d;
	}

	public void setModified() {
		saved = false;
	}

	public boolean getFileOpened() {
		return getFileOpened();
	}

	public void newFile() throws FileNotFoundException, IOException {
		boolean continueToMakeNew = true;
		if (!saved) {
			continueToMakeNew = promptToSave();
		}

		if (continueToMakeNew) {
			PropertiesManager props = PropertiesManager.getPropertiesManager();
			String selectedLayout = InfoRequest.selectionDialog(
					props.getProperty(DIALOG_CONFIG_SELECT_TITLE), props.getProperty(DIALOG_CONFIG_SELECT_TITLE), props.getProperty(DIALOG_CONFIG_SELECT_PROMPT), listLayouts());
			data.getDataModel().setLayoutFolder(PATH_LAYOUT_FOLDER + selectedLayout);

			saved = false;
			fileOpened = true;

			data.getUI().displayPageEditView(null);

			data.getUI().setUserHint(USERHINT_NEW_EPORTFOLIO_CREATED, 0);
			data.getUI().changeFileToolbarStatus(1);
			data.getUI().updatePageSelector(true);
		}
	}

	public boolean openFile() throws FileNotFoundException, IOException, PropertyCreationException, ContentCreationException {
		boolean continueToOpen = true;
		if (!saved) {
			continueToOpen = promptToSave();
		}

		if (continueToOpen) {
			return promptToOpen();
		}
		
		return false;
	}

	public void saveFile() throws FileNotFoundException {
		openedFile = saveDataToJson(openedFile);

		saved = true;
	}

	public void saveAsFile() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON File (*.json)", "*.JSON", "*.json");
		fileChooser.getExtensionFilters().add(jsonFilter);
		fileChooser.setTitle(PropertiesManager.getPropertiesManager().getProperty(DIALOG_SAVE_AS_TITLE));
		File file = fileChooser.showSaveDialog(data.getUI().getWindow());

		saveDataToJson(file);

		saved = true;
	}

	public void exit() throws FileNotFoundException, IOException {
		boolean continueToExit = true;
		if (!saved) {
			continueToExit = promptToSave();
		}

		if (continueToExit) {
			System.exit(0);
		}
	}

	/**
	 * This helper method asks the user for a file to open. The user-selected
	 * file is then loaded and the GUI updated. Note that if the user cancels
	 * the open process, nothing is done. If an error occurs loading the file, a
	 * message is displayed, but nothing changes.
	 */
	private boolean promptToOpen() throws IOException, FileNotFoundException, PropertyCreationException, ContentCreationException {
		// AND NOW ASK THE USER FOR THE COURSE TO OPEN
		FileChooser slideShowFileChooser = new FileChooser();
		//slideShowFileChooser.setInitialDirectory(new File(PATH_SLIDE_SHOWS));
		File selectedFile = slideShowFileChooser.showOpenDialog(data.getUI().getWindow());

		// ONLY OPEN A NEW FILE IF THE USER SAYS OK
		if (selectedFile != null) {
			loadJsonToData(selectedFile.getAbsolutePath());
			data.getUI().displayPageEditView(null);
			saved = true;
			fileOpened = true;
			openedFile = selectedFile;

			data.getUI().changeFileToolbarStatus(1);
			data.getUI().updatePageSelector(true);
			
			return true;
		}
		return false;
	}

	/**
	 * This helper method verifies that the user really wants to save their
	 * unsaved work, which they might not want to do. Note that it could be used
	 * in multiple contexts before doing other actions, like creating a new
	 * pose, or opening another pose, or exiting. Note that the user will be
	 * presented with 3 options: YES, NO, and CANCEL. YES means the user wants
	 * to save their work and continue the other action (we return true to
	 * denote this), NO means don't save the work but continue with the other
	 * action (true is returned), CANCEL means don't save the work and don't
	 * continue with the other action (false is retuned).
	 *
	 * @return true if the user presses the YES option to save, true if the user
	 * presses the NO option to not save, false if the user presses the CANCEL
	 * option to not continue.
	 */
	private boolean promptToSave() throws FileNotFoundException {
		// PROMPT THE USER TO SAVE UNSAVED WORK
		boolean saveWork = false;

		// PROMPT FOR USER INPUT
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		String[] selections = {props.getProperty(YES), props.getProperty(NO)};
		System.out.println(props.getProperty(DIALOG_SAVE_CONFIRMATION));
		String result = InfoRequest.ButtonSelection(
				props.getProperty(DIALOG_SAVE_CONFIRMATION),
				props.getProperty(DIALOG_SAVE_CONFIRMATION),
				props.getProperty(DIALOG_SAVE_CONFIRMATION_WARNING),
				selections,
				props.getProperty(CANCEL));
		if (result == null) {
			return false;
		} else if (result.equals(selections[0])) {
			saveWork = true;
		} else if (result.equals(selections[1])) {
			saveWork = false;
		}

		// IF THE USER SAID YES, THEN SAVE BEFORE MOVING ON
		if (saveWork) {
			saveDataToJson(openedFile);
			saved = true;
		}

		// IF THE USER SAID NO, WE JUST GO ON WITHOUT SAVING
		// BUT FOR BOTH YES AND NO WE DO WHATEVER THE USER
		// HAD IN MIND IN THE FIRST PLACE
		return true;
	}

	private File saveDataToJson(File pathToStore) throws FileNotFoundException {
		JsonObject json = data.getDataModel().saveAsJson();

		if (pathToStore == null) {
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON File (*.json)", "*.JSON", "*.json");
			fileChooser.getExtensionFilters().add(jsonFilter);
			fileChooser.setTitle(PropertiesManager.getPropertiesManager().getProperty(DIALOG_SAVE_AS_TITLE));
			pathToStore = fileChooser.showSaveDialog(data.getUI().getWindow());
		}

		// INIT THE WRITER
		OutputStream os = new FileOutputStream(pathToStore);
		JsonWriter jsonWriter = Json.createWriter(os);

		jsonWriter.writeObject(json);
		
		return pathToStore;
	}

	private void loadJsonToData(String jsonPath) throws FileNotFoundException, IOException, PropertyCreationException, ContentCreationException {
		InputStream is = new FileInputStream(jsonPath);
		JsonReader jsonReader = Json.createReader(is);
		JsonObject json = jsonReader.readObject();
		jsonReader.close();
		is.close();

		data.getDataModel().loadFromJson(json);
	}

	private String[] listLayouts() {
		File file = new File(PATH_LAYOUT_FOLDER);
		String[] directories = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		return directories;
	}
}
