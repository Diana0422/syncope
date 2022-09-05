package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.ResourceOperation;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskInfo;
import org.apache.syncope.core.provisioning.java.propagation.utils.ParamType;
import org.apache.syncope.core.provisioning.java.propagation.utils.ReturnType;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class GetCreateTasksTest extends DefaultPropagationManagerTest {

    public GetCreateTasksTest(AnyTypeKind anyTypeKind, ParamType keyType, Boolean enable,
                              ParamType propByResType, ParamType vAttrType, ParamType noPropResourceKeysType,
                              ReturnType returnType) {
        super(anyTypeKind);
        configure(anyTypeKind, keyType, enable, propByResType, vAttrType, noPropResourceKeysType, returnType);
    }

    private void configure(AnyTypeKind anyTypeKind, ParamType keyType, Boolean enable,
                           ParamType propByResType, ParamType vAttrType, ParamType noPropResourceKeysType, ReturnType returnType) {
        this.propagationManager = new DefaultPropagationManager(
                virSchemaDAO,
                externalResourceDAO,
                null,
                null,
                mappingManager,
                derAttrHandler,
                anyUtilsFactory
        );

        this.anyTypeKind = anyTypeKind;
        this.enable = enable;
        configureAnyType(anyTypeKind, this.getClass().getName());
        configureKey(keyType);
        configurePropByRes(propByResType);
        configureVAttr(vAttrType);
        configureNoPropResourceKeys(noPropResourceKeysType);
        configureExpected(returnType);
    }

    @Override
    protected void configureExpected(ReturnType returnType) {
        switch (returnType) {
            case OK:
                PropagationTaskInfo taskInfo = new PropagationTaskInfo(externalResourceDAO.find("testResource"));
                taskInfo.setAnyType(anyTypeKind.name());
                taskInfo.setAnyTypeKind(anyTypeKind);
                taskInfo.setObjectClassName("__ACCOUNT__");
                taskInfo.setConnObjectKey("info");
                switch (anyTypeKind) {
                    case USER:
                        taskInfo.setEntityKey("userTested");
                        break;
                    case GROUP:
                        taskInfo.setEntityKey("groupTested");
                        break;
                    case ANY_OBJECT:
                        taskInfo.setEntityKey("anyObjectTested");
                        break;
                    default:
                        break;
                }
                taskInfo.setOperation(ResourceOperation.CREATE);
                this.expected = new ArrayList<>(Collections.singleton(taskInfo));
                break;
            case NULL_PTR_ERROR:
                this.expectedError = new NullPointerException();
                break;
            case NOT_FOUND_ERROR:
                this.expectedError = new NotFoundException("msg");
                break;
            case FAIL:
                this.expected = new ArrayList<>();
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                // {anyTypeKind, key, enable, propByRes, vAttr, noPropResourceKeys, resultType}
                /* Iteration 1 */
                {AnyTypeKind.USER, ParamType.VALID, null, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.GROUP, ParamType.VALID, null, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.ANY_OBJECT, ParamType.VALID, null, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.INVALID, null, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.NULL, null, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, false, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, null, ParamType.INVALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, null, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, null, ParamType.VALID, ParamType.EMPTY, ParamType.VALID, ReturnType.FAIL},

                /* Iteration 2 */
                {AnyTypeKind.USER, ParamType.VALID, null, ParamType.NULL, ParamType.EMPTY, ParamType.EMPTY, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, null, ParamType.EMPTY, ParamType.EMPTY, ParamType.EMPTY, ReturnType.FAIL},

                /* Mutation Testing */
                // no test case added
        });
    }

    @Test
    public void testGetCreateTask() {
        List<PropagationTaskInfo> createTasks;
        try {
            createTasks = propagationManager.getCreateTasks(anyTypeKind, key, enable, propByRes, vAttr, noPropResourceKeys);
        } catch (Exception e) {
            assertEquals(expectedError.getClass(), e.getClass());
            return;
        }

        /* test result size of created tasks */
        if (createTasks.size() == 1) {
            PropagationTaskInfo propagationTaskInfo = createTasks.get(0);
            PropagationTaskInfo expectedTask = expected.get(0);
            String attributes = propagationTaskInfo.getAttributes();
            String anyType = propagationTaskInfo.getAnyType();

            // attributes are correct
            if (anyType.equals("GROUP")) {
                assertTrue(attributes.contains("Group Name"));
                assertTrue(attributes.contains("groupuid"));
            } else if (anyType.equals("USER")){
                assertTrue(attributes.contains("Diana Pasquali"));
                assertTrue(attributes.contains("diapascal"));
            } else {
                assertTrue(attributes.contains("Any Name"));
                assertTrue(attributes.contains("anyobjuid"));
            }

            // assert that are avoided potential conflicts by not doing create or update on any resource
            // for which a delete is requested, and by not doing any create on any resource
            // for which an update is requested.

            // the state of the task is correct
            // (mutation testing n° 364, 365, 366, 367, 368, 369)
            assertEquals(expectedTask.getOperation(), propagationTaskInfo.getOperation());
            assertEquals(expectedTask.getObjectClassName(), propagationTaskInfo.getObjectClassName());
            assertEquals(expectedTask.getAnyType(), propagationTaskInfo.getAnyType());
            assertEquals(expectedTask.getAnyTypeKind(), propagationTaskInfo.getAnyTypeKind());
            assertEquals(expectedTask.getEntityKey(), propagationTaskInfo.getEntityKey());
            assertEquals(expectedTask.getConnObjectKey(), propagationTaskInfo.getConnObjectKey());

        }


        // the size is correct
        assertEquals(expected.size(), createTasks.size());
    }
}
