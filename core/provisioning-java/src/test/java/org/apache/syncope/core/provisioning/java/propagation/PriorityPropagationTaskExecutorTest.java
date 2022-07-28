package org.apache.syncope.core.provisioning.java.propagation;

import org.apache.syncope.core.provisioning.api.propagation.PropagationTaskExecutor;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.h2.security.auth.DefaultAuthenticator;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class PriorityPropagationTaskExecutorTest {

    protected MockedStatic<ApplicationContextProvider> context;
    protected MockedStatic<SecurityContextHolder> holder;
    protected PropagationTaskExecutor taskExecutor;

    @Before
    public void setUp() {
        SecurityContext ctx = Mockito.mock(SecurityContext.class);
        List<GrantedAuthority> authorityList = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROOT");
        authorityList.add(authority);
        Authentication auth = new UsernamePasswordAuthenticationToken("principal", "credentials", authorityList);
        holder = Mockito.mockStatic(SecurityContextHolder.class);
        holder.when(SecurityContextHolder::getContext).thenReturn(ctx);
        Mockito.when(ctx.getAuthentication()).thenReturn(auth);

        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        DefaultPropagationTaskCallable taskCallable = new DefaultPropagationTaskCallable();
        factory.registerSingleton("callable", taskCallable);
        context = Mockito.mockStatic(ApplicationContextProvider.class);
        context.when(ApplicationContextProvider::getBeanFactory).thenReturn(factory);
    }

    @After
    public void tearDown() {
        holder.close();
        context.close();
    }
}
