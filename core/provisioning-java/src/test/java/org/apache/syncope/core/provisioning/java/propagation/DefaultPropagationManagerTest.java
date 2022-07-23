package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.core.persistence.api.dao.*;
import org.apache.syncope.core.persistence.api.entity.*;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.provisioning.api.DerAttrHandler;
import org.apache.syncope.core.provisioning.api.MappingManager;
import org.apache.syncope.core.provisioning.java.propagation.dummies.*;
import org.apache.syncope.core.provisioning.java.propagation.utils.*;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.junit.BeforeClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

abstract class DefaultPropagationManagerTest {

    protected VirSchemaDAO virSchemaDAO; //yes
    protected AnyUtilsFactory anyUtilsFactory; //yes
    protected ExternalResourceDAO externalResourceDAO; //yes
    protected MappingManager mappingManager; //yes
    protected DerAttrHandler derAttrHandler; //yes

    protected DummyProvision provision;
    private DummyVirSchema virSchema;
    private DummyExternalResource externalResource;
    private DummyMapping mapping;
    private UserAnyType anyType;

    public DefaultPropagationManagerTest(AnyTypeKind anyTypeKind) {
        initDummyImpl();
        this.externalResourceDAO = getMockedExternalResourceDAO();
        this.virSchemaDAO = getMockedVirSchemaDAO();
        this.mappingManager = getMockedMappingManager();
        this.derAttrHandler = getMockedDerAttrHandler();
        this.anyUtilsFactory = getMockedAnyUtilsFactory();
        settingDummyImpl(anyTypeKind);
    }

    @BeforeClass
    public static void setUp(){
        DummyAnyTypeDAO dummyAnyTypeDAO = new DummyAnyTypeDAO();
        DefaultListableBeanFactory factory=new DefaultListableBeanFactory();
        factory.registerSingleton("dummyAnyTypeDAO", dummyAnyTypeDAO);
        factory.autowireBean(dummyAnyTypeDAO);
        factory.initializeBean(dummyAnyTypeDAO, "Master");
        MockedStatic<ApplicationContextProvider> util = Mockito.mockStatic(ApplicationContextProvider.class);
        util.when(ApplicationContextProvider::getBeanFactory).thenReturn(factory);
    }

    private void initDummyImpl() {
        /* init dummies */
        this.virSchema = new DummyVirSchema();
        this.provision = new DummyProvision();
        this.mapping = new DummyMapping();
        this.anyType = new UserAnyType();
    }

    private void settingDummyImpl(AnyTypeKind anyTypeKind) {
        /* setting dummies */
        this.mapping.add(new DummyMappingItem());
        if (anyTypeKind != null) anyType.setKey(anyTypeKind.name());
        provision.setResource(this.externalResource);
        provision.setMapping(this.mapping);
        provision.setAnyType(this.anyType);
        provision.setObjectClass(new ObjectClass(ObjectClass.ACCOUNT_NAME));
        this.externalResource.add(provision);
    }

    private AnyUtilsFactory getMockedAnyUtilsFactory() {
        AnyUtilsFactory anyUtilsFactory = Mockito.mock(AnyUtilsFactory.class);
        // TODO anyObjectTested
        // TODO groupTested

        /* User */
        Mockito.when(anyUtilsFactory.getInstance(AnyTypeKind.USER)).thenReturn(new DummyAnyUtils(AnyTypeKind.USER, anyUtilsFactory));
        Mockito.when(anyUtilsFactory.getInstance(any(User.class))).thenReturn(new DummyAnyUtils(AnyTypeKind.USER, anyUtilsFactory));

        /* AnyObject */
        Mockito.when(anyUtilsFactory.getInstance(AnyTypeKind.ANY_OBJECT)).thenReturn(new DummyAnyUtils(AnyTypeKind.ANY_OBJECT, anyUtilsFactory));

        /* Group */
        Mockito.when(anyUtilsFactory.getInstance(AnyTypeKind.GROUP)).thenReturn(new DummyAnyUtils(AnyTypeKind.GROUP, anyUtilsFactory));
        return anyUtilsFactory;
    }

    private DerAttrHandler getMockedDerAttrHandler() {
        return Mockito.mock(DerAttrHandler.class);
    }

    private MappingManager getMockedMappingManager() {
        return Mockito.mock(MappingManager.class);
    }

    protected ExternalResourceDAO getMockedExternalResourceDAO() {
        this.externalResource = new DummyExternalResource();
        ExternalResourceDAO externalResource = Mockito.mock(ExternalResourceDAO.class);
        Mockito.when(externalResource.find("validKey")).thenReturn(this.externalResource);
        return externalResource;
    }

    protected VirSchemaDAO getMockedVirSchemaDAO() {
        VirSchemaDAO virSchema = Mockito.mock(VirSchemaDAO.class);
        Mockito.when(virSchema.find("vSchema")).thenReturn(this.virSchema);
        virSchema.find("vSchema").setProvision(provision);
        Mockito.when(virSchema.findByProvision(provision)).thenReturn(new ArrayList<>(Collections.singleton(this.virSchema)));
        return virSchema;
    }
}
