package cc.vant.vsfallbacklocationcleaner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作为主要的运行类
 * @author Vant
 * @since 2019/4/19 上午 9:38
 */
public class Runner {
    public static void main(String[] args) {
        File propsFile = new File("init.props");
        PropertyLoader propertyLoader = new PropertyLoader(propsFile);

        String fallBackLocation = propertyLoader.getProp("FallBackLocation");

        File loc = new File(fallBackLocation);
        if (!loc.exists()) {
            System.out.println("FallBackLocation 配置位置不存在");
        }

        File[] dirs = loc.listFiles((dir, name) -> dir.isDirectory());
        Map<String, List<String>> maps = new HashMap<>();

        getObjMap(dirs, maps);

        for (Map.Entry<String, List<String>> obj : maps.entrySet()) {
            List<String> value = obj.getValue();

        }
    }

    private static void getObjMap(File[] dirs, Map<String, List<String>> maps) {
        for (File dir : dirs) {
            ArrayList<String> namesList = new ArrayList<>();
            maps.put(dir.getName(), namesList);

            Path path = Paths.get(dir.getAbsolutePath(), "Browse.VC.db");

            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
                Statement statement = connection.createStatement();
                ResultSet names = statement.executeQuery("select name from projects");
                while (names.next()) {
                    String name = names.getString("name");
                    namesList.add(name);
                }

            } catch (SQLException e) {
                System.err.println("打开SQLite数据库失败  " + path.toAbsolutePath());
                e.printStackTrace();
            }
        }
    }
}
