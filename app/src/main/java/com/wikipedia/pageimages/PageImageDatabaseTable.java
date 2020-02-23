package com.wikipedia.pageimages;

import android.content.ContentValues;
import android.database.Cursor;

import com.wikipedia.page.PageTitle;

import androidx.annotation.NonNull;

import com.wikipedia.database.DatabaseTable;
import com.wikipedia.database.column.Column;
import com.wikipedia.database.contract.PageImageHistoryContract;
import com.wikipedia.database.contract.PageImageHistoryContract.Col;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.page.PageTitle;


// todo: network caching preserves images. Remove this class and drop table?
public class PageImageDatabaseTable extends DatabaseTable<PageImage> {
    private static final int DB_VER_NAMESPACE_ADDED = 7;
    private static final int DB_VER_LANG_ADDED = 10;
    private static final int DB_VER_DISPLAY_TITLE_ADDED = 19;

    public PageImageDatabaseTable() {
        super(PageImageHistoryContract.TABLE, PageImageHistoryContract.Image.URI);
    }

    @Override
    public PageImage fromCursor(Cursor cursor) {
        WikiSite wiki = new WikiSite(Col.SITE.val(cursor), Col.LANG.val(cursor));
        PageTitle title = new PageTitle(Col.NAMESPACE.val(cursor), Col.API_TITLE.val(cursor), wiki);
        String imageName = Col.IMAGE_NAME.val(cursor);
        title.setDisplayText(Col.DISPLAY_TITLE.val(cursor));
        return new PageImage(title, imageName);
    }

    @Override
    protected ContentValues toContentValues(PageImage obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Col.SITE.getName(), obj.getTitle().getWikiSite().authority());
        contentValues.put(Col.LANG.getName(), obj.getTitle().getWikiSite().languageCode());
        contentValues.put(Col.NAMESPACE.getName(), obj.getTitle().getNamespace());
        contentValues.put(Col.API_TITLE.getName(), obj.getTitle().getPrefixedText());
        contentValues.put(Col.DISPLAY_TITLE.getName(), obj.getTitle().getDisplayText());
        contentValues.put(Col.IMAGE_NAME.getName(), obj.getImageName());
        return contentValues;
    }

    @NonNull
    @Override
    public Column<?>[] getColumnsAdded(int version) {
        switch (version) {
            case INITIAL_DB_VERSION:
                return new Column<?>[] {Col.ID, Col.SITE, Col.API_TITLE, Col.IMAGE_NAME};
            case DB_VER_NAMESPACE_ADDED:
                return new Column<?>[] {Col.NAMESPACE};
            case DB_VER_LANG_ADDED:
                return new Column<?>[] {Col.LANG};
            case DB_VER_DISPLAY_TITLE_ADDED:
                return new Column<?>[] {Col.DISPLAY_TITLE};
            default:
                return super.getColumnsAdded(version);
        }
    }

    @Override
    protected String getPrimaryKeySelection(@NonNull PageImage obj, @NonNull String[] selectionArgs) {
        return super.getPrimaryKeySelection(obj, Col.SELECTION);
    }

    @Override
    protected String[] getUnfilteredPrimaryKeySelectionArgs(@NonNull PageImage obj) {
        return new String[] {
                obj.getTitle().getWikiSite().authority(),
                obj.getTitle().getWikiSite().languageCode(),
                obj.getTitle().getNamespace(),
                obj.getTitle().getText()
        };
    }

    @Override
    protected int getDBVersionIntroducedAt() {
        return INITIAL_DB_VERSION;
    }
}
