package com.cugb.andy.rpc.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cugb.andy.pojo.RpcRequest;
import com.cugb.andy.pojo.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * Created by jbcheng on 3/3/17.
 * 请求实际处理逻辑类
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);
    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
        LOGGER.info("handlerMap {}",handlerMap);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t);
        }
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    // bean map中取对应实例,利用cglib反射方式invoke实际方法调用
    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        LOGGER.info("request {},className {}",request,className);
        Object bean = handlerMap.get(className);

        Class<?> clazz = bean.getClass();
        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();
        Class<?>[] parameterTypes = request.getParameterTypes();

        FastClass fastClass = FastClass.create(clazz);
        FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);
        return fastMethod.invoke(bean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("server caught exeption:", cause);
        ctx.close();
    }
}
