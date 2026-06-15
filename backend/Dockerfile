# 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 拷贝加速配置
COPY settings.xml /usr/share/maven/ref/settings.xml
COPY pom.xml .

# 预下载依赖
RUN mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline -B

# 拷贝源码
COPY src ./src

# 打包
RUN mvn -s /usr/share/maven/ref/settings.xml package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 拷贝jar包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8000

# 启动命令 (通过JVM参数设置时区)
ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]
