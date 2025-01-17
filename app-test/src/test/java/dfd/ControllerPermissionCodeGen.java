package dfd;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dfd.common.annotation.Authorization;
import dfd.rbac.common.model.enums.PermissionType;
import dfd.rbac.controller.RbacPermissionController;
import dfd.rbac.model.dto.RbacPermissionDTO;
import dfd.rbac.model.query.RbacPermissionQuery;
import dfd.rbac.service.RbacPermissionService;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 新增权限生成器
 * @author binchao
 */
@Slf4j
public class ControllerPermissionCodeGen extends AppBootstrapTest {

    @Resource
    private RbacPermissionService rbacPermissionService;

    @Test
    public void gen() {
        List<Class<?>> classList = getAllClassByPackageName(RbacPermissionController.class.getPackage());
        //获取所有的类
        log.info("All Class: {}", classList);
        List<String> permissionList = getAnnotationInfo(classList);
        log.info("All Permission: {}", permissionList);
        RbacPermissionQuery query = RbacPermissionQuery.builder().permissionType(PermissionType.API).build();
        List<RbacPermissionDTO> pList = rbacPermissionService.list(query);
        List<RbacPermissionDTO> expireList = new ArrayList<>();

        Map<String, Long> codeMap = new HashMap<>();

        // 找出新增的接口权限、过时的权限
        for (RbacPermissionDTO p : pList) {
            if (PermissionType.API.equals(p.getPermissionType())) {
                codeMap.put(p.getPermissionCode(), p.getId());
                if (permissionList.contains(p.getPermissionCode())) {
                    permissionList.remove(p.getPermissionCode());
                } else {
                    expireList.add(p);
                }
            }
        }
        log.info("New Permission List: {}", permissionList);
        log.info("Expire Permission List: {}", expireList.size());

    }

    public static List<String> getAnnotationInfo(List<Class<?>> clsList){
        List<String> permissionList = new ArrayList<>();
        if (clsList != null && !clsList.isEmpty()) {
            for (Class<?> cls : clsList) {
                boolean exits = cls.isAnnotationPresent(RestController.class) || cls.isAnnotationPresent(Controller.class);
                if (!exits) {
                    continue;
                }
                RequestMapping requestMappingCls = cls.getAnnotation(RequestMapping.class);
                String prefix = "";
                if (requestMappingCls != null) {
                    prefix = requestMappingCls.value()[0];
                }

                //获取类中的所有的方法
                Method[] methods = cls.getDeclaredMethods();
                if (methods != null && methods.length > 0) {
                    for (Method method : methods) {
                        boolean mExits = method.isAnnotationPresent(Authorization.class);
                        if (!mExits) {
                            continue;
                        }
                        Authorization permission = method.getAnnotation(Authorization.class);

                        if (permission != null && CharSequenceUtil.isNotEmpty(permission.value())) {
                            log.info(prefix + " - " + permission.value());
                            permissionList.add(permission.value());
                        }
                    }
                }
            }
        }
        return permissionList;
    }

    public static List<Class<?>> getAllClassByPackageName(Package pkg) {
        String packageName = pkg.getName();
        // 获取当前包下以及子包下所以的类
        return getClasses(packageName);
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param packageName
     * @return
     */
    private static List<Class<?>> getClasses(String packageName) {
        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                log.info(url.getFile());
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));

        assert dirFiles != null;
        // 循环所有文件
        for (File file : dirFiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
