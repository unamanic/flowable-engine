package org.flowable.dmn.engine.test.logging;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.common.engine.impl.logging.LoggingListener;

import java.util.ArrayList;
import java.util.List;

public class FlowableLoggingListener implements LoggingListener {
    
    public static List<ObjectNode> TEST_LOGGING_NODES = new ArrayList<>();

    @Override
    public void loggingGenerated(List<ObjectNode> loggingNodes) {
        TEST_LOGGING_NODES.addAll(loggingNodes);
    }

    public static void clear() {
        TEST_LOGGING_NODES.clear();
    }
}
