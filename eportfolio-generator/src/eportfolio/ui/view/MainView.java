/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.ui.view;

import eportfolio.ui.pane.EditPane;
import eporfolio.util.ErrorHandler;
import eporfolio.util.dialog.AlertUser;
import eportfolio.exception.TitleDuplicationException;
import static eportfolio.main.Constant.*;
import eportfolio.main.LanguageEnum;
import static eportfolio.main.LanguageEnum.*;
import eportfolio.model.page.PageModel;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class MainView {

	private MainViewDataInterface data = new MainViewDataInterface(this);

	private Stage primaryStage;
	private ErrorHandler eH = new ErrorHandler(this);

	private BorderPane mainBorderPane = new BorderPane();

	private BorderPane topfileToolBarAndViewBar;
	private HBox viewBar;
	private FlowPane fileToolBar;
	private FlowPane pageSelectorBar;
	private VBox topToolBar = new VBox();
	private BorderPane editWorkspace;
	private EditPane editPane;
	private HBox userHintPane;
	private Timer userHintTimer = new Timer();

	private Button newFileButton;
	private Button openFileButton;
	private Button saveFileButton;
	private Button saveAsFileButton;
	private Button exportFileButton;
	private Button exitButton;
	private ToggleGroup editAndViewToggleGroup = new ToggleGroup();
	private RadioButton editViewRadioButton = new RadioButton();
	private RadioButton viewViewRadioButton = new RadioButton();
	private Label pageSelectorLable = new Label();
	private ComboBox<String> pageSelector = new ComboBox();
	private ChangeListener pageSelectorListener;
	private Button newPageButton = new Button();
	private Button removePageButton = new Button();
	private Button refreshPreviewButton = new Button();
	
	private WebView viewWebView = new WebView();
	private ChangeListener<Worker.State> previewReloadListener;

	public MainView() {

	}

	public ErrorHandler getErrorHandler() {
		return eH;
	}

	public Stage getWindow() {
		return primaryStage;
	}

	public String getPageSelect() {
		return pageSelector.getValue();
	}
	
	public EditPane getEditPane() {return editPane;}

	public void startUI(Stage primaryStage) {
		this.primaryStage = primaryStage;
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		primaryStage.setTitle(props.getProperty(PROGRAM_TITLE));

		fileToolBar = initFileToolbar();
		viewBar = initViewBar();
		topfileToolBarAndViewBar = new BorderPane();
		topfileToolBarAndViewBar.setLeft(fileToolBar);
		topfileToolBarAndViewBar.setRight(viewBar);

		pageSelectorBar = initPageSelectorBar();
		userHintPane = new HBox();
		userHintPane.setMinHeight(20);

		bindHandler();

		prepareForEditView();

		Scene scene = new Scene(mainBorderPane);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public HBox initViewBar() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		editViewRadioButton.setText(props.getProperty(RADIOBUTTON_EDIT));
		viewViewRadioButton.setText(props.getProperty(RADIOBUTTON_VIEW));
		editViewRadioButton.setTooltip(new Tooltip(props.getProperty(TOOLTIP_EDIT_VIEW)));
		viewViewRadioButton.setTooltip(new Tooltip(props.getProperty(TOOLTIP_VIEW_VIEW)));
		editViewRadioButton.getStyleClass().add(CSS_FILE_TOOLBAR_RADIOBUTTON);
		viewViewRadioButton.getStyleClass().add(CSS_FILE_TOOLBAR_RADIOBUTTON);
		
		editViewRadioButton.setToggleGroup(editAndViewToggleGroup);
		viewViewRadioButton.setToggleGroup(editAndViewToggleGroup);
		editViewRadioButton.setSelected(true);
		viewViewRadioButton.setDisable(true);
		
		editAndViewToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (editAndViewToggleGroup.getSelectedToggle() != null) {
					if(editAndViewToggleGroup.getSelectedToggle()==editViewRadioButton){
						prepareForEditView();
						updatePageSelector(true);
						data.refreshEditView();
					}else{
						if(data.getDataModel().getPageCount()<1){
							PropertiesManager props = PropertiesManager.getPropertiesManager();
							setUserHint(props.getProperty(ERROR_PAGE_NOT_ENOUGH_TO_VIEW), 1);
							editViewRadioButton.setSelected(true);
							return;
						}
						prepareForView();
						try {
							data.getDataModel().view();
						} catch (TitleDuplicationException ex) {
							Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
						} catch (IOException ex) {
							Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
						}
						File viewFile = new File(PATH_PREVIEW_FOLDER+data.getDataModel().getPageModel(0).getPageTitle() + ".html");
						showView(viewFile.toURI().toString());
					}
				}
			}
		});

		HBox viewBar = new HBox();
		viewBar.getChildren().addAll(editViewRadioButton, viewViewRadioButton);
		return viewBar;
	}

	@SuppressWarnings("LocalVariableHidesMemberVariable")
	public FlowPane initFileToolbar() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();

		FlowPane fileToolBar = new FlowPane();
		fileToolBar.setHgap(5);//d
		fileToolBar.setPadding(new Insets(1, 5, 1, 5));//d

		newFileButton = initChildButton(fileToolBar, ICON_NEW_FILE, TOOLTIP_NEW_FILE, CSS_FILE_TOOLBAR_BUTTON, true);
		openFileButton = initChildButton(fileToolBar, ICON_OPEN_FILE, TOOLTIP_OPEN_FILE, CSS_FILE_TOOLBAR_BUTTON, true);
		saveFileButton = initChildButton(fileToolBar, ICON_SAVE_FILE, TOOLTIP_SAVE_FILE, CSS_FILE_TOOLBAR_BUTTON, false);
		saveAsFileButton = initChildButton(fileToolBar, ICON_SAVE_AS_FILE, TOOLTIP_SAVE_AS_FILE, CSS_FILE_TOOLBAR_BUTTON, false);
		exportFileButton = initChildButton(fileToolBar, ICON_EXPORT_FILE, TOOLTIP_EXPORT, CSS_FILE_TOOLBAR_BUTTON, false);
		exitButton = initChildButton(fileToolBar, ICON_EXIT, TOOLTIP_EXIT, CSS_FILE_TOOLBAR_BUTTON, true);

		fileToolBar.getStyleClass().add(CSS_FILE_TOOLBAR);
		return fileToolBar;
	}

	@SuppressWarnings("LocalVariableHidesMemberVariable")
	public FlowPane initPageSelectorBar() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();

		FlowPane pageSelectorBar = new FlowPane();
		pageSelectorBar.setHgap(5);//todo:css
		pageSelectorBar.setPadding(new Insets(1, 5, 1, 5));//todo:css

		pageSelectorLable.setText(props.getProperty(LABEL_PAGE_SELECTOR));
		pageSelectorBar.getChildren().addAll(pageSelectorLable, pageSelector);
		pageSelectorLable.getStyleClass().add(CSS_PAGE_SELECTOR_LABEL);
		pageSelector.getStyleClass().add(CSS_PAGE_SELECTOR);
		pageSelector.setPrefWidth(200);// todo:css

		pageSelector.setDisable(true);

		newPageButton = initChildButton(pageSelectorBar, ICON_NEW_PAGE_FILE, TOOLTIP_NEW_PAGE, CSS_NEW_PAGE_BUTTON, false);
		removePageButton = initChildButton(pageSelectorBar, ICON_REMOVE_PAGE_FILE, TOOLTIP_REMOVE_PAGE, CSS_REMOVE_PAGE_BUTTON, false);
		refreshPreviewButton = initChildButton(pageSelectorBar, ICON_REFRESH_PREVIEW, TOOLTIP_REFRESH_PREVIEW, CSS_REFRESH_PREVIEW_BUTTON, false);

		pageSelectorListener = data.new pageSelectorHandler();

		pageSelectorBar.getStyleClass().add(CSS_PAGE_TOOLBAR);
		return pageSelectorBar;
	}

	public void bindHandler() {
		newFileButton.setOnAction(data.new NewFileHandler());
		saveFileButton.setOnAction(data.new SaveFileHandler());
		saveAsFileButton.setOnAction(data.new SaveAsHandler());
		openFileButton.setOnAction(data.new OpenFileHandler());
		exportFileButton.setOnAction(data.new ExportHandler());
		exitButton.setOnAction(data.new ExitHandler());
		newPageButton.setOnAction(data.new NewPageHandler());
		removePageButton.setOnAction(data.new removePageHandler());
		refreshPreviewButton.setOnAction(data.new refreshPreviewHandler());
	}

	public void prepareForEditView() {
		topToolBar.getChildren().clear();
		topToolBar.getChildren().add(topfileToolBarAndViewBar);
		topToolBar.getChildren().add(pageSelectorBar);
		mainBorderPane.setTop(topToolBar);
		mainBorderPane.setCenter(editPane);
		mainBorderPane.setBottom(userHintPane);

		displayPageEditView(null);
	}
	
	public void prepareForView(){
		topToolBar.getChildren().clear();
		topToolBar.getChildren().add(topfileToolBarAndViewBar);
		mainBorderPane.setTop(topToolBar);
		mainBorderPane.setCenter(viewWebView);
		mainBorderPane.setBottom(userHintPane);
	}

	public void displayPageEditView(PageModel pageModel) {
		if(editPane == null)editPane = new EditPane(pageModel, this);
		else editPane.displayPage(pageModel);
		mainBorderPane.setCenter(editPane);
		if (pageModel != null) {
			updatePageSelector(true);
		}
		System.gc();
	}

	public void updatePageSelector(boolean valid) {
		String[] titles = data.getDataModel().getAllPageTitle();
		ObservableList<String> titlesList = FXCollections.observableArrayList();
		for (int i = 0; i < titles.length; i++) {
			titlesList.add(titles[i]);
		}
		pageSelector.valueProperty().removeListener(pageSelectorListener);
		pageSelector.setItems(titlesList);
		pageSelector.setValue(data.getCurrentPageTitle());
		pageSelector.valueProperty().addListener(pageSelectorListener);
		pageSelector.setDisable(!valid);
		if((data.getDataModel().getPageCount()==0))
			pageSelector.setDisable(true);
		newPageButton.setDisable(!valid);
		removePageButton.setDisable((data.getDataModel().getPageCount()==0)?true:false);
		refreshPreviewButton.setDisable((data.getDataModel().getPageCount()==0)?true:false);
		
		viewViewRadioButton.setDisable(false);//todo: change if close file is implement
	}
	
	/**
	 * 
	 * @param status 
	 * 1: file created
	 * 2: file saved
	 *    file loaded
	 *    file save as
	 * 3: any changes made to data model
	 */
	public void changeFileToolbarStatus(int status) {//todo:finish it
		if (status == 1) {
			saveFileButton.setDisable(false);
			saveAsFileButton.setDisable(false);
			exportFileButton.setDisable(false);
		}else if(status == 2){
			saveFileButton.setDisable(true);
			saveAsFileButton.setDisable(false);
			exportFileButton.setDisable(false);
		}else if(status == 3){
			saveFileButton.setDisable(false);
			saveAsFileButton.setDisable(false);
			exportFileButton.setDisable(false);
		}
		//TODO: add more status
	}
	
	public void showView(String url){
		viewWebView.getEngine().load(url);
		if(previewReloadListener!=null)
			viewWebView.getEngine().getLoadWorker().stateProperty().removeListener(previewReloadListener);
		previewReloadListener = getPreviewReloadListener();
		viewWebView.getEngine().getLoadWorker().stateProperty().addListener(previewReloadListener);
		
		System.out.println("viewing: " + url);
	}

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
	public Button initChildButton(
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

	public void setUserHint(String userHint, int seriousness) {
		userHintPane.getChildren().clear();
		Label userHintLabel = new Label();
		userHintLabel.setText(userHint);
		userHintPane.getChildren().add(userHintLabel);
		setupUserHintTimer(userHintLabel, seriousness);
	}
	
	public void setUserHint(LanguageEnum userHint, String additionUserHint, int seriousness) {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		userHintPane.getChildren().clear();
		Label userHintLabel = new Label();
		userHintLabel.setText(props.getProperty(userHint)+"("+additionUserHint+")");
		userHintPane.getChildren().add(userHintLabel);
		setupUserHintTimer(userHintLabel, seriousness);
	}
	
	public void setUserHint(LanguageEnum userHint, int seriousness) {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		userHintPane.getChildren().clear();
		Label userHintLabel = new Label();
		userHintLabel.setText(props.getProperty(userHint));
		userHintPane.getChildren().add(userHintLabel);
		setupUserHintTimer(userHintLabel, seriousness);
	}

	public void setUserHint(String userHint, Exception ex, int seriousness) {
		Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
		userHintPane.getChildren().clear();
		Label userHintLabel = new Label();
		userHintLabel.setText(userHint);
		Hyperlink exceptionLink = new Hyperlink();
		exceptionLink.setText("View Exception");
		exceptionLink.setOnAction(event -> {
			AlertUser.textDialog("Exception", "Detail of exception are followed: ", getStackTrace(ex));
		});
		userHintPane.getChildren().addAll(userHintLabel, exceptionLink);
		setupUserHintTimer(userHintLabel, seriousness);
	}

	public void setUserHint(LanguageEnum userHint, Exception ex, int seriousness) {
		Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		userHintPane.getChildren().clear();
		Label userHintLabel = new Label();
		userHintLabel.setText(props.getProperty(userHint));
		Hyperlink exceptionLink = new Hyperlink();
		exceptionLink.setText(props.getProperty(USERHINT_VIEW_EXCEPTION_DETAIL));
		exceptionLink.setOnAction(event -> {
			AlertUser.textDialog(
					props.getProperty(DIALOG_VIEW_EXCEPTION_TITLE),
					props.getProperty(DIALOG_VIEW_EXCEPTION_HEADER),
					getStackTrace(ex));
		});
		userHintPane.getChildren().addAll(userHintLabel, exceptionLink);
		setupUserHintTimer(userHintLabel, seriousness);
	}
	
	private void setupUserHintTimer(Label userHintLabel, int seriousness){//todo: overlap timer
		userHintTimer.cancel();
		if (seriousness == 0) {
			userHintTimer = new Timer();
			userHintTimer.schedule(new TimerTask() {
				public void run() {
					Platform.runLater(() -> {userHintPane.getChildren().clear();});
					userHintTimer.cancel();
				}
			}, 3000);
		} else if (seriousness == 1) {
			userHintLabel.setTextFill(Color.web("#f00"));
			userHintTimer = new Timer();
			userHintTimer.schedule(new TimerTask() {
				public void run() {
					Platform.runLater(() -> {userHintPane.getChildren().clear();});
					userHintTimer.cancel();
				}
			}, 3000);
		} else if (seriousness == 2) {
			userHintLabel.setTextFill(Color.web("#f00"));
			userHintTimer = new Timer();
			userHintTimer.schedule(new TimerTask() {
				int counter = 0;

				public void run() {
					if (counter > 6) {
						Platform.runLater(() -> {userHintPane.getChildren().clear();});
						userHintTimer.cancel();
					} else if (counter % 2 == 0) {
						Platform.runLater(() -> {userHintLabel.setTextFill(Color.web("#000"));});
					} else {
						Platform.runLater(() -> {userHintLabel.setTextFill(Color.web("#f00"));});
					}
					counter++;
				}
			}, 0, 500);
		}
	}

	private String getStackTrace(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		return sw.toString();
	}
	
	private ChangeListener<Worker.State> getPreviewReloadListener(){
		return new ChangeListener<Worker.State>() {
			boolean first = true;
			@Override
			public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) {
				if (t1.equals(Worker.State.SUCCEEDED) && first) {
					first = false;
					viewWebView.getEngine().reload();
				}
			}
		};
	}
}
