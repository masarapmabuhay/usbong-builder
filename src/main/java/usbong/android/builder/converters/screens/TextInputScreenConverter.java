package usbong.android.builder.converters.screens;

import com.google.gson.Gson;
import usbong.android.builder.enums.UsbongScreenType;
import usbong.android.builder.models.Screen;
import usbong.android.builder.models.details.TextInputScreenDetails;
import usbong.android.builder.utils.JsonUtils;
import usbong.android.builder.utils.StringUtils;

/**
 * Created by Rocky Camacho on 8/10/2014.
 */
public class TextInputScreenConverter implements ScreenConverter {

    private static final String STORE_VARIABLE_FORMAT = "@%s=getInput()";

    @Override
    public String getName(Screen screen) {
        TextInputScreenDetails textInputScreenDetails = JsonUtils.fromJson(screen.details, TextInputScreenDetails.class);
        String screenType = UsbongScreenType.TEXT_FIELD.getName();
        String variablePart = StringUtils.EMPTY;
        if (textInputScreenDetails.isStoreVariable()) {
            variablePart = String.format(STORE_VARIABLE_FORMAT, textInputScreenDetails.getVariableName()) + SEPARATOR;
        }
        String content = StringUtils.toUsbongText(textInputScreenDetails.getText());
        if (textInputScreenDetails.isHasUnit()) {
            screenType = UsbongScreenType.TEXT_FIELD_WITH_UNIT.getName();
            return screenType + SEPARATOR + textInputScreenDetails.getUnit() + SEPARATOR + variablePart + content;
        }
        if (TextInputScreenDetails.ALPHA_NUMERIC.equals(textInputScreenDetails.getInputType())) {
            if (textInputScreenDetails.isMultiLine()) {
                screenType = UsbongScreenType.TEXT_AREA.getName();
            } else {
                screenType = UsbongScreenType.TEXT_FIELD.getName();
            }
        } else if (TextInputScreenDetails.NUMERIC.equals(textInputScreenDetails.getInputType())) {
            screenType = UsbongScreenType.TEXT_FIELD_NUMERICAL.getName();
        }
        return screenType + SEPARATOR + content;
    }
}