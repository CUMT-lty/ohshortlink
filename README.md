# ohshortlink 短链接平台

## ohshortlink 有哪些功能？

ohshortlink 是一个功能强大的短链接平台，提供全面的链接管理服务。用户可以通过后台或开放接口轻松管理短链接。我们的特色功能包括：
- 链接缩短：将完整长网址转换为短链接，一键复制链接，方便分享；
- 点击统计：提供链接的点击次数、设备信息、IP信息等相关分析数据，并提供可视化展示，让你轻松掌握链接访问数据；
- 历史记录：记录每条短链接的访问详情，为你提供全面的历史数据；
- 批量创建：支持批量创建短链接，提升效率，并为你准备好 excel 表格；
- 期限设置：灵活设置短链接的有效期； 
- 灵活删除：提供回收站功能，轻松恢复误删链接；
- API 支持：提供开放接口，方便开发者集成。

![image-20240818011337856](https://typora-img-1304045815.cos.ap-shanghai.myqcloud.com/202408180113981.png)

## 如何快速部署？
### 如何部署后端服务？
```shell
# 0. 准备工作：项目打包，停掉 8000、8001、8002 端口上的进程，创建合适的日志目录。
# （如果使用的是配置不高的服务器，一定要等 cpu 监控稳定！）

# 1. mysql
$ docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_HOST='%' -e MYSQL_ROOT_PASSWORD=88888888 -d mysql:5.7.36
# 建库建表，DDL 在 sql 目录下～

# 3. redis
$ docker run -p 6379:6379 --name redis  -d redis redis-server # --requirepass "123456"

# 4. nacos
$ docker run -d -p 8848:8848 -p 9848:9848 --name nacos2 -e MODE=standalone -e TIME_ZONE='Asia/Shanghai' nacos/nacos-server:v2.1.2

#（接入的是第三方 rocketmq 服务，所以不用部署～）

# 5. 运行 project
$ nohup java -Xms2048m -Xmx2048m -Dshort-link.domain.default=[your-ip]:8001 -jar project.jar > logs/project.log 2>&1 &

# 6. 运行 admin
$ nohup java -Xms2048m -Xmx2048m -jar admin.jar > logs/admin.log 2>&1 &

# 7. 运行 gateway
$ nohup java -Xms2048m -Xmx2048m -jar gateway.jar > logs/gateway.log 2>&1 &

# 8. 查看服务运行情况：通过端口占用，确保服务都启动完成
$ netstat -tunlp | grep 8000 # 换成要排查的端口

# 9. 查看日志，没有报异常
$ vim ./logs/project.log # 换成要查看的日志
```
### 如何部署前端服务？
前端模块为 console-vue，打包一下，然后配置 nginx:
```text
http {
    server {
        listen       80;
        server_name  localhost;
   
         location / {
            root   /home/shortlink/dist;
            index  index.html index.htm;
            try_files $uri $uri/ /index.html;
         }
    
        location /api {
            proxy_read_timeout 10s;
            proxy_pass http://127.0.0.1:8000/api;
        }
    }
}
```
```shell
$ sudo systemctl reload nginx
```
然后就可以使用了！！！

## 涵盖哪些技术栈？
- web 框架：spring boot
- orm 框架：mybatis-plus
- 微服务组件：spring cloud、nacos
- 数据存储：mysql、redis
- 消息队列中间件：rocketmq
- 服务治理组件：Sentinel
- 前端框架：vue

