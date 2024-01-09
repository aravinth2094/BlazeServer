package io.blaze.server.resolver;

import io.blaze.server.annotation.*;
import io.blaze.server.context.AppContext;
import io.blaze.server.model.HttpRequest;

import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Aravinth Sundaram on 09/01/24
 */
public class MethodInvoker {

    public Object invoke(final Method method,
                         final Object target,
                         final HttpRequest request) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, generateParameters(method, request));
    }

    public Object construct(final Constructor<?> constructor) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        return constructor.newInstance(generateParameters(constructor, null));
    }

    private Object[] generateParameters(final Executable method,
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
            requestAttribute(parameter, arg, request);
            args.add(arg.get());
        }
        return args.toArray();
    }

    private void httpRequest(final Parameter parameter,
                             final AtomicReference<Object> arg,
                             final HttpRequest request) {
        if (request == null) {
            return;
        }
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
        if (request == null) {
            return;
        }
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
        if (request == null) {
            return;
        }
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

    private void requestAttribute(final Parameter parameter,
                                  final AtomicReference<Object> arg,
                                  final HttpRequest request) {
        if (request == null) {
            return;
        }
        if (!parameter.isAnnotationPresent(RequestAttribute.class)) {
            return;
        }
        final Object attribute = request
                .getAttributes()
                .get(parameter.getAnnotation(RequestAttribute.class).value());
        if (attribute == null) {
            throw new IllegalStateException(
                    MessageFormat.format(
                            "Cannot set attribute \"{0}\" to parameter. " +
                                    "Attribute \"{0}\" not found.",
                            parameter.getAnnotation(RequestAttribute.class).value())
            );
        }
        arg.set(attribute);
    }

}
