package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.core.persistence.api.dao.ExternalResourceDAO;
import org.apache.syncope.core.persistence.api.dao.VirSchemaDAO;
import org.apache.syncope.core.persistence.api.entity.AnyType;
import org.apache.syncope.core.persistence.api.entity.AnyUtilsFactory;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.apache.syncope.core.persistence.api.entity.group.Group;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.provisioning.api.DerAttrHandler;
import org.apache.syncope.core.provisioning.api.MappingManager;
import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskExecutor;
import org.apache.syncope.core.provisioning.java.propagation.dummies.*;
import org.apache.syncope.core.provisioning.java.propagation.utils.AnyObjectAnyType;
import org.apache.syncope.core.provisioning.java.propagation.utils.GroupAnyType;
import org.apache.syncope.core.provisioning.java.propagation.utils.UserAnyType;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

public abstract class PropagationManagerAndExecutorIT {

    /* PropagationTaskExecutor attributes */
    protected MockedStatic<SecurityContextHolder> holder;
    protected PropagationTaskExecutor taskExecutor;

    /* DefaultPropagationManager attributes */
    protected VirSchemaDAO virSchemaDAO;
    protected AnyUtilsFactory anyUtilsFactory;
    protected ExternalResourceDAO externalResourceDAO;
    protected MappingManager mappingManager;
    protected DerAttrHandler derAttrHandler;

    protected DummyProvision provision;
    private DummyVirSchema virSchema;
    private DummyExternalResource externalResource;
    private DummyMapping mapping;
    private AnyType anyType;

    /* Common attributes */
    protected MockedStatic<ApplicationContextProvider> context;

    @Before
    public void setUp() {
        /* Module PropagationTaskExecutor set up */
        SecurityContext ctx = Mockito.mock(SecurityContext.class);
        List<GrantedAuthority> authorityList = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROOT");
        authorityList.add(authority);
        Authentication auth = new UsernamePasswordAuthenticationToken("principal", "credentials", authorityList);
        holder = Mockito.mockStatic(SecurityContextHolder.class);
        holder.when(SecurityContextHolder::getContext).thenReturn(ctx);
        Mockito.when(ctx.getAuthentication()).thenReturn(auth);

        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        DefaultPropagationTaskCallable taskCallable = new DefaultPropagationTaskCallable();
        factory.registerSingleton("callable", taskCallable);

        /* Module DefaultPropagationManager set up */
        DummyAnyTypeDAO dummyAnyTypeDAO = new DummyAnyTypeDAO();
        factory.registerSingleton("dummyAnyTypeDAO", dummyAnyTypeDAO);
        factory.autowireBean(dummyAnyTypeDAO);
        factory.initializeBean(dummyAnyTypeDAO, "Master");

        context = Mockito.mockStatic(ApplicationContextProvider.class);
        context.when(ApplicationContextProvider::getBeanFactory).thenReturn(factory);
    }

    @After
    public void tearDown() {
        holder.close();
        context.close();
    }

    public PropagationManagerAndExecutorIT(AnyTypeKind anyTypeKind) {
        /* init DefaultPropagationManager test */
        initDummyImpl(anyTypeKind);
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
}
