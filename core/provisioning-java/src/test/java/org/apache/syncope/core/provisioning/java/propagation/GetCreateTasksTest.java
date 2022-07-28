package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.syncope.common.lib.Attr;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.ResourceOperation;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.apache.syncope.core.persistence.api.entity.group.Group;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.provisioning.api.PropagationByResource;
import org.apache.syncope.core.provisioning.api.propagation.PropagationManager;
import org.apache.syncope.core.provisioning.api.propagation.PropagationReporter;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskExecutor;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskInfo;
import org.apache.syncope.core.provisioning.java.propagation.dummies.DummyAnyTypeDAO;
import org.apache.syncope.core.provisioning.java.propagation.utils.ParamType;
import org.apache.syncope.core.provisioning.java.propagation.utils.ReturnType;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.apache.syncope.core.spring.security.SyncopeGrantedAuthority;
import org.identityconnectors.framework.common.objects.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(Parameterized.class)
public class GetCreateTasksTest extends DefaultPropagationManagerTest {

    protected AnyTypeKind anyTypeKind;
    protected String key;
    protected Boolean enable;
    protected PropagationByResource<String> propByRes;
    protected Collection<Attr> vAttr;
    protected Collection<String> noPropResourceKeys;
    protected PropagationManager propagationManager;

    private MockedStatic<ApplicationContextProvider> context;

    protected List<PropagationTaskInfo> expected;
    protected Exception expectedError;

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

