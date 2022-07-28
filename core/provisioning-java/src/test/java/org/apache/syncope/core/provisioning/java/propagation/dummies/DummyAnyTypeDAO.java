package org.apache.syncope.core.provisioning.java.propagation.dummies;

import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.core.persistence.api.dao.AnyTypeDAO;
import org.apache.syncope.core.persistence.api.entity.AnyType;
import org.apache.syncope.core.persistence.api.entity.AnyTypeClass;
import org.apache.syncope.core.persistence.jpa.dao.AbstractDAO;
import org.apache.syncope.core.provisioning.java.propagation.utils.AnyObjectAnyType;
import org.apache.syncope.core.provisioning.java.propagation.utils.GroupAnyType;
import org.apache.syncope.core.provisioning.java.propagation.utils.UserAnyType;

import java.util.List;

public class DummyAnyTypeDAO extends AbstractDAO<AnyType> implements AnyTypeDAO {
    @Override
    public AnyType find(String key) {
        AnyType result = null;
        switch (key) {
            case "USER":
                result = new UserAnyType();
                break;
            case "GROUP":
                result = new GroupAnyType();
                break;
            case "ANY_OBJECT":
                result = new AnyObjectAnyType();
                break;
        }
        if (result != null) {
            result.setKind(AnyTypeKind.valueOf(key));
            result.setKey(key);
        }
        return result;
    }

    @Override
    public AnyType findUser() {
        return find(AnyTypeKind.USER.name());
    }

    @Override
    public AnyType findGroup() { return find(AnyTypeKind.GROUP.name()); }

    @Override
    public List<AnyType> findByTypeClass(AnyTypeClass anyTypeClass) {
        return null;
    }

    @Override
    public List<AnyType> findAll() {
        return null;
    }

    @Override
    public AnyType save(AnyType anyType) {
        return null;
    }

    @Override
    public void delete(String key) {

    }

    @Override
    public void refresh(AnyType entity) {

    }

    @Override
    public void detach(AnyType entity) {

    }

    @Override
    public void clear() {

    }
}
