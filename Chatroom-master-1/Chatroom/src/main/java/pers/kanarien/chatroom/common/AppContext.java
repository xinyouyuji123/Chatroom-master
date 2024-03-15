package pers.kanarien.chatroom.common;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import pers.kanarien.chatroom.dao.GroupInfoDao;
import pers.kanarien.chatroom.dao.UserInfoDao;
import pers.kanarien.chatroom.web.websocket.WebSocketServer;

@Component
@Scope("singleton")
public class AppContext {

    private final Logger logger = LoggerFactory.getLogger(AppContext.class);

    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private GroupInfoDao groupDao;
    
    private Thread nettyThread;
    
    /**
     * 描述：Tomcat加载完ApplicationContext-main和netty文件后：
     *      1. 启动Netty WebSocket服务器；
     *      2. 加载用户数据；
     *      3. 加载用户交流群数据。
     */
    @PostConstruct
    public void init() {
        nettyThread = new Thread(webSocketServer);
        logger.info("Start an independent thread and start the Netty WebSocket server...");
        nettyThread.start();
        logger.info("Loading user data...");
        userInfoDao.loadUserInfo();
        logger.info("Load user communication group data...");
        groupDao.loadGroupInfo();
    }

    /**
     * 描述：Tomcat服务器关闭前需要手动关闭Netty Websocket相关资源，否则会造成内存泄漏。
     *      1. 释放Netty Websocket相关连接；
     *      2. 关闭Netty Websocket服务器线程。（强行关闭，是否有必要？）
     */
    @SuppressWarnings("deprecation")
    @PreDestroy
    public void close() {
        logger.info("Releasing Netty Websocket related connections...");
        webSocketServer.close();
        logger.info("Closing Netty Websocket server thread...");
        nettyThread.stop();
        logger.info("The system shut down successfully!");
    }
}
