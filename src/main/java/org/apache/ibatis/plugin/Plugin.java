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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.reflection.ExceptionUtil;

/**
 * 实现了 InvocationHandler - jdk动态代理
 *
 * 两个重要方法：
 * {@link Plugin#invoke(Object,Method,Object[])} JDK动态代理 InvocationHanlder接口的方法，执行代理增强部分，mybatis的代理增强放到了{@link Interceptor}中实现。
 *
 * {@link Plugin#wrap(Object,Interceptor)} 插入代理方法，都在 {@link Interceptor#plugin(Object)} 方法中调用：Plugin.wrap(target, this);
 *
 *
 * @author Clinton Begin
 */
public class Plugin implements InvocationHandler{

    /**
     * 被代理的目标类
     */
    private final Object target;

    /**
     * 对应的拦截器
     */
    private final Interceptor interceptor;

    /**
     * 拦截器拦截的方法缓存
     */
    private final Map<Class<?>, Set<Method>> signatureMap;

    private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap){
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    /**
     * 插入目标类 ,一般封装后 ，都在 {@link Interceptor#plugin(Object)} 方法中调用：
     *
     * <pre>
     *    Plugin.wrap(target, this);
     *      -- target：代理目标对象
     *      -- this：Intercepter，业务增强逻辑
     * </pre>
     *
     *
     * @param target
     *            目标对象
     * @param interceptor
     *            目标对象要增强的业务逻辑
     * @return
     */
    public static Object wrap(Object target,Interceptor interceptor){

        //从拦截器Intercepts.class的注解中获取拦截的 类名 ，方法信息
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);

        //取得要改变行为的类(ParameterHandler|ResultSetHandler|StatementHandler|Executor)
        Class<?> type = target.getClass();

        //解析被拦截对象的所有接口 , 注意是接口
        Class<?>[] interfaces = getAllInterfaces(type, signatureMap);

        //如果判断当前接口要被代理，那么久生成代理对象
        if (interfaces.length > 0){
            //生成代理对象， Plugin对象为该代理对象的InvocationHandler
            return Proxy.newProxyInstance(type.getClassLoader(), interfaces, new Plugin(target, interceptor, signatureMap));
        }
        return target;
    }

    @Override
    public Object invoke(Object proxy,Method method,Object[] args) throws Throwable{
        try{
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            if (methods != null && methods.contains(method)){

                //调用代理类所属拦截器的intercept方法，
                return interceptor.intercept(new Invocation(target, method, args));
            }
            return method.invoke(target, args);
        }catch (Exception e){
            throw ExceptionUtil.unwrapThrowable(e);
        }
    }

    /**
     * 从拦截器的注解中获取拦截的 类名 ， 方法信息
     * 取得签名Map,就是获取Interceptor实现类上面的注解，要拦截的是那个类（Executor，ParameterHandler， ResultSetHandler，StatementHandler）的那个方法
     *
     * @param interceptor
     * @return
     */
    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor){

        Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
        // issue #251
        if (interceptsAnnotation == null){
            throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }
        //value是数组型，Signature的数组
        Signature[] sigs = interceptsAnnotation.value();

        //每个class里有多个Method需要被拦截,所以这么定义
        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        for (Signature sig : sigs){
            Set<Method> methods = signatureMap.computeIfAbsent(sig.type(), k -> new HashSet<>());
            try{
                Method method = sig.type().getMethod(sig.method(), sig.args());
                methods.add(method);
            }catch (NoSuchMethodException e){
                throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
            }
        }
        return signatureMap;
    }

    /**
     * 解析被拦截对象的所有接口 , 注意是接口
     *
     * @param type
     * @param signatureMap
     * @return
     */
    private static Class<?>[] getAllInterfaces(Class<?> type,Map<Class<?>, Set<Method>> signatureMap){
        Set<Class<?>> interfaces = new HashSet<>();
        while (type != null){
            for (Class<?> c : type.getInterfaces()){
                if (signatureMap.containsKey(c)){
                    interfaces.add(c);
                }
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }

}
