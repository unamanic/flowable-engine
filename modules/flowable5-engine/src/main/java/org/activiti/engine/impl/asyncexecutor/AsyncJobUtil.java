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
package org.activiti.engine.impl.asyncexecutor;

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cmd.ExecuteAsyncJobCmd;
import org.activiti.engine.impl.cmd.LockExclusiveJobCmd;
import org.activiti.engine.impl.cmd.UnlockExclusiveJobCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncJobUtil {
  
  private static Logger log = LoggerFactory.getLogger(AsyncJobUtil.class);

  public static void executeJob(final JobEntity job, final CommandExecutor commandExecutor) {
    try {
      if (job.isExclusive()) {
        commandExecutor.execute(new LockExclusiveJobCmd(job));
      }
      
    } catch (Throwable lockException) { 
      if (log.isDebugEnabled()) {
        log.debug("Could not lock exclusive job. Unlocking job so it can be acquired again. Caught exception: {}", lockException.getMessage());
      }
      
      unacquireJob(commandExecutor, job);
      return;
    
    }
    
    try {
      commandExecutor.execute(new ExecuteAsyncJobCmd(job));
      
    } catch (final ActivitiOptimisticLockingException e) {
      
      handleFailedJob(job, e, commandExecutor);
      
      if (log.isDebugEnabled()) {
        log.debug("Optimistic locking exception during job execution. If you have multiple async executors running against the same database, " +
            "this exception means that this thread tried to acquire an exclusive job, which already was changed by another async executor thread." +
            "This is expected behavior in a clustered environment. " +
            "You can ignore this message if you indeed have multiple job executor threads running against the same database. " +
            "Exception message: {}", e.getMessage());
      }
      
    } catch (Throwable exception) {
      handleFailedJob(job, exception, commandExecutor);
       
      // Finally, Throw the exception to indicate the ExecuteAsyncJobCmd failed
      String message = "Job " + job.getId() + " failed";
      log.error(message, exception);
    }
    
    try {
      if (job.isExclusive()) {
        commandExecutor.execute(new UnlockExclusiveJobCmd(job));
      }
      
    } catch (ActivitiOptimisticLockingException optimisticLockingException) { 
      if (log.isDebugEnabled()) {
        log.debug("Optimistic locking exception while unlocking the job. If you have multiple async executors running against the same database, " +
            "this exception means that this thread tried to acquire an exclusive job, which already was changed by another async executor thread." +
            "This is expected behavior in a clustered environment. " +
            "You can ignore this message if you indeed have multiple job executor acquisition threads running against the same database. " +
            "Exception message: {}", optimisticLockingException.getMessage());
      }
    } catch (Throwable t) {
      log.error("Error while unlocking exclusive job {}", job.getId(), t);
    }
  }
  
  protected static void unacquireJob(final CommandExecutor commandExecutor, final JobEntity job) {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext != null) {
      commandContext.getJobEntityManager().unacquireJob(job.getId());
    } else {
      commandExecutor.execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          commandContext.getJobEntityManager().unacquireJob(job.getId());
          return null;
        }
      });
    }
  }
  
  public static void handleFailedJob(final JobEntity job, final Throwable exception, final CommandExecutor commandExecutor) {
    commandExecutor.execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        CommandConfig commandConfig = commandExecutor.getDefaultConfig().transactionRequiresNew();
        FailedJobCommandFactory failedJobCommandFactory = commandContext.getFailedJobCommandFactory();
        Command<Object> cmd = failedJobCommandFactory.getCommand(job.getId(), exception);

        log.trace("Using FailedJobCommandFactory '{}' and command of type '{}'", failedJobCommandFactory.getClass(), cmd.getClass());
        commandExecutor.execute(commandConfig, cmd);
        
        // Dispatch an event, indicating job execution failed in a try-catch block, to prevent the original
        // exception to be swallowed
        if (commandContext.getEventDispatcher().isEnabled()) {
          try {
            commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityExceptionEvent(
                FlowableEngineEventType.JOB_EXECUTION_FAILURE, job, exception));
          } catch(Throwable ignore) {
            log.warn("Exception occurred while dispatching job failure event, ignoring.", ignore);
          }
        }
        
        return null;
      }
      
    });
  }
}
