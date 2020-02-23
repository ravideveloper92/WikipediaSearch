package com.wikipedia.feed.dayheader;

import androidx.annotation.NonNull;

import com.wikipedia.feed.model.Card;
import com.wikipedia.feed.model.CardType;
import com.wikipedia.util.DateUtil;

public class DayHeaderCard extends Card {
    private int age;

    public DayHeaderCard(int age) {
        this.age = age;
    }

    @Override @NonNull public String title() {
        return DateUtil.getFeedCardDayHeaderDate(age);
    }

    @NonNull @Override public CardType type() {
        return CardType.DAY_HEADER;
    }
}
