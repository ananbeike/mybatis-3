/**
 *    Copyright 2009-2019 the original author or authors.
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

import java.util.Properties;

/**
 * @author Clinton Begin
 */
public interface Interceptor{

    /**
     * 直接覆盖 所拦截对象原有的方法，插件的核心方法
     *
     * 内部要通过invocation.proceed()显式地推进责任链前进，也就是调用下一个拦截器拦截目标方法。
     *
     *
     * @param invocation
     *            通过此参数可以反射调度原来对象的方法
     * @return
     * @throws Throwable
     */
    Object intercept(Invocation invocation) throws Throwable;

    /**
     *
     * 用当前这个拦截器生成对目标target的代理，实际是通过Plugin.wrap(target,this) 来完成的，把目标target和拦截器this传给了包装函数
     *
     * @param target
     *            被代理的对象，
     * @return
     */
    default Object plugin(Object target){
        return Plugin.wrap(target, this);
    }

    /**
     * 用于设置额外的参数，参数配置在拦截器的Properties节点里
     *
     * @param properties
     */
    default void setProperties(Properties properties){
        // NOP
    }

}
