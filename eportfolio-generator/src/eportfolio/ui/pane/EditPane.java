/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.ui.pane;

import eporfolio.util.dialog.AlertUser;
import static eportfolio.main.Constant.CSS_CONTROL_PANE;
import static eportfolio.main.Constant.PATH_PREVIEW_FOLDER;
import eportfolio.main.LanguageEnum;
import eportfolio.model.page.PageModel;
import eportfolio.model.page.elements.ContentElement;
import eportfolio.model.page.elements.TextElement;
import eportfolio.ui.view.MainView;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 *
 * @author zmc94
 */
public class EditPane extends HBox {

	private MainView mainUI;
	private PageModel pageData;

	private VBox controlPane = new VBox();
	private ScrollPane propertyPane = new ScrollPane();
	private ScrollPane elementPane = new ScrollPane();
	private StackPane previewPane = new StackPane();

	private WebView preview = new WebView();
	private ChangeListener previewReloadListener;

	public EditPane(PageModel pageData, MainView ui) {
		mainUI = ui;
		this.pageData = pageData;

		controlPane.getStyleClass().add(CSS_CONTROL_PANE);// change to css
		controlPane.setMinWidth(335);
		controlPane.setMinHeight(0);
		controlPane.heightProperty().addListener((o, oldValue, newValue) -> {
			propertyPane.setMaxHeight(controlPane.getHeight() / 2);
			elementPane.setPrefHeight(controlPane.getHeight() - propertyPane.getHeight());
		});

		controlPane.getChildren().add(propertyPane);
		controlPane.getChildren().add(elementPane);
		getChildren().add(controlPane);
		getChildren().add(previewPane);
		previewPane.getChildren().add(preview);
		
		previewPane.widthProperty().addListener(ob -> resizeWebView());
		previewPane.heightProperty().addListener(ob -> resizeWebView());
		
		elementPane.setPadding(new Insets(5));
		
		previewReloadListener = getPreviewReloadListener();
		
		displayPage(pageData);
	}

	public final void displayPage(PageModel pageData) {
		if (pageData == null) {
			propertyPane.setContent(null);
			elementPane.setContent(null);
			return;
		}
		this.pageData = pageData;
		propertyPane.setContent(pageData.getPropertiesManager().getEditPane());

		WebEngine webEngine = preview.getEngine();
		webEngine.load((new File(PATH_PREVIEW_FOLDER + pageData.getPageTitle() + ".html")).toURI().toString());
		// prevent caching
		webEngine.getLoadWorker().stateProperty().removeListener(previewReloadListener);
		previewReloadListener = getPreviewReloadListener();
		webEngine.getLoadWorker().stateProperty().addListener(previewReloadListener);
//		webEngine.setOnAlert(e -> {
//			AlertUser.errorAlert("alert", "alert from web page", e.toString(), "ok");
//		});

		System.out.println("previewing: " + PATH_PREVIEW_FOLDER + pageData.getPageTitle() + ".html");

		Platform.runLater(() -> {
			propertyPane.autosize();
			propertyPane.setMaxHeight(controlPane.getHeight()/ 2);
			elementPane.setPrefHeight(controlPane.getHeight() - propertyPane.getHeight());
			
			previewPane.setPrefWidth(100000);
		});
	}
	
	public void stopPreview(){
		if(preview!=null) preview.getEngine().load(null);
	}
	
	public void cancelElementSelection(){
		elementPane.setContent(null);
	}
	
	private ChangeListener<Worker.State> getPreviewReloadListener(){
		return new ChangeListener<Worker.State>() {
			boolean first = true;
			public void reset(){first=true;}
			@Override
			public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) {
				if (t1.equals(Worker.State.SUCCEEDED) && first) {
					first = false;
					preview.getEngine().reload();
				}
				JSObject JSwindow = (JSObject) preview.getEngine().executeScript("window");
				JSwindow.setMember("javaInterface", new JavascriptBridge());
			}
		};
	}

	public class JavascriptBridge {

		public JavascriptBridge() {
		}

		public void selectItem(String elementId) {
			System.out.println("element " + elementId + " got selected");
			Platform.runLater(() -> {
				ContentElement elem = pageData.getElementById(elementId);
				if(elem!=null)
					elementPane.setContent(elem.getEditPane());
				else
					mainUI.setUserHint(LanguageEnum.ERROR_ELEMENT_SELECTION, elementId, 2);
			});
		}

		public void selectText(String elementId, int selectionStart, int selectionEnd) {
			System.out.println("element " + elementId + " got selected, start from " + selectionStart + " to " + selectionEnd);
			Platform.runLater(() -> {
				ContentElement elem = pageData.getElementById(elementId);
				if(elem!=null){
					if(!elem.getType().equals("text"))
						mainUI.setUserHint(LanguageEnum.ERROR_ELEMENT_NOT_TEXT, elementId, 1);
					((TextElement)elem).setProtentialLink(selectionStart, selectionEnd);
					elementPane.setContent(elem.getEditPane());
				}else
					mainUI.setUserHint(LanguageEnum.ERROR_ELEMENT_SELECTION, elementId, 2);
			});
		}
	}
	
	public void resizeWebView(){
		previewPane.setPrefWidth(100000);
		preview.setPrefHeight(previewPane.getHeight());
		preview.setPrefWidth(previewPane.getWidth());
	}
}
