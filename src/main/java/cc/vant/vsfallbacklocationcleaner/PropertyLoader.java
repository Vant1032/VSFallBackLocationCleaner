package cc.vant.vsfallbacklocationcleaner;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 解析配置文件
 * @author Vant
 * @since 2019/4/19 上午 10:03
 */
public class PropertyLoader {
    private String propPath;
    private Map<String, String> props = new HashMap<>();

    public PropertyLoader(String propPath) {
        this.propPath = propPath;
    }

    private void parse() {
        Scanner in = new Scanner(propPath);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.isEmpty() || line.isBlank()) {
                return;
            }
            String[] split = line.split("=");
            props.put(split[0], split[1]);
        }
    }

    public String getProp(String key) {
        return props.get(key);
    }
}
