package com.haoyanbing.java.demo.util;

import com.alibaba.excel.EasyExcel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 导出指定目录controller的接口列表
 *
 * @author haoyanbing
 * @date 2022/8/2
 */
public class ExportApiUtil {

    @AllArgsConstructor
    @lombok.Data
    static class Data {
        private String moduleName;
        private String methodDesc;
        private String apiPath;
    }

    public static void main(String[] args) {
        List<Data> list = new ArrayList<>();

        List<Class<?>> classes = new ArrayList<>();
        findAndAddClassesInPackageByFile("com.wjj.application.controller", "/Users/haoyanbing/nahefa/wjj-longhua-project/longhua/target/classes/com/wjj/application/controller",
                true, classes);
        classes.forEach(clazz -> {

            String moduleName = "";
            Api apiAnnotation = clazz.getAnnotation(Api.class);
            if (apiAnnotation != null) {
                String[] tags = apiAnnotation.tags();
                if (tags.length > 0) {
                    moduleName = tags[0];
                }
            }

            String classRequestMapping = "";
            RequestMapping requestMappingAnnotation = clazz.getAnnotation(RequestMapping.class);
            if (requestMappingAnnotation != null) {
                String[] values = requestMappingAnnotation.value();
                if (values.length > 0) {
                    classRequestMapping = values[0];
                }
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                String methodRequestMapping = "";
                String methodDesc = "";
                PostMapping postMappingAnnotation = method.getAnnotation(PostMapping.class);
                if (postMappingAnnotation != null) {
                    String[] values = postMappingAnnotation.value();
                    if (values.length > 0) {
                        methodRequestMapping = values[0];
                    }
                }

                GetMapping getMappingAnnotation = method.getAnnotation(GetMapping.class);
                if (getMappingAnnotation != null) {
                    String[] values = getMappingAnnotation.value();
                    if (values.length > 0) {
                        methodRequestMapping = values[0];
                    }
                }

                ApiOperation apiOperationAnnotation = method.getAnnotation(ApiOperation.class);
                if (apiOperationAnnotation != null) {
                    methodDesc = apiOperationAnnotation.value();
                }

                String apiPath = classRequestMapping + "/" + methodRequestMapping;
                apiPath = apiPath.replace("//", "/");

                list.add(new Data(moduleName, methodDesc, apiPath));
            }
        });

        EasyExcel.write(new File("/Users/haoyanbing/Desktop/longhua服务接口列表.xls"), Data.class).sheet("sheet1").doWrite(list);
    }


    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));

        // 循环所有文件
        assert dirFiles != null;
        for (File file : dirFiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
