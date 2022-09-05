package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.ResourceOperation;
import org.apache.syncope.core.provisioning.api.PropagationByResource;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskInfo;
import org.apache.syncope.core.provisioning.java.propagation.utils.ParamType;
import org.apache.syncope.core.provisioning.java.propagation.utils.ReturnType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * This test was implemented to increment SC and CC in class DefaultPropagationManager and cover conditions at lines:
 * - 159
 * - 170
 * - 171
 */
@RunWith(Parameterized.class)
public class GetUserCreateTasksTest extends DefaultPropagationManagerTest {

    private String password;
    private PropagationByResource<Pair<String, String>> propByLinkedAccount;

    /* Added with mutation testing */
    private boolean noProp;

    public GetUserCreateTasksTest(ParamType keyType, ParamType passType, Boolean enable, ParamType propByResType,
                                  ParamType propByLinkedAccountType, boolean noProp,
                                  ParamType vAttrType, ParamType noPropResourceKeysType, ReturnType returnType) {
        super(AnyTypeKind.USER);
        configure(keyType, passType, enable, propByResType,
                propByLinkedAccountType, noProp,
                vAttrType, noPropResourceKeysType, returnType);
    }

    private void configure(ParamType keyType, ParamType passType, Boolean enable, ParamType propByResType,
                           ParamType propByLinkedAccountType, boolean noProp,
                           ParamType vAttrType, ParamType noPropResourceKeysType, ReturnType returnType) {
        this.propagationManager = new DefaultPropagationManager(
                virSchemaDAO,
                externalResourceDAO,
                null,
                null,
                mappingManager,
                derAttrHandler,
                anyUtilsFactory
        );

        this.enable = enable;
        this.anyTypeKind = AnyTypeKind.USER;
        this.noProp = noProp;

        configureAnyType(AnyTypeKind.USER, this.getClass().getName());
        configureKey(keyType);
        configurePass(passType);
        configurePropByRes(propByResType);
        configureLinkedAccount(propByLinkedAccountType, noProp);
        configureVAttr(vAttrType);
        configureNoPropResourceKeys(noPropResourceKeysType);
        configureExpected(returnType);
    }

    private void configurePass(ParamType passType) {
        switch (passType) {
            case NULL:
                System.out.println("CASE NULL");
                this.password = null;
                break;
            case EMPTY:
                System.out.println("CASE EMPTY");
                this.password = "";
                break;
            case VALID:
                System.out.println("CASE VALID");
                this.password = "myPass";
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }
    }

    private void configureLinkedAccount(ParamType propByLinkedAccountType, boolean noProp) {
        Pair<String, String> pair = new ImmutablePair<>("email", "diamerita@gmail.com");
        PropagationByResource<Pair<String, String>> linked = new PropagationByResource<>();
        switch (propByLinkedAccountType) {
            case NULL:
                System.out.println("CASE NULL");
                this.propByLinkedAccount = null;
                break;
            case EMPTY:
                /* propByLinkedAccount is empty
                * (mutation testing n° 159)
                * */
                System.out.println("CASE EMPTY");
                this.propByLinkedAccount = linked;
                break;
            case VALID:
                System.out.println("CASE VALID");
                if (noProp) {
                    pair = new ImmutablePair<>("validKey", "myAccount");
                    linked.add(ResourceOperation.CREATE, pair);
                } else {
                    linked.add(ResourceOperation.CREATE, pair);
                }
                this.propByLinkedAccount = linked;
                break;
            case INVALID:
                System.out.println("CASE INVALID");
                linked.add(ResourceOperation.DELETE, pair);
                this.propByLinkedAccount = linked;
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }

    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                // {key, password, enable, propByRes, propByLinkedaccount, noProp, vAttrs, noPropResourceKeys, expected}
                {ParamType.VALID, ParamType.VALID, null, ParamType.VALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {ParamType.INVALID, ParamType.VALID, null, ParamType.VALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.NOT_FOUND_ERROR},
                {ParamType.EMPTY, ParamType.VALID, null, ParamType.VALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.NOT_FOUND_ERROR},
                {ParamType.NULL, ParamType.VALID, null, ParamType.VALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.NOT_FOUND_ERROR},
                {ParamType.VALID, ParamType.EMPTY, null, ParamType.VALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {ParamType.VALID, ParamType.NULL, null, ParamType.VALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, false, ParamType.VALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, null, ParamType.INVALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.FAIL},
                {ParamType.VALID, ParamType.VALID, null, ParamType.VALID, ParamType.INVALID, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, null, ParamType.VALID, ParamType.VALID, false, ParamType.VALID, ParamType.EMPTY, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, null, ParamType.VALID, ParamType.VALID, false, ParamType.EMPTY, ParamType.VALID, ReturnType.FAIL},

                /* Mutation Testing */
                {ParamType.VALID, ParamType.VALID, null, ParamType.VALID, ParamType.VALID, true, ParamType.EMPTY, ParamType.VALID, ReturnType.FAIL}, // kills mutant n° 171
                {ParamType.VALID, ParamType.VALID, null, ParamType.EMPTY, ParamType.VALID, true, ParamType.EMPTY, ParamType.VALID, ReturnType.FAIL}, // kills mutant n° 171
                {ParamType.VALID, ParamType.VALID, null, ParamType.EMPTY, ParamType.VALID, true, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK}, // kills mutant n° 171
                {ParamType.VALID, ParamType.VALID, null, ParamType.EMPTY, ParamType.EMPTY, false, ParamType.EMPTY, ParamType.EMPTY, ReturnType.FAIL}, // kills mutant n° 159
        });
    }

    @Test
    public void testGetUserCreateTask() {
        List<PropagationTaskInfo> createTasks;
        try {
            createTasks = propagationManager.getUserCreateTasks(key, password, enable, propByRes, propByLinkedAccount, vAttr, noPropResourceKeys);
        } catch (Exception e) {
            assertEquals(expectedError.getClass(), e.getClass());
            return;
        }

        if (createTasks.size() == 1) {
            PropagationTaskInfo propagationTaskInfo = createTasks.get(0);
            String anyType = propagationTaskInfo.getAnyType();
            assertEquals("USER", anyType);
            if (!vAttr.isEmpty()) {
                String attributes = propagationTaskInfo.getAttributes();
                assertTrue(attributes.contains("Diana Pasquali") && attributes.contains("diapascal"));
            }
        }

        assertEquals(expected.size(), createTasks.size());
    }

}
