/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.form.engine.impl.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.scope.ScopeTypes;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.logging.LoggingSessionUtil;
import org.flowable.form.api.FormInfo;
import org.flowable.form.api.FormInstance;
import org.flowable.form.engine.impl.persistence.entity.FormInstanceEntity;


import java.util.Date;

public class FormLoggingSessionUtil {
    public static void addLoggingData(String type, String message, FormInstance formInstance) {
        ObjectNode loggingNode = LoggingSessionUtil.fillLoggingData(message, formInstance.getId(), null, ScopeTypes.DMN);
        loggingNode.put("scopeDefinitionId", formInstance.getFormDefinitionId());
        LoggingSessionUtil.addLoggingData(type, loggingNode);
    }

    protected static void putIfNotNull(String name, String value, ObjectNode loggingNode) {
        if (StringUtils.isNotEmpty(value)) {
            loggingNode.put(name, value);
        }
    }

    protected static void putIfNotNull(String name, Integer value, ObjectNode loggingNode) {
        if (value != null) {
            loggingNode.put(name, value);
        }
    }

    protected static void putIfNotNull(String name, Date value, ObjectNode loggingNode) {
        if (value != null) {
            loggingNode.put(name, LoggingSessionUtil.formatDate(value));
        }
    }
}
