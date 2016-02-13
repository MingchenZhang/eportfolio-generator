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
import java.util.Vector;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
public class TextElement extends ContentElement {

	private final static String JSON_ID = "id";
	private final static String JSON_CONTENT = "content";
	private final static String JSON_LINK = "link";

	private final static String JSON_EXPORT_ID = "id";
	private final static String JSON_EXPORT_TYPE = "type";
	private final static String JSON_EXPORT_CONTENT = "content";
	private final static String JSON_EXPORT_LINK = "link";

	private String content;
	private Vector<LinkInfo> links = new Vector();
	private int[] protentialLink = new int[2];

	public TextElement(int elementCount, PageModel page, ContentProperty contentSet) {
		super("text-" + elementCount, elementCount, page, contentSet);
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		content = props.getProperty(TEXT_ELEMENT_DEFAULT);
		protentialLink[0] = -1;
		protentialLink[1] = -1;
	}

	public TextElement(String id, PageModel page, ContentProperty contentSet) throws ContentCreationException {
		super(id, page, contentSet);
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		content = props.getProperty(TEXT_ELEMENT_DEFAULT);
		protentialLink[0] = -1;
		protentialLink[1] = -1;
	}

	@Override
	public JsonObject saveAsJson() {
		JsonArrayBuilder jsonLinkArray = Json.createArrayBuilder();
		for (LinkInfo info : links) {
			jsonLinkArray.add(info.toJsonArray());
		}
		return Json.createObjectBuilder()
				.add(JSON_ID, id)
				.add(JSON_CONTENT, content)
				.add(JSON_LINK, jsonLinkArray.build())
				.build();
	}

	@Override
	public void loadFromJson(JsonObject json) {
		id = json.getString(JSON_ID);
		content = json.getString(JSON_CONTENT);
		JsonArray jsonLinkInfo = json.getJsonArray(JSON_LINK);
		for (int i = 0; i < jsonLinkInfo.size(); i++) {
			links.add(new LinkInfo(jsonLinkInfo.getJsonArray(i)));
		}
	}

	@Override
	public JsonObject export(String workingDirectory, boolean inEdit) {
		JsonArrayBuilder jsonLinkArray = Json.createArrayBuilder();
		for (LinkInfo info : links) {
			jsonLinkArray.add(info.toJsonArray());
		}

		return Json.createObjectBuilder()
				.add(JSON_EXPORT_ID, id)
				.add(JSON_EXPORT_TYPE, getType())
				.add(JSON_EXPORT_CONTENT, content)
				.add(JSON_EXPORT_LINK, jsonLinkArray.build())
				.build();
	}

	@Override
	public VBox getEditPane() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		Label typePrompt = new Label(props.getProperty(PROMPT_TEXT_ELEMENT) + id);

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

		TextArea contentField = new TextArea(content);
		contentField.setMaxWidth(300); // todo: css
		contentField.setWrapText(true);
		contentField.textProperty().addListener((o, oldText, newText) -> {
			page.getDataInterface().setModified();
			content = contentField.getText();
		});

