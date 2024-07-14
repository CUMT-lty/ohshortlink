package com.litianyu.ohshortlink.project.test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MysqlClearAllTest {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/ohshortlink";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "88888888";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);  // 获取数据库连接
            List<String> tableNames = getTableNames(connection); // 获取所有表名
            for (String tableName : tableNames) { // 清空每个表的数据
                clearTableData(connection, tableName);
                System.out.println("Table '" + tableName + "' has been cleared.");
            }
            connection.close(); // 关闭连接
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getTableNames(Connection connection) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SHOW TABLES");
        while (resultSet.next()) {
            tableNames.add(resultSet.getString(1));
        }
        resultSet.close();
        statement.close();
        return tableNames;
    }

    private static void clearTableData(Connection connection, String tableName) throws SQLException {
        Statement statement = connection.createStatement();
        String sql = "DELETE FROM " + tableName;
        int rowsAffected = statement.executeUpdate(sql);
        System.out.println("Cleared " + rowsAffected + " rows from table '" + tableName + "'.");
        statement.close();
    }
}
