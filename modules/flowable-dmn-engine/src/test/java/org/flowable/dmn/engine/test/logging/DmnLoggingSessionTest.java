package org.flowable.dmn.engine.test.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.common.engine.impl.logging.DmnLoginSessionConstants;
import org.flowable.common.engine.impl.logging.LoggingSessionFactory;
import org.flowable.common.engine.impl.logging.LoggingSessionUtil;
import org.flowable.dmn.api.DecisionExecutionAuditContainer;
import org.flowable.dmn.engine.test.AbstractFlowableDmnTest;
import org.flowable.dmn.engine.test.DmnDeployment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DmnLoggingSessionTest extends AbstractFlowableDmnTest {
    public ObjectMapper objectMapper = new ObjectMapper();
    public FlowableLoggingListener loggingListener = new FlowableLoggingListener();

    @Override
    public void initDmnEngine() {
        super.initDmnEngine();

        LoggingSessionFactory loggingSessionFactory = new LoggingSessionFactory();
        loggingSessionFactory.setLoggingListener(loggingListener);
        this.dmnEngineConfiguration.setLoggingListener(loggingListener);
        this.dmnEngineConfiguration.addSessionFactory(loggingSessionFactory);
    }

    @Test
    @DmnDeployment(resources = "org/flowable/dmn/engine/test/deployment/simple.dmn")
    public void testSimpleDmnLoggingSession() {

        Map<String, Object> processVariablesInput = new HashMap<>();
        processVariablesInput.put("inputVariable1", 1D);
        processVariablesInput.put("inputVariable2", "test2");

        DecisionExecutionAuditContainer result = ruleService.createExecuteDecisionBuilder()
                .decisionKey("decision")
                .variables(processVariablesInput)
                .executeWithAuditTrail();

        ObjectNode loggingNode = FlowableLoggingListener.TEST_LOGGING_NODES.get(0);
        assertNotNull(loggingNode.get(LoggingSessionUtil.ID).asText());
        assertEquals(DmnLoginSessionConstants.TYPE_DECISION_STARTED, loggingNode.get("type").asText());
        assertTrue(loggingNode.get("message").asText().contains("Started evaluating decision table"));

        loggingNode = FlowableLoggingListener.TEST_LOGGING_NODES.get(1);
        assertNotNull(loggingNode.get(LoggingSessionUtil.ID).asText());
        assertEquals(DmnLoginSessionConstants.TYPE_DECISION_RULE_EVALUATED, loggingNode.get("type").asText());
        assertTrue(loggingNode.get("message").asText().contains("DMN rule evaluated"));

        loggingNode = FlowableLoggingListener.TEST_LOGGING_NODES.get(2);
        assertNotNull(loggingNode.get(LoggingSessionUtil.ID).asText());
        assertEquals(DmnLoginSessionConstants.TYPE_DECISION_RULE_EVALUATED, loggingNode.get("type").asText());
        assertTrue(loggingNode.get("message").asText().contains("DMN rule evaluated"));

        loggingNode = FlowableLoggingListener.TEST_LOGGING_NODES.get(3);
        assertNotNull(loggingNode.get(LoggingSessionUtil.ID).asText());
        assertEquals(DmnLoginSessionConstants.TYPE_DECISION_RULE_EVALUATED, loggingNode.get("type").asText());
        assertTrue(loggingNode.get("message").asText().contains("DMN rule evaluated"));

        loggingNode = FlowableLoggingListener.TEST_LOGGING_NODES.get(4);
        assertNotNull(loggingNode.get(LoggingSessionUtil.ID).asText());
        assertEquals(DmnLoginSessionConstants.TYPE_DECISION_RULE_HIT, loggingNode.get("type").asText());
        assertTrue(loggingNode.get("message").asText().contains("DMN rule hit"));

        loggingNode = FlowableLoggingListener.TEST_LOGGING_NODES.get(5);
        assertNotNull(loggingNode.get(LoggingSessionUtil.ID).asText());
        assertEquals(DmnLoginSessionConstants.TYPE_DECISION_COMPLETE, loggingNode.get("type").asText());
        assertTrue(loggingNode.get("message").asText().contains("Completed evaluating decision table"));
    }

    @After
    public void clearLoggingSession() {
        FlowableLoggingListener.clear();
        this.dmnEngineConfiguration.setLoggingListener(null);
    }
}
