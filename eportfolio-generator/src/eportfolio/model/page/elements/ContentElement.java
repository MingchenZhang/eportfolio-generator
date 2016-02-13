/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page.elements;

import eportfolio.exception.ContentCreationException;
import static eportfolio.main.Constant.PATH_ICONS;
import eportfolio.main.LanguageEnum;
import eportfolio.model.page.PageModel;
import eportfolio.model.page.properties.ContentProperty;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javax.json.JsonObject;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public abstract class ContentElement {

	protected String id;
	protected int elementCount;
	protected PageModel page;
	protected ContentProperty contentSet;

	public String getID() {
		return id;
	}

	public int getElementCount() {
		return elementCount;
	}

	public ContentElement(String id, int elementCount, PageModel page, ContentProperty contentSet) {
		this.elementCount = elementCount;
		this.id = id;
		this.page = page;
		this.contentSet = contentSet;
	}

	public ContentElement(String id, PageModel page, ContentProperty contentSet) throws ContentCreationException {
		this.id = id;
		this.page = page;
		this.contentSet = contentSet;
		Pattern p = Pattern.compile("[0-9]+$");
		Matcher m = p.matcher(id);
		if (m.find()) {
			elementCount = Integer.parseInt(m.group());
		}else{
			throw new ContentCreationException("invalid element id: "+id);
		}
	}

	public abstract JsonObject saveAsJson();

	public abstract void loadFromJson(JsonObject json);

	public abstract JsonObject export(String workingDirectory, boolean inEdit);

	public abstract Pane getEditPane();

	public abstract String getType();
	
	/**
	 * This helps initialize buttons in a toolbar, constructing a custom button
	 * with a customly provided icon and tooltip, adding it to the provided
	 * toolbar pane, and then returning it.
	 *
	 * @param parent the parent which will contain the button
	 * @param iconFileName fileName for icon
	 * @param tooltip the functionality hint for user
	 * @param cssClass CSS style for button
	 * @param enabled set the button to enable
	 * @return button reference
	 */
	public static Button initChildButton(
			Pane parent,
			String iconFileName,
			LanguageEnum tooltip,
			String cssClass,
			boolean enabled) {
		String imagePath = "file:" + PATH_ICONS + iconFileName;
		Image buttonImage = new Image(imagePath);
		Button button = new Button();
		button.getStyleClass().add(cssClass);
		button.setDisable(!enabled);
		button.setGraphic(new ImageView(buttonImage));
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		Tooltip buttonTooltip = new Tooltip(props.getProperty(tooltip.toString()));
		button.setTooltip(buttonTooltip);
		parent.getChildren().add(button);

		button.setPrefHeight(20);

		// BIND ENTER KEY TO BUTTON ACTION
		button.defaultButtonProperty().bind(button.focusedProperty());

		return button;
	}
}
