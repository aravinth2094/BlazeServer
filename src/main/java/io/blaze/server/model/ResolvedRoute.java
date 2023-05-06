package io.blaze.server.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public record ResolvedRoute(Object controller,
                            Method method,
                            Map<String, Object> pathVariables) {

    public Object invoke(final HttpRequest request) throws InvocationTargetException, IllegalAccessException {
        request.setPathVariables(pathVariables);
        return method.invoke(controller, request);
    }

}
