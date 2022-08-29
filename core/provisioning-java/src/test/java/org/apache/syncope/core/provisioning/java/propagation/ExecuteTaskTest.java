package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.syncope.common.lib.to.PropagationStatus;
import org.apache.syncope.common.lib.types.ExecStatus;
import org.apache.syncope.common.lib.types.ResourceOperation;
import org.apache.syncope.core.persistence.api.dao.TaskDAO;
import org.apache.syncope.core.persistence.api.entity.EntityFactory;
import org.apache.syncope.core.persistence.api.entity.task.PropagationTask;
import org.apache.syncope.core.persistence.api.entity.task.TaskExec;
import org.apache.syncope.core.persistence.jpa.dao.JPAExternalResourceDAO;
import org.apache.syncope.core.persistence.jpa.entity.JPAConnInstance;
import org.apache.syncope.core.persistence.jpa.entity.resource.JPAExternalResource;
import org.apache.syncope.core.persistence.jpa.entity.task.JPAPropagationTask;
import org.apache.syncope.core.persistence.jpa.entity.task.JPATaskExec;
import org.apache.syncope.core.provisioning.api.AuditManager;
import org.apache.syncope.core.provisioning.api.Connector;
import org.apache.syncope.core.provisioning.api.ConnectorManager;
import org.apache.syncope.core.provisioning.api.notification.NotificationManager;
import org.apache.syncope.core.provisioning.api.propagation.PropagationReporter;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskExecutor;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskInfo;
import org.apache.syncope.core.provisioning.java.propagation.utils.ParamType;
import org.apache.syncope.core.provisioning.java.propagation.utils.ReturnType;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.identityconnectors.framework.common.objects.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.*;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class ExecuteTaskTest extends PriorityPropagationTaskExecutorTest {

    @Spy protected DefaultListableBeanFactory factory;
    protected PropagationTaskExecutor propagationTaskExecutor;
    @InjectMocks protected DefaultPropagationTaskCallable taskCallable;
    @Spy protected PropagationTaskExecutor taskExecutor;
    @Mock protected JPAExternalResourceDAO resourceDAO;
    @Spy private EntityFactory entityFactory;
    @Mock private ConnectorManager connectorManager;
    @Mock private NotificationManager notificationManager;
    @Mock private AuditManager auditManager;
    @Mock private Connector connector;
    @Mock private TaskDAO taskDAO;

    private Collection<PropagationTaskInfo> taskInfos;
    private int numElems; // added with second iteration
    private int numSamePriority; // added with mutation testing (mutant 132)
    private int numWithPriority; // added with mutation testing (mutant 137)
    private boolean nullPriorityAsync;
    private String executor;

    private Exception expectedError;
    private PropagationReporter expected;

    public ExecuteTaskTest(ParamType taskInfoType, int numElems, int numSamePriority, int numWithPriority,
                           boolean nullPriorityAsync, ParamType executorType, ReturnType returnType) {
        MockitoAnnotations.initMocks(this);
        configure(taskInfoType, numElems, numSamePriority, numWithPriority, nullPriorityAsync, executorType, returnType);
    }

    private void configure(ParamType taskInfoType, int numElems, int numSamePriority, int numWithPriority,
                           boolean nullPriorityAsync, ParamType executorType, ReturnType returnType) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        this.propagationTaskExecutor = new PriorityPropagationTaskExecutor(connectorManager, null,
                null, null, null, taskDAO, resourceDAO, notificationManager,
                auditManager, null, null, null,
                entityFactory, null, executor);
        this.nullPriorityAsync = nullPriorityAsync;
        this.numElems = numElems;
        this.numSamePriority = numSamePriority;
        this.numWithPriority = numWithPriority;
        List<Boolean> hasPriority = configureTaskInfos(taskInfoType, numElems, numSamePriority, numWithPriority);
        configureExecutor(executorType);
        configureResult(returnType, hasPriority);
    }

    private void configureResult(ReturnType returnType, List<Boolean> hasPriority) {
        List<PropagationStatus> statuses = new ArrayList<>();
        /* order tasks by priority (mutant n° 132) */
        List<PropagationTaskInfo> sorted = taskInfos.stream()
                .sorted(Comparator.comparing(o -> o.getExternalResource().getPropagationPriority()))
                .collect(Collectors.toList());
        switch (returnType) {
            case OK:
                /* Execution with SUCCESS or CREATED */
                for (int i = 0; i < numElems; i++) {
                    PropagationTaskInfo taskInfo = sorted.get(i);
                    this.expected = Mockito.mock(DefaultPropagationReporter.class);
                    PropagationStatus status = new PropagationStatus();
                    if (!hasPriority.get(i) && nullPriorityAsync) {
                        status.setStatus(ExecStatus.CREATED);
                    } else {
                        status.setStatus(ExecStatus.SUCCESS);
                    }
                    status.setResource(taskInfo.getExternalResource().getKey());
                    statuses.add(status);
                }
                break;
            case FAIL:
                /* Execution with FAILURE */
                PropagationTaskInfo taskInfo = sorted.get(0);
                this.expected = Mockito.mock(DefaultPropagationReporter.class);
                PropagationStatus statusFail = new PropagationStatus();
                statusFail.setStatus(ExecStatus.FAILURE);
                statusFail.setResource(taskInfo.getExternalResource().getKey());
                statuses.add(statusFail);
                break;
            case VOID:
                /* No Task Executed */
                this.expected = Mockito.mock(DefaultPropagationReporter.class);
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

    private List<Boolean> configureTaskInfos(ParamType taskInfoType, int numElems,
                                             int numSamePriority, int numWithPriority) {
        List<Boolean> hasPriority = new ArrayList<>();
        switch (taskInfoType) {
            case EMPTY:
                /* empty task collection */
                this.taskInfos = new ArrayList<>();
                break;
            case VALID:
                /* non-empty collection with priority */
                this.taskInfos = new ArrayList<>();
                for (int i = 0; i < numElems; i++) {
                    boolean priority = false;
                    JPAExternalResource resource = new JPAExternalResource();

                    if (i < numWithPriority) {
                        // we consider the case when there is a mixed set of task (with and without priority)
                        // (mutation testing - mutant 137)
                        if (i < numSamePriority) {
                            // we consider the case when multiple tasks have same priority
                            // (mutation testing - mutant 132)
                            resource.setPropagationPriority(numElems);
                        } else {
                            // we can cover the lines that sort tasks by priority
                            // (2° iteration)
                            resource.setPropagationPriority(i);
                        }
                        resource.setKey("priorityResource"+i);
                        priority = true;
                    } else {
                        resource.setKey("nonPriorityResource"+i);
                    }

                    resource.setConnector(new JPAConnInstance());
                    PropagationTaskInfo task = new PropagationTaskInfo(resource);
                    task.setObjectClassName("objectClassName"+i);
                    task.setOperation(ResourceOperation.CREATE);
                    task.setAttributes("[{\"name\":\"__NAME__\",\"value\":[\"Name"+i+"\"]}," +
                            "{\"name\":\"__UID__\",\"value\":[\"uid"+i+"\"]}]");
                    hasPriority.add(priority);
                    this.taskInfos.add(task);
                }
                break;
            default:
                break;
        }
        return hasPriority;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                // {taskInfoType, numElems, numSamePriority, numWithPriority, nullPriorityAsync, executorType, returnType} ** numElems added with second Iteration
                /* Iteration 1 */
                {ParamType.EMPTY, 1, 1, 1, false, ParamType.VALID, ReturnType.VOID},
                {ParamType.VALID, 1, 1, 0, false, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, 1, 1, 0, true, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, 1, 1, 1, false, ParamType.EMPTY, ReturnType.FAIL},
                {ParamType.VALID, 1, 1, 1, false, ParamType.NULL, ReturnType.FAIL},

                /* Iteration 2 */
                {ParamType.VALID, 2, 1, 2, false, ParamType.VALID, ReturnType.OK},

                /* Mutation testing */
                {ParamType.VALID, 3, 2, 3, false, ParamType.VALID, ReturnType.OK}, // kills mutant n°132
//                {ParamType.VALID, 3, 2, 2, true, ParamType.VALID, ReturnType.OK}, // kills mutant n°137
        });
    }

    @Before
    public void mockExecute() {
        /* Maybe taskExecutor in taskCallable not null */
        factory.registerSingleton("callable", taskCallable);
        context = Mockito.mockStatic(ApplicationContextProvider.class);
        context.when(ApplicationContextProvider::getBeanFactory).thenReturn(factory);

        doReturn(taskCallable)
                .when(factory)
                .createBean(DefaultPropagationTaskCallable.class, AbstractBeanDefinition.AUTOWIRE_BY_TYPE, false);

        /* MOCK: killing mutant 79: Stub execute() method of taskExecutor */
        doAnswer(invocationOnMock -> {
            DefaultPropagationReporter reporter = invocationOnMock.getArgument(1, DefaultPropagationReporter.class);
            PropagationTaskInfo propTaskInfo = invocationOnMock.getArgument(0, PropagationTaskInfo.class);
            return propagationTaskExecutor.execute(propTaskInfo, reporter, "validExecutor");
        }).when(taskExecutor).execute(any(), any(), anyString());

        /* mock entityFactory newEntity() method calls */
        PropagationTask task = new JPAPropagationTask();
        TaskExec exec = new JPATaskExec();
        when(entityFactory.newEntity(PropagationTask.class)).thenReturn(task);
        when(entityFactory.newEntity(TaskExec.class)).thenReturn(exec);

        /* Mock read connector from connectorManager */
        when(connectorManager.getConnector(any())).thenReturn(connector);
        doAnswer(invocationOnMock -> {
            AtomicReference propagationAttempt = invocationOnMock.getArgument(3, AtomicReference.class);
            propagationAttempt.set(true);
            return new Uid("diamerita");
        }).when(connector).create(any(), any(), any(), any());
    }

    @Test
    public void testExecute() {
        PropagationReporter reporter;
        try {
            reporter = propagationTaskExecutor.execute(taskInfos, nullPriorityAsync, executor);
        } catch (Exception e) {
            assertEquals(expectedError.getClass(), e.getClass());
            return;
        }

        assertEquals(expected.getStatuses().size(), reporter.getStatuses().size());

        for (int i = 0; i < expected.getStatuses().size(); i++) {
            /* verify that tasks are executed in order (mutant n° 132) */
            assertEquals(expected.getStatuses().get(i).getStatus(), reporter.getStatuses().get(i).getStatus());
            assertEquals(expected.getStatuses().get(i).getResource(), reporter.getStatuses().get(i).getResource());
        }
    }

}
