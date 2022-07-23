package org.apache.syncope.core.provisioning.java.propagation.dummies;

import org.apache.syncope.core.persistence.api.entity.resource.Mapping;
import org.apache.syncope.core.persistence.api.entity.resource.MappingItem;
import org.apache.syncope.core.persistence.api.entity.resource.Provision;
import org.apache.syncope.core.persistence.jpa.entity.resource.JPAMappingItem;
import org.apache.syncope.core.persistence.jpa.entity.resource.JPAProvision;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DummyMapping implements Mapping {

    private List<MappingItem> items = new ArrayList<>();
    private Provision provision;
    /**
     * A JEXL expression for determining how to find the connector object link in external resource's space.
     */
    private String connObjectLink;

    @Override
    public String getKey() {
        return null;
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
    public String getConnObjectLink() {
        return connObjectLink;
    }

    @Override
    public void setConnObjectLink(String connObjectLink) {
        this.connObjectLink = connObjectLink;
    }

    @Override
    public boolean add(MappingItem item) {
        return items.contains(item) || items.add(item);
    }

    @Override
    public Optional<? extends MappingItem> getConnObjectKeyItem() {
        return getItems().stream().filter(MappingItem::isConnObjectKey).findFirst();
    }

    @Override
    public void setConnObjectKeyItem(MappingItem item) {
        item.setConnObjectKey(true);
        this.add(item);
    }

    @Override
    public List<? extends MappingItem> getItems() {
        return items;
    }
}
