package org.example.server;

import com.owlike.genson.Genson;
import org.example.Pair;
import org.example.annotations.Controller;
import org.example.annotations.Payload;
import org.example.annotations.Route;
import org.example.enums.HttpMethod;
import org.example.enums.HttpStatus;
import org.example.response.HttpResponse;
import org.example.spec.URLSpec;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.lang.reflect.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RouteResolver {
    public static HashMap<URLSpec, Method> mappings = new HashMap<>();
    public static List<Object> controllerInstances = new ArrayList<>();

    public static Genson genson = new Genson();

    static {
        try {

            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);

            Class[] classes = getClasses("org.example.controller");
            List<Class> controllers = Arrays
                .stream(classes)
                .filter(cls -> cls.isAnnotationPresent(Controller.class))
                .toList();

            for(Class ctrl : controllers){
                controllerInstances.add(unsafe.allocateInstance(ctrl));
            }

            controllers.forEach(
                ctlr -> {
                    List<Method> methods = Arrays
                                                .stream(ctlr.getMethods())
                                                .filter(mtd -> mtd.isAnnotationPresent(Route.class))
                                                .collect(Collectors.toList());

                    for(Method method : methods){
                        Route route = method.getAnnotation(Route.class);
                        Controller controller = (Controller) ctlr.getAnnotation(Controller.class);
                        URLSpec urlSpec = new URLSpec(controller.context() + route.route(), route.method());
                        mappings.put(urlSpec, method);
                    }
                }
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Pair<Method, Object[]> resolve (String[] httpRequestLine, String body) throws MalformedURLException {
        String httpMethod = httpRequestLine[0];
        if(httpRequestLine.length == 1){
            return new Pair<>(null, new Object[]{});
        }
        String path = httpRequestLine[1];
        URLSpec urlSpec = new URLSpec(path, HttpMethod.valueOf(httpMethod));
        Method method = mappings.get(urlSpec);
        Matcher matcher = matchUrl(path);

        while (matcher.find()) {
            System.out.println("Captured group: " + matcher.group(1));
        }

        if(method == null){
            String [] pathArray = path.split("/");
            String pathCopy = path;
            for(int i = pathArray.length - 1; i > 0; i--){
                pathCopy = pathCopy.replace(pathArray[i], "*");
                URLSpec urlSpec1 = new URLSpec(pathCopy, HttpMethod.valueOf(httpMethod));
                Method methodMatched = mappings.get(urlSpec1);
                if(methodMatched != null){
                    Object[] args = resolveArgs(path, pathCopy, httpRequestLine, methodMatched, body);
                    return new Pair<>(methodMatched, args);
                }
            }
        } else {
                Object[] args = resolveArgs(path, path, httpRequestLine, method, body);
                return new Pair<>(method, args);
        }
        return null;
    }

    private static Object[] resolveArgs(String path, String pathCopy, String[] httpRequestLine, Method methodMatched, String body) {
        String[] pathSplit = path.split("/");
        String[] pathCopySplit = pathCopy.split("/");
        ArrayList<Object> args = new ArrayList<>();
        for(int i = 0; i < pathSplit.length; i++){
            if(!Objects.equals(pathSplit[i], pathCopySplit[i])){
                args.add(pathSplit[i]);
            }
        }

        String method = httpRequestLine[0];
        if(HttpMethod.POST.name().equalsIgnoreCase(method) || HttpMethod.PUT.name().equalsIgnoreCase(method)){
            args.add(body);
        }

        return args.toArray();
    }

    public static HttpResponse process(String[] httpRequestLine, String body) throws MalformedURLException {
        Pair<Method, Object[]> method = resolve(httpRequestLine, body);
        return controllerInstances.stream()
                .filter(ctrl -> Arrays.asList(ctrl.getClass().getMethods()).contains(method.getKey()))
                .findFirst()
                .map(instance -> {
                    try {
                        return  (HttpResponse) method.getKey().invoke(instance, method.getValue());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(new HttpResponse("404", HttpStatus.NOT_FOUND));
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

    private static Matcher matchUrl(String path){
        // Define the regex pattern with a capturing group for everything after each forward slash
        String regex = "/([^/]+)";

        // Compile the pattern
        Pattern pattern = Pattern.compile(regex);

        // Create a matcher for the input text
        Matcher matcher = pattern.matcher(path);

        return matcher;
    }
}
