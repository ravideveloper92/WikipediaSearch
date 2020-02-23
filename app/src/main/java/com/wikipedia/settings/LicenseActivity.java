package com.wikipedia.settings;

import android.os.Bundle;
import android.widget.TextView;

import com.wikipedia.util.FileUtil;
import com.wikipedia.util.StringUtil;

import com.wikipedia.R;
import com.wikipedia.activity.BaseActivity;
import com.wikipedia.util.ResourceUtil;
import com.wikipedia.util.StringUtil;

import java.io.IOException;

import static com.wikipedia.util.FileUtil.readFile;

/**
 * Displays license text of the libraries we use.
 */
public class LicenseActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        setNavigationBarColor(ResourceUtil.getThemedColor(this, android.R.attr.windowBackground));

        final int libraryNameStart = 24;
        if (getIntent().getData() == null
                || getIntent().getData().getPath() == null
                || getIntent().getData().getPath().length() <= libraryNameStart) {
            return;
        }
        final String path = getIntent().getData().getPath();
        // Example string: "/android_asset/licenses/Otto"
        setTitle(getString(R.string.license_title, path.substring(libraryNameStart)));

        try {
            TextView textView = findViewById(R.id.license_text);
            final int assetPathStart = 15;
            final String text = FileUtil.readFile(getAssets().open(path.substring(assetPathStart)));
            textView.setText(StringUtil.fromHtml(text.replace("\n\n", "<br/><br/>")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
