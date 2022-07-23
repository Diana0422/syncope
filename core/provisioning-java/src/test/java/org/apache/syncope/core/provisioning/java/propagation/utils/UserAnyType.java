package org.apache.syncope.core.provisioning.java.propagation.utils;

import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.core.persistence.api.entity.AnyType;
import org.apache.syncope.core.persistence.api.entity.AnyTypeClass;
import org.apache.syncope.core.persistence.jpa.entity.AbstractProvidedKeyEntity;

import java.util.ArrayList;
import java.util.List;

public class UserAnyType extends AbstractProvidedKeyEntity implements AnyType {

    private AnyTypeKind kind;
    private List<AnyTypeClass> classes = new ArrayList<>();

    @Override
    public AnyTypeKind getKind() {
        return kind;
    }

    @Override
    public void setKind(AnyTypeKind kind) {
        this.kind = kind;
    }

    @Override
    public boolean add(AnyTypeClass anyTypeClass) {
        return classes.contains(anyTypeClass) || this.classes.add(anyTypeClass);
    }

    @Override
    public List<? extends AnyTypeClass> getClasses() {
        return classes;
    }
}
