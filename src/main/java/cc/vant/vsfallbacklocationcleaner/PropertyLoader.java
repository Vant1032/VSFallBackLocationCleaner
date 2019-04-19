package cc.vant.vsfallbacklocationcleaner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 解析配置文件
 * @author Vant
 * @since 2019/4/19 上午 10:03
 */
public class PropertyLoader {
    private File propsFile;
    private Map<String, String> props = new HashMap<>();

    public PropertyLoader(File propsFile) {
        this.propsFile = propsFile;
    }

    /**
     * @throws FileNotFoundException 未找到属性文件
     */
    private void parse() throws FileNotFoundException {
        Scanner in = new Scanner(propsFile);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.isEmpty() || line.isBlank()) {
                return;
            }
            String[] split = line.split("=");
            props.put(split[0], split[1]);
        }
    }

    /**
     * 底层调用的是Map.get（）方法
     * @see java.util.Map#get(Object)
     * @param key
     * @return
     */
    public String getProp(String key) {
        return props.get(key);
    }
}
