package com.infinite.downloaderapp.chain;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2020/4/13 - 16:38
 * Description: Class description
 */
public class Request {
    private String name;

    public Request(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void process(String info) {
        name = name + " process " + info + ";\n";
    }

}
