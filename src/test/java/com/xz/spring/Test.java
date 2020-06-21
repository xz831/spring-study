package com.xz.spring;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @Package: com.xz.spring
 * @ClassName: Test
 * @Author: xz
 * @Date: 2020/6/21 14:08
 * @Version: 1.0
 */
public class Test {

    @org.junit.Test
    public void test1() throws IOException {
        //创建一个工厂
        Map<String,Object> map = new HashMap<>();
        Package aPackage = ScanService.class.getPackage();
        String replace = aPackage.getName().replace(".", "/");
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(replace);
        while (resources.hasMoreElements()){
            URL url = resources.nextElement();
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                findClassInPackageByFile(aPackage.getName(), filePath, true, map);
            }
        }
        map.forEach((k,v)->{
            Field[] declaredFields = v.getClass().getDeclaredFields();
            Stream.of(declaredFields).forEach(item->{
                Autowired annotation = item.getAnnotation(Autowired.class);
                if(annotation!=null){
                    item.setAccessible(true);
                    try {
                        item.set(v,map.get(item.getType().getName()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        System.out.println(map);
    }

    /**
     * 在package对应的路径下找到所有的class
     */
    private void findClassInPackageByFile(String packageName, String filePath, final boolean recursive,
                                                Map<String,Object> clazzs) {
        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 过滤文件，获取文件集合
        File[] dirFiles = dir.listFiles(file -> {
            boolean acceptDir = recursive && file.isDirectory();
            boolean acceptClass = file.getName().endsWith("class");
            return acceptDir || acceptClass;
        });
        if(dirFiles!=null){
            for (File file : dirFiles) {
                if (file.isDirectory()) {
                    findClassInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, clazzs);
                } else {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
                        Component annotation = aClass.getAnnotation(Component.class);
                        if(annotation!= null){
                            clazzs.put(aClass.getName(),aClass.newInstance());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
