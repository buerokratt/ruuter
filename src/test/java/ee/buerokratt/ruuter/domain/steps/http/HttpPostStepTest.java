package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.StepTestBase;
import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import ee.buerokratt.ruuter.helper.HttpHelper;
import ee.buerokratt.ruuter.helper.MappingHelper;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import ee.buerokratt.ruuter.service.ConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WireMockTest
class HttpPostStepTest extends StepTestBase {

    @Mock
    private MappingHelper mappingHelper;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private HttpHelper httpHelper;

    @Mock
    private ScriptingHelper scriptingHelper;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private BiPredicate<String, String> biPredicate;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ApplicationProperties.Logging logging;

    private HttpHeaders httpHeaders;

    @BeforeEach
    protected void mockDependencies() {
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        when(ci.getProperties()).thenReturn(properties);
        when(ci.getHttpHelper()).thenReturn(httpHelper);
        when(ci.getMappingHelper()).thenReturn(mappingHelper);
        httpHeaders = HttpHeaders.of(new HashMap<>(), biPredicate);
    }

    @Test
    void execute_shouldSendPostRequestAndStoreResponse(WireMockRuntimeInfo wireMockRuntimeInfo) {
        HashMap<String, Object> testContext = new HashMap<>();
        HttpQueryArgs expectedPostArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
        }};
        HttpStep expectedPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_response");
        }};

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getHttpHelper().makeHttpPostRequest(eq(expectedPostArgs), anyMap())).thenReturn(httpResponse);
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
        when(scriptingHelper.evaluateMapValues(anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(new HashMap<>());
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        when(properties.getLogging()).thenReturn(logging);
        when(logging.getDisplayRequestContent()).thenReturn(false);
        expectedPostStep.execute(ci);

        assertEquals(200, ((HttpStepResult) ci.getContext().get("the_response")).getResponse().getStatus());
    }

    @Test
    void execute_shouldExecuteDefaultActionWhenResponseCodeIsNotInWhitelist(WireMockRuntimeInfo wireMockRuntimeInfo) {
        DefaultAction defaultAction = Mockito.spy(new DefaultAction() {{
            setService("default-action");
            setBody(new HashMap<>());
            setQuery(new HashMap<>());
        }});
        HashMap<String, Object> testContext = new HashMap<>();
        HttpQueryArgs expectedPostArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wireMockRuntimeInfo.getHttpPort()));
        }};
        HttpStep expectedPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_response");
        }};

        when(ci.getContext()).thenReturn(testContext);
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
        when(ci.getHttpHelper().makeHttpPostRequest(any(), any())).thenReturn(httpResponse);
        when(ci.getConfigurationService()).thenReturn(configurationService);
        when(properties.getDefaultAction()).thenReturn(defaultAction);
        doCallRealMethod().when(defaultAction).executeDefaultAction(eq(ci), anyString());
        when(ci.getRequestOrigin()).thenReturn("");
        when(properties.getHttpCodesAllowList()).thenReturn(new ArrayList<>() {{add(200);}});
        when(httpResponse.body()).thenReturn("body");
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        expectedPostStep.execute(ci);

        verify(configurationService, times(1)).execute(eq("default-action"), anyMap(), anyMap(), anyString());
    }
}
