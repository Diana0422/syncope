package org.apache.syncope.core.provisioning.java.propagation.utils;

import org.apache.syncope.core.persistence.api.entity.AnyType;
import org.apache.syncope.core.persistence.api.entity.AnyTypeClass;
import org.apache.syncope.core.persistence.api.entity.Realm;
import org.apache.syncope.core.persistence.api.entity.anyobject.ADynGroupMembership;
import org.apache.syncope.core.persistence.api.entity.group.GPlainAttr;
import org.apache.syncope.core.persistence.api.entity.group.Group;
import org.apache.syncope.core.persistence.api.entity.group.TypeExtension;
import org.apache.syncope.core.persistence.api.entity.resource.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.user.UDynGroupMembership;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.provisioning.java.propagation.dummies.DummyAnyTypeDAO;
import org.apache.syncope.core.spring.ApplicationContextProvider;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class GroupTested implements Group {
    private String key;

    @Override
    public OffsetDateTime getCreationDate() {
        return null;
    }

    @Override
    public String getCreator() {
        return null;
    }

    @Override
    public String getCreationContext() {
        return null;
    }

    @Override
    public OffsetDateTime getLastChangeDate() {
        return null;
    }

    @Override
    public String getLastModifier() {
        return null;
    }

    @Override
    public String getLastChangeContext() {
        return null;
    }

    @Override
    public void setCreationDate(OffsetDateTime creationDate) {

    }

    @Override
    public void setCreator(String creator) {

    }

    @Override
    public void setCreationContext(String context) {

    }

    @Override
    public void setLastChangeDate(OffsetDateTime lastChangeDate) {

    }

    @Override
    public void setLastModifier(String lastModifier) {

    }

    @Override
    public void setLastChangeContext(String context) {

    }

    @Override
    public AnyType getType() {
        return ApplicationContextProvider
                .getBeanFactory()
                .getBean(DummyAnyTypeDAO.class)
                .findGroup();
    }

    @Override
    public void setType(AnyType type) {
        // nothing to do
    }

    @Override
    public Realm getRealm() {
        return null;
    }

    @Override
    public void setRealm(Realm realm) {

    }

    @Override
    public String getStatus() {
        return null;
    }

    @Override
    public void setStatus(String status) {

    }

    @Override
    public boolean add(ExternalResource resource) {
        return false;
    }

    @Override
    public List<? extends ExternalResource> getResources() {
        return null;
    }

    @Override
    public boolean add(AnyTypeClass auxClass) {
        return false;
    }

    @Override
    public List<? extends AnyTypeClass> getAuxClasses() {
        return null;
    }

    @Override
    public boolean add(GPlainAttr attr) {
        return false;
    }

    @Override
    public boolean remove(GPlainAttr attr) {
        return false;
    }

    @Override
    public Optional<? extends GPlainAttr> getPlainAttr(String plainSchema) {
        return Optional.empty();
    }

    @Override
    public List<? extends GPlainAttr> getPlainAttrs() {
        return null;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public Group getGroupOwner() {
        return null;
    }

    @Override
    public User getUserOwner() {
        return null;
    }

    @Override
    public void setGroupOwner(Group groupOwner) {

    }

    @Override
    public void setUserOwner(User userOwner) {

    }

    @Override
    public UDynGroupMembership getUDynMembership() {
        return null;
    }

    @Override
    public void setUDynMembership(UDynGroupMembership uDynMembership) {

    }

    @Override
    public boolean add(ADynGroupMembership dynGroupMembership) {
        return false;
    }

    @Override
    public Optional<? extends ADynGroupMembership> getADynMembership(AnyType anyType) {
        return Optional.empty();
    }

    @Override
    public List<? extends ADynGroupMembership> getADynMemberships() {
        return null;
    }

    @Override
    public boolean add(TypeExtension typeExtension) {
        return false;
    }

    @Override
    public Optional<? extends TypeExtension> getTypeExtension(AnyType anyType) {
        return Optional.empty();
    }

    @Override
    public List<? extends TypeExtension> getTypeExtensions() {
        return null;
    }
}
