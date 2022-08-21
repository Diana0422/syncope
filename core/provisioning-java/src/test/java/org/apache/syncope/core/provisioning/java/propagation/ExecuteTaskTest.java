package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.syncope.common.lib.to.PropagationStatus;
import org.apache.syncope.common.lib.types.ExecStatus;
import org.apache.syncope.common.lib.types.ResourceOperation;
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

    private Collection<PropagationTaskInfo> taskInfos;
    private int numElems;
    private boolean nullPriorityAsync;
    private String executor;

    private Exception expectedError;
    private PropagationReporter expected;

    public ExecuteTaskTest(ParamType taskInfoType, int numElems, boolean nullPriorityAsync, ParamType executorType, ReturnType returnType) {
        MockitoAnnotations.initMocks(this);
        configure(taskInfoType, numElems, nullPriorityAsync, executorType, returnType);
    }

    private void configure(ParamType taskInfoType, int numElems, boolean nullPriorityAsync, ParamType executorType, ReturnType returnType) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        this.propagationTaskExecutor = new PriorityPropagationTaskExecutor(connectorManager, null, null,
                null, null, null, resourceDAO, notificationManager,
                auditManager, null, null, null,
                entityFactory, null, executor);
        this.nullPriorityAsync = nullPriorityAsync;
        boolean withPriority = configureTaskInfos(taskInfoType, numElems);
        configureExecutor(executorType);
        configureResult(returnType, withPriority);
    }

    private void configureResult(ReturnType returnType, boolean withPriority) {
        List<PropagationStatus> statuses = new ArrayList<>();
        switch (returnType) {
            case OK:
                /* Execution with SUCCESS or CREATED */
                this.expected = Mockito.mock(DefaultPropagationReporter.class);
                PropagationStatus status = new PropagationStatus();
                if (withPriority) {
                    status.setStatus(ExecStatus.SUCCESS);
                } else {
                    status.setStatus(ExecStatus.CREATED);
                }
                statuses.add(status);
                break;
            case FAIL:
                /* Execution with FAILURE */
                this.expected = Mockito.mock(DefaultPropagationReporter.class);
                PropagationStatus statusFail = new PropagationStatus();
                statusFail.setStatus(ExecStatus.FAILURE);
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

    private boolean configureTaskInfos(ParamType taskInfoType, int numElems) {
        boolean withPriority = false;
        switch (taskInfoType) {
            case EMPTY:
                /* empty task collection */
                this.taskInfos = new ArrayList<>();
                break;
            case VALID:
                /* non-empty collection with priority */
                withPriority = true;
                this.taskInfos = new ArrayList<>();
                for (int i = 0; i < numElems; i++) {
                    JPAExternalResource resource = new JPAExternalResource();
                    resource.setKey("priorityResource"+i);
                    resource.setPropagationPriority(numElems-i); // we can verify that task are sorted
                    resource.setConnector(new JPAConnInstance());
                    PropagationTaskInfo task = new PropagationTaskInfo(resource);
                    task.setObjectClassName("objectClassName"+i);
                    task.setOperation(ResourceOperation.CREATE);
                    task.setAttributes("[{\"name\":\"__NAME__\",\"value\":[\"Name"+i+"\"]},{\"name\":\"__UID__\",\"value\":[\"uid"+i+"\"]}]");
                    this.taskInfos.add(task);
                }
                break;
            case INVALID:
                /* non-empty collection without priority */
                this.taskInfos = new ArrayList<>();
                this.taskInfos.add(new PropagationTaskInfo(new JPAExternalResource()));
                break;
            default:
                break;
        }
        return withPriority;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                // {taskInfoType, numElems, nullPriorityAsync, executorType, returnType} ** numElems added with second Iteration
                /* Iteration 1 */
                {ParamType.EMPTY, 1, false, ParamType.VALID, ReturnType.VOID},
                {ParamType.VALID, 1, false, ParamType.VALID, ReturnType.OK},
                {ParamType.INVALID, 1, false, ParamType.VALID, ReturnType.VOID},
                {ParamType.INVALID, 1, true, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, 1, false, ParamType.EMPTY, ReturnType.FAIL},
                {ParamType.VALID, 1, false, ParamType.NULL, ReturnType.FAIL},

                /* Iteration 2 */
                {ParamType.VALID, 2, false, ParamType.VALID, ReturnType.OK}
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

        /* Stub execute() method of taskExecutor */

        taskInfos.forEach(taskInfo -> {
            doAnswer(invocationOnMock -> {
                DefaultPropagationReporter reporter = invocationOnMock.getArgument(1, DefaultPropagationReporter.class);
                return propagationTaskExecutor.execute(taskInfo, reporter, "validExecutor");
            })
                    .when(taskExecutor)
                    .execute(any(), argThat(propagationReporter -> propagationReporter.getStatuses().isEmpty()), anyString());

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

//            System.out.println(taskInfo.getExternalResource());
//
//            /* resourceDAO finds resource */
//            doReturn(taskInfo.getExternalResource()).when(resourceDAO).find(taskInfo.getResource());
        });
    }

    @Test
    public void testExecute() {
        PropagationReporter reporter = null;
        try {
            reporter = propagationTaskExecutor.execute(taskInfos, nullPriorityAsync, executor);
        } catch (Exception e) {
            assertEquals(expectedError.getClass(), e.getClass());
            return;
        }

        for (int i = 0; i < expected.getStatuses().size(); i++) {
            assertEquals(expected.getStatuses().get(i).getStatus(), reporter.getStatuses().get(i).getStatus());
        }
    }


}
