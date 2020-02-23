package com.wikipedia.page

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.widget.ScrollView
import com.wikipedia.WikipediaApp
import com.wikipedia.analytics.FeedFunnel
import com.wikipedia.analytics.LoginFunnel
import com.wikipedia.feed.announcement.Announcement
import com.wikipedia.feed.announcement.AnnouncementCard
import com.wikipedia.feed.announcement.AnnouncementCardView
import com.wikipedia.feed.configure.ConfigureActivity
import com.wikipedia.feed.model.Card
import com.wikipedia.feed.model.CardType
import com.wikipedia.language.LanguageSettingsInvokeSource
import com.wikipedia.login.LoginActivity
import com.wikipedia.settings.Prefs
import com.wikipedia.settings.SettingsActivity
import com.wikipedia.settings.languages.WikipediaLanguagesActivity
import com.wikipedia.util.UriUtil

class AnnouncementDialog internal constructor(context: Context, val announcement: com.wikipedia.feed.announcement.Announcement) : AlertDialog(context), com.wikipedia.feed.announcement.AnnouncementCardView.Callback {

    // TODO: refactor this item when the new Modern Event Platform is finished.
    private val funnel: com.wikipedia.analytics.FeedFunnel = com.wikipedia.analytics.FeedFunnel(com.wikipedia.WikipediaApp.getInstance())

    init {
        val scrollView = ScrollView(context)
        val cardView = com.wikipedia.feed.announcement.AnnouncementCardView(context)
        cardView.setCard(com.wikipedia.feed.announcement.AnnouncementCard(announcement))
        cardView.setCallback(this)
        scrollView.addView(cardView)
        scrollView.isVerticalScrollBarEnabled = true
        setView(scrollView)
    }

    override fun show() {
        funnel.cardShown(com.wikipedia.feed.model.CardType.ARTICLE_ANNOUNCEMENT, com.wikipedia.WikipediaApp.getInstance().appOrSystemLanguageCode)
        super.show()
    }

    override fun onAnnouncementPositiveAction(card: com.wikipedia.feed.model.Card, uri: Uri) {
        when {
            uri.toString() == com.wikipedia.util.UriUtil.LOCAL_URL_LOGIN ->
                context.startActivity(com.wikipedia.login.LoginActivity.newIntent(context, com.wikipedia.analytics.LoginFunnel.SOURCE_NAV))
            uri.toString() == com.wikipedia.util.UriUtil.LOCAL_URL_SETTINGS ->
                context.startActivity(com.wikipedia.settings.SettingsActivity.newIntent(context))
            uri.toString() == com.wikipedia.util.UriUtil.LOCAL_URL_CUSTOMIZE_FEED ->
                context.startActivity(com.wikipedia.feed.configure.ConfigureActivity.newIntent(context, card.type().code()))
            uri.toString() == com.wikipedia.util.UriUtil.LOCAL_URL_LANGUAGES ->
                context.startActivity(com.wikipedia.settings.languages.WikipediaLanguagesActivity.newIntent(context, com.wikipedia.language.LanguageSettingsInvokeSource.ANNOUNCEMENT.text()))
            else -> com.wikipedia.util.UriUtil.handleExternalLink(context, uri)
        }
        funnel.cardClicked(com.wikipedia.feed.model.CardType.ARTICLE_ANNOUNCEMENT, com.wikipedia.WikipediaApp.getInstance().appOrSystemLanguageCode)
        dismissDialog()
    }

    override fun onAnnouncementNegativeAction(card: com.wikipedia.feed.model.Card) {
        funnel.dismissCard(com.wikipedia.feed.model.CardType.ARTICLE_ANNOUNCEMENT, 0)
        dismissDialog()
    }

    private fun dismissDialog() {
        com.wikipedia.settings.Prefs.setAnnouncementShownDialogs(setOf(announcement.id()))
        dismiss()
    }
}
