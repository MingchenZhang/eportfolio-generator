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
public class VideoElement extends ContentElement {

	private final static String JSON_VIDEO_PATH = "videoPath";

	private final static String JSON_EXPORT_ID = "id";
	private final static String JSON_EXPORT_TYPE = "type";
	private final static String JSON_EXPORT_CONTENT = "content";
	private final static String JSON_EXPORT_CAPTION = "caption";

	private String videoPath = "";
	private String caption = "";

	public VideoElement(int elementCount, PageModel page, ContentProperty contentSet) {
		super("video-" + elementCount, elementCount, page, contentSet);
	}

	public VideoElement(String id, PageModel page, ContentProperty contentSet) throws ContentCreationException {
		super(id, page, contentSet);
	}

	@Override
	public JsonObject saveAsJson() {
		return Json.createObjectBuilder()
				.add(JSON_VIDEO_PATH, videoPath)
				.add(JSON_EXPORT_CAPTION, caption)
				.build();
	}

	@Override
	public void loadFromJson(JsonObject json) {
		videoPath = json.getString(JSON_VIDEO_PATH);
		caption = json.getString(JSON_EXPORT_CAPTION);
	}

	@Override
	public JsonObject export(String workingDirectory, boolean inEdit) {
		String videoRelativePath = page.getDataInterface().getDataModel().importResource(workingDirectory, videoPath);
		return Json.createObjectBuilder()
				.add(JSON_EXPORT_ID, id)
				.add(JSON_EXPORT_TYPE, getType())
				.add(JSON_EXPORT_CONTENT, videoRelativePath)
				.add(JSON_EXPORT_CAPTION, caption)
				.build();
	}

	@Override
	public VBox getEditPane() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();

		Label typePrompt = new Label(props.getProperty(PROMPT_IMAGE_ELEMENT) + id);
		TextField contentField = new TextField(videoPath);
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
		
		Label captionPrompt = new Label(props.getProperty(PROMPT_VIDEO_ELEMENT_CAPTION));
		TextField captionField = new TextField(caption);
		captionField.setPrefWidth(300);

		//Browser button
		Button browserButton = new Button(props.getProperty(BROWSE));
		browserButton.setPrefWidth(300);
		browserButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();

			FileChooser.ExtensionFilter mp4Filter = new FileChooser.ExtensionFilter("MP4 files (*.mp4)", "*.MP4", "*.mp4");// Added lower case for linux
			fileChooser.getExtensionFilters().addAll(mp4Filter);

			File file = fileChooser.showOpenDialog(null);

			if (file != null) {
				page.getDataInterface().setModified();
				videoPath = file.getAbsolutePath();
				caption = captionField.getText();
				page.getDataInterface().refreshEditView();
			}
		});

		//Submit button
		Button submitButton = new Button(props.getProperty(SUBMIT));
		submitButton.setPrefWidth(300);
		submitButton.setOnAction(e -> {
			page.getDataInterface().setModified();
			videoPath = contentField.getText();
			caption = captionField.getText();
			page.getDataInterface().refreshEditView();
		});

		AnchorPane elementInfoPane = new AnchorPane(typePrompt, elementControl);
		elementInfoPane.setLeftAnchor(typePrompt, 0.0);
		elementInfoPane.setRightAnchor(elementControl, 0.0);

		VBox editPane = new VBox(elementInfoPane, contentField, browserButton,captionPrompt, captionField, submitButton);
		editPane.setSpacing(10); // todo: css
		editPane.setPadding(new Insets(10.0));
		return editPane;
	}

	@Override
	public String getType() {
		return "video";
	}

}
