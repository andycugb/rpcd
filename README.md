使用zk,netty实现的简单rpc服务
RPC原理:
1）服务消费方（client）调用以本地调用方式调用服务；

2）client stub接收到调用后负责将方法、参数等组装成能够进行网络传输的消息体；

3）client stub找到服务地址，并将消息发送到服务端；

4）server stub收到消息后进行解码；

5）server stub根据解码结果调用本地的服务；

6）本地服务执行并将结果返回给server stub；

7）server stub将返回结果打包成消息并发送至消费方；

8）client stub接收到消息，并进行解码；

9）服务消费方得到最终结果。

　　RPC的目标就是要2~8这些步骤都封装起来，让用户对这些细节透明。


使用步骤:
1——加载server服务,主要逻辑为保存提供服务的元数据信息(bean map),同时向zk注册server端的机器ip与port,监听server端口等待client端请求
2——client端发送请求,主要逻辑,:动态代理,代理类先向zk获取server端元数据信息,并建立tcp连接,同时发送请求(netty实现)
3——server端接收到请求(netty实现),需要从bean map中获取对应服务提供信息,使用动态代理invoke实际执行逻辑,并将处理结果返回至client