import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestProcessor {

    public static void runTests(Class<?> testClass) {
        final Constructor<?> constructor;
        try {
            constructor = testClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Не найден конструктор без аргументов для класса \"" + testClass.getName() + "\"");
        }

        final Object testObj;
        try {
            testObj = constructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Не удалось создать экземпляр класса \"" + testClass.getName() + "\"");
        }

        Map<Integer, List<Method>> testMethods = new HashMap<>();
        List<Method> beforeEachMethods = new ArrayList<>();
        List<Method> afterEachMethods = new ArrayList<>();
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                checkMethod(method);
                int order = method.getAnnotation(Test.class).order();
                if (!testMethods.containsKey(order)) {
                    testMethods.put(order, new ArrayList<>());
                }
                testMethods.get(order).add(method);
            } else if (method.isAnnotationPresent(BeforeEach.class)) {
                checkMethod(method);
                beforeEachMethods.add(method);
            } else if (method.isAnnotationPresent(AfterEach.class)) {
                checkMethod(method);
                afterEachMethods.add(method);
            }
        }

        testMethods.keySet().stream()
                .sorted()
                .forEachOrdered(it ->
                        testMethods.get(it).forEach(method ->
                                runTest(method, testObj, beforeEachMethods, afterEachMethods)));
    }

    private static void checkMethod(Method method) {
        if (!method.getReturnType().isAssignableFrom(void.class) || !(method.getParameterCount() == 0)) {
            throw new IllegalArgumentException("Метод \"" + method.getName() + "\" должен быть void и не иметь аргументов.");
        }
    }

    private static void runTest(Method testMethod, Object testObj, List<Method> beforeEach, List<Method> afterEach) {
        if (testMethod.isAnnotationPresent(Skip.class)) {
            return;
        }
        try {
            for (Method method : beforeEach) {
                method.invoke(testObj);
            }
            testMethod.invoke(testObj);
            for (Method method : afterEach) {
                method.invoke(testObj);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Не удалось запустить метод\"" + testMethod.getName() + "\"");
        }
    }
}
