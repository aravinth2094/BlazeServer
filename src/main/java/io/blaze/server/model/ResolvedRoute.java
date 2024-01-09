package io.blaze.server.model;

import io.blaze.server.resolver.MethodInvoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public record ResolvedRoute(Object controller,
                            Method method,
                            Map<String, Object> pathVariables) {

    private static final MethodInvoker INVOKER = new MethodInvoker();

    public Object invoke(final HttpRequest request) throws InvocationTargetException, IllegalAccessException {
        request.setPathVariables(pathVariables);
        return INVOKER.invoke(method, controller, request);
    }

}
