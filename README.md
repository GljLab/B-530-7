# 智慧酒店管理平台

## 项目简介

基于 **Spring Boot + MyBatis-Flex + Vue + JWT + MySQL** 的智慧酒店管理平台，在权限管理系统基础上扩展实现酒店基础架构管理，包括酒店信息、楼栋楼层、房型分类、客房管理等核心功能模块。

> [!NOTE]
> **技术说明**: 原始需求中的 "mybatis-flux" 为笔误，实际采用的是 **MyBatis-Flex** 技术栈。MyBatis-Flex 是一个优雅的 MyBatis 增强框架，提供了更简洁的 API 和更好的性能。

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.x
- **ORM**: MyBatis-Flex 1.7.x
- **数据库**: MySQL 8.0
- **安全**: Spring Security + JWT
- **连接池**: HikariCP

### 前端
- **框架**: Vue 3
- **构建工具**: Vite
- **UI 组件**: Element Plus
- **路由**: Vue Router
- **状态管理**: Pinia

## 快速开始

> [!TIP]
> 使用 Docker Compose 一键启动，无需本地安装 Java、Node.js 等环境

### 环境变量配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `JWT_SECRET` | JWT 签名密钥（**必须设置**，至少32字符） | 无（占位值，启动时校验） |
| `CORS_ALLOWED_ORIGINS` | 允许的跨域来源（逗号分隔） | `http://localhost:3000` |

> [!IMPORTANT]
> 生产部署前 **必须** 通过环境变量设置 `JWT_SECRET`，否则后端将拒绝启动。
> 可在项目根目录创建 `.env` 文件供 Docker Compose 读取：
> ```env
> JWT_SECRET=YourSuperSecretKeyAtLeast32Characters!!
> CORS_ALLOWED_ORIGINS=https://your-domain.com
> ```

### Docker 部署（推荐）

```bash
# 一键启动
docker compose up -d --build

# 查看日志
docker compose logs -f
```

### 服务访问

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:3000 | Vue 3 + Nginx |
| 后端 | http://localhost:8000 | Spring Boot API |

### 测试账号

| 用户名 | 密码 | 角色 | 权限说明 |
|--------|------|------|----------|
| admin | 123456 | 超级管理员 | 系统所有权限，包括系统管理和酒店管理 |
| hotel_admin | 123456 | 酒店管理员 | 酒店管理完全权限：增删改查酒店信息、楼栋、楼层、房型、房间 |
| frontdesk_manager | 123456 | 前厅部经理 | 查看所有信息，可修改房间属性和状态，不能修改价格和删除数据 |
| housekeeping_manager | 123456 | 客房部经理 | 查看所有信息，可修改房间状态/备注/房型设施，不能修改价格和删除数据 |
| receptionist | 123456 | 普通前台 | 只能查看空闲/已预订房间基本信息，看不到成本价和内部备注 |
| finance_staff | 123456 | 财务人员 | 可查看价格/成本信息并导出，不能修改物理属性和房间状态 |
| test | 123456 | 普通用户 | 基础查看权限 |
| maintenance_manager | 123456 | 维修部主管 | 查看所有维护单、分配维护单、查看维修人员工作量统计，不能验收 |
| maintenance_staff_a | 123456 | 维修人员 | 只能查看分配给自己的维护单，可接单、更新进度、提交完成 |
| maintenance_staff_b | 123456 | 维修人员 | 只能查看分配给自己的维护单，可接单、更新进度、提交完成 |

## 功能模块

### 系统管理模块
- ✅ **用户管理** - 用户增删改查、状态切换、密码重置
- ✅ **角色管理** - 角色配置、权限分配
- ✅ **菜单权限** - 菜单树维护、权限标识配置
- ✅ **数据权限** - 基于角色的数据访问控制

