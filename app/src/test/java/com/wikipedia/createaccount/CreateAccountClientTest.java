package com.wikipedia.createaccount;

import com.google.gson.stream.MalformedJsonException;
import com.wikipedia.dataclient.Service;
import com.wikipedia.dataclient.mwapi.CreateAccountResponse;
import com.wikipedia.test.MockRetrofitTest;

import org.junit.Test;
import com.wikipedia.dataclient.Service;
import com.wikipedia.dataclient.mwapi.CreateAccountResponse;
import com.wikipedia.test.MockRetrofitTest;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

public class CreateAccountClientTest extends MockRetrofitTest {

    private Observable<CreateAccountResponse> getObservable() {
        return getApiService().postCreateAccount("user", "pass", "pass", "token", Service.WIKIPEDIA_URL, null, null, null);
    }

    @Test public void testRequestSuccess() throws Throwable {
        enqueueFromFile("create_account_success.json");
        TestObserver<CreateAccountResponse> observer = new TestObserver<>();
        getObservable().subscribe(observer);

        observer.assertComplete().assertNoErrors()
                .assertValue(result -> result.status().equals("PASS")
                        && result.user().equals("Farb0nucci"));
    }

    @Test public void testRequestFailure() throws Throwable {
        enqueueFromFile("create_account_failure.json");
        TestObserver<CreateAccountResponse> observer = new TestObserver<>();
        getObservable().subscribe(observer);

        observer.assertComplete().assertNoErrors()
                .assertValue(result -> result.status().equals("FAIL"));
    }

    @Test public void testRequestResponse404() {
        enqueue404();
        TestObserver<CreateAccountResponse> observer = new TestObserver<>();
        getObservable().subscribe(observer);

        observer.assertError(Exception.class);
    }

    @Test public void testRequestResponseMalformed() {
        enqueueMalformed();
        TestObserver<CreateAccountResponse> observer = new TestObserver<>();
        getObservable().subscribe(observer);

        observer.assertError(MalformedJsonException.class);
    }
}