        configureAnyType(anyTypeKind);
        configureKey(keyType);
        configurePropByRes(propByResType);
        configureVAttr(vAttrType);
        configureNoPropResourceKeys(noPropResourceKeysType);
        configureExpected(returnType);
        System.out.println("anyTypeKind = " + anyTypeKind + ", key = " + key + ", enable = " + enable + ", propByRes = " + propByRes+ ", vAttr = " + vAttr + ", noPropResourceKeys = " + noPropResourceKeys + ", returnType = " + returnType);
    }

    private void configureExpected(ReturnType returnType) {
        switch (returnType) {
            case OK:
                this.expected = new ArrayList<>(Collections.singleton(new PropagationTaskInfo(externalResourceDAO.find("testResource"))));
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

    private void configureNoPropResourceKeys(ParamType noPropResourceKeysType) {
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
                this.noPropResourceKeys.add("invalidKey");
                break;
            case INVALID:
                System.out.println("CASE INVALID");
                this.noPropResourceKeys = new ArrayList<>();
                this.noPropResourceKeys.add("validKey");
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }
    }

    private void configureVAttr(ParamType vAttrType) {
        Attr attr = new Attr();
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
                attr.setSchema("vSchema");
                this.vAttr = new ArrayList<>();
                this.vAttr.add(attr);
                break;
            case INVALID:
                System.out.println("CASE INVALID");
                attr.setSchema("invalidSchema");
                this.vAttr = new ArrayList<>();
                this.vAttr.add(attr);
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }
    }

    private void configurePropByRes(ParamType propByResType) {
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
    }


    private void configureKey(ParamType keyType) {
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
    }

    private void configureAnyType(AnyTypeKind anyTypeKind) {
        if (anyTypeKind != null) {
            Set<Attribute> attributes = new HashSet<>();
            Attribute name;
            Attribute uid;
            ImmutablePair<String, Set<Attribute>> attrs = new ImmutablePair<>("info", attributes);
            switch (anyTypeKind) {
                case USER:
                    name = new Name("Diana Pasquali");
                    uid = new Uid("diapascal");
                    attributes.add(name);
                    attributes.add(uid);

                    /* User */
                    Mockito.when(mappingManager.prepareAttrsFromAny(any(User.class), eq("myPass"), eq(true), eq(enable), any(provision.getClass()))).thenReturn(attrs);
                    Mockito.when(mappingManager.prepareAttrsFromAny(any(User.class), eq(null), eq(true), eq(enable), any(provision.getClass()))).thenReturn(attrs);
                    break;
                case GROUP:
                    name = new Name("Group Name");
                    uid = new Uid("groupuid");
                    attributes.add(name);
                    attributes.add(uid);

                    /* Group */
                    Mockito.when(mappingManager.prepareAttrsFromAny(any(Group.class), eq(null), eq(true), eq(enable), any(provision.getClass()))).thenReturn(attrs);
                    break;
                case ANY_OBJECT:
                    name = new Name("Any Name");
                    uid = new Uid("anyobjuid");
                    attributes.add(name);
                    attributes.add(uid);

                    /* Group */
                    Mockito.when(mappingManager.prepareAttrsFromAny(any(AnyObject.class), eq(null), eq(true), eq(enable), any(provision.getClass()))).thenReturn(attrs);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + anyTypeKind);
            }
        }
    }

    @BeforeClass
    public static void init() {
        SecurityContext context = SecurityContextHolder.getContext();
        SecurityContextHolder.getContextHolderStrategy().setContext(context);
        SyncopeGrantedAuthority auth = new SyncopeGrantedAuthority("test");
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(auth);
        SecurityContextHolder.getContextHolderStrategy().getContext()
                .setAuthentication(new AnonymousAuthenticationToken("test", "test", authorities));
    }

    @Before
    public void setUp() {
        DummyAnyTypeDAO dummyAnyTypeDAO = new DummyAnyTypeDAO();
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerSingleton("dummyAnyTypeDAO", dummyAnyTypeDAO);
        factory.autowireBean(dummyAnyTypeDAO);
        factory.initializeBean(dummyAnyTypeDAO, "Master");

        context = Mockito.mockStatic(ApplicationContextProvider.class);
        context.when(ApplicationContextProvider::getBeanFactory).thenReturn(factory);
    }

    @After
    public void tearDown() {
        context.close();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                // {anyTypeKind, key, enable, propByRes, vAttr, noPropResourceKeys, resultType}

                /* ITER 1 */
                // monodimensionale
                {null, ParamType.NULL, true, ParamType.NULL, ParamType.NULL, ParamType.NULL, ReturnType.NULL_PTR_ERROR},
                {AnyTypeKind.USER, ParamType.NULL, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.EMPTY, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.INVALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.VALID, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.EMPTY, ParamType.VALID, ParamType.VALID, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.INVALID, ParamType.VALID, ParamType.VALID, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.EMPTY, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.NULL, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.INVALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.NULL, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.INVALID, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.EMPTY, ReturnType.OK},

                /* ITER 2 */
                // multidimensionale tra propByRes e noPropResourceKeys
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.INVALID, ParamType.VALID, ParamType.NULL, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.INVALID, ParamType.VALID, ParamType.EMPTY, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.NULL, ParamType.VALID, ParamType.VALID, ReturnType.FAIL},
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.EMPTY, ParamType.VALID, ParamType.VALID, ReturnType.FAIL},

                // multidimensionale tra anyTypeKind, key, enable
                {AnyTypeKind.USER, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.VALID, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.USER, ParamType.INVALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.INVALID, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.EMPTY, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.EMPTY, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.NULL, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.USER, ParamType.NULL, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.GROUP, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.GROUP, ParamType.VALID, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.GROUP, ParamType.INVALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.GROUP, ParamType.INVALID, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.GROUP, ParamType.EMPTY, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.GROUP, ParamType.EMPTY, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.GROUP, ParamType.NULL, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.GROUP, ParamType.NULL, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.ANY_OBJECT, ParamType.VALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.ANY_OBJECT, ParamType.VALID, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.OK},
                {AnyTypeKind.ANY_OBJECT, ParamType.INVALID, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.ANY_OBJECT, ParamType.INVALID, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.ANY_OBJECT, ParamType.EMPTY, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.ANY_OBJECT, ParamType.EMPTY, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.ANY_OBJECT, ParamType.NULL, true, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},
                {AnyTypeKind.ANY_OBJECT, ParamType.NULL, false, ParamType.VALID, ParamType.VALID, ParamType.VALID, ReturnType.NOT_FOUND_ERROR},


                /* Iter 3 */
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
