package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.ResourceOperation;
import org.apache.syncope.core.provisioning.api.PropagationByResource;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskInfo;
import org.apache.syncope.core.provisioning.java.propagation.utils.ParamType;
import org.apache.syncope.core.provisioning.java.propagation.utils.ReturnType;
import org.identityconnectors.framework.common.objects.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;

@RunWith(Parameterized.class)
public class GetUpdateTasksTest extends DefaultPropagationManagerTest {

    private boolean changePwd;
    private PropagationByResource<Pair<String, String>> propByLinkedAccount;

    public GetUpdateTasksTest(AnyTypeKind anyTypeKind, ParamType keyType, boolean changePwd,
                              Boolean enable, ParamType propByResType, ParamType propByLinkedType,
                              ParamType vAttrType, ParamType noPropResourceKeysType, ReturnType returnType) {
        super(anyTypeKind);
        configure(anyTypeKind, keyType, enable, changePwd, propByResType, propByLinkedType,
                vAttrType, noPropResourceKeysType, returnType);
    }

    private void configure(AnyTypeKind anyTypeKind, ParamType keyType, Boolean enable, boolean changePwd, ParamType propByResType,
                           ParamType propByLinkedType, ParamType vAttrType,
                           ParamType noPropResourceKeysType, ReturnType returnType) {
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
        this.changePwd = changePwd;

        configureAnyType(anyTypeKind, this.getClass().getName());
        configureKey(keyType);
        configurePropByRes(propByResType);
        configurePropByLinkedAccount(propByLinkedType);
        configureVAttr(vAttrType);
        configureNoPropResourceKeys(noPropResourceKeysType);
        configureExpected(returnType);
    }

    protected void configurePropByRes(ParamType propByResType) {
        switch (propByResType) {
            case INVALID:
                System.out.println("CASE INVALID");
                this.propByRes = new PropagationByResource<>();
                this.propByRes.add(ResourceOperation.DELETE, "invalidKey");
                break;
            case VALID:
                System.out.println("CASE VALID");
                this.propByRes = new PropagationByResource<>();
                this.propByRes.add(ResourceOperation.UPDATE, "validKey");
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }

    }

    protected void configureNoPropResourceKeys(ParamType noPropResourceKeysType) {
        switch (noPropResourceKeysType) {
            case VALID:
                this.noPropResourceKeys = new ArrayList();
                this.noPropResourceKeys.add("validKey");
                break;
            case EMPTY:
                this.noPropResourceKeys = new ArrayList();
                break;
            default:
                /* case NULL */
                break;
        }
    }

    private void configurePropByLinkedAccount(ParamType propByLinkedType) {
        Pair<String, String> pair = new ImmutablePair<>("resourceKey", "connObjectKey");
        PropagationByResource<Pair<String, String>> linked = new PropagationByResource<>();
        switch (propByLinkedType) {
            case VALID:
                linked.add(ResourceOperation.UPDATE, pair);
                break;
            case INVALID:
                linked.add(ResourceOperation.DELETE, pair);
                break;
            case NULL:
                linked = null;
                break;
            default:
                /* case EMPTY */
                break;
        }
        this.propByLinkedAccount = linked;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                // {kind, key, changePwd, enable, propByRes, propByLinkedAccount, vAttrs, noPropResourceKey, Result}
                /* Iteration 1 */
                {AnyTypeKind.USER, ParamType.VALID, true, null, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.GROUP, ParamType.VALID, true, null, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.ANY_OBJECT, ParamType.VALID, true, null, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.INVALID, true, null, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.NULL, true, null, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.VALID, false, null, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, true, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, false, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, null, ParamType.INVALID, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, null, ParamType.VALID, ParamType.INVALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, null, ParamType.VALID, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, null, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.VALID, ReturnType.FAIL},

                /* Iteration 2 */
                {AnyTypeKind.USER, ParamType.VALID, true, null, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, null, ParamType.VALID, ParamType.NULL, ParamType.EMPTY, ParamType.EMPTY, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, null, ParamType.NULL, ParamType.VALID, ParamType.EMPTY, ParamType.EMPTY, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, null, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ParamType.NULL, ReturnType.OK}
        });
    }

    @Before
    public void setUpMocks() {
        Mockito.when(mappingManager.prepareAttrsFromAny(any(),
                argThat(s -> s == null || s.equals("myPass")), eq(changePwd), eq(enable), any(provision.getClass())))
                .thenAnswer(invocationOnMock -> {
            Set<Attribute> attributes = new HashSet<>();
            String[] connObjectKeyValue = new String[1];

            if (enable != null) {
                attributes.add(AttributeBuilder.buildEnabled(enable));
            }
            if (!changePwd) {
                Attribute pwdAttr = AttributeUtil.find(OperationalAttributes.PASSWORD_NAME, attributes);
                if (pwdAttr != null) {
                    attributes.remove(pwdAttr);
                }
            }

            return Pair.of(connObjectKeyValue[0], attributes);
        });
    }

    @Test
    public void test() {
        System.out.println("anyType: "+anyTypeKind);
        System.out.println("key: "+key);
        System.out.println("enable: "+enable);
        System.out.println("propByRes: "+propByRes);
        System.out.println("vAttr: "+vAttr);
        System.out.println("noPropResourceKeys: "+noPropResourceKeys);
        List<PropagationTaskInfo> updateTasks;

        try {
            updateTasks = propagationManager.getUpdateTasks(anyTypeKind,
                    key,
                    changePwd,
                    enable,
                    propByRes,
                    propByLinkedAccount,
                    vAttr,
                    noPropResourceKeys);

            assertEquals(expected.size(), updateTasks.size());

        } catch (Exception e) {
            assertEquals(expectedError.getClass(), e.getClass());
        }
    }
}
