package org.apache.syncope.core.provisioning.java.propagation.utils;

import org.apache.syncope.core.persistence.api.entity.*;
import org.apache.syncope.core.persistence.api.entity.anyobject.AMembership;
import org.apache.syncope.core.persistence.api.entity.anyobject.APlainAttr;
import org.apache.syncope.core.persistence.api.entity.anyobject.ARelationship;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.apache.syncope.core.persistence.api.entity.resource.ExternalResource;
import org.apache.syncope.core.persistence.jpa.entity.JPAAnyType;
import org.apache.syncope.core.provisioning.java.propagation.dummies.DummyAnyTypeDAO;
import org.apache.syncope.core.spring.ApplicationContextProvider;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AnyObjectTested implements AnyObject {
    private String key = "anyObjectTested";
    private AnyType type;

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
        return type;
    }

    @Override
    public void setType(AnyType type) {
        this.type = type;
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
    public boolean add(APlainAttr attr) {
        return false;
    }

    @Override
    public boolean remove(APlainAttr attr) {
        return false;
    }

    @Override
    public Optional<? extends APlainAttr> getPlainAttr(String plainSchema) {
        return Optional.empty();
    }

    @Override
    public List<? extends APlainAttr> getPlainAttrs() {
        return null;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Optional<? extends APlainAttr> getPlainAttr(String plainSchema, Membership<?> membership) {
        return Optional.empty();
    }

    @Override
    public Collection<? extends APlainAttr> getPlainAttrs(String plainSchema) {
        return null;
    }

    @Override
    public Collection<? extends APlainAttr> getPlainAttrs(Membership<?> membership) {
        return null;
    }

    @Override
    public boolean add(AMembership membership) {
        return false;
    }

    @Override
    public boolean remove(AMembership membership) {
        return false;
    }

    @Override
    public Optional<? extends AMembership> getMembership(String groupKey) {
        return Optional.empty();
    }

    @Override
    public List<? extends AMembership> getMemberships() {
        return null;
    }

    @Override
    public boolean add(ARelationship relationship) {
        return false;
    }

    @Override
    public Optional<? extends ARelationship> getRelationship(RelationshipType relationshipType, String otherEndKey) {
        return Optional.empty();
    }

    @Override
    public Collection<? extends ARelationship> getRelationships(String otherEndKey) {
        return null;
    }

    @Override
    public Collection<? extends ARelationship> getRelationships(RelationshipType relationshipType) {
        return null;
    }

    @Override
    public List<? extends ARelationship> getRelationships() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }
}
