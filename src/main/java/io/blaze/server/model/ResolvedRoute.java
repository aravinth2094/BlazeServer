package io.blaze.server.model;

import io.blaze.server.annotation.Body;
import io.blaze.server.annotation.Inject;
import io.blaze.server.annotation.PathVariable;
import io.blaze.server.annotation.Value;
import io.blaze.server.context.AppContext;
import io.blaze.server.resolver.ValueUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public record ResolvedRoute(Object controller,
                            Method method,
                            Map<String, Object> pathVariables) {

    public Object invoke(final HttpRequest request) throws InvocationTargetException, IllegalAccessException {
        request.setPathVariables(pathVariables);
        return method.invoke(controller, generateParameters(method, request));
    }

    private Object[] generateParameters(final Method method,
                                        final HttpRequest request) {
        if (method.getParameterCount() == 0) {
            return null;
        }
        final List<Object> args = new LinkedList<>();
        for (final Parameter parameter : method.getParameters()) {
            final AtomicReference<Object> arg = new AtomicReference<>();
            httpRequest(parameter, arg, request);
            inject(parameter, arg);
            body(parameter, arg, request);
            pathVariable(parameter, arg, request);
            value(parameter, arg);
            args.add(arg.get());
        }
        return args.toArray();
    }

    private void httpRequest(final Parameter parameter,
                             final AtomicReference<Object> arg,
                             final HttpRequest request) {
        if (parameter.getType() != HttpRequest.class) {
            return;
        }
        arg.set(request);
    }

    private void inject(final Parameter parameter,
                        final AtomicReference<Object> arg) {
        if (!parameter.isAnnotationPresent(Inject.class)) {
            return;
        }
        final Object instance = AppContext.get(parameter.getType());
        if (instance == null) {
            return;
        }
        arg.set(instance);
    }

    private void body(final Parameter parameter,
                      final AtomicReference<Object> arg,
                      final HttpRequest request) {
        if (!parameter.isAnnotationPresent(Body.class)) {
            return;
        }
        try {
            arg.set(request.body(parameter.getType()));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void pathVariable(final Parameter parameter,
                              final AtomicReference<Object> arg,
                              final HttpRequest request) {
        if (!parameter.isAnnotationPresent(PathVariable.class)) {
            return;
        }
        final String pathVariableName = parameter.getAnnotation(PathVariable.class).value();
        arg.set(request.getPathVariable(pathVariableName));
    }

    private void value(final Parameter parameter,
                       final AtomicReference<Object> arg) {
        if (!parameter.isAnnotationPresent(Value.class)) {
            return;
        }
        final Object value = ValueUtils.extractValue(
                parameter.getAnnotation(Value.class).value(),
                parameter.getType()
        );
        if (value == null) {
            throw new IllegalStateException(
                    MessageFormat.format(
                            "Cannot set property \"{0}\" to parameter. " +
                                    "Property \"{0}\" not found. Assign a default value to avoid exception.",
                            parameter.getAnnotation(Value.class).value().split(":")[0])
            );
        }
        arg.set(value);
    }

}
