package org.apache.syncope.core.provisioning.java.propagation.dummies;

import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.request.AnyCR;
import org.apache.syncope.common.lib.request.AnyUR;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.core.persistence.api.dao.*;
import org.apache.syncope.core.persistence.api.entity.*;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.apache.syncope.core.persistence.api.entity.group.Group;
import org.apache.syncope.core.persistence.api.entity.resource.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.persistence.jpa.dao.AbstractAnyDAO;
import org.apache.syncope.core.persistence.jpa.dao.JPAUserDAO;
import org.apache.syncope.core.provisioning.java.propagation.utils.AnyObjectAnyType;
import org.apache.syncope.core.provisioning.java.propagation.utils.AnyObjectTested;
import org.apache.syncope.core.provisioning.java.propagation.utils.GroupTested;
import org.apache.syncope.core.provisioning.java.propagation.utils.UserTested;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class DummyAnyUtils implements AnyUtils {

    private AnyTypeKind anyTypeKind;
    private AnyUtilsFactory anyUtilsFactory;

    public DummyAnyUtils(AnyTypeKind type, AnyUtilsFactory anyUtilsFactory) {
        this.anyTypeKind = type;
        this.anyUtilsFactory = anyUtilsFactory;
    }

    @Override
    public AnyTypeKind anyTypeKind() {
        return anyTypeKind;
    }

    @Override
    public <T extends Any<?>> Class<T> anyClass() {
        return null;
    }

    @Override
    public Field getField(String name) {
        return null;
    }

    @Override
    public <T extends PlainAttr<?>> Class<T> plainAttrClass() {
        return null;
    }

    @Override
    public <T extends PlainAttr<?>> T newPlainAttr() {
        return null;
    }

    @Override
    public <T extends PlainAttrValue> Class<T> plainAttrValueClass() {
        return null;
    }

    @Override
    public <T extends PlainAttrValue> T newPlainAttrValue() {
        return null;
    }

    @Override
    public <T extends PlainAttrValue> Class<T> plainAttrUniqueValueClass() {
        return null;
    }

    @Override
    public <T extends PlainAttrValue> T newPlainAttrUniqueValue() {
        return null;
    }

    @Override
    public <T extends PlainAttrValue> T clonePlainAttrValue(T src) {
        return null;
    }

    @Override
    public <T extends AnyTO> T newAnyTO() {
        return null;
    }

    @Override
    public <C extends AnyCR> C newAnyCR() {
        return null;
    }

    @Override
    public <U extends AnyUR> U newAnyUR(String key) {
        return null;
    }

    @Override
    public <A extends Any<?>> AnyDAO<A> dao() {
        AnyDAO<A> result = null;

        switch (anyTypeKind()) {
            case USER:
                result = (AnyDAO<A>) Mockito.mock(JPAUserDAO.class);
                User user = new UserTested();
                Mockito.when(result.authFind("validKey")).thenReturn((A) user);
                Mockito.when(result.findAllowedSchemas((A) any(User.class), eq(VirSchema.class))).thenReturn(new AllowedSchemas<>());
                break;

            case GROUP:
                result = (AnyDAO<A>) Mockito.mock(GroupDAO.class);
                Group group = new GroupTested();
                Mockito.when(result.authFind("validKey")).thenReturn((A) group);
                Mockito.when(result.findAllowedSchemas((A) any(Group.class), eq(VirSchema.class))).thenReturn(new AllowedSchemas<>());
                break;

            case ANY_OBJECT:
                result = (AnyDAO<A>) Mockito.mock(AnyObjectDAO.class);
                AnyObject obj = new AnyObjectTested();
                AnyType type = new AnyObjectAnyType();
                if (anyTypeKind != null) type.setKey(anyTypeKind.name());
                obj.setType(type);
                Mockito.when(result.authFind("validKey")).thenReturn((A) obj);
                Mockito.when(result.findAllowedSchemas((A) any(AnyObject.class), eq(VirSchema.class))).thenReturn(new AllowedSchemas<>());
                break;
            default:
                break;
        }
        Mockito.when(result.authFind(null)).thenAnswer(invocationOnMock -> {
            throw new NotFoundException("Null key");
        });
        Mockito.when(result.authFind("invalidKey")).thenAnswer(invocationOnMock -> {
            throw new NotFoundException(StringUtils.substringBefore(
                    StringUtils.substringAfter(getClass().getSimpleName(), "JPA"), "DAO") + ' ' + "invalidKey");
        });
        Mockito.when(result.authFind("")).thenAnswer(invocationOnMock -> {
            throw new NotFoundException(StringUtils.substringBefore(
                    StringUtils.substringAfter(getClass().getSimpleName(), "JPA"), "DAO") + ' ' + "");
        });

        return result;
    }

    @Override
    public Set<ExternalResource> getAllResources(Any<?> any) {
        return null;
    }

    @Override
    public void addAttr(String key, PlainSchema schema, String value) {

    }
}
