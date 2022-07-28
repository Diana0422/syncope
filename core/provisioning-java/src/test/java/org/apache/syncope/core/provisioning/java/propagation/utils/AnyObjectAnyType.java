package org.apache.syncope.core.provisioning.java.propagation.utils;

import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.core.persistence.api.entity.AnyType;
import org.apache.syncope.core.persistence.api.entity.AnyTypeClass;
import org.apache.syncope.core.persistence.jpa.entity.AbstractProvidedKeyEntity;
import org.apache.syncope.core.persistence.jpa.entity.JPAAnyTypeClass;

import java.util.ArrayList;
import java.util.List;

public class AnyObjectAnyType extends AbstractProvidedKeyEntity implements AnyType {

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
        return classes.contains((JPAAnyTypeClass) anyTypeClass) || this.classes.add((JPAAnyTypeClass) anyTypeClass);
    }

    @Override
    public List<? extends AnyTypeClass> getClasses() {
        return classes;
    }
}
