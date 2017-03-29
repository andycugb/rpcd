package com.cugb.andy.rpc.server;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import com.cugb.andy.annotation.RpcService;
import com.cugb.andy.pojo.RpcRequest;
import com.cugb.andy.pojo.RpcResponse;
import com.cugb.andy.rpc.registry.ServiceRegistry;
import com.cugb.andy.serialize.RpcDecoder;
import com.cugb.andy.serialize.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by jbcheng on 3/3/17.
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String address;
    private ServiceRegistry serviceRegistry;
    private Map<String, Object> handlerMap = new HashMap<String, Object>();

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.address = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        Map<String, Object> serviceBeanMap =
                applicationContext.getBeansWithAnnotation(RpcService.class);
        if (!CollectionUtils.isEmpty(serviceBeanMap)) {
            for (Object bean : serviceBeanMap.values()) {
                String interfaceName =
                        bean.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName, bean);
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new RpcDecoder(RpcRequest.class))
                                    .addLast(new RpcEncoder(RpcResponse.class))
                                    .addLast(new RpcHandler(handlerMap));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] array = address.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.info("server start on port:" + port);

            if (serviceRegistry != null) { // 注册服务
                serviceRegistry.register(address);
            }
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
