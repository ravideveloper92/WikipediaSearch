package com.wikipedia.bridge

import android.content.Context
import com.wikipedia.BuildConfig
import com.wikipedia.R
import com.wikipedia.WikipediaApp
import com.wikipedia.auth.AccountUtil
import com.wikipedia.dataclient.RestService
import com.wikipedia.page.Namespace
import com.wikipedia.page.PageTitle
import com.wikipedia.page.PageViewModel
import com.wikipedia.settings.Prefs
import com.wikipedia.util.DimenUtil
import com.wikipedia.util.DimenUtil.getDensityScalar
import com.wikipedia.util.DimenUtil.leadImageHeightForDevice
import com.wikipedia.util.L10nUtil
import com.wikipedia.util.L10nUtil.formatDateRelative
import kotlin.math.roundToInt

object JavaScriptActionHandler {
    @JvmStatic
    fun setTopMargin(top: Int): String {
        return String.format("pcs.c1.Page.setMargins({ top:'%dpx', right:'%dpx', bottom:'%dpx', left:'%dpx' })", top + 16, 16, 48, 16)
    }

    @JvmStatic
    fun getTextSelection(): String {
        return "pcs.c1.InteractionHandling.getSelectionInfo()"
    }

    @JvmStatic
    fun getOffsets(): String {
        return "pcs.c1.Sections.getOffsets(document.body);"
    }

    @JvmStatic
    fun getSections(): String {
        return "pcs.c1.Page.getTableOfContents()"
    }

    @JvmStatic
    fun getProtection(): String {
        return "pcs.c1.Page.getProtection()"
    }

    @JvmStatic
    fun getRevision(): String {
        return "pcs.c1.Page.getRevision();"
    }

    @JvmStatic
    fun scrollToFooter(context: Context): String {
        return "window.scrollTo(0, document.getElementById('pcs-footer-container-menu').offsetTop - ${com.wikipedia.util.DimenUtil.getNavigationBarHeight(context)});"
    }

    @JvmStatic
    fun scrollToAnchor(anchorLink: String): String {
        val anchor = if (anchorLink.contains("#")) anchorLink.substring(anchorLink.indexOf("#") + 1) else anchorLink
        return "var el = document.getElementById('$anchor');" +
                "window.scrollTo(0, el.offsetTop - (screen.height / 2));" +
                "setTimeout(function(){ el.style.backgroundColor='#ee0';" +
                "    setTimeout(function(){ el.style.backgroundColor=null; }, 500);" +
                "}, 250);"
    }

    @JvmStatic
    fun setUp(title: com.wikipedia.page.PageTitle): String {
        val app: com.wikipedia.WikipediaApp = com.wikipedia.WikipediaApp.getInstance()
        val topActionBarHeight = (app.resources.getDimensionPixelSize(R.dimen.lead_no_image_top_offset_dp) / getDensityScalar()).roundToInt()
        val res = com.wikipedia.util.L10nUtil.getStringsForArticleLanguage(title, intArrayOf(R.string.description_edit_add_description,
                R.string.table_infobox, R.string.table_other, R.string.table_close))

        return String.format("{" +
                "   \"platform\": \"pcs.c1.Platforms.ANDROID\"," +
                "   \"clientVersion\": \"${com.wikipedia.BuildConfig.VERSION_NAME}\"," +
                "   \"l10n\": {" +
                "       \"addTitleDescription\": \"${res[R.string.description_edit_add_description]}\"," +
                "       \"tableInfobox\": \"${res[R.string.table_infobox]}\"," +
                "       \"tableOther\": \"${res[R.string.table_other]}\"," +
                "       \"tableClose\": \"${res[R.string.table_close]}\"" +
                "   }," +
                "   \"theme\": \"${app.currentTheme.funnelName}\"," +
                "   \"dimImages\": ${(app.currentTheme.isDark && com.wikipedia.settings.Prefs.shouldDimDarkModeImages())}," +
                "   \"margins\": { \"top\": \"%dpx\", \"right\": \"%dpx\", \"bottom\": \"%dpx\", \"left\": \"%dpx\" }," +
                "   \"leadImageHeight\": \"%dpx\"," +
                "   \"areTablesInitiallyExpanded\": ${!com.wikipedia.settings.Prefs.isCollapseTablesEnabled()}," +
                "   \"textSizeAdjustmentPercentage\": \"100%%\"," +
                "   \"loadImages\": ${com.wikipedia.settings.Prefs.isImageDownloadEnabled()}," +
                "   \"userGroups\": \"${com.wikipedia.auth.AccountUtil.getGroups()}\"" +
                "}", topActionBarHeight + 16, 16, 48, 16, (leadImageHeightForDevice() / getDensityScalar()).roundToInt() - topActionBarHeight)
    }

