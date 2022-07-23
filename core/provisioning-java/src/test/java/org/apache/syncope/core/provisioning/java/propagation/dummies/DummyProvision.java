package org.apache.syncope.core.provisioning.java.propagation.dummies;


import org.apache.syncope.core.persistence.api.entity.AnyType;
import org.apache.syncope.core.persistence.api.entity.AnyTypeClass;
import org.apache.syncope.core.persistence.api.entity.PlainSchema;
import org.apache.syncope.core.persistence.api.entity.resource.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.resource.Mapping;
import org.apache.syncope.core.persistence.api.entity.resource.Provision;
import org.apache.syncope.core.persistence.jpa.entity.JPAAnyType;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.SyncToken;

import java.util.List;
import java.util.Optional;

public class DummyProvision implements Provision {

    private ExternalResource resource;
    private Mapping mapping;
    private AnyType anyType;

    private String objectClass;

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public ExternalResource getResource() {
        return resource;
    }

    @Override
    public void setResource(ExternalResource resource) {
        this.resource = resource;
    }

    @Override
    public AnyType getAnyType() {
        return this.anyType;
    }

    @Override
    public void setAnyType(AnyType anyType) {
        this.anyType = anyType;
    }

    @Override
    public ObjectClass getObjectClass() {
        return Optional.ofNullable(objectClass).map(ObjectClass::new).orElse(null);
    }

    @Override
    public void setObjectClass(ObjectClass objectClass) {
        this.objectClass = Optional.ofNullable(objectClass).map(ObjectClass::getObjectClassValue).orElse(null);
    }

    @Override
    public boolean add(AnyTypeClass anyTypeClass) {
        return false;
    }

    @Override
    public List<? extends AnyTypeClass> getAuxClasses() {
        return null;
    }

    @Override
    public SyncToken getSyncToken() {
        return null;
    }

    @Override
    public String getSerializedSyncToken() {
        return null;
    }

    @Override
    public void setSyncToken(SyncToken syncToken) {

    }

    @Override
    public boolean isIgnoreCaseMatch() {
        return false;
    }

    @Override
    public void setIgnoreCaseMatch(boolean ignoreCaseMatch) {

    }

    @Override
    public PlainSchema getUidOnCreate() {
        return null;
    }

    @Override
    public void setUidOnCreate(PlainSchema uidOnCreate) {

    }

    @Override
    public Mapping getMapping() {
        return mapping;
    }

    @Override
    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }
}