### 酒店管理模块
- ✅ **酒店信息配置** - 酒店名称、品牌、星级、地址、联系方式、营业执照、开业时间、酒店简介
- ✅ **服务设施管理** - 停车场、游泳池、健身房等设施管理，记录开放时间
- ✅ **酒店图片管理** - 多图上传、设置主图、排序、删除
- ✅ **楼栋管理** - 楼栋增删改查、编号、总楼层数、启用/停用状态
- ✅ **楼层管理** - 楼层号/名称、楼层特点（无烟/安静等）、房间数量自动统计、楼层状态
- ✅ **房型管理** - 房型编码、面积、床型、入住人数、设施清单、图片、平日/周末/成本价格
- ✅ **房间管理** - 房间号唯一、楼栋/楼层/房型联动、朝向、景观、位置特点、特殊标识
- ✅ **房间状态管理** - 状态流转（空闲→已预订→已入住→待清洁→清洁中→空闲），变更记录
- ✅ **批量创建房间** - 选择楼栋/楼层/房型，输入房号前缀和起止号，预览确认创建
- ✅ **房间搜索筛选** - 房号搜索、楼栋/楼层联动筛选、房型/状态/朝向/景观多条件筛选
- ✅ **统计看板** - 总体数据、房型分布、房间状态统计、楼层分布图表

## 使用指南

### 登录测试

1. 访问 http://localhost:3000，进入登录页面
2. 使用上方测试账号表格中的用户名和密码登录
3. 不同角色登录后，侧边栏菜单和操作按钮会有所不同

### 酒店管理操作流程

1. **配置酒店信息**：使用 hotel_admin 登录 → 酒店管理 → 酒店概览 → 填写酒店基本信息
2. **创建楼栋楼层**：酒店管理 → 楼栋楼层 → 新增楼栋 → 在楼栋下新增楼层
3. **创建房型**：酒店管理 → 房型管理 → 新增房型，填写面积、床型、设施、价格等
4. **创建房间**：酒店管理 → 房间管理 → 新增房间（单个）或批量创建
5. **管理房间状态**：房间管理 → 修改状态，或进入房间详情页操作
6. **查看统计**：酒店管理 → 统计看板

### 权限验证示例

- 使用 **receptionist** 登录：只能看到房间管理菜单，房间列表只显示空闲和已预订的房间，看不到成本价
- 使用 **finance_staff** 登录：可以看到酒店概览、房型管理、房间管理、统计看板，能看到成本价和导出按钮，但不能修改任何数据
- 使用 **frontdesk_manager** 登录：可以查看所有信息，修改房间属性和状态，但看不到编辑价格和删除按钮

## 项目结构

```
├── backend/          # 后端 Spring Boot 项目
│   ├── src/main/java/com/example/permission/
│   │   ├── config/      # 配置类
│   │   ├── controller/  # 控制器（系统+酒店）
│   │   ├── entity/      # 实体类
│   │   ├── mapper/      # MyBatis-Flex Mapper
│   │   ├── service/     # 服务层
│   │   ├── security/    # JWT 安全配置
│   │   └── common/      # 公共类
│   └── pom.xml
├── frontend/         # 前端 Vue 3 项目
│   ├── src/
│   │   ├── views/       # 页面组件
│   │   │   ├── system/      # 系统管理页面
│   │   │   ├── hotel/       # 酒店管理页面
│   │   │   └── maintenance/ # 维护管理页面
│   │   ├── router/      # 路由配置
│   │   ├── stores/      # 状态管理
│   │   └── api/         # API 接口
│   └── package.json
├── db/               # 数据库脚本
│   └── init.sql      # 初始化脚本（含酒店表和权限数据）
└── docker-compose.yml
```

## Docker 配置

| 容器名 | 服务 |
|--------|------|
| label-530-db | MySQL 数据库 |
| label-530-backend | Spring Boot 后端 |
| label-530-frontend | Vue 3 前端 |

## 常用命令

```bash
# 启动所有服务
docker compose up -d --build

# 查看后端日志
docker compose logs -f backend

# 进入数据库
docker exec -it label-530-db mysql -uroot -proot permission_system

# 停止所有服务
docker compose down
```

## 本地开发

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 后端启动
```bash
cd backend
mvn spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

## 许可证

MIT License
