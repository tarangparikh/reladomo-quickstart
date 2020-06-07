package com.apricot;
/* 
    Apricot Management Suite
    Copyright (C) 2020 Tarang Parikh
    
    Email : tp0265@gmail.com
    Project Home : https://github.com/tarangparikh/apricot
    
    Original Author : @author Tarang Parikh <tp0265@gmail.com>
    
*/

import com.apricot.reladomo.ReladomoConnectionManager;
import com.gs.fw.common.mithra.MithraManager;

import java.io.IOException;
import java.io.InputStream;

public class App {
    public static void main(String...args){
        try {
            ReladomoConnectionManager.getInstance().createTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try(InputStream inputStream = App.class.getClassLoader().getResourceAsStream("RuntimeConfig.xml")) {
            MithraManager mithraManager = MithraManager.getInstance();
            mithraManager.readConfiguration(inputStream);

            DepartmentList departments = DepartmentFinder.findMany(DepartmentFinder.all());
            System.out.println(departments);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
