package core.scenario;

import com.apighost.model.scenario.Scenario;
import com.apighost.model.scenario.result.ResultStep;
import com.apighost.model.scenario.ScenarioResult;
import com.apighost.model.scenario.step.*;
import com.apighost.model.scenario.request.*;
import com.apighost.scenario.callback.ScenarioResultCallback;
import com.apighost.scenario.executor.ScenarioTestExecutor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit test for {@link ScenarioTestExecutor}.
 * <p>
 * This test class verifies the successful execution of a multi-step scenario consisting of HTTP GET
 * requests. It uses a public HTTP endpoint (https://httpbin.org/status/200) to simulate real HTTP
 * responses.
 * </p>
 *
 * <p>Test ensures:</p>
 * <ul>
 *     <li>All steps are executed in sequence according to the route configuration</li>
 *     <li>Each step returns a successful response</li>
 *     <li>The scenario result reflects the correct number of steps</li>
 *     <li>The scenario is marked as successful</li>
 * </ul>
 *
 * <p>Note: This test makes real HTTP requests and may be slower or fail in offline environments.
 * For pure unit testing, use mock-based approaches instead.</p>
 *
 * @author haazz
 * @version BETA-0.0.1
 */
class ScenarioTestExecutorTest {

    /**
     * Tests that a scenario with two valid HTTP GET steps is executed successfully.
     *
     * <p>
     * Verifies that:
     * <ul>
     *     <li>The scenario result is not null</li>
     *     <li>Two steps are executed as expected</li>
     *     <li>Each step is marked as successful</li>
     *     <li>The overall scenario is marked as successful</li>
     * </ul>
     * </p>
     */
    @Test
    void testExecute_successScenario_returnsValidResult() {
        Step step = new Step.Builder()
            .type(ProtocolType.HTTP)
            .request(new Request.Builder()
                .method(HTTPMethod.GET)
                .url("https://httpbin.org/status/200")
                .build())
            .route(List.of(new Route.Builder()
                .expected(new Expected.Builder().status("200").build())
                .then(new Then.Builder().step("end").build())
                .build()))
            .build();

        LinkedHashMap<String, Step> steps = new LinkedHashMap<>();
        steps.put("start", step);
        steps.put("end", new Step.Builder().type(ProtocolType.HTTP)
            .request(new Request.Builder()
                .method(HTTPMethod.GET)
                .url("https://httpbin.org/status/200")
                .build())
            .route(List.of(new Route.Builder()
                .expected(new Expected.Builder().status("200").build())
                .then(new Then.Builder().step(null).build())
                .build()))
            .build());

        Scenario scenario = new Scenario.Builder()
            .name("Simple GET Scenario")
            .steps(steps)
            .timeoutMs(50000L)
            .build();

        ScenarioTestExecutor executor = new ScenarioTestExecutor();

        ScenarioResult result = executor.execute(scenario, new ScenarioResultCallback() {});

        assertNotNull(result);
        assertEquals(2, result.getResults().size());
        for (ResultStep stepResult : result.getResults()) {
            assertTrue(stepResult.getIsRequestSuccess());
        }
        assertTrue(result.getIsScenarioSuccess());
    }
}
