/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page.elements;

import eportfolio.exception.ContentCreationException;
import static eportfolio.main.Constant.*;
import static eportfolio.main.LanguageEnum.*;
import eportfolio.model.page.PageModel;
import eportfolio.model.page.properties.ContentProperty;
import java.io.File;
import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javax.json.Json;
import javax.json.JsonObject;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class ImageElement extends ContentElement {

	private final static String JSON_IMAGE_PATH = "imagePath";

	private final static String JSON_EXPORT_ID = "id";
	private final static String JSON_EXPORT_TYPE = "type";
	private final static String JSON_EXPORT_CONTENT = "content";
	private final static String JSON_EXPORT_CAPTION = "caption";
	private final static String JSON_EXPORT_FLOATING = "floating";

	private String imagePath = "";
	private String caption = "";
	private String floating = "left";

	public ImageElement(int elementCount, PageModel page, ContentProperty contentSet) {
		super("image-" + elementCount, elementCount, page, contentSet);
	}

	public ImageElement(String id, PageModel page, ContentProperty contentSet) throws ContentCreationException {
		super(id, page, contentSet);
	}

	@Override
	public JsonObject saveAsJson() {
		return Json.createObjectBuilder()
				.add(JSON_IMAGE_PATH, imagePath)
				.add(JSON_EXPORT_CAPTION, caption)
				.add(JSON_EXPORT_FLOATING, floating)
				.build();
	}

	@Override
	public void loadFromJson(JsonObject json) {
		imagePath = json.getString(JSON_IMAGE_PATH);
		caption = json.getString(JSON_EXPORT_CAPTION);
		floating = json.getString(JSON_EXPORT_FLOATING);
	}

	@Override
	public JsonObject export(String workingDirectory, boolean inEdit) {
		String imageRelativePath = "";
		if (!imagePath.isEmpty()) {
			imageRelativePath = page.getDataInterface().getDataModel().importResource(workingDirectory, imagePath);
		}
		return Json.createObjectBuilder()
				.add(JSON_EXPORT_ID, id)
				.add(JSON_EXPORT_TYPE, getType())
				.add(JSON_EXPORT_CONTENT, imageRelativePath)
				.add(JSON_EXPORT_CAPTION, caption)
				.add(JSON_EXPORT_FLOATING, floating)
				.build();
	}

	@Override
	public VBox getEditPane() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();

		Label typePrompt = new Label(props.getProperty(PROMPT_IMAGE_ELEMENT) + id);
		TextField contentField = new TextField(imagePath);
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
		
		Label captionPrompt = new Label(props.getProperty(PROMPT_IMAGE_ELEMENT_CAPTION));
		TextField captionField = new TextField(caption);
		captionField.setPrefWidth(300);
		
		Label floatingPrompt = new Label(props.getProperty(PROMPT_IMAGE_ELEMENT_FLOATING));
		ComboBox<String> floatingBox = new ComboBox<String>();
		String[] floatingOptions = {"left","right","neither"};//todo: change to better way
		ObservableList<String> options = FXCollections.observableArrayList();
		for(String str:floatingOptions) options.add(str);
		floatingBox.setItems(options);
		floatingBox.setValue(floating);
		floatingBox.setPrefWidth(300);

		//Browser button
		Button browserButton = new Button(props.getProperty(BROWSE));
		browserButton.setPrefWidth(300);
		browserButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();

			FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG", "*.jpg");// Added lower case for linux
			FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG", "*.png");// Added lower case for linux
			FileChooser.ExtensionFilter gifFilter = new FileChooser.ExtensionFilter("GIF files (*.gif)", "*.GIF", "*.gif");// Added lower case for linux
			fileChooser.getExtensionFilters().addAll(jpgFilter, pngFilter, gifFilter);

			File file = fileChooser.showOpenDialog(null);

			if (file != null) {
				page.getDataInterface().setModified();
				imagePath = file.getAbsolutePath();
				caption = captionField.getText();
				floating = floatingBox.getValue();
				page.getDataInterface().refreshEditView();
			}
		});

		//Submit button
		Button submitButton = new Button(props.getProperty(SUBMIT));
		submitButton.setPrefWidth(300);
		submitButton.setOnAction(e -> {
			page.getDataInterface().setModified();
			imagePath = contentField.getText();
			caption = captionField.getText();
			floating = floatingBox.getValue();
			page.getDataInterface().refreshEditView();
		});

		AnchorPane elementInfoPane = new AnchorPane(typePrompt, elementControl);
		elementInfoPane.setLeftAnchor(typePrompt, 0.0);
		elementInfoPane.setRightAnchor(elementControl, 0.0);

		VBox editPane = new VBox(elementInfoPane, contentField, browserButton, captionPrompt, captionField, floatingPrompt, floatingBox, submitButton);
		editPane.setSpacing(10); // todo: css
		editPane.setPadding(new Insets(10.0));
		return editPane;
	}

	@Override
	public String getType() {
		return "image";
	}
}
