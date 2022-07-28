package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.core.persistence.api.dao.*;
import org.apache.syncope.core.persistence.api.entity.*;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.apache.syncope.core.persistence.api.entity.group.Group;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.provisioning.api.DerAttrHandler;
import org.apache.syncope.core.provisioning.api.MappingManager;
import org.apache.syncope.core.provisioning.java.propagation.dummies.*;
import org.apache.syncope.core.provisioning.java.propagation.utils.*;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

abstract class DefaultPropagationManagerTest {

    protected VirSchemaDAO virSchemaDAO; //yes
    protected AnyUtilsFactory anyUtilsFactory; //yes
    protected ExternalResourceDAO externalResourceDAO; //yes
    protected MappingManager mappingManager; //yes
    protected DerAttrHandler derAttrHandler; //yes

    protected MockedStatic<ApplicationContextProvider> context;

    protected DummyProvision provision;
    private DummyVirSchema virSchema;
    private DummyExternalResource externalResource;
    private DummyMapping mapping;
    private AnyType anyType;

    public DefaultPropagationManagerTest(AnyTypeKind anyTypeKind) {
        initDummyImpl(anyTypeKind);
        this.externalResourceDAO = getMockedExternalResourceDAO();
        this.virSchemaDAO = getMockedVirSchemaDAO();
        this.mappingManager = getMockedMappingManager();
        this.derAttrHandler = getMockedDerAttrHandler();
        this.anyUtilsFactory = getMockedAnyUtilsFactory();
        settingDummyImpl(anyTypeKind);
    }

    private void initDummyImpl(AnyTypeKind anyTypeKind) {
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
        Mockito.when(virSchema.findByProvision(provision))
                .thenReturn(new ArrayList<>(Collections.singleton(this.virSchema)));
        return virSchema;
    }
}
