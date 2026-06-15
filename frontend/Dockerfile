# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app

# 配置npm淘宝镜像源加速
RUN npm config set registry https://registry.npmmirror.com

# 拷贝依赖文件
COPY package*.json ./

# 使用npm ci加速安装（需要package-lock.json）
RUN npm ci

# 拷贝源码
COPY . .

# 构建生产版本
RUN npm run build

# 生产阶段
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 删除默认配置
RUN rm -rf ./*

# 拷贝构建产物
COPY --from=builder /app/dist .

# 拷贝nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
