package com.apricot.reladomo;
/* 
    Apricot Management Suite
    Copyright (C) 2020 Tarang Parikh
    
    Email : tp0265@gmail.com
    Project Home : https://github.com/tarangparikh/apricot
    
    Original Author : @author Tarang Parikh <tp0265@gmail.com>
    
*/

import com.gs.fw.common.mithra.bulkloader.BulkLoader;
import com.gs.fw.common.mithra.bulkloader.BulkLoaderException;
import com.gs.fw.common.mithra.connectionmanager.SourcelessConnectionManager;
import com.gs.fw.common.mithra.connectionmanager.XAConnectionManager;
import com.gs.fw.common.mithra.databasetype.DatabaseType;
import com.gs.fw.common.mithra.databasetype.H2DatabaseType;
import org.apache.commons.io.FilenameUtils;
import org.h2.tools.RunScript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.stream.Stream;

public class ReladomoConnectionManager implements SourcelessConnectionManager {

    private static ReladomoConnectionManager instance;

    private XAConnectionManager xaConnectionManager;

    private final String databaseName = "myDb";

    public static synchronized ReladomoConnectionManager getInstance() {
        if (instance == null) {
            instance = new ReladomoConnectionManager();
        }
        return instance;
    }

    private ReladomoConnectionManager() {
        this.createConnectionManager();
    }

    private XAConnectionManager createConnectionManager() {
        xaConnectionManager = new XAConnectionManager();
        xaConnectionManager.setDriverClassName("org.h2.Driver");
        xaConnectionManager.setJdbcConnectionString("jdbc:h2:mem:" + databaseName);
        xaConnectionManager.setJdbcUser("sa");
        xaConnectionManager.setJdbcPassword("");
        xaConnectionManager.setPoolName("My Connection Pool");
        xaConnectionManager.setInitialSize(1);
        xaConnectionManager.setPoolSize(10);
        xaConnectionManager.initialisePool();
        return xaConnectionManager;
    }

    @Override
    public BulkLoader createBulkLoader() throws BulkLoaderException {
        return null;
    }

    @Override
    public Connection getConnection() {
        return xaConnectionManager.getConnection();
    }

    @Override
    public DatabaseType getDatabaseType() {
        return H2DatabaseType.getInstance();
    }

    @Override
    public TimeZone getDatabaseTimeZone() {
        return TimeZone.getDefault();
    }

    @Override
    public String getDatabaseIdentifier() {
        return databaseName;
    }

    public void createTables() throws Exception{
        Path ddlPath = Paths.get(this.getClass().getClassLoader().getResource("sql").toURI());
        ArrayList<String> executionOrder = new ArrayList<>(Arrays.asList("ddl","idx","fk"));
        executionOrder.forEach(extension -> {
            try {
                execute(extension,ddlPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void execute(String extension, Path ddlPath) throws Exception{
        try(Connection connection = xaConnectionManager.getConnection()){
            Files.list(ddlPath)
                    .filter(path -> FilenameUtils.getExtension(path.toString()).equals(extension))
                    .forEach(path -> {
                        try {
                            RunScript.execute(connection, Files.newBufferedReader(path));
                        } catch (SQLException | IOException exc) {
                            exc.printStackTrace();
                        }
                    });
        }
    }

}