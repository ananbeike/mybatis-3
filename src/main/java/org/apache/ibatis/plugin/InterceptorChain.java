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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拦截器 调用链
 *
 * 内部对executor执行了多次plugin,
 * 第一次plugin后通过Plugin.wrap方法生成了第一个代理类，姑且就叫executorProxy1，这个代理类的target属性是该executor对象。
 * 第二次plugin后通过Plugin.wrap方法生成了第二个代理类，姑且叫executorProxy2，这个代理类的target属性是executorProxy1...
 * 这样通过每个代理类的target属性就构成了一个代理链（从最后一个executorProxyN往前查找，通过target属性可以找到最原始的executor类）。
 *
 *
 * 代理链生成后，对原始目标的方法调用都转移到代理者的invoke方法上来了{@link Plugin#invoke(Object,Method,Object[])}
 *
 * @author Clinton Begin
 */
public class InterceptorChain{

    private final List<Interceptor> interceptors = new ArrayList<>();

    /**
     * 每一个拦截器对目标类都做一次代理
     *
     * 内部对executor执行了多次plugin,
     * 第一次plugin后通过Plugin.wrap方法生成了第一个代理类，姑且就叫executorProxy1，这个代理类的target属性是该executor对象。
     * 第二次plugin后通过Plugin.wrap方法生成了第二个代理类，姑且叫executorProxy2，这个代理类的target属性是executorProxy1...
     * 这样通过每个代理类的target属性就构成了一个代理链（从最后一个executorProxyN往前查找，通过target属性可以找到最原始的executor类）。
     *
     *
     * 代理链生成后，对原始目标的方法调用都转移到代理者的invoke方法上来了{@link Plugin#invoke(Object,Method,Object[])}
     *
     * @param target
     * @return 代理后的对象
     */
    public Object pluginAll(Object target){
        for (Interceptor interceptor : interceptors){
            target = interceptor.plugin(target);
        }
        return target;
    }

    public void addInterceptor(Interceptor interceptor){
        interceptors.add(interceptor);
    }

    public List<Interceptor> getInterceptors(){
        return Collections.unmodifiableList(interceptors);
    }

}
