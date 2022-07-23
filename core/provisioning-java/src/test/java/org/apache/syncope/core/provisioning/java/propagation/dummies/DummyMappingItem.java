package org.apache.syncope.core.provisioning.java.propagation.dummies;

import org.apache.syncope.core.persistence.api.entity.Implementation;
import org.apache.syncope.core.persistence.api.entity.resource.Mapping;
import org.apache.syncope.core.persistence.api.entity.resource.MappingItem;
import org.apache.syncope.core.persistence.jpa.entity.resource.AbstractItem;

import java.util.List;

public class DummyMappingItem extends AbstractItem implements MappingItem {

    Mapping mapping;

    @Override
    public boolean add(Implementation transformer) {
        return false;
    }

    @Override
    public List<? extends Implementation> getTransformers() {
        return null;
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
