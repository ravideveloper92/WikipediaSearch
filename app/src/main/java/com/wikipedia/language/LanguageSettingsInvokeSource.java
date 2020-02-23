package com.wikipedia.language;

public enum LanguageSettingsInvokeSource {
    DESCRIPTION_EDITING("description_editing"),
    SEARCH("search"),
    SETTINGS("settings"),
    ONBOARDING("onboarding"),
    CHINESE_VARIANT_REMOVAL("chinese_variant_removal"),
    ANNOUNCEMENT("announcement"),
    SUGGESTED_EDITS("suggested_edits");

    private final String text;

    public String text() {
        return text;
    }

    LanguageSettingsInvokeSource(String text) {
        this.text = text;
    }
}
