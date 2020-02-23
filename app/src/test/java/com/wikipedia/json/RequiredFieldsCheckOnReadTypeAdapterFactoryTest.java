package com.wikipedia.json;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wikipedia.json.annotations.Required;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import com.wikipedia.dataclient.Service;
import com.wikipedia.json.annotations.Required;
import com.wikipedia.model.BaseModel;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static com.wikipedia.json.GsonMarshaller.marshal;
import static com.wikipedia.json.GsonUnmarshaller.unmarshal;

@RunWith(RobolectricTestRunner.class)
public class RequiredFieldsCheckOnReadTypeAdapterFactoryTest {
    private final Gson gson = GsonUtil.getDefaultGsonBuilder().serializeNulls().create();

    @Test
    public void testRequireNonNull() {
        RequiredModel expected = new RequiredModel();
        expected.field = 1;
        RequiredModel result = GsonUnmarshaller.unmarshal(gson, RequiredModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, is(expected));
    }

    @Test
    public void testRequireNull() {
        RequiredModel model = new RequiredModel();
        RequiredModel result = GsonUnmarshaller.unmarshal(gson, RequiredModel.class, GsonMarshaller.marshal(gson, model));
        assertThat(result, nullValue());
    }

    @Test
    public void testRequireMissing() {
        RequiredModel result = GsonUnmarshaller.unmarshal(gson, RequiredModel.class, "{}");
        assertThat(result, nullValue());
    }

    @Test
    public void testOptionalNonNull() {
        OptionalModel expected = new OptionalModel();
        expected.field = 1;
        OptionalModel result = GsonUnmarshaller.unmarshal(gson, OptionalModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, is(expected));
    }

    @Test
    public void testOptionalNull() {
        OptionalModel expected = new OptionalModel();
        OptionalModel result = GsonUnmarshaller.unmarshal(gson, OptionalModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, is(expected));
    }

    @Test
    public void testOptionalMissing() {
        OptionalModel expected = new OptionalModel();
        OptionalModel result = GsonUnmarshaller.unmarshal(gson, OptionalModel.class, "{}");
        assertThat(result, is(expected));
    }

    @Test
    public void testRequiredTypeAdapterNonNull() {
        RequiredTypeAdapterModel expected = new RequiredTypeAdapterModel();
        expected.uri = Uri.parse(Service.WIKIPEDIA_URL);
        RequiredTypeAdapterModel result = GsonUnmarshaller.unmarshal(gson, RequiredTypeAdapterModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, is(expected));
    }

    @Test
    public void testRequiredTypeAdapterNull() {
        RequiredTypeAdapterModel expected = new RequiredTypeAdapterModel();
        RequiredTypeAdapterModel result = GsonUnmarshaller.unmarshal(gson, RequiredTypeAdapterModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, nullValue());
    }

    @Test
    public void testRequiredTypeAdapterMissing() {
        RequiredTypeAdapterModel result = GsonUnmarshaller.unmarshal(gson, RequiredTypeAdapterModel.class, "{}");
        assertThat(result, nullValue());
    }

    @Test
    public void testOptionalTypeAdapterNonNull() {
        OptionalTypeAdapterModel expected = new OptionalTypeAdapterModel();
        expected.uri = Uri.parse(Service.WIKIPEDIA_URL);
        OptionalTypeAdapterModel result = GsonUnmarshaller.unmarshal(gson, OptionalTypeAdapterModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, is(expected));
    }

    @Test
    public void testOptionalTypeAdapterNull() {
        OptionalTypeAdapterModel expected = new OptionalTypeAdapterModel();
        OptionalTypeAdapterModel result = GsonUnmarshaller.unmarshal(gson, OptionalTypeAdapterModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, is(expected));
    }

    @Test
    public void testOptionalTypeAdapterMissing() {
        OptionalTypeAdapterModel expected = new OptionalTypeAdapterModel();
        OptionalTypeAdapterModel result = GsonUnmarshaller.unmarshal(gson, OptionalTypeAdapterModel.class, "{}");
        assertThat(result, is(expected));
    }

    @Test
    public void testRequiredSerializedNameNonNull() {
        SerializedNameModel expected = new SerializedNameModel();
        expected.bar = "hello world";
        SerializedNameModel result = GsonUnmarshaller.unmarshal(gson, SerializedNameModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, is(expected));
    }

    @Test
    public void testRequiredSerializedNameNull() {
        SerializedNameModel expected = new SerializedNameModel();
        SerializedNameModel result = GsonUnmarshaller.unmarshal(gson, SerializedNameModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, nullValue());
    }

    @Test
    public void testRequiredSerializedNameMissing() {
        SerializedNameModel result = GsonUnmarshaller.unmarshal(gson, SerializedNameModel.class, "{}");
        assertThat(result, nullValue());
    }

    @Test
    public void testComposedValid() {
        RequiredModel required = new RequiredModel();
        required.field = 1;
        OptionalModel optional = new OptionalModel();
        ComposedModel expected = new ComposedModel();
        expected.required = required;
        expected.optional = optional;

        ComposedModel result = GsonUnmarshaller.unmarshal(gson, ComposedModel.class, GsonMarshaller.marshal(gson, expected));
        assertThat(result, is(expected));
    }

    @Test
    public void testComposedInvalid() {
        RequiredModel required = new RequiredModel();
        OptionalModel optional = new OptionalModel();
        ComposedModel aggregated = new ComposedModel();
        aggregated.required = required;
        aggregated.optional = optional;

        ComposedModel result = GsonUnmarshaller.unmarshal(gson, ComposedModel.class, GsonMarshaller.marshal(gson, aggregated));
        assertThat(result, nullValue());
    }

    private static class RequiredModel extends BaseModel {
        @SuppressWarnings("NullableProblems") @Required
        @NonNull private Integer field;
    }

    private static class OptionalModel extends BaseModel {
        @Nullable private Integer field;
    }

    private static class ComposedModel extends BaseModel {
        @SuppressWarnings("NullableProblems") @Required @NonNull private RequiredModel required;
        @Nullable private OptionalModel optional;
    }

    private static class RequiredTypeAdapterModel extends BaseModel {
        @SuppressWarnings("NullableProblems") @Required @NonNull private Uri uri;
    }

    private static class OptionalTypeAdapterModel extends BaseModel {
        @Nullable private Uri uri;
    }

    private static class SerializedNameModel extends BaseModel {
        @SuppressWarnings("NullableProblems") @SerializedName("foo") @Required @NonNull private String bar;
    }
}
