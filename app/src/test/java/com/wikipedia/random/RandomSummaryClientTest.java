package com.wikipedia.random;

import com.google.gson.stream.MalformedJsonException;

import org.junit.Test;
import com.wikipedia.dataclient.page.PageSummary;
import com.wikipedia.test.MockRetrofitTest;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

public class RandomSummaryClientTest extends MockRetrofitTest {

    @Test
    public void testRequestEligible() throws Throwable {
        enqueueFromFile("rb_page_summary_valid.json");

        TestObserver<PageSummary> observer = new TestObserver<>();
        getObservable().subscribe(observer);

        observer.assertComplete().assertNoErrors()
                .assertValue(summary -> summary != null
                        && summary.getDisplayTitle().equals("Fermat's Last Theorem")
                        && summary.getDescription().equals("theorem in number theory"));
    }

    @Test public void testRequestMalformed() {
        enqueueMalformed();

        TestObserver<PageSummary> observer = new TestObserver<>();
        getObservable().subscribe(observer);
        observer.assertError(MalformedJsonException.class);
    }

    @Test public void testRequestFailure() {
        enqueue404();

        TestObserver<PageSummary> observer = new TestObserver<>();
        getObservable().subscribe(observer);
        observer.assertError(Exception.class);
    }

    private Observable<PageSummary> getObservable() {
        return getRestService().getRandomSummary();
    }
}
