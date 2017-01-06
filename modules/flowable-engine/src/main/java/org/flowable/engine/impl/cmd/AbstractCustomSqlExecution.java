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
package org.flowable.engine.impl.cmd;

import org.flowable.engine.common.impl.cmd.CustomSqlExecution;

/**
 * @author jbarrez
 */
public abstract class AbstractCustomSqlExecution<Mapper, ResultType> implements CustomSqlExecution<Mapper, ResultType> {

  protected Class<Mapper> mapperClass;

  public AbstractCustomSqlExecution(Class<Mapper> mapperClass) {
    this.mapperClass = mapperClass;
  }

  @Override
  public Class<Mapper> getMapperClass() {
    return mapperClass;
  }

}
