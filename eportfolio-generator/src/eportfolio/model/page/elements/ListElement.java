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
import static eportfolio.main.LanguageEnum.ERROR_FAIL_TO_MOVE_DOWN_ELEMENT;
import static eportfolio.main.LanguageEnum.ERROR_FAIL_TO_MOVE_UP_ELEMENT;
import static eportfolio.main.LanguageEnum.ERROR_FAIL_TO_REMOVE_ELEMENT;
import static eportfolio.main.LanguageEnum.PROMPT_LABEL_LIST_COMPONENT;
import static eportfolio.main.LanguageEnum.PROMPT_LIST_ELEMENT;
import static eportfolio.main.LanguageEnum.PROMPT_TEXT_ELEMENT;
import static eportfolio.main.LanguageEnum.SUBMIT;
import static eportfolio.main.LanguageEnum.TEXT_ELEMENT_DEFAULT;
import static eportfolio.main.LanguageEnum.TOOLTIP_MOVE_DOWN_ELEMENT;
import static eportfolio.main.LanguageEnum.TOOLTIP_MOVE_UP_ELEMENT;
import static eportfolio.main.LanguageEnum.TOOLTIP_REMOVE_ELEMENT;
import eportfolio.model.page.PageModel;
import static eportfolio.model.page.elements.ContentElement.initChildButton;
import eportfolio.model.page.properties.ContentProperty;
import java.util.Vector;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class ListElement extends ContentElement{
	private final static String JSON_ID = "id";
	private final static String JSON_CONTENT = "list";
	
	private final static String JSON_EXPORT_ID = "id";
	private final static String JSON_EXPORT_TYPE = "type";
	private final static String JSON_EXPORT_LIST = "list";
	
	private Vector<String> list = new Vector();

	public ListElement(int elementCount, PageModel page, ContentProperty contentSet) {
		super("list-" + elementCount, elementCount, page, contentSet);
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		String content = props.getProperty(TEXT_ELEMENT_DEFAULT);
		list.add(content);
	}
	
	public ListElement(String id, PageModel page, ContentProperty contentSet) throws ContentCreationException {
		super(id, page, contentSet);
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		String content = props.getProperty(TEXT_ELEMENT_DEFAULT);
		list.add(content);
	}
	
	@Override
	public JsonObject saveAsJson() {
		JsonArrayBuilder listArray = Json.createArrayBuilder();
		for (String list : list) {
			listArray.add(list);
		}
		return Json.createObjectBuilder()
				.add(JSON_ID, id)
				.add(JSON_CONTENT, listArray)
				.build();
	}

	@Override
	public void loadFromJson(JsonObject json) {
		id = json.getString(JSON_ID);
		list.clear();
		JsonArray listArray = json.getJsonArray(JSON_CONTENT);
		for (int i = 0; i < listArray.size(); i++) {
			list.add(listArray.getString(i));
		}
	}

	@Override
	public JsonObject export(String workingDirectory, boolean inEdit) {
		JsonArrayBuilder listArray = Json.createArrayBuilder();
		for (String item : list) {
			listArray.add(item);
		}
		
		return Json.createObjectBuilder()
				.add(JSON_EXPORT_ID, id)
				.add(JSON_EXPORT_TYPE, getType())
				.add(JSON_EXPORT_LIST, listArray.build())
				.build();
	}

	@Override
	public Pane getEditPane() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		Label typePrompt = new Label(props.getProperty(PROMPT_LIST_ELEMENT) + id);
		
		HBox elementControl = new HBox();
		elementControl.setSpacing(3);
		Button moveDownButton = initChildButton(elementControl, ICON_MOVE_DOWN_ELEMENT, TOOLTIP_MOVE_DOWN_ELEMENT, CSS_ELEMENT_CONTROL_BUTTON, true);
		Button moveUpButton = initChildButton(elementControl, ICON_MOVE_UP_ELEMENT, TOOLTIP_MOVE_UP_ELEMENT, CSS_ELEMENT_CONTROL_BUTTON, true);
		Button removeButton = initChildButton(elementControl, ICON_REMOVE_ELEMENT, TOOLTIP_REMOVE_ELEMENT, CSS_ELEMENT_CONTROL_BUTTON, true);
		
		removeButton.setOnAction(e -> {
			if (contentSet.removeElement(this)) {
				page.getDataInterface().setModified();
				page.getDataInterface().refreshEditView();
				page.getDataInterface().getUI().getEditPane().cancelElementSelection();
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
		
		Label hint = new Label(props.getProperty(PROMPT_LABEL_LIST_COMPONENT));
		
		StringBuilder content = new StringBuilder();
		for(int i=0; i<list.size(); i++){
			content.append(list.get(i));
			if(i<list.size()-1) content.append("\n");
		}
		
		TextArea contentField = new TextArea(content.toString());
		contentField.setMaxWidth(300); // todo: css
		contentField.setWrapText(true);
		contentField.textProperty().addListener((o, oldText, newText) -> {
			page.getDataInterface().setModified();
			String lines[] = contentField.getText().split("\\r?\\n");
			list.clear();
			for(String item: lines)
				list.add(item);
		});
		
		Button submitButton = new Button(props.getProperty(SUBMIT));
		submitButton.setPrefWidth(300);
		submitButton.setOnAction(e -> {
			page.getDataInterface().setModified();
			String lines[] = contentField.getText().split("\\r?\\n");
			list.clear();
			for(String item: lines)
				list.add(item);
			page.getDataInterface().refreshEditView();
		});
		
		AnchorPane elementInfoPane = new AnchorPane(typePrompt, elementControl);
		elementInfoPane.setLeftAnchor(typePrompt, 0.0);
		elementInfoPane.setRightAnchor(elementControl, 0.0);
		
		VBox editPane = new VBox();
		editPane.setSpacing(10); // todo: css
		editPane.setPadding(new Insets(10.0));
		editPane.getChildren().addAll(elementInfoPane, hint, contentField, submitButton);
		return editPane;
	}

	@Override
	public String getType() {
		return "list";
	}
	
}
