package org.geektimes.web.mvc;

import org.apache.commons.lang.StringUtils;
import org.geektimes.web.mvc.annotation.Autowired;
import org.geektimes.web.mvc.annotation.Component;
import org.geektimes.web.mvc.annotation.Service;
import org.geektimes.web.mvc.controller.Controller;
import org.geektimes.web.mvc.controller.PageController;
import org.geektimes.web.mvc.controller.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.substringAfter;

public class FrontControllerServlet extends HttpServlet {

    /**
     * 请求路径和 Controller 的映射关系缓存
     */
    private Map<String, Controller> controllersMapping = new HashMap<>();

    /**
     * 请求路径和 {@link HandlerMethodInfo} 映射关系缓存
     */
    private Map<String, HandlerMethodInfo> handleMethodInfoMapping = new HashMap<>();
    /**
     * 类与实例的映射关系缓存
     */
    private Map<Class<?>, Object> classInstanceMapping = new HashMap<>();

    /**
     * 初始化 Servlet
     *
     * @param servletConfig
     */
    public void init(ServletConfig servletConfig) {
        try {
            initialInstance();
            autowire();
            initHandleMethods();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取所有的 RestController 的注解元信息 @Path
     * 利用 ServiceLoader 技术（Java SPI）
     */
    private void initHandleMethods() {
        for (Map.Entry<Class<?>, Object> entry : classInstanceMapping.entrySet()) {
            Class<?> controllerClass = entry.getKey();
            if(controllerClass.isAnnotationPresent(org.geektimes.web.mvc.annotation.Controller.class)){
                Path pathFromClass = controllerClass.getAnnotation(Path.class);
                String requestPath = pathFromClass.value();
                Method[] publicMethods = controllerClass.getDeclaredMethods();
                // 处理方法支持的 HTTP 方法集合
                for (Method method : publicMethods) {
                    Set<String> supportedHttpMethods = findSupportedHttpMethods(method);
                    Path pathFromMethod = method.getAnnotation(Path.class);
                    if (pathFromMethod != null) {
                        requestPath += pathFromMethod.value();
                    }
                    handleMethodInfoMapping.put(requestPath,
                            new HandlerMethodInfo(requestPath, method, supportedHttpMethods));
                }
                controllersMapping.put(requestPath, (Controller) entry.getValue());
            }
        }
    }

    /**
     * 获取处理方法中标注的 HTTP方法集合
     *
     * @param method 处理方法
     * @return
     */
    private Set<String> findSupportedHttpMethods(Method method) {
        Set<String> supportedHttpMethods = new LinkedHashSet<>();
        for (Annotation annotationFromMethod : method.getAnnotations()) {
            HttpMethod httpMethod = annotationFromMethod.annotationType().getAnnotation(HttpMethod.class);
            if (httpMethod != null) {
                supportedHttpMethods.add(httpMethod.value());
            }
        }

        if (supportedHttpMethods.isEmpty()) {
            supportedHttpMethods.addAll(asList(HttpMethod.GET, HttpMethod.POST,
                    HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.OPTIONS));
        }

        return supportedHttpMethods;
    }

    /**
     * SCWCD
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 建立映射关系
        // requestURI = /a/hello/world
        String requestURI = request.getRequestURI();
        // contextPath  = /a or "/" or ""
        String servletContextPath = request.getContextPath();
        String prefixPath = servletContextPath;
        // 映射路径（子路径）
        String requestMappingPath = substringAfter(requestURI,
                StringUtils.replace(prefixPath, "//", "/"));
        // 映射到 Controller
        Controller controller = controllersMapping.get(requestMappingPath);

        if (controller != null) {

            HandlerMethodInfo handlerMethodInfo = handleMethodInfoMapping.get(requestMappingPath);

            try {
                if (handlerMethodInfo != null) {

                    String httpMethod = request.getMethod();

                    if (!handlerMethodInfo.getSupportedHttpMethods().contains(httpMethod)) {
                        // HTTP 方法不支持
                        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        return;
                    }

                    if (controller instanceof PageController) {
                        PageController pageController = PageController.class.cast(controller);
                        String viewPath = pageController.execute(request, response);
                        // 页面请求 forward
                        // request -> RequestDispatcher forward
                        // RequestDispatcher requestDispatcher = request.getRequestDispatcher(viewPath);
                        // ServletContext -> RequestDispatcher forward
                        // ServletContext -> RequestDispatcher 必须以 "/" 开头
                        ServletContext servletContext = request.getServletContext();
                        if (!viewPath.startsWith("/")) {
                            viewPath = "/" + viewPath;
                        }
                        RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher(viewPath);
                        requestDispatcher.forward(request, response);
                        return;
                    } else if (controller instanceof RestController) {
                        System.out.println("请求方法名为" + request.getMethod());
                        Method handlerMethod = handlerMethodInfo.getHandlerMethod();
                        handlerMethod.invoke(controller, request, response);
                        // TODO 取RestController里的所有方法名
                    }

                }
            } catch (Throwable throwable) {
                if (throwable.getCause() instanceof IOException) {
                    throw (IOException) throwable.getCause();
                } else {
                    throw new ServletException(throwable.getCause());
                }
            }
        }
    }

    //    private void beforeInvoke(Method handleMethod, HttpServletRequest request, HttpServletResponse response) {
//
//        CacheControl cacheControl = handleMethod.getAnnotation(CacheControl.class);
//
//        Map<String, List<String>> headers = new LinkedHashMap<>();
//
//        if (cacheControl != null) {
//            CacheControlHeaderWriter writer = new CacheControlHeaderWriter();
//            writer.write(headers, cacheControl.value());
//        }
//    }
    //初始化实例
    public void initialInstance() throws InstantiationException, IllegalAccessException {

        List<Class<?>> classes = scanPackage("org.geektimes",new ArrayList<>());
//        List<Class<?>> classes = scanPackage("WEB-INF/classes");
        //扫描类
        for (Class<?> clz : classes) {
            if (clz.isAnnotationPresent(Component.class) || clz.isAnnotationPresent(Service.class)
                    || clz.isAnnotationPresent(org.geektimes.web.mvc.annotation.Controller.class)) {
                if(!clz.isInterface()&&!clz.isAnnotation()){
                    Object obj = clz.newInstance();
                    classInstanceMapping.put(clz, obj);
                }
            }
        }
    }

    //依赖注入
    public void autowire() throws IllegalAccessException {
        //注入实例
        for (Map.Entry<Class<?>, Object> entry : classInstanceMapping.entrySet()) {
            Class<?> initialedClass = entry.getKey();
            Field[] declaredFields = initialedClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object instance = entry.getValue();
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    field.set(instance, classInstanceMapping.get(fieldType));
                }
            }
        }
    }

    public static List<Class<?>> scanPackage(String packageName,List<Class<?>> classes) {
        File directory = null;
        String fullPath;
        String relPath = packageName.replace('.', '/');
        ClassLoader cl = FrontControllerServlet.class.getClassLoader();
        URL resource = cl != null ? cl.getResource(relPath) : ClassLoader.getSystemClassLoader().getResource(relPath);
        if (resource == null) {
            throw new RuntimeException("No resource for " + relPath);
        }
        fullPath = resource.getFile();

        try {
            directory = new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(packageName + " (" + resource + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
        } catch (IllegalArgumentException e) {
            directory = null;
        }
        if (directory != null && directory.exists()) {
            // Get the list of the files contained in the package
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                // we are only interested in .class files
                if(files[i].isDirectory()){
                    scanPackage(packageName + "." + files[i].getName(),classes);
                }else  if (files[i].getName().endsWith(".class")) {
                    // removes the .class extension
                    String className = packageName + '.' + files[i].getName().substring(0, files[i].getName().length() - 6);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("ClassNotFoundException loading " + className);
                    }
                }
            }
        } else {
            try {
                String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
                JarFile jarFile = new JarFile(jarPath);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {
                        String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("ClassNotFoundException loading " + className);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(packageName + " (" + directory + ") does not appear to be a valid package", e);
            }
        }
        return classes;
    }

}
