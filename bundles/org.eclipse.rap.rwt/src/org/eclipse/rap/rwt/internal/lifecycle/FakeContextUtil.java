/*******************************************************************************
 * Copyright (c) 2010, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.util.ClassUtil;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.widgets.Display;


public final class FakeContextUtil {

  private static final ClassLoader CLASS_LOADER = FakeContextUtil.class.getClassLoader();
  private static final HttpServletResponse RESPONSE_PROXY = newResponse();
  private static final Class<?> REQUEST_PROXY_CLASS = getRequestProxyClass();

  private FakeContextUtil() {
    // prevent instantiation
  }

  public static void runNonUIThreadWithFakeContext( Display display, Runnable runnable ) {
    // Don't replace local variables by method calls, since the context may
    // change during the methods execution.
    Display sessionDisplay = LifeCycleUtil.getSessionDisplay();
    boolean useDifferentContext =  ContextProvider.hasContext() && sessionDisplay != display;
    ServiceContext contextBuffer = null;
    // TODO [fappel]: The context handling's getting very awkward in case of
    //                having the context mapped instead of stored it in
    //                the ContextProvider's ThreadLocal (see ContextProvider).
    //                Because of this the wasMapped variable is used to
    //                use the correct way to restore the buffered context.
    //                See whether this can be done more elegantly and supplement
    //                the test cases...
    boolean wasMapped = false;
    if( useDifferentContext ) {
      contextBuffer = ContextProvider.getContext();
      wasMapped = ContextProvider.releaseContextHolder();
    }
    boolean useFakeContext = !ContextProvider.hasContext();
    if( useFakeContext ) {
      IDisplayAdapter adapter = display.getAdapter( IDisplayAdapter.class );
      UISession uiSession = adapter.getUISession();
      ContextProvider.setContext( createFakeContext( uiSession ) );
    }
    try {
      runnable.run();
    } finally {
      if( useFakeContext ) {
        ContextProvider.disposeContext();
      }
      if( useDifferentContext ) {
        if( wasMapped ) {
          ContextProvider.setContext( contextBuffer, Thread.currentThread() );
        } else {
          ContextProvider.setContext( contextBuffer );
        }
      }
    }
  }

  public static ServiceContext createFakeContext( UISession uiSession ) {
    HttpServletRequest request = newRequest( uiSession );
    ServiceContext result = new ServiceContext( request, RESPONSE_PROXY, uiSession );
    result.setServiceStore( new ServiceStore() );
    return result;
  }

  private static HttpServletRequest newRequest( UISession uiSession ) {
    InvocationHandler invocationHandler = new RequestInvocationHandler( uiSession );
    Class[] paramTypes = new Class[] { InvocationHandler.class };
    Object[] paramValues = new Object[] { invocationHandler };
    Object proxy = ClassUtil.newInstance( REQUEST_PROXY_CLASS, paramTypes, paramValues );
    return ( HttpServletRequest )proxy;
  }

  private static Class<?> getRequestProxyClass() {
    return Proxy.getProxyClass( CLASS_LOADER, new Class<?>[] { HttpServletRequest.class } );
  }

  private static HttpServletResponse newResponse() {
    Class[] interfaces = new Class[] { HttpServletResponse.class };
    ResponseInvocationHandler invocationHandler = new ResponseInvocationHandler();
    Object proxy = Proxy.newProxyInstance( CLASS_LOADER, interfaces , invocationHandler );
    return ( HttpServletResponse )proxy;
  }

  private static final class ResponseInvocationHandler implements InvocationHandler {
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
      throw new UnsupportedOperationException();
    }
  }

  private static class RequestInvocationHandler implements InvocationHandler {
    private final UISession uiSession;

    RequestInvocationHandler( UISession uiSession ) {
      this.uiSession = uiSession;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
      Object result;
      if( "getSession".equals( method.getName() ) ) {
        result = uiSession.getHttpSession();
      } else if( "getLocale".equals( method.getName() ) ) {
        result = null;
      } else {
        throw new UnsupportedOperationException();
      }
      return result;
    }
  }

}
