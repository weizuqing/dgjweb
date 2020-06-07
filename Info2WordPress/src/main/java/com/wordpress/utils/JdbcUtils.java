package com.wordpress.utils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JdbcUtils {
    /**
     * 更新操作通用方法
     *
     * @param sql
     * @param args
     */
    public void update(String sql, Object... args) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = JDBCTools.getConnection();
            statement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTools.release(null, statement, connection);
        }
    }

    /**
     * 根据某一个字段名查询一个对象
     *
     * @param clazz
     * @param sql
     * @param args
     * @return
     */
    public <T> T get(Class<T> clazz, String sql, Object... args) {
        List<T> result = getForList(clazz, sql, args);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    /**
     * 传入 SQL 语句和 Class 对象, 返回 SQL 语句查询到的记录对应的 Class 类的对象的集合
     *
     * @param clazz: 对象的类型
     * @param sql:   SQL 语句
     * @param args:  填充 SQL 语句的占位符的可变参数.
     * @return
     */
    public <T> List<T> getForList(Class<T> clazz,
                                  String sql, Object... args) {
        List<T> list = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            //1. 得到结果集
            connection = JDBCTools.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            resultSet = preparedStatement.executeQuery();
            //2. 处理结果集, 得到 Map 的 List, 其中一个 Map 对象
            //就是一条记录. Map 的 key 为 reusltSet 中列的别名, Map 的 value
            //为列的值.
            List<Map<String, Object>> values =
                    handleResultSetToMapList(resultSet);
            //3. 把 Map 的 List 转为 clazz 对应的 List
            //其中 Map 的 key 即为 clazz 对应的对象的 propertyName,
            //而 Map 的 value 即为 clazz 对应的对象的 propertyValue
            list = transfterMapListToBeanList(clazz, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTools.release(resultSet, preparedStatement, connection);
        }
        return list;
    }

    /**
     * 得到结果集
     *
     * @param clazz
     * @param values
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public <T> List<T> transfterMapListToBeanList(Class<T> clazz,
                                                  List<Map<String, Object>> values) throws InstantiationException,
            IllegalAccessException, InvocationTargetException {
        List<T> result = new ArrayList<>();
        T bean = null;
        ConvertUtils.register(new DateConverter(null), java.util.Date.class);
        if (values.size() > 0) {
            for (Map<String, Object> m : values) {
                bean = clazz.newInstance();
                for (Map.Entry<String, Object> entry : m.entrySet()) {
                    String propertyName = entry.getKey();
                    Object value = entry.getValue();
                    BeanUtils.setProperty(bean, propertyName, value);
                }
                // 13. 把 Object 对象放入到 list 中.
                result.add(bean);
            }
        }
        return result;
    }

    /**
     * 处理结果集, 得到 Map 的一个 List, 其中一个 Map 对象对应一条记录
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    public List<Map<String, Object>> handleResultSetToMapList(
            ResultSet resultSet) throws SQLException {
        // 5. 准备一个 List<Map<String, Object>>:
        // 键: 存放列的别名, 值: 存放列的值. 其中一个 Map 对象对应着一条记录
        List<Map<String, Object>> values = new ArrayList<>();
        List<String> columnLabels = getColumnLabels(resultSet);
        Map<String, Object> map = null;
        // 7. 处理 ResultSet, 使用 while 循环
        while (resultSet.next()) {
            map = new HashMap<>();
            for (String columnLabel : columnLabels) {
                Object value = resultSet.getObject(columnLabel);
                map.put(columnLabel, value);
            }
            // 11. 把一条记录的一个 Map 对象放入 5 准备的 List 中
            values.add(map);
        }
        return values;
    }

    /**
     * 获取结果集的 ColumnLabel 对应的 List
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    private List<String> getColumnLabels(ResultSet rs) throws SQLException {
        List<String> labels = new ArrayList<>();

        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            labels.add(rsmd.getColumnLabel(i + 1));
        }
        return labels;
    }

    /**
     * 返回某条记录的某一个字段的值 或 一个统计的值(一共有多少条记录等.)
     *
     * @param sql
     * @param args
     * @return
     */
    public <E> E getForValues(String sql, Object... args) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = JDBCTools.getConnection();
            statement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return (E) resultSet.getObject(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCTools.release(resultSet, statement, connection);
        }
        return null;
    }
}