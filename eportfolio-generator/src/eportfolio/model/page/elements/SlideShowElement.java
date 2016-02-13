/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page.elements;

import eportfolio.exception.ContentCreationException;
import static eportfolio.main.Constant.CSS_ELEMENT_CONTROL_BUTTON;
import static eportfolio.main.Constant.ICON_MOVE_DOWN_ELEMENT;
import static eportfolio.main.Constant.ICON_MOVE_UP_ELEMENT;
import static eportfolio.main.Constant.ICON_REMOVE_ELEMENT;
import static eportfolio.main.LanguageEnum.*;
import eportfolio.model.page.PageModel;
import static eportfolio.model.page.elements.ContentElement.initChildButton;
import eportfolio.model.page.properties.ContentProperty;
import java.io.File;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javax.json.Json;
import javax.json.JsonObject;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class SlideShowElement extends ContentElement {

	private final static String JSON_SLIDESHOW_PATH = "slideShowPath";

	private final static String JSON_EXPORT_ID = "id";
	private final static String JSON_EXPORT_TYPE = "type";
	private final static String JSON_EXPORT_CONTENT = "content";

	private String slideShowPath = "";

	public SlideShowElement(int elementCount, PageModel page, ContentProperty contentSet) {
		super("slideshow-" + elementCount, elementCount, page, contentSet);
	}

	public SlideShowElement(String id, PageModel page, ContentProperty contentSet) throws ContentCreationException {
		super(id, page, contentSet);
	}

	@Override
	public JsonObject saveAsJson() {
		return Json.createObjectBuilder().add(JSON_SLIDESHOW_PATH, slideShowPath).build();
	}

	@Override
	public void loadFromJson(JsonObject json) {
		slideShowPath = json.getString(JSON_SLIDESHOW_PATH);
	}

	@Override
	public JsonObject export(String workingDirectory, boolean inEdit) {
		String relativePath = page.getDataInterface().getDataModel().importFolderResource(workingDirectory, slideShowPath);
		return Json.createObjectBuilder()
				.add(JSON_EXPORT_ID, id)
				.add(JSON_EXPORT_TYPE, getType())
				.add(JSON_EXPORT_CONTENT, relativePath+"/index.html")
				.build();
	}

	@Override
	public VBox getEditPane() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();

		Label typePrompt = new Label(props.getProperty(PROMPT_SLIDESHOW_ELEMENT) + id);
		TextField contentField = new TextField(slideShowPath);
		contentField.setPrefWidth(300); // todo: css

		HBox elementControl = new HBox();
		elementControl.setSpacing(3);
		Button moveDownButton = initChildButton(elementControl, ICON_MOVE_DOWN_ELEMENT, TOOLTIP_MOVE_DOWN_ELEMENT, CSS_ELEMENT_CONTROL_BUTTON, true);
		Button moveUpButton = initChildButton(elementControl, ICON_MOVE_UP_ELEMENT, TOOLTIP_MOVE_UP_ELEMENT, CSS_ELEMENT_CONTROL_BUTTON, true);
		Button removeButton = initChildButton(elementControl, ICON_REMOVE_ELEMENT, TOOLTIP_REMOVE_ELEMENT, CSS_ELEMENT_CONTROL_BUTTON, true);

		removeButton.setOnAction(e -> {
			if (contentSet.removeElement(this)) {
				page.getDataInterface().setModified();
				page.getDataInterface().refreshEditView();
			} else {
				page.getDataInterface().getUI().setUserHint(ERROR_FAIL_TO_REMOVE_ELEMENT, 1);
			}
		});
		moveUpButton.setOnAction(e -> {
			if (contentSet.moveUpElement(this)) {
				page.getDataInterface().setModified();
				page.getDataInterface().refreshEditView();
			} else {
				page.getDataInterface().getUI().setUserHint(ERROR_FAIL_TO_MOVE_UP_ELEMENT, 1);
			}
		});
		moveDownButton.setOnAction(e -> {
			if (contentSet.moveDownElement(this)) {
				page.getDataInterface().setModified();
				page.getDataInterface().refreshEditView();
			} else {
				page.getDataInterface().getUI().setUserHint(ERROR_FAIL_TO_MOVE_DOWN_ELEMENT, 1);
			}
		});

		//Browser button
		Button browserButton = new Button(props.getProperty(BROWSE));
		browserButton.setPrefWidth(300);
		browserButton.setOnAction(e -> {
			DirectoryChooser fileChooser = new DirectoryChooser();
			
			File file = fileChooser.showDialog(null);

			if (file != null) {
				page.getDataInterface().setModified();
				slideShowPath = file.getAbsolutePath();
				page.getDataInterface().refreshEditView();
			}
		});

		//Submit button
		Button submitButton = new Button(props.getProperty(SUBMIT));
		submitButton.setPrefWidth(300);
		submitButton.setOnAction(e -> {
			page.getDataInterface().setModified();
			slideShowPath = contentField.getText();
			page.getDataInterface().refreshEditView();
		});

		AnchorPane elementInfoPane = new AnchorPane(typePrompt, elementControl);
		elementInfoPane.setLeftAnchor(typePrompt, 0.0);
		elementInfoPane.setRightAnchor(elementControl, 0.0);

		VBox editPane = new VBox(elementInfoPane, contentField, browserButton, submitButton);
		editPane.setSpacing(10); // todo: css
		editPane.setPadding(new Insets(10.0));
		return editPane;
	}

	@Override
	public String getType() {
		return "slideShow";
	}

}
