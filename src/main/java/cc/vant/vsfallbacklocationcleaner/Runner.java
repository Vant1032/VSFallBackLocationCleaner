package cc.vant.vsfallbacklocationcleaner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作为主要的运行类
 * @author Vant
 * @since 2019/4/19 上午 9:38
 */
public class Runner {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        PropertyLoader propertyLoader = getProperty();

        String fallBackLocation = propertyLoader.getProp("FallBackLocation");

        File loc = new File(fallBackLocation);
        if (!loc.exists()) {
            System.out.println("FallBackLocation 配置位置不存在");

            System.exit(0);
        }

        System.out.println("正在搜索...\n");

        File[] dirs = loc.listFiles((dir, name) -> dir.isDirectory());
        Map<String, List<String>> maps = new HashMap<>();

        getObjMap(dirs, maps);

        String manualMode = propertyLoader.getProp("ManualMode");
        if (manualMode != null && "true".equals(manualMode.trim())) {
            System.out.println();
            for (Map.Entry<String, List<String>> projListEntry : maps.entrySet()) {
                System.out.println(" " + projListEntry.getKey());
                for (String s : projListEntry.getValue()) {
                    System.out.println(" - " + s);
                }
            }
            System.out.println();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(maps);
            try {
                Files.writeString(Path.of("ManualMode.txt"), json);
            } catch (IOException e) {
                System.out.println("无法写入手动模式数据");
                e.printStackTrace();
                System.exit(0);
            }

            System.exit(0);
        }

        List<String> toRemove = new ArrayList<>();

        Pattern pattern = Pattern.compile(".VCXPROJ$", Pattern.CASE_INSENSITIVE);
        for (Map.Entry<String, List<String>> proj : maps.entrySet()) {
            List<String> value = proj.getValue();
            boolean cachedProjectExist = false;
            for (String s : value) {//通过数据检查被缓存的项目是否存在，只要有一项存在，就证明被缓存的项目存在
                s = s.trim();

                //以 .VCXPROJ 结尾的判断是否存在
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    if (Files.exists(Path.of(s))) {
                        cachedProjectExist = true;
                    }
                    continue;
                }

                //如果是 \\?\:WORKSPACE 则放弃检查
                if (s.equals("\\\\?\\:WORKSPACE")) {
                    continue;
                }

                //如果包含分号，则判断分号前的字符串组成的路径是否存在。
                int t;
                if ((t = s.indexOf(';')) >= 0) {
                    if (Files.exists(Path.of(s.substring(0, t)))) {
                        cachedProjectExist = true;
                    }
                }
            }

            if (!cachedProjectExist) {
                toRemove.add(proj.getKey());
            }
        }

        System.out.println("将要删除以下目录：");
        toRemove.forEach(s -> {
            System.out.println(" " + s + " 代表： ");
            List<String> list = maps.get(s);
            for (String s1 : list) {
                System.out.println("  - " + s1);
            }
        });

        System.out.println("\n是否删除这些目录？ Y/N  （回车代表Y）");
        String s = in.nextLine().trim();
        if (s.charAt(0) == 'n' || s.charAt(0) == 'N') {
            System.exit(0);
        }

        toRemove.forEach(s1 -> {
            try {
                Files.delete(Path.of(s1));
            } catch (IOException e) {
                System.err.println("删除 " + s1 + " 失败");
                e.printStackTrace();
            }
        });
    }

    private static PropertyLoader getProperty() {
        File propsFile = new File("init.props");
        if (!propsFile.exists()) {
            try {
                propsFile.createNewFile();
                Files.writeString(propsFile.toPath(), "FallBackLocation=C:\\VSTemp\\\nManualMode=false\n");
            } catch (IOException e) {
                System.err.println("无法创建init.props，可以尝试手动创建");
                e.printStackTrace();

                System.exit(0);
            }
        }
        try {
            return new PropertyLoader(propsFile);
        } catch (FileNotFoundException e) {
            System.err.println("无法读取配置文件");
            e.printStackTrace();

            System.exit(0);
            return null;
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
