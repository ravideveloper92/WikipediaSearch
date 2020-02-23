package com.wikipedia.feed.suggestededits

import com.wikipedia.Constants.InvokeSource
import com.wikipedia.R
import com.wikipedia.WikipediaApp
import com.wikipedia.analytics.SuggestedEditsFunnel
import com.wikipedia.dataclient.WikiSite
import com.wikipedia.descriptions.DescriptionEditActivity.Action
import com.wikipedia.feed.model.CardType
import com.wikipedia.feed.model.WikiSiteCard
import com.wikipedia.suggestededits.SuggestedEditsSummary

class SuggestedEditsCard(
        wiki: com.wikipedia.dataclient.WikiSite,
        val action: Action,
        val sourceSummary: SuggestedEditsSummary?,
        val targetSummary: SuggestedEditsSummary?,
        val age: Int
) : com.wikipedia.feed.model.WikiSiteCard(wiki) {

    override fun type(): com.wikipedia.feed.model.CardType {
        return com.wikipedia.feed.model.CardType.SUGGESTED_EDITS
    }

    override fun title(): String {
        return com.wikipedia.WikipediaApp.getInstance().getString(R.string.suggested_edits_feed_card_title)
    }

    fun logImpression() {
        com.wikipedia.analytics.SuggestedEditsFunnel.get(InvokeSource.FEED).impression(action)
    }
}
