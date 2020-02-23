package com.wikipedia.test;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wikipedia.json.NamespaceTypeAdapter;
import com.wikipedia.json.PostProcessingTypeAdapter;
import com.wikipedia.json.UriTypeAdapter;
import com.wikipedia.json.WikiSiteTypeAdapter;
import com.wikipedia.page.Namespace;

import org.junit.Before;
import com.wikipedia.dataclient.RestService;
import com.wikipedia.dataclient.Service;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.json.NamespaceTypeAdapter;
import com.wikipedia.json.PostProcessingTypeAdapter;
import com.wikipedia.json.UriTypeAdapter;
import com.wikipedia.json.WikiSiteTypeAdapter;
import com.wikipedia.page.Namespace;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class MockRetrofitTest extends MockWebServerTest {
    private Service apiService;
    private RestService restService;
    private WikiSite wikiSite = WikiSite.forLanguageCode("en");

    protected WikiSite wikiSite() {
        return wikiSite;
    }

    @Override
    @Before
    public void setUp() throws Throwable {
        super.setUp();
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .baseUrl(server().getUrl())
                .build();
        apiService = retrofit.create(Service.class);
        restService = retrofit.create(RestService.class);
    }

    protected Service getApiService() {
        return apiService;
    }

    protected RestService getRestService() {
        return restService;
    }

    private Gson getGson() {
        return new GsonBuilder()
                .registerTypeHierarchyAdapter(Uri.class, new UriTypeAdapter().nullSafe())
                .registerTypeHierarchyAdapter(Namespace.class, new NamespaceTypeAdapter().nullSafe())
                .registerTypeAdapter(WikiSite.class, new WikiSiteTypeAdapter().nullSafe())
                .registerTypeAdapterFactory(new PostProcessingTypeAdapter())
                .create();
    }
}
