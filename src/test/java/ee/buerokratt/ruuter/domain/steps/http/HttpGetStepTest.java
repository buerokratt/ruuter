package ee.buerokratt.ruuter.domain.steps.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import ee.buerokratt.ruuter.BaseTest;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest
class HttpGetStepTest extends BaseTest {

    @Test
    void execute_shouldQueryEndpointAndStoreResponse(WireMockRuntimeInfo wmRuntimeInfo) {
        ConfigurationInstance instance = new ConfigurationInstance(scriptingHelper, applicationProperties, new HashMap<>(), new HashMap<>(), new HashMap<>(), "", tracer);
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("http://localhost:%s/endpoint".formatted(wmRuntimeInfo.getHttpPort()));
        }};
        HttpStep expectedGetStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
        }};

        stubFor(get("/endpoint?some_val=Hello+World&another_val=123").willReturn(ok()));
        expectedGetStep.execute(instance);

        assertEquals(200, ((HttpStepResult) instance.getContext().get("the_response")).getResponse().getStatus());
    }
}
