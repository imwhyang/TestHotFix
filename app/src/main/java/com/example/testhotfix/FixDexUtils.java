package com.example.testhotfix;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Author:Wayne
 * Time:2018/4/24 19:07
 * Description: This is FixDexUtils
 */
class FixDexUtils {
    public static HashSet<File> loadeDex = new HashSet<File>();

    static {
        loadeDex.clear();
    }

    /**
     * 1 得到 dex
     *
     * @param context
     */
    public static void loadFixeDes(Context context) {
        if (context == null)
            return;
//        所有 修复dex
//        得到当前应用内所有dex文件
        String dexDir = "odex";
        File dir = context.getDir(dexDir, Context.MODE_PRIVATE);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().startsWith("classes") && file.getName().endsWith(".dex")) {
                loadeDex.add(file);
            }
        }

//        dex合并
        doDexInject(context, dir, loadeDex);
    }

    /**
     * @param context
     * @param dir
     * @param loadeDex
     */
    private static void doDexInject(Context context, File dir, HashSet<File> loadeDex) {
        String optimizedDirectory = dir.getAbsolutePath() + File.separator + "opt_dex";
        File fopt = new File(optimizedDirectory);
        if (!fopt.exists()) {
            fopt.mkdirs();
        }
//        1.加载应用的dex
        PathClassLoader pathLoader = (PathClassLoader) context.getClassLoader();
//        2.加载修复的dex
        for (File dex : loadeDex) {
            DexClassLoader classLoader = new DexClassLoader(
                    dex.getAbsolutePath(),//
                    fopt.getAbsolutePath(),//类加载器目录
                    null,
                    pathLoader
            );
//         3.合并
//            BaseDexClassLoader
            try {

//                得到两个 classloader里的 pathList
                Object dexObj = getPathList(classLoader);
                Object pathObj = getPathList(pathLoader);

//                 再得到pathList 中的element[]
                Object dexElements = getDexElements(dexObj);
                Object pathElements = getDexElements(pathObj);
//                合并两个 element 数组
                Object dexElement = combineArray(dexElements, pathElements);

                Object pathList = getPathList(pathLoader);
                setField(pathList, pathList.getClass(), "dexElements", dexElement);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            getField();
        }


    }

    private static void setField(Object obj, Class<?> cl, String field, Object value) throws Exception {
        Field loadField = cl.getDeclaredField(field);
        loadField.setAccessible(true);
        loadField.set(obj, value);
    }

    /**
     * 反射拿到属性值
     *
     * @param obj
     * @param cl
     * @param field
     * @return
     * @throws Exception
     */
    private static Object getField(Object obj, Class<?> cl, String field) throws Exception {
        Field loadField = cl.getDeclaredField(field);
        loadField.setAccessible(true);
        return loadField.get(obj);
    }

    /**
     * 得到 pathList
     *
     * @param baseDexClassLoader
     * @return
     * @throws Exception
     */
    private static Object getPathList(Object baseDexClassLoader) throws Exception {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 得到 pathList 中的dexElements
     *
     * @param obj
     * @return
     * @throws Exception
     */
    private static Object getDexElements(Object obj) throws Exception {
        return getField(obj, obj.getClass(), "dexElements");
    }

    /**
     * 数组合并
     *
     * @param arrayLhs DexClassLoader 中的（用于更新的）
     * @param arrayRhs PathClassLoader 中的（应用中）
     * @return
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
//        得到数组类型
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
//        得到合并后长度
        int j = i + Array.getLength(arrayRhs);
//        新建一个数据用于合并
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; k++) {
            if (k < i) {//DexClassLoader 中的放于top
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }
}
