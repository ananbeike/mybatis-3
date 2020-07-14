/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invocation类保存了代理对象的目标类，执行的目标类方法以及传递给它的参数
 * Invocation 类就是被代理对象的封装，也就是要拦截的真正对象
 * @author Clinton Begin
 */
public class Invocation{

    /**
     * 目标类
     */
    private final Object target;

    /**
     * 执行的目标类方法
     */
    private final Method method;

    /**
     * 传递给它的参数
     */
    private final Object[] args;

    public Invocation(Object target, Method method, Object[] args){
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object getTarget(){
        return target;
    }

    public Method getMethod(){
        return method;
    }

    public Object[] getArgs(){
        return args;
    }

    public Object proceed() throws InvocationTargetException,IllegalAccessException{
        return method.invoke(target, args);
    }

}
