package com.dreamcast.automation.model;

public enum StepAction {

    CLICK("click"),
    SEND_KEYS("sendKeys"),
    CLEAR("clear"),
    SUBMIT("submit"),
    MOUSE_HOVER("mouseHover"),
    DOUBLE_CLICK("doubleClick"),
    RIGHT_CLICK("rightClick"),
    GET_TEXT("getText"),
    GET_ATTRIBUTE("getAttribute"),
    SELECT_BY_TEXT("selectByText"),
    SELECT_BY_VALUE("selectByValue"),
    SELECT_BY_INDEX("selectByIndex"),
    IS_DISPLAYED("isDisplayed"),
    IS_ENABLED("isEnabled"),
    IS_SELECTED("isSelected"),
    SCROLL_TO("scrollTo"),
    SCROLL_INTO_VIEW("scrollIntoView"),
    WAIT_VISIBLE("waitVisible"),
    WAIT_CLICKABLE("waitClickable"),
    WAIT_PRESENCE("waitPresence"),
    SWITCH_FRAME("switchFrame"),
    SWITCH_WINDOW("switchWindow"),
    ALERT_ACCEPT("alertAccept"),
    ALERT_DISMISS("alertDismiss"),
    NAVIGATE_BACK("navigateBack"),
    NAVIGATE_FORWARD("navigateForward"),
    REFRESH("refresh"),
    EXECUTE_JS("executeJS"),
    UPLOAD_FILE("uploadFile"),
    DRAG_AND_DROP("dragAndDrop"),
    PRESS_KEY("pressKey"),
    TAKE_SCREENSHOT("takeScreenshot"),
    VERIFY_TEXT("verifyText"),
    VERIFY_TITLE("verifyTitle"),
    VERIFY_URL("verifyURL"),
    ASSERT_TEXT("assertText"),
    CLEAR_AND_TYPE("clearAndType"),
    HIGHLIGHT("highlight"),
    WAIT("wait");

    private final String label;

    StepAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String[] allLabels() {
        StepAction[] values = values();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].label;
        }
        return labels;
    }

    @Override
    public String toString() {
        return label;
    }
}
