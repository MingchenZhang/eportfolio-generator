/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.ui.view;

import eportfolio.exception.ContentCreationException;
import eportfolio.exception.PropertyCreationException;
import eportfolio.exception.TitleDuplicationException;
import eportfolio.file.SaveFileManager;
import static eportfolio.main.LanguageEnum.*;
import eportfolio.model.DataModel;
import eportfolio.model.page.PageModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.DirectoryChooser;

/**
 *
 * @author zmc94
 */
public class MainViewDataInterface {

	private MainView ui;
	private SaveFileManager fileManager;
	private DataModel dataModel;

	private int currentPage = -1;

	public MainView getUI() {
		return ui;
	}
	
	public void setModified(){
		fileManager.setModified();
		ui.changeFileToolbarStatus(3);
	}

	public SaveFileManager getSaveFileManager() {
		return fileManager;
	}

	public DataModel getDataModel() {
		return dataModel;
	}

	public String getCurrentPageTitle() {
		if (currentPage >= 0) {
			return dataModel.getPageModel(currentPage).getPageTitle();
		}
		return null;
	}

	public MainViewDataInterface(MainView ui) {
		this.ui = ui;
		fileManager = new SaveFileManager(this);
		dataModel = new DataModel(this);
	}

	public void startEditing(String layoutFolderPath) throws IOException {
		dataModel.setLayoutFolder(layoutFolderPath);
	}

	class NewFileHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			try {
				fileManager.newFile();
			} catch (FileNotFoundException ex) {
				ui.setUserHint(ERROR_FAIL_TO_SAVE, ex, 2);
			} catch (IOException ex) {
				ui.setUserHint(ERROR_IO_EXCEPTION, ex, 2);
			}
		}
	}

	class OpenFileHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			try {
				if(!fileManager.openFile())
					return;

				if (dataModel.getPageCount() >= 1) {
					currentPage = 0;
					refreshEditView();
				} else {
					currentPage = -1;
					refreshNullEditView();
				}
				ui.updatePageSelector(true);
				ui.changeFileToolbarStatus(2);
			} catch (FileNotFoundException ex) {
				ui.setUserHint(ERROR_FILE_NOT_FOUND, ex, 2);
			} catch (IOException ex) {
				ui.setUserHint(ERROR_IO_EXCEPTION, ex, 2);
			} catch (PropertyCreationException ex) {
				ui.setUserHint(ERROR_PROPERTY_CTEATTION, ex, 2);
			} catch (ContentCreationException ex) {
				ui.setUserHint(ERROR_CONTENT_CREATION, ex, 2);
			}
		}
	}

	class SaveFileHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			try {
				fileManager.saveFile();
				ui.changeFileToolbarStatus(2);
			} catch (FileNotFoundException ex) {
				ui.setUserHint(ERROR_FILE_NOT_FOUND, ex, 2);
			}
		}
	}

	class SaveAsHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			try {
				fileManager.saveAsFile();
				ui.changeFileToolbarStatus(2);
			} catch (FileNotFoundException ex) {
				ui.setUserHint(ERROR_FILE_NOT_FOUND, ex, 2);
			}
		}
	}

	class ExportHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			
			File folder = directoryChooser.showDialog(null);
			
			try {
				dataModel.export(folder);
			} catch (TitleDuplicationException ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				ui.setUserHint(ERROR_TITLE_DUPLICATION, ex, 2);
			} catch (IOException ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				ui.setUserHint(ERROR_FAIL_TO_PREVIEW, ex, 2);
			}
		}
	}

	class ExitHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			try {
				fileManager.exit();
			} catch (FileNotFoundException ex) {
				ui.setUserHint(ERROR_FILE_NOT_FOUND, ex, 2);
			} catch (IOException ex) {
				ui.setUserHint(ERROR_IO_EXCEPTION, ex, 2);
			}
		}
	}

	class NewPageHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			PageModel newPageModel;
			try {
				newPageModel = dataModel.addPage();
			} catch (PropertyCreationException ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				ui.setUserHint(ERROR_PROPERTY_CTEATTION, ex, 2);
				return;
			}
			currentPage = dataModel.getPageCount() - 1;

			ui.updatePageSelector(true);
			try {
				ui.getEditPane().stopPreview();
				dataModel.preViewExport();
			} catch (IOException ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				ui.setUserHint(ERROR_FAIL_TO_PREVIEW, ex, 2);
				return;
			} catch (TitleDuplicationException ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				ui.setUserHint(ERROR_TITLE_DUPLICATION, ex, 2);
			}
			ui.displayPageEditView(newPageModel);
		}
	}

	class removePageHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if(currentPage<0) return;
			dataModel.removePage(currentPage);
			currentPage = (dataModel.getPageCount()<=0)?-1:0;
			refreshEditView();
			ui.updatePageSelector(true);
		}
	}

	class pageSelectorHandler implements ChangeListener<String> {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			currentPage = dataModel.getPageModelIndex(newValue);
			try {
				ui.getEditPane().stopPreview();
				dataModel.preViewExport();
			} catch (IOException ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				ui.setUserHint(ERROR_FAIL_TO_PREVIEW, ex, 2);
				return;
			} catch (TitleDuplicationException ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				ui.setUserHint(ERROR_TITLE_DUPLICATION, ex, 2);
			}
			ui.displayPageEditView(dataModel.getPageModel(currentPage));
		}
	}

	class refreshPreviewHandler implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			try {
				ui.getEditPane().stopPreview();
				dataModel.preViewExport();
			} catch (IOException ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				ui.setUserHint(ERROR_FAIL_TO_PREVIEW, ex, 2);
				return;
			} catch (TitleDuplicationException ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				ui.setUserHint(ERROR_TITLE_DUPLICATION, ex, 2);
			}
			ui.displayPageEditView(dataModel.getPageModel(currentPage));
		}
	}

	public void refreshEditView() {
		try {
			ui.getEditPane().stopPreview();
			dataModel.preViewExport();
		} catch (TitleDuplicationException ex) {
			Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
			ui.setUserHint(ERROR_TITLE_DUPLICATION, ex, 2);
		} catch (IOException ex) {
			Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
			ui.setUserHint(ERROR_FAIL_TO_PREVIEW, ex, 2);
			return;
		}
		ui.displayPageEditView(dataModel.getPageModel(currentPage));
	}

	public void refreshNullEditView() {
		ui.displayPageEditView(null);
	}
}
