package com.coin.neo.sdk.abi;


import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * smartcode abi function
 */
public class AbiFunction {
    public String name;
    public String returntype;
    public List<Parameter> parameters;

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
    public void setParamsValue(Object... objs) throws Exception{
        if(objs.length != parameters.size()){
            throw new Exception("setParamsValue value num error");
        }
        for (int i = 0; i < objs.length; i++) {
            parameters.get(i).setValue(objs[i]);
        }
    }
    public Parameter getParameter(String name) {
        for (Parameter e : parameters) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }
    public void clearParamsValue() {
        for (Parameter e : parameters) {
            e.setValue(null);
        }
    }
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
