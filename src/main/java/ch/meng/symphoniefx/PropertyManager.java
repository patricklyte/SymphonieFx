package ch.meng.symphoniefx;

import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyManager {
    public static final String TRUE_VALUE = "true";
    public static final String FALSE_VALUE = "false";
    protected org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MethodHandles.lookup().lookupClass());
    java.util.Properties properties = new Properties();
    private final String fileName;
    private static final String USER_DIRECTORY = System.getProperty("user.dir");
    private static final String FILE_SEPERATOR = System.getProperty("file.separator");
    private final String prefix;

    public PropertyManager(String fileName, String prefix) {
        this.fileName = fileName;
        this.prefix = prefix + ":";
    }

    private String getFullFilename() {
        return USER_DIRECTORY + FILE_SEPERATOR + fileName + ".properties";
    }

    public String getProperty(final String key) {
        String text = properties.getProperty(prefix + key);
        if (text == null) return "";
        return text;
    }

    public boolean getBooleanProperty(final String key) {
        return getProperty(key).equals(TRUE_VALUE);
    }

    public double getDoubleProperty(final String key) {
        String text = getProperty(key);
        if (text.isEmpty()) return 0;
        return Double.parseDouble(text);
    }

    public long getLongProperty(final String key, long defaultValue) {
        String text = getProperty(key);
        if (text.isEmpty()) return defaultValue;
        try {
            return Long.valueOf(text);
        } catch (Exception ignore) {
            logger.error(ignore);
        }
        return defaultValue;
    }

    public void setProperty(final String key, final String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) return;
        properties.put(prefix + key, value);
    }

    public void setProperty(final String key, final boolean value) {
        if(value) setProperty(key, TRUE_VALUE);
        else setProperty(key, FALSE_VALUE);
    }

    public void setProperty(final String key, double value) {
        setProperty(key, Double.toString(value));
    }

    public void setProperty(final String key, int value) {
        setProperty(key, Integer.toString(value));
    }

    public boolean load() {
        File propertyFile = new File(getFullFilename());
        if (!propertyFile.canRead()) {
            return false;
        }

        FileInputStream stream;
        try {
            stream = new FileInputStream(propertyFile);
            properties.loadFromXML(stream);
            stream.close();
        } catch (IOException exception) {
            logger.error(exception);
            return false;
        }
        return true;
    }

    public boolean save() {
        File propertyFile = new File(getFullFilename());

        try {
            propertyFile.delete();
            if (!propertyFile.createNewFile()) {
                Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, "Failed to Save to:" + getFullFilename());
                return false;
            }
            FileOutputStream stream = new FileOutputStream(propertyFile);
            properties.storeToXML(stream, "Test");
            stream.close();
        } catch (IOException exception) {
            logger.error(exception);
        }
        return true;
    }

    public void initFromSavedProperty(final Object uiObject) {
        if(uiObject==null) return;
        initFromSavedProperty(uiObject, "");
    }

    public boolean initFromSavedProperty(final Object uiObject, final String key) {
        try {
            final String value = getProperty(key);
            if (key == null || value == null) return false;
            if (uiObject instanceof ChoiceBox choiceBox) {
                if (value.isEmpty()) return false;
                choiceBox.setValue(value);
                return true;
            } else if (uiObject instanceof CheckBox choiceBox) {
                choiceBox.setSelected((Boolean.parseBoolean(value)));
                return true;
            } else if (uiObject instanceof TabPane tabPane) {
                tabPane.getSelectionModel().select((Integer.parseInt(value)));
                return true;
            } else if (uiObject instanceof Slider slider) {
                slider.setValue(Double.parseDouble(value));
                return true;
            } else if (uiObject instanceof CheckMenuItem item) {
                if(!value.isEmpty()) item.setSelected(Boolean.parseBoolean(value));
                return true;
            } else if (uiObject instanceof Stage) loadStagePosition((Stage) uiObject);
            else if (uiObject instanceof TextField textField) {
                textField.setText(value);
                return true;
            }
        } catch (Exception exception) {
            logger.error(exception);
        }
        return false;
    }

    private void loadStagePosition(final Stage stage) {
        if(stage==null) return;
        if (!load()) return;
        final double x = getDoubleProperty("X");
        final double y = getDoubleProperty("Y");
        final double width = getDoubleProperty("Width");
        final double height = getDoubleProperty("Height");
        if (width < 40 || height < 40 || x < -(width - 20) || y < -(height - 20)) return;
        stage.setX(x);
        stage.setY(y);
        stage.setWidth(width);
        stage.setHeight(height);
    }

    public void saveUIState(final Object uiObject) {
        if(uiObject!=null) saveUIState(uiObject, "");
    }

    public void saveUIState(final Object uiObject, final String key) {
        if(uiObject==null) return;
        if (uiObject instanceof ChoiceBox choiceBox) {
            if (choiceBox.getValue() instanceof String) {
                setProperty(key, (String) choiceBox.getValue());
            }
            return;
        } else if (uiObject instanceof CheckBox choiceBox) {
            if (choiceBox.isSelected()) {
                setProperty(key, TRUE_VALUE);
            } else {
                setProperty(key, FALSE_VALUE);
            }
        } else if (uiObject instanceof CheckMenuItem item) {
            if (item.isSelected()) {
                setProperty(key, TRUE_VALUE);
            } else {
                setProperty(key, FALSE_VALUE);
            }
        } else if (uiObject instanceof Stage stage) {
            setProperty("Width", stage.getWidth());
            setProperty("Height", stage.getHeight());
            setProperty("X", stage.getX());
            setProperty("Y", stage.getY());
        } else if (uiObject instanceof TextField textField) {
            setProperty(key, textField.getText());
        }  else if (uiObject instanceof Slider slider) {
            setProperty(key, slider.getValue());
        } else if (uiObject instanceof TabPane pane) {
            setProperty(key, pane.getSelectionModel().getSelectedIndex());
        }
    }


}
