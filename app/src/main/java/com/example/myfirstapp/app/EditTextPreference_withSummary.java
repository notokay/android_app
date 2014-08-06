package com.example.myfirstapp.app;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by tommyhu on 8/5/2014.
 */
public class EditTextPreference_withSummary extends EditTextPreference {
    public EditTextPreference_withSummary(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditTextPreference_withSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreference_withSummary(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        String summary = super.getSummary().toString();
        return String.format(summary, getText());
    }

}
