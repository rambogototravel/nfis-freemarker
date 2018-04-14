package org.rambo.nfis.util;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.*;

/**
 * map表存储类
 * @author Rambo Yang
 */
public class MapCache {
    private static final Logger logger = LoggerFactory.getLogger(MapCache.class);

    /** MapCache实例（单例） */
    public static MapCache instance = null;

    /** 存储并操控map表 */
    public static JSONObject map = null;

    /**
     * 初始化，加载map表
     * @param servletContext
     */
    public void init(ServletContext servletContext) {
        if (map == null) {
            reloadMap();
        }
    }

    /**
     * 获取MapCache实例（同步）
     * @return
     */
    public static synchronized MapCache getInstance() {
        if (instance == null) {
            instance = new MapCache();
        }

        return instance;
    }

    /**
     * 设置MapCache实例（同步）
     * @param inst
     */
    public static synchronized void setInstance(MapCache inst) {
        if (instance != null) {
            System.err.println("MapCache has been created, so ignore setInstance.");
        }
        else {
            instance = inst;
            System.out.println("MapCache setInstance: " + inst.getClass().getName());
        }
    }

    /**
     * 加载Map表
     */
    public void reloadMap() {
        String mapDir = Settings.getMapDir();

        if (map != null) {
            logger.info("Reload all map files in " + mapDir + "[" + map.hashCode() + "]");
        }

        try {
            map = loadAllMap(mapDir);

            logger.info("Reload finished all maps [" + map.hashCode() + "]");
        } catch (Exception e) {
            logger.error("Failed to reload all maps: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载map表，合并“res”、“pkg”键的数据
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public JSONObject loadAllMap(String filePath) throws Exception {
        JSONObject resMap = new JSONObject();
        JSONObject pkgMap = new JSONObject();

        JSONObject newMap = new JSONObject();
        newMap.put("res", resMap);
        newMap.put("pkg", pkgMap);

        logger.info("Load map files in : " + filePath);

        File root = new File(filePath);
        if (!root.exists() || !root.isDirectory()) {
            logger.warn("Map dir is not exists or is not an directory. `" + filePath + "`");
            return newMap;
        }

        /*
         * 遍历指定目录下的所有“.json”后缀的文件内容，合并“res”、“pkg”键的数据
         */
        File[] files = root.listFiles();
        for (File file : files) {
            if (!file.isDirectory() && file.canRead()) {
                String fileName = file.getName();
                if (fileName.matches(".*\\.json")) {
                    JSONObject fileContent = loadFileContent(file);

                    if (fileContent != null) {
                        logger.info("Load map file : " + fileName);

                        resMap = mergeJSONObjects(resMap, fileContent.getJSONObject("res"));
                        pkgMap = mergeJSONObjects(pkgMap, fileContent.getJSONObject("pkg"));
                    }
                }
            }
        }

        return newMap;
    }

    /**
     * 加载文件内容为JSON数据对象
     * @param file
     * @return
     * @throws Exception
     */
    private JSONObject loadFileContent(File file) throws Exception {
        FileInputStream input = null;

        try {
            if (file.canRead()) {
                input = new FileInputStream(file);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (input == null) {
            return null;
        }

        try{
            String data = readStream(input);
            if (data != null) {
                return JSONObject.parseObject(data);
            }
        }catch(Exception e){
            String msg = "Error while parse JSON file: " + file.getName() + e.getMessage();
            logger.error(msg);
            throw new Exception(msg);
        }

        return null;
    }

    /**
     * 读取文件内容
     * @param inputStream
     * @return
     */
    private String readStream(InputStream inputStream) {
        StringBuilder data = new StringBuilder();

        try {
            String encoding = Settings.getValue("encoding", "UTF-8");
            BufferedReader in = new BufferedReader(new UnicodeReader(inputStream, encoding));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                data.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data.toString();
    }

    /**
     * 合并JSON数据对象
     * @param toJSON
     * @param fromJSON
     * @return
     */
    private JSONObject mergeJSONObjects(JSONObject toJSON, JSONObject fromJSON) {
        toJSON.putAll(fromJSON);
        return toJSON;
    }

    /**
     * 获取map表JSON数据对象
     * @return
     */
    public JSONObject getMap() {
        return map;
    }

    /**
     * 根据键值，获取map表JSON数据对象
     * @param id
     * @return
     */
    public JSONObject getMap(String id) {
        return map;
    }

    /**
     * 通过id，获取map表节点
     * @param key
     * @param type
     * @return
     */
    public JSONObject getNode(String key, String type){
        JSONObject node;
        JSONObject info;

        // 尝试读取
        try{
            node = map.getJSONObject(type);
            info = node.getJSONObject(key);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return info;
    }

    /**
     * @param key
     * @return
     */
    public JSONObject getNode(String key) {
        return getNode(key, "res");
    }
}
