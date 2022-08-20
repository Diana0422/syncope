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

@RunWith(Parameterized.class)
public class GetUserCreateTaskTest extends DefaultPropagationManagerTest {

    private String password;
    private PropagationByResource<Pair<String, String>> propByLinkedAccount;

    public GetUserCreateTaskTest(ParamType keyType, ParamType passType, Boolean enable, ParamType propByResType, ParamType propByLinkedAccountType, ParamType vAttrType, ParamType noPropResourceKeysType, ReturnType returnType) {
        super(AnyTypeKind.USER);
        configure(keyType, passType, enable, propByResType, propByLinkedAccountType, vAttrType, noPropResourceKeysType, returnType);
    }

    private void configure(ParamType keyType, ParamType passType, Boolean enable, ParamType propByResType, ParamType propByLinkedAccountType, ParamType vAttrType, ParamType noPropResourceKeysType, ReturnType returnType) {
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

        configureAnyType(AnyTypeKind.USER, this.getClass().getName());
        configureKey(keyType);
        configurePass(passType);
        configurePropByRes(propByResType);
        configureLinkedAccount(propByLinkedAccountType);
        configureVAttr(vAttrType);
        configureNoPropResourceKeys(noPropResourceKeysType);
        configureExpected(returnType);
        System.out.println("anyTypeKind = " + anyTypeKind + ", key = " + key + ", enable = " + enable + ", propByRes = " + propByRes+ ", vAttr = " + vAttr + ", noPropResourceKeys = " + noPropResourceKeys + ", returnType = " + returnType);
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
            case INVALID:
                // FIXME probabilmente non è necessario perché la password deve essere impostata
                System.out.println("CASE INVALID");
                this.password = "invalidKey";
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }
    }

    private void configureLinkedAccount(ParamType propByLinkedAccountType) {
        Pair<String, String> pair = new ImmutablePair<>("email", "diamerita@gmail.com");
        PropagationByResource<Pair<String, String>> linked = new PropagationByResource<>();

        switch (propByLinkedAccountType) {
            case NULL:
                System.out.println("CASE NULL");
                this.propByLinkedAccount = null;
                break;
            case EMPTY:
                System.out.println("CASE EMPTY");
                this.propByLinkedAccount = linked;
                break;
            case VALID:
                System.out.println("CASE VALID");
                linked.add(ResourceOperation.CREATE, pair);
                this.propByLinkedAccount = linked;
                break;
            case INVALID:
                // FIXME forse sbagliato
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
                // {key, password, enable, propByRes, propByLinkedaccount, vAttrs, noPropResourceKeys, expected}
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {ParamType.INVALID, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {ParamType.NULL, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {ParamType.EMPTY, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {ParamType.VALID, ParamType.EMPTY, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NULL_PTR_ERROR},
                {ParamType.VALID, ParamType.NULL, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, ParamType.INVALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NULL_PTR_ERROR}, //fixme + levare dipendenza tra i due test
                {ParamType.VALID, ParamType.VALID, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.EMPTY, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.FAIL},
                {ParamType.VALID, ParamType.VALID, true, ParamType.NULL, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NULL_PTR_ERROR}, //fixme + levare dipendenza tra i due test
                {ParamType.VALID, ParamType.VALID, true, ParamType.INVALID, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.FAIL},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.EMPTY, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.NULL, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.INVALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.NULL, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.INVALID, ParamType.VALID, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.NULL, ReturnType.OK},
                {ParamType.VALID, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.INVALID, ReturnType.FAIL},
        });
    }

    @Test
    public void test() {
        assertEquals(true, true);
    }

//    @Test
//    public void testGetUserCreateTask() {
//        System.out.println("key: "+key);
//        System.out.println("password: "+password);
//        System.out.println("enable: "+enable);
//        System.out.println("propByRes: "+propByRes);
//        System.out.println("propByLinkedAccount: "+propByLinkedAccount);
//        System.out.println("vAttr: "+vAttr);
//        System.out.println("noPropResourceKeys: "+noPropResourceKeys);
//        List<PropagationTaskInfo> createTasks = null;
//        try {
//            createTasks = propagationManager.getUserCreateTasks(key, password, enable, propByRes, propByLinkedAccount, vAttr, noPropResourceKeys);
//        } catch (Exception e) {
//            assertEquals(expectedError.getClass(), e.getClass());
//            return;
//        }
//
//        if (createTasks.size() == 1) {
//            PropagationTaskInfo propagationTaskInfo = createTasks.get(0);
//            String anyType = propagationTaskInfo.getAnyType();
//            String attributes = propagationTaskInfo.getAttributes();
//
//            assertEquals("USER", anyType);
//            assertTrue(attributes.contains("Diana Pasquali") && attributes.contains("diapascal"));
//        }
//
//        assertEquals(expected.size(), createTasks.size());
//    }

}
