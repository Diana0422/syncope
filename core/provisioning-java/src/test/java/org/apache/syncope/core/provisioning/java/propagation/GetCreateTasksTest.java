package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.syncope.common.lib.Attr;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.ResourceOperation;
import org.apache.syncope.core.persistence.api.entity.Any;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.provisioning.api.PropagationByResource;
import org.apache.syncope.core.provisioning.api.propagation.PropagationManager;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskInfo;
import org.apache.syncope.core.provisioning.java.propagation.utils.ParamType;
import org.apache.syncope.core.provisioning.java.propagation.utils.ReturnType;
import org.apache.syncope.core.provisioning.java.propagation.utils.UserAnyType;
import org.checkerframework.checker.units.qual.A;
import org.identityconnectors.framework.common.objects.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(Parameterized.class)
public class GetCreateTasksTest extends DefaultPropagationManagerTest {

    private AnyTypeKind anyTypeKind;
    private String key;
    private Boolean enable;
    private PropagationByResource<String> propByRes;
    private Collection<Attr> vAttr;
    private Collection<String> noPropResourceKeys;
    private PropagationManager propagationManager;

    private List<PropagationTaskInfo> expected;
    private Exception expectedError;



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

        if (anyTypeKind != null) {
            switch (anyTypeKind) {
                case USER:
                    Set<Attribute> attributes = new HashSet<>();
                    Attribute name = new Name("Diana Pasquali");
                    Attribute uid = new Uid("diapascal");
                    attributes.add(name);
                    attributes.add(uid);

                    ImmutablePair<String, Set<Attribute>> attrs = new ImmutablePair<>("info", attributes);
                    /* User */
                    Mockito.when(mappingManager.prepareAttrsFromAny(any(User.class), eq(null), eq(true), eq(enable), any(provision.getClass()))).thenReturn(attrs);
                    break;
                case GROUP:
                    break;
                case ANY_OBJECT:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + anyTypeKind);
            }
        }

        switch (keyType) {
            case NULL:
                System.out.println("CASE NULL");
                this.key = null;
                break;
            case EMPTY:
                System.out.println("CASE EMPTY");
                this.key = "";
                break;
            case VALID:
                System.out.println("CASE VALID");
                this.key = "validKey";
                break;
            case INVALID:
                System.out.println("CASE INVALID");
                this.key = "invalidKey";
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }

        switch (propByResType) {
            case NULL:
                System.out.println("CASE NULL");
                this.propByRes = null;
                break;
            case EMPTY:
                System.out.println("CASE EMPTY");
                this.propByRes = new PropagationByResource<>();
                break;
            case INVALID:
                System.out.println("CASE INVALID");
                this.propByRes = new PropagationByResource<>();
                this.propByRes.add(ResourceOperation.DELETE, "invalidKey");
                break;
            case VALID:
                System.out.println("CASE VALID");
                this.propByRes = new PropagationByResource<>();
                this.propByRes.add(ResourceOperation.CREATE, "validKey");
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }

        switch (vAttrType) {
            case NULL:
                System.out.println("CASE NULL");
                this.vAttr = null;
                break;
            case EMPTY:
                System.out.println("CASE EMPTY");
                this.vAttr = new ArrayList<>();
                break;
            case VALID:
                System.out.println("CASE VALID");
                Attr attr = new Attr();
                attr.setSchema("vSchema");
                this.vAttr = new ArrayList<>();
                this.vAttr.add(attr);
                break;
            case INVALID:
                System.out.println("CASE INVALID");
                //TODO to be implemented
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }

        switch (noPropResourceKeysType) {
            case NULL:
                System.out.println("CASE NULL");
                this.noPropResourceKeys = null;
                break;
            case EMPTY:
                System.out.println("CASE EMPTY");
                this.noPropResourceKeys = new ArrayList<>();
                break;
            case VALID:
                System.out.println("CASE VALID");
                this.noPropResourceKeys = new ArrayList<>();
                this.noPropResourceKeys.add("testResource");
                break;
            case INVALID:
                System.out.println("CASE INVALID");
                this.noPropResourceKeys = new ArrayList<>();
                this.noPropResourceKeys.add("invalidResource");
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }

        switch (returnType) {
            case OK:
                this.expected = new ArrayList<>(Collections.singleton(new PropagationTaskInfo(externalResourceDAO.find("testResource"))));
                break;
            case ERROR:
                this.expectedError = new NullPointerException();
                break;
            case FAIL:
                this.expected = new ArrayList<>();
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }
        System.out.println("anyTypeKind = " + anyTypeKind + ", key = " + key + ", enable = " + enable + ", propByRes = " + propByRes+ ", vAttr = " + vAttr + ", noPropResourceKeys = " + noPropResourceKeys + ", returnType = " + returnType);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                // {anyTypeKind, key, enable, propByRes, vAttr, noPropResourceKeys, resultType}
                {null, ParamType.NULL, true, ParamType.NULL, ParamType.NULL, ParamType.NULL, ReturnType.ERROR},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.EMPTY, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.ERROR},
                {AnyTypeKind.USER, ParamType.INVALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.ERROR},
                {AnyTypeKind.USER, ParamType.VALID, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.EMPTY, ParamType.VALID, ParamType.VALID, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.INVALID, ParamType.VALID, ParamType.VALID, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.EMPTY, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.NULL, ParamType.VALID, ReturnType.FAIL}, //FIXME
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.INVALID, ParamType.VALID, ReturnType.FAIL}, //FIXME
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.INVALID, ReturnType.FAIL}, //FIXME
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.NULL, ReturnType.FAIL}, //FIXME
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
            System.out.println(e);
            assertEquals(expectedError.getClass(), e.getClass());
            return;
        }
        /* test result size of created tasks */
        assertEquals(expected.size(), createTasks.size());
    }
}
