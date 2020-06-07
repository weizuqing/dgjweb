package com.wordpress.utils;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;


public class JDBCTools {

    public static void release(ResultSet rs, Statement statement, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //关闭Statement和Connection
    public static void releaseSource(Statement statement, Connection conn) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * 1.操作JDBC的工具类 通过配置文件从数据库服务器获取一个连接
     */
    public static Connection getConnection() throws Exception {
        // 1.准备链接数据库的4个字符串
        // 1)创建Properties对象
        Properties properties = new Properties();
        // 2)获取jdbc.properties对应的输入流
        InputStream in = JDBCTools.class.getClassLoader().getResourceAsStream(
                "jdbc.properties");
        // 3)加载2)对应的输入流
        properties.load(in);
        // 4)具体决定是那个user、password等4个字符串
        String user = properties.getProperty("username");
        String password = properties.getProperty("password");
        String url = properties.getProperty("jdbcUrl");
        String dirver = properties.getProperty("driver");

        // 2.加载数据库驱动程序(注册驱动)
        Class.forName(dirver);
        // 3.通过DriverManager的getConnection的方法获取数据库连接
        return DriverManager.getConnection(url, user, password);
    }


}