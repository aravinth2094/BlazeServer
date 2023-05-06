package io.blaze.server.resolver;

import io.blaze.server.annotation.Route;
import io.blaze.server.model.ResolvedRoute;
import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RouteResolver {

    public ResolvedRoute resolve(final HttpMethod method,
                                 final String uri,
                                 final List<?> controllers) {
        for (final Object controller : controllers) {
            final Route route = controller.getClass().getAnnotation(Route.class);
            String routeValue = null;
            boolean routeFound = false;
            for (final String value : route.value()) {
                if (uri.startsWith(value)) {
                    routeFound = true;
                    routeValue = value;
                    break;
                }
            }
            if (!routeFound) {
                continue;
            }
            for (final Method declaredMethod : controller.getClass().getDeclaredMethods()) {
                final String[] paths = PathAnnotationEnum.getForMethod(method).getValue(declaredMethod);
                if (paths == null) {
                    continue;
                }
                for (final String path : paths) {
                    final Map<String, Object> pathVariables = getMatch(uri, routeValue + path);
                    if (pathVariables != null) {
                        return new ResolvedRoute(
                                controller,
                                declaredMethod,
                                Collections.unmodifiableMap(pathVariables)
                        );
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Object> getMatch(final String uri,
                                         final String path) {
        final String[] uriComponents = uri.split("/");
        final String[] pathComponents = path.split("/");
        if (uriComponents.length != pathComponents.length) {
            return null;
        }
        final Map<String, Object> variables = new HashMap<>();
        for (int i = 0; i < pathComponents.length; i++) {
            final String pathComponent = pathComponents[i];
            final String uriComponent = uriComponents[i];
            if (pathComponent.equals(uriComponent)) {
                continue;
            }
            if (!pathComponent.startsWith(":")) {
                return null;
            }
            variables.put(pathComponent.substring(1), convertType(uriComponent));
        }
        return variables;
    }

    public Object convertType(final String input) {
        if (input.matches("^-?\\d*\\.\\d+$")) {
            return Double.parseDouble(input);
        } else if (input.matches("^-?\\d+$")) {
            return Integer.parseInt(input);
        } else {
            return input;
        }
    }


}
