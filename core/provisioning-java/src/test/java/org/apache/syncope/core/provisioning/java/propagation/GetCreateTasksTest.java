package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.syncope.common.lib.types.AnyTypeKind;
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

    public GetCreateTasksTest(AnyTypeKind anyTypeKind, ParamType keyType, Boolean enable, ParamType propByResType, ParamType vAttrType, ParamType noPropResourceKeysType, ReturnType returnType) {
        super(anyTypeKind);
        System.out.println("anyTypeKind = " + anyTypeKind + ", keyType = " + keyType + ", enable = " + enable + ", propByResType = " + propByResType + ", vAttrType = " + vAttrType + ", noPropResourceKeysType = " + noPropResourceKeysType + ", returnType = " + returnType);
        configure(anyTypeKind, keyType, enable, propByResType, vAttrType, noPropResourceKeysType, returnType);
    }

    private void configure(AnyTypeKind anyTypeKind, ParamType keyType, Boolean enable, ParamType propByResType, ParamType vAttrType, ParamType noPropResourceKeysType, ReturnType returnType) {
        System.out.println("IN CONFIG: "+"anyTypeKind = " + anyTypeKind + ", keyType = " + keyType + ", enable = " + enable + ", propByResType = " + propByResType + ", vAttrType = " + vAttrType + ", noPropResourceKeysType = " + noPropResourceKeysType + ", returnType = " + returnType);
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
        System.out.println("anyTypeKind = " + anyTypeKind + ", key = " + key + ", enable = " + enable + ", propByRes = " + propByRes+ ", vAttr = " + vAttr + ", noPropResourceKeys = " + noPropResourceKeys + ", returnType = " + returnType);
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
                {AnyTypeKind.USER, ParamType.VALID, null, ParamType.EMPTY, ParamType.EMPTY, ParamType.EMPTY, ReturnType.FAIL}
        });
    }

    @Test
    public void testGetCreateTask() {
        System.out.println("anyType: "+anyTypeKind);
        System.out.println("key: "+key);
        System.out.println("enable: "+enable);
        System.out.println("propByRes: "+propByRes);
        System.out.println("vAttr: "+vAttr);
        System.out.println("noPropResourceKeys: "+noPropResourceKeys);
        List<PropagationTaskInfo> createTasks = null;
        try {
            createTasks = propagationManager.getCreateTasks(anyTypeKind, key, enable, propByRes, vAttr, noPropResourceKeys);
        } catch (Exception e) {
            assertEquals(expectedError.getClass(), e.getClass());
            return;
        }

        /* test result size of created tasks */
        if (createTasks.size() == 1) {
            PropagationTaskInfo propagationTaskInfo = createTasks.get(0);
            String attributes = propagationTaskInfo.getAttributes();
            String anyType = propagationTaskInfo.getAnyType();

            System.out.println(attributes);
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
        }
        assertEquals(expected.size(), createTasks.size());
    }
}
