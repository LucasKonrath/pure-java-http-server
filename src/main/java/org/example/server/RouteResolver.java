package org.example.server;

import org.example.annotations.Controller;
import org.example.annotations.Route;
import org.example.enums.HttpMethod;
import org.example.spec.URLSpec;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.lang.reflect.*;
import java.util.stream.Collectors;

public class RouteResolver {
    public static HashMap<URLSpec, Method> mappings = new HashMap<>();

    static {
        try {
            Class[] classes = getClasses("org.example.controller");
            List<Class> controllers = Arrays
                .stream(classes)
                .filter(cls -> cls.isAnnotationPresent(Controller.class))
                .collect(Collectors.toList());

            controllers.stream().forEach(
                ctlr -> {
                    List<Method> methods = Arrays
                                                .stream(ctlr.getMethods())
                                                .filter(mtd -> mtd.isAnnotationPresent(Route.class))
                                                .collect(Collectors.toList());

                    for(Method method : methods){
                        Route route = method.getAnnotation(Route.class);
                        URLSpec urlSpec = new URLSpec(route.route(), route.method());
                        mappings.put(urlSpec, method);
                    }
                }
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Method resolve (String[] httpRequestLine){
        String method = httpRequestLine[0];
        String path = httpRequestLine[1];
        URLSpec urlSpec = new URLSpec(path, HttpMethod.valueOf(method));

        return mappings.get(urlSpec);
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