		VBox linkPane = new VBox();
		if (protentialLink[0] != -1 && protentialLink[1] != -1) { // select element
			Label startLabel = new Label(props.getProperty(PROMPT_TEXT_LINK_START_LABEL) + protentialLink[0]);
			Label endLabel = new Label(props.getProperty(PROMPT_TEXT_LINK_END_LABEL) + protentialLink[1]);
			startLabel.setUserData(protentialLink[0]);
			endLabel.setUserData(protentialLink[1]);
			AnchorPane positionPane = new AnchorPane(startLabel, endLabel);
			positionPane.setLeftAnchor(startLabel, 0.0);
			positionPane.setRightAnchor(endLabel, 0.0);
			TextField linkTarget = new TextField();
			Button confirmButton = new Button(props.getProperty(CONFIRM));
			confirmButton.setMaxWidth(80);
			AnchorPane linkInfoPane = new AnchorPane(linkTarget, confirmButton);
			linkInfoPane.setLeftAnchor(linkTarget, 0.0);
			linkInfoPane.setRightAnchor(confirmButton, 0.0);
			linkPane.getChildren().addAll(positionPane, linkInfoPane);
			confirmButton.setOnAction(e -> {
				if (setLink((int) startLabel.getUserData(), (int) endLabel.getUserData(), linkTarget.getText())) {
					page.getDataInterface().setModified();
					page.getDataInterface().refreshEditView();
					page.getDataInterface().getUI().getEditPane().cancelElementSelection();
				} else {
					page.getDataInterface().getUI().setUserHint(ERROR_LINK_OVERLAPED, 1);
				}
			});
			protentialLink[0] = -1;
			protentialLink[1] = -1;
		} else if (protentialLink[0] != -1) {// select link
			int linkIndex = -1;
			for (int i = 0; i < links.size(); i++) {
				if (links.get(i).getStartIndex() == protentialLink[0]) {
					linkIndex = i;
					break;
				}
			}
			if (linkIndex == -1) {
				page.getDataInterface().getUI().setUserHint(ERROR_LINK_NOT_FOUND, 1);
				return null;
			}
			Label startLabel = new Label(props.getProperty(PROMPT_TEXT_LINK_START_LABEL) + links.get(linkIndex).getStartIndex());
			Label endLabel = new Label(props.getProperty(PROMPT_TEXT_LINK_END_LABEL) + links.get(linkIndex).getEndIndex());
			startLabel.setUserData(linkIndex);
			AnchorPane positionPane = new AnchorPane(startLabel, endLabel);
			positionPane.setLeftAnchor(startLabel, 0.0);
			positionPane.setRightAnchor(endLabel, 0.0);
			TextField linkTarget = new TextField(links.get(linkIndex).getLink());
			Button confirmButton = new Button(props.getProperty(CONFIRM));
			confirmButton.setPrefWidth(80);
			Button deleteButton = new Button(props.getProperty(DELETE));
			deleteButton.setPrefWidth(80);
			AnchorPane linkInfoPane = new AnchorPane(linkTarget, deleteButton, confirmButton);
			linkInfoPane.setLeftAnchor(linkTarget, 0.0);
			linkInfoPane.setRightAnchor(confirmButton, 0.0);
			linkInfoPane.setRightAnchor(deleteButton, 90.0);
			linkPane.getChildren().addAll(positionPane, linkInfoPane);
			deleteButton.setOnAction(e -> {
				page.getDataInterface().setModified();
				links.remove((int) startLabel.getUserData());
				page.getDataInterface().refreshEditView();
				page.getDataInterface().getUI().getEditPane().cancelElementSelection();
			});
			confirmButton.setOnAction(e -> {
				page.getDataInterface().setModified();
				int start = links.get((int) startLabel.getUserData()).getStartIndex();
				int end = links.get((int) startLabel.getUserData()).getEndIndex();
				String link = links.get((int) startLabel.getUserData()).getLink();
				links.remove((int) startLabel.getUserData());
				if (setLink(start, end, link)) {
					page.getDataInterface().refreshEditView();
				} else {
					page.getDataInterface().getUI().setUserHint(ERROR_LINK_OVERLAPED, 1);
				}
			});
			protentialLink[0] = -1;
			protentialLink[1] = -1;
		}

		Button submitButton = new Button(props.getProperty(SUBMIT));
		submitButton.setPrefWidth(300);
		submitButton.setOnAction(e -> {
			page.getDataInterface().setModified();
			content = contentField.getText();
			page.getDataInterface().refreshEditView();
		});

		AnchorPane elementInfoPane = new AnchorPane(typePrompt, elementControl);
		elementInfoPane.setLeftAnchor(typePrompt, 0.0);
		elementInfoPane.setRightAnchor(elementControl, 0.0);

		VBox editPane = new VBox();
		editPane.setSpacing(10); // todo: css
		editPane.setPadding(new Insets(10.0));
		editPane.getChildren().addAll(elementInfoPane, contentField, linkPane, submitButton);
		return editPane;
	}

	public void setProtentialLink(int start, int end) {
		protentialLink[0] = start;
		protentialLink[1] = end;
	}

	public boolean setLink(int start, int end, String linkTarget) {
		if (start >= end) {
			return false;
		}
		for (LinkInfo link : links) {
			if (!(link.getEndIndex() < start || link.getStartIndex() > end)) {
				return false;
			}
		}
		int insertIndex = 0;
		for (; insertIndex < links.size(); insertIndex++) {
			if (links.get(insertIndex).getStartIndex() > start) {
				break;
			}
		}
		links.add(insertIndex, new LinkInfo(start, end, linkTarget));
		return true;
	}

	private static class LinkInfo {

		private int startIndex;
		private int endIndex;
		private String link;

		public int getStartIndex() {
			return startIndex;
		}

		public int getEndIndex() {
			return endIndex;
		}

		public String getLink() {
			return link;
		}

		private LinkInfo(int start, int end, String link) {
			startIndex = start;
			endIndex = end;
			this.link = link;
		}

		private LinkInfo(JsonArray json) {
			startIndex = json.getInt(0);
			endIndex = json.getInt(1);
			this.link = json.getString(2);
		}

		JsonArray toJsonArray() {
			return Json.createArrayBuilder()
					.add(startIndex)
					.add(endIndex)
					.add(link)
					.build();
		}
	}

	@Override
	public String getType() {
		return "text";
	}
}
