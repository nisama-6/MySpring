package com.myspring.demo.servlet;

import com.alibaba.fastjson.JSONObject;
import com.myspring.demo.annotaiton.MyAutoWrited;
import com.myspring.demo.annotaiton.MyController;
import com.myspring.demo.annotaiton.MyRequestMapping;
import com.myspring.demo.annotaiton.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MyDispatcherServlet extends HttpServlet {
    private Properties properties = new Properties();
    private List<String> beanNames = new ArrayList<String>();
    private Map<String, Object> ioc = new HashMap<String, Object>();
    private Map<String, Method> urlMapping = new HashMap<String, Method>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatcherServlet(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatcherServlet(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        String url = req.getRequestURI();//获取请求的url
        url = url.replace(req.getContextPath(), "").replaceAll("/+", "/");
        if (!urlMapping.containsKey(url)) {//判断urlMapping中是否有对应的url
            resp.getWriter().write("404! url is not found!");
            return;
        }

        Method method = urlMapping.get(url);//获取对应的处理方法
        String className = method.getDeclaringClass().getSimpleName();
        className = firstLowerCase(className);
        if (!ioc.containsKey(className)) {
            resp.getWriter().write("500! claas not defind !");
            return;
        }
        Object[] args ;//参数列表
        if ("POST".equals(req.getMethod()) && req.getContentType().contains("json")) {//判断请求的参数格式
            String str = getJson(req);
            args = getRequestParam(str, method);//处理json格式的参数
        } else {
            args = getRequestParam(req.getParameterMap(), method);//处理form格式参数
        }
        //调用目标方法
        Object res = method.invoke(ioc.get(className), args);

        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().write(res.toString());
    }

    /**
     * 处理json参数
     * @param json 参数
     * @param method 方法
     * @return
     */
    private Object[] getRequestParam(String json, Method method) {
        if (null == json || json.isEmpty()) {
            return null;
        }
        Parameter[] parameters = method.getParameters();
        Object[] requestParam = new Object[parameters.length];
        JSONObject jsonObject = JSONObject.parseObject(json);
        int i = 0;
        for (Parameter p : parameters) {
            Object val = jsonObject.getObject(p.getName(), p.getType());
            requestParam[i] = val;
            i++;
        }
        return requestParam;
    }

    /**
     * 处理form参数
     * @param map
     * @param method
     * @return
     */
    private Object[] getRequestParam(Map<String, String[]> map, Method method) {
        if (null == map || map.size() == 0) {
            return null;
        }
        Parameter[] parameters = method.getParameters();
        int i = 0;
        Object[] requestParam = new Object[parameters.length];
        for (Parameter p : parameters) {
            if (!map.containsKey(p.getName())) {
                requestParam[i] = null;
                i++;
                continue;
            }
            try {
                Class typeClass = p.getType();
                String[] val = map.get(p.getName());
                if (null == val) {
                    requestParam[i] = null;
                    i++;
                    continue;
                }
                Constructor con = null;
                try {
                    con = typeClass.getConstructor(val[0].getClass());
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                Object obj = null;
                try {
                    assert con != null;
                    obj = con.newInstance(val[0]);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                requestParam[i] = obj;
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }


            i++;
        }
        return requestParam;
    }

    private String getJson(HttpServletRequest req) {
        String param = null;
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            param = responseStrBuilder.toString();
            System.out.println("request param="+param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return param;
    }

    @Override
    public void init(ServletConfig config) {
        //加载配置
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");

        loadConfig(contextConfigLocation);
        //获取要扫描的包地址
        String dirpath = properties.getProperty("scanner.package");

        //扫描要加载的类
        doScanner(dirpath);

        //实例化要加载的类
        doInstance();

        //加载依赖注入，给属性赋值
        doAutoWrited();

        //加载映射地址
        doRequestMapping();
    }

    /**
     * 实例化需要实例的类
     */
    private void doInstance() {
        if (beanNames.isEmpty()) {
            return;
        }
        for (String beanName : beanNames) {
            try {
                Class cls = Class.forName(beanName);
                if (cls.isAnnotationPresent(MyController.class)) { //判断类是否有MyController注解修饰
                    //使用反射实例化对象
                    Object instance = cls.newInstance();
                    //默认类名首字母小写
                    beanName = firstLowerCase(cls.getSimpleName()); //获取类的类名（不含有路径）
                    //写入ioc容器
                    ioc.put(beanName, instance);


                } else if (cls.isAnnotationPresent(MyService.class)) { //判断类是否有MyService注解修饰
                    Object instance = cls.newInstance();
                    MyService MyService = (MyService) cls.getAnnotation(MyService.class);

                    String alisName = MyService.value();
                    if (alisName==null || alisName.trim().length() == 0) { //判断是否有注解值
                        beanName = cls.getSimpleName(); //若没有注解值把类名作为key放在ioc容器中
                    } else {
                        beanName = alisName; //若有注解值把注解值作为key放在ioc容器中
                    }
                    beanName = firstLowerCase(beanName);
                    ioc.put(beanName, instance);
                    //如果是接口，自动注入它的实现类
                    Class<?>[] interfaces = cls.getInterfaces();
                    for (Class<?> c :
                            interfaces) {
                        ioc.put(firstLowerCase(c.getSimpleName()), instance);
                    }
                } else {
                    continue;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 自动注入
     */
    private void doAutoWrited() {

        for (Map.Entry<String, Object> obj : ioc.entrySet()) {//entrySet()方法返回map中各个键值对映射关系的集合
            try {
                for (Field field : obj.getValue().getClass().getDeclaredFields()) {//获取对象所有的属性
                    if (!field.isAnnotationPresent(MyAutoWrited.class)) {//判断属性是否有MyAutoWrited注解
                        continue;
                    }
                    MyAutoWrited autoWrited = field.getAnnotation(MyAutoWrited.class);
                    String beanName = autoWrited.value();
                    if ("".equals(beanName)) {
                        beanName = field.getType().getSimpleName();//获取类名
                    }

                    field.setAccessible(true);

                    field.set(obj.getValue(), ioc.get(firstLowerCase(beanName)));//为属性值注入实体
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 遍历ioc容器里的实体，找到含有MyController注解的对象。遍历对象中的方法，把有MyRequestMapping注解的方法放入urlMapping容器中
     */
    private void doRequestMapping() {

        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> obj : ioc.entrySet()) {//遍历ioc容器里的实体，找到含有MyController注解的对象
            if (!obj.getValue().getClass().isAnnotationPresent(MyController.class)) {
                continue;
            }
            Method[] methods = obj.getValue().getClass().getMethods();//获取对象中的所有方法
            for (Method method : methods) {//遍历对象中的方法，把有MyRequestMapping注解的方法
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                String baseUrl = "";
                if (obj.getValue().getClass().isAnnotationPresent(MyRequestMapping.class)) {
                    baseUrl = obj.getValue().getClass().getAnnotation(MyRequestMapping.class).value();
                }
                MyRequestMapping MyRequestMapping = method.getAnnotation(MyRequestMapping.class);
                if ("".equals(MyRequestMapping.value())) {
                    continue;
                }
                String url = (baseUrl + "/" + MyRequestMapping.value()).replaceAll("/+", "/");
                urlMapping.put(url, method);//把方法的url作为key，放入urlMapping容器中
                System.out.println(url);
            }
        }
    }

    /**
     * 加载application.properties文件
     * @param contextConfigLocation
     */
    private void loadConfig(String contextConfigLocation) {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            assert is != null;
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 通过递归扫描../com/myspring/demo文件下的所有文件
     * @param dirpath
     */
    private void doScanner(String dirpath) {

        URL url = this.getClass().getClassLoader().getResource("/" + dirpath.replaceAll("\\.", "/"));
        assert url != null;
        File dir = new File(url.getFile());
        File[] files = dir.listFiles();//获取文件列表
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {//判断file是否是文件夹，若是文件夹则进行递归扫描
                doScanner(dirpath + "." + file.getName());
                continue;
            }

            //取文件名
            String beanName = dirpath + "." + file.getName().replaceAll(".class", "");
            beanNames.add(beanName); //将扫描到的文件名添加到beanNames列表中
        }
    }


    /**
     * 将str首字母改为小写
     * @param str
     * @return
     */
    private String firstLowerCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
