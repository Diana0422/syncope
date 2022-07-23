package org.apache.syncope.core.provisioning.java.propagation.dummies;

import org.apache.syncope.common.lib.types.AttrSchemaType;
import org.apache.syncope.core.persistence.api.entity.AnyTypeClass;
import org.apache.syncope.core.persistence.api.entity.SchemaLabel;
import org.apache.syncope.core.persistence.api.entity.VirSchema;
import org.apache.syncope.core.persistence.api.entity.resource.MappingItem;
import org.apache.syncope.core.persistence.api.entity.resource.Provision;
import org.apache.syncope.core.persistence.jpa.entity.AbstractSchema;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class DummyVirSchema implements VirSchema {

    private Provision provision;

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void setKey(String key) {

    }

    @Override
    public AnyTypeClass getAnyTypeClass() {
        return null;
    }

    @Override
    public void setAnyTypeClass(AnyTypeClass anyTypeClass) {

    }

    @Override
    public AttrSchemaType getType() {
        return null;
    }

    @Override
    public String getMandatoryCondition() {
        return null;
    }

    @Override
    public boolean isMultivalue() {
        return false;
    }

    @Override
    public boolean isReadonly() {
        return false;
    }

    @Override
    public boolean isUniqueConstraint() {
        return false;
    }

    @Override
    public boolean add(SchemaLabel label) {
        return false;
    }

    @Override
    public Optional<? extends SchemaLabel> getLabel(Locale locale) {
        return Optional.empty();
    }

    @Override
    public List<? extends SchemaLabel> getLabels() {
        return null;
    }

    @Override
    public void setReadonly(boolean readonly) {

    }

    @Override
    public Provision getProvision() {
        return provision;
    }

    @Override
    public void setProvision(Provision provision) {
        this.provision = provision;
    }

    @Override
    public String getExtAttrName() {
        return null;
    }

    @Override
    public void setExtAttrName(String extAttrName) {

    }

    @Override
    public MappingItem asLinkingMappingItem() {
        return null;
    }
}
