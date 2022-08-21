package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.common.lib.Attr;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.ResourceOperation;
import org.apache.syncope.core.persistence.api.dao.*;
import org.apache.syncope.core.persistence.api.entity.*;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.apache.syncope.core.persistence.api.entity.group.Group;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.provisioning.api.*;
import org.apache.syncope.core.provisioning.api.propagation.PropagationManager;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskInfo;
import org.apache.syncope.core.provisioning.java.propagation.dummies.*;
import org.apache.syncope.core.provisioning.java.propagation.utils.*;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.apache.syncope.core.spring.security.SyncopeGrantedAuthority;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.*;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;

abstract class DefaultPropagationManagerTest {

    protected VirSchemaDAO virSchemaDAO;
    protected AnyUtilsFactory anyUtilsFactory;
    protected ExternalResourceDAO externalResourceDAO;
    protected MappingManager mappingManager;
    protected DerAttrHandler derAttrHandler;

    protected MockedStatic<ApplicationContextProvider> context;

    protected DummyProvision provision;
    private DummyVirSchema virSchema;
    private DummyExternalResource externalResource;
    private DummyMapping mapping;
    private AnyType anyType;

    /* SUT and test parameters */
    protected PropagationManager propagationManager;

    protected AnyTypeKind anyTypeKind;
    protected String key;
    protected Boolean enable;
    protected PropagationByResource<String> propByRes;
    protected Collection<Attr> vAttr;
    protected Collection<String> noPropResourceKeys;

    protected List<PropagationTaskInfo> expected;
    protected Exception expectedError;

    public DefaultPropagationManagerTest(AnyTypeKind anyTypeKind) {
        initDummyImpl(anyTypeKind);
        MockitoAnnotations.initMocks(this);
        this.externalResourceDAO = getMockedExternalResourceDAO();
        this.virSchemaDAO = getMockedVirSchemaDAO();
        this.mappingManager = getMockedMappingManager();
        this.derAttrHandler = getMockedDerAttrHandler();
        this.anyUtilsFactory = getMockedAnyUtilsFactory();
        settingDummyImpl(anyTypeKind);
    }

    public void initDummyImpl(AnyTypeKind anyTypeKind) {
        /* init dummies */
        this.virSchema = new DummyVirSchema();
        this.provision = new DummyProvision();
        this.mapping = new DummyMapping();
        // TODO forse riunire in un unica classe AnyType e passare il tipo al costruttore
        if (anyTypeKind != null) {
            switch (anyTypeKind) {
                case USER:
                    this.anyType = new UserAnyType();
                    break;
                case GROUP:
                    this.anyType = new GroupAnyType();
                    break;
                case ANY_OBJECT:
                    this.anyType = new AnyObjectAnyType();
            }
        }
    }

    public void settingDummyImpl(AnyTypeKind anyTypeKind) {
        /* setting dummies */
        this.mapping.add(new DummyMappingItem());
        if (anyTypeKind != null) anyType.setKey(anyTypeKind.name());
        provision.setResource(this.externalResource);
        provision.setMapping(this.mapping);
        provision.setAnyType(this.anyType);
        provision.setObjectClass(new ObjectClass(ObjectClass.ACCOUNT_NAME));
        this.externalResource.add(provision);
    }

    public AnyUtilsFactory getMockedAnyUtilsFactory() {
        AnyUtilsFactory anyUtilsFactory = Mockito.mock(AnyUtilsFactory.class);

        /* User */
        Mockito.when(anyUtilsFactory.getInstance(AnyTypeKind.USER))
                .thenReturn(new DummyAnyUtils(AnyTypeKind.USER, anyUtilsFactory));
        Mockito.when(anyUtilsFactory.getInstance(any(User.class)))
                .thenReturn(new DummyAnyUtils(AnyTypeKind.USER, anyUtilsFactory));

        /* AnyObject */
        Mockito.when(anyUtilsFactory.getInstance(AnyTypeKind.ANY_OBJECT))
                .thenReturn(new DummyAnyUtils(AnyTypeKind.ANY_OBJECT, anyUtilsFactory));
        Mockito.when(anyUtilsFactory.getInstance(any(AnyObject.class)))
                .thenReturn(new DummyAnyUtils(AnyTypeKind.ANY_OBJECT, anyUtilsFactory));

        /* Group */
        Mockito.when(anyUtilsFactory.getInstance(AnyTypeKind.GROUP))
                .thenReturn(new DummyAnyUtils(AnyTypeKind.GROUP, anyUtilsFactory));
        Mockito.when(anyUtilsFactory.getInstance(any(Group.class)))
                .thenReturn(new DummyAnyUtils(AnyTypeKind.GROUP, anyUtilsFactory));
        return anyUtilsFactory;
    }

    public DerAttrHandler getMockedDerAttrHandler() {
        return Mockito.mock(DerAttrHandler.class);
    }

    public MappingManager getMockedMappingManager() {
        return Mockito.mock(MappingManager.class);
    }

    public ExternalResourceDAO getMockedExternalResourceDAO() {
        this.externalResource = new DummyExternalResource();
        ExternalResourceDAO externalResource = Mockito.mock(ExternalResourceDAO.class);
        Mockito.when(externalResource.find("validKey")).thenReturn(this.externalResource);
        return externalResource;
    }

    public VirSchemaDAO getMockedVirSchemaDAO() {
        VirSchemaDAO virSchema = Mockito.mock(VirSchemaDAO.class);
        Mockito.when(virSchema.find("vSchema")).thenReturn(this.virSchema);
        virSchema.find("vSchema").setProvision(provision);
        Mockito.when(virSchema.findByProvision(provision))
                .thenReturn(new ArrayList<>(Collections.singleton(this.virSchema)));
        return virSchema;
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

    protected void configureExpected(ReturnType returnType) {
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

    protected void configureNoPropResourceKeys(ParamType noPropResourceKeysType) {
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
                this.noPropResourceKeys.add("validKey");
                break;
            case INVALID:
                System.out.println("CASE INVALID");
                this.noPropResourceKeys = new ArrayList<>();
                this.noPropResourceKeys.add("invalidKey");
                break;
            default:
                System.out.println("CASE DEFAULT");
                break;
        }
    }

    protected void configureVAttr(ParamType vAttrType) {
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

    protected void configurePropByRes(ParamType propByResType) {
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


    protected void configureKey(ParamType keyType) {
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

    protected void configureAnyType(AnyTypeKind anyTypeKind, String testName) {
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
                    Mockito.when(mappingManager.prepareAttrsFromAny(any(User.class), any(), eq(true), any(), any(provision.getClass()))).thenReturn(attrs);
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
}
