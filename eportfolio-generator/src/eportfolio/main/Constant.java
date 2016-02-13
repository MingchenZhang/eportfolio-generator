/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.main;

/**
 *
 * @author zmc94
 */
public class Constant {
	// program data path
	public final static String PATH_DATA = "./data/";
	public final static String PATH_ICONS = PATH_DATA+"icons/";
	public final static String PATH_PAGE_INFO = PATH_DATA+"page_ptototypes/";
	public final static String PATH_LAYOUT_FOLDER = PATH_DATA + "layouts/";
	public final static String PATH_PREVIEW_FOLDER = "./preview_site/";
	// program data file name
	public final static String UI_PROPERTIES_FILE_NAME = "properties_";
    public final static String UI_PROPERTIES_TYPES[] = {"English","Chinese"};
    public final static String UI_PROPERTIES_TYPES_NAMES[] = {"EN.xml","CN.xml"};
    public final static String PROPERTIES_SCHEMA_FILE_NAME = "properties_schema.xsd";
	
	// LANGUAGE SELECTION WINDOW
    public final static String LANGUAGE_WINDOW_TITLE = "Please select a language";
    public final static String LANGUAGE_WINDOW_LANGUAGE = "Language";
	
	//icon file names
	public final static String ICON_NEW_FILE = "newfile.png";
	public final static String ICON_OPEN_FILE = "open.png";
	public final static String ICON_SAVE_FILE = "save.png";
	public final static String ICON_SAVE_AS_FILE = "saveas.png";
	public final static String ICON_EXPORT_FILE = "export.png";
	public final static String ICON_EXIT = "close.png";
	public final static String ICON_NEW_PAGE_FILE = "newpage.png";
	public final static String ICON_REMOVE_PAGE_FILE = "removepage.png";
	public final static String ICON_REFRESH_PREVIEW = "refresh.png";
	public final static String ICON_REMOVE_ELEMENT = "delete-element.png";
	public final static String ICON_MOVE_UP_ELEMENT = "move-up-element.png";
	public final static String ICON_MOVE_DOWN_ELEMENT = "move-down-element.png";
	
	//css
	public final static String CSS_FILE_TOOLBAR = "file-toolbar";
	public final static String CSS_PAGE_TOOLBAR = "page-toolbar";
	public final static String CSS_FILE_TOOLBAR_BUTTON = "file-toolbar-button";
	public final static String CSS_FILE_TOOLBAR_RADIOBUTTON = "file-toolbar-radiobutton";
	public final static String CSS_PAGE_SELECTOR_LABEL = "page-selector-label";
	public final static String CSS_PAGE_SELECTOR = "page-selector";
	public final static String CSS_NEW_PAGE_BUTTON = "new-page-button";
	public final static String CSS_REMOVE_PAGE_BUTTON = "remove-page-button";
	public final static String CSS_REFRESH_PREVIEW_BUTTON = "refresh-preview-button";
	public final static String CSS_USERHINT_LABEL = "userhint-label";
	public final static String CSS_USERHINT_BAR = "userhint-bar";
	public final static String CSS_PROPERTY_VBOX = "property-vbox";
	public final static String CSS_CONTROL_PANE = "control-pane";
	public final static String CSS_ELEMENT_CONTROL_BUTTON = "element-control-button";
}
