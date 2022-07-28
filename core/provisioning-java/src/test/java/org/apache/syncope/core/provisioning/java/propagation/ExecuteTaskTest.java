package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.syncope.common.lib.to.PropagationStatus;
import org.apache.syncope.common.lib.types.ExecStatus;
import org.apache.syncope.core.persistence.jpa.entity.resource.JPAExternalResource;
import org.apache.syncope.core.provisioning.api.propagation.PropagationReporter;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskExecutor;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskInfo;
import org.apache.syncope.core.provisioning.java.propagation.utils.ParamType;
import org.apache.syncope.core.provisioning.java.propagation.utils.ReturnType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ExecuteTaskTest extends PriorityPropagationTaskExecutorTest {

    private Collection<PropagationTaskInfo> taskInfos;
    private boolean nullPriorityAsync; // TRUE or FALSE
    private String executor;

    private Exception expectedError;
    private PropagationReporter expected;

    public ExecuteTaskTest(ParamType taskInfoType, boolean nullPriorityAsync, ParamType executorType, ReturnType returnType) {
        configure(taskInfoType, nullPriorityAsync, executorType, returnType);
    }

    private void configure(ParamType taskInfoType, boolean nullPriorityAsync, ParamType executorType, ReturnType returnType) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        this.taskExecutor = new PriorityPropagationTaskExecutor(null, null, null,
                null, null, null, null, null,
                null, null, null, null,
                null, null, executor);
        this.nullPriorityAsync = nullPriorityAsync;
        configureTaskInfos(taskInfoType);
        configureExecutor(executorType);
        configureResult(returnType);
    }

    private void configureResult(ReturnType returnType) {
        List<PropagationStatus> statuses = new ArrayList<>();
        switch (returnType) {
            case OK:
                this.expected = Mockito.mock(DefaultPropagationReporter.class);
                PropagationStatus statusCreated = new PropagationStatus();
                statusCreated.setStatus(ExecStatus.CREATED);
                statuses.add(statusCreated);
                break;
            case FAIL:
                this.expected = Mockito.mock(DefaultPropagationReporter.class);
                // FIXME
//                PropagationStatus statusFail = new PropagationStatus();
//                statusFail.setStatus(ExecStatus.FAILURE);
//                statuses.add(statusFail);
                break;
            case NULL_PTR_ERROR:
                this.expectedError = new NullPointerException();
                break;
        }
        if (expected != null) Mockito.when(this.expected.getStatuses()).thenReturn(statuses);
    }

    private void configureExecutor(ParamType executorType) {
        switch (executorType) {
            case EMPTY:
                this.executor = "";
                break;
            case NULL:
                this.executor = null;
                break;
            case VALID:
                this.executor = "validExecutor";
                break;
            case INVALID:
                // TODO forse non ci sta un executor non valido?
                this.executor = "invalidExecutor";
                break;
        }
    }

    private void configureTaskInfos(ParamType taskInfoType) {
        switch (taskInfoType) {
            case EMPTY:
                this.taskInfos = new ArrayList<>();
                break;
            case NULL:
                this.taskInfos = null;
                break;
            case VALID:
                this.taskInfos = new ArrayList<>();
                this.taskInfos.add(new PropagationTaskInfo(new JPAExternalResource()));
                break;
            case INVALID:
                this.taskInfos = new ArrayList<>();
                this.taskInfos.add(new PropagationTaskInfo(null));
                break;
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                // {taskInfoType, nullPriorityAsync, executorType, returnType}
                {ParamType.VALID, true, ParamType.VALID, ReturnType.OK},
                {ParamType.EMPTY, true, ParamType.VALID, ReturnType.FAIL},
                {ParamType.NULL, true, ParamType.VALID, ReturnType.NULL_PTR_ERROR},
                {ParamType.INVALID, true, ParamType.VALID, ReturnType.NULL_PTR_ERROR},
                {ParamType.VALID, false, ParamType.VALID, ReturnType.FAIL},
                {ParamType.VALID, true, ParamType.EMPTY, ReturnType.OK},
                {ParamType.VALID, true, ParamType.NULL, ReturnType.OK},
                {ParamType.VALID, true, ParamType.INVALID, ReturnType.OK}
        });
    }

    @Test
    public void testExecute() {
        PropagationReporter reporter = null;
        try {
            reporter = taskExecutor.execute(taskInfos, nullPriorityAsync, executor);
        } catch (Exception e) {
            assertEquals(expectedError.getClass(), e.getClass());
            return;
        }
        assertEquals(expected.getStatuses(), reporter.getStatuses());
    }


}