    @JvmStatic
    fun setUpEditButtons(isEditable: Boolean, isProtected: Boolean): String {
        return "pcs.c1.Page.setEditButtons($isEditable, $isProtected)"
    }

    @JvmStatic
    fun setFooter(model: com.wikipedia.page.PageViewModel): String {
        if (model.page == null) {
            return ""
        }
        val showEditHistoryLink = !(model.page!!.isMainPage || model.page!!.isFilePage)
        val lastModifiedDate = formatDateRelative(model.page!!.pageProperties.lastModified)
        val showTalkLink = !(model.page!!.title.namespace() === com.wikipedia.page.Namespace.TALK)
        val showMapLink = model.page!!.pageProperties.geo != null
        val res = com.wikipedia.util.L10nUtil.getStringsForArticleLanguage(model.title, intArrayOf(R.string.read_more_section,
                R.string.page_similar_titles, R.string.about_article_section,
                R.string.edit_history_link_text, R.string.last_updated_text, R.string.page_footer_license_text,
                R.string.talk_page_link_text, R.string.page_view_in_browser, R.string.content_license_cc_by_sa,
                R.string.map_view_link_text, R.string.reference_list_title))

        // TODO: page-library also supports showing disambiguation ("similar pages") links and
        // "page issues". We should be mindful that they exist, even if we don't want them for now.

        return "pcs.c1.Footer.add({" +
                "   platform: pcs.c1.Platforms.ANDROID," +
                "   clientVersion: '${com.wikipedia.BuildConfig.VERSION_NAME}'," +
                "   title: '${model.title!!.prefixedText}'," +
                "   menu: {" +
                "       items: [" +
                                (if (showEditHistoryLink) "pcs.c1.Footer.MenuItemType.lastEdited, " else "") +
                                (if (showTalkLink) "pcs.c1.Footer.MenuItemType.talkPage, " else "") +
                                (if (showMapLink) "pcs.c1.Footer.MenuItemType.coordinate, " else "") +
                "               pcs.c1.Footer.MenuItemType.referenceList " +
                "              ]" +
                "   }," +
                "   l10n: {" +
                "           'readMoreHeading': '${res[R.string.read_more_section]}'," +
                "           'menuDisambiguationTitle': '${res[R.string.page_similar_titles]}'," +
                "           'menuHeading': '${res[R.string.about_article_section]}'," +
                "           'menuLastEditedSubtitle': '${res[R.string.edit_history_link_text]}'," +
                "           'menuLastEditedTitle': '${String.format(res[R.string.last_updated_text], lastModifiedDate)}'," +
                "           'licenseString': '${res[R.string.page_footer_license_text]}'," +
                "           'menuTalkPageTitle': '${res[R.string.talk_page_link_text]}'," +
                "           'viewInBrowserString': '${res[R.string.page_view_in_browser]}'," +
                "           'licenseSubstitutionString': '${res[R.string.content_license_cc_by_sa]}'," +
                "           'menuCoordinateTitle': '${res[R.string.map_view_link_text]}'," +
                "           'menuReferenceListTitle': '${res[R.string.reference_list_title]}'" +
                "       }," +
                "   readMore: { " +
                "       itemCount: 3," +
                "       baseURL: '${model.title?.wikiSite?.url() + com.wikipedia.dataclient.RestService.REST_API_PREFIX}'" +
                "   }" +
                "})"
    }
}
