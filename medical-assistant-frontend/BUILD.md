# 构建说明

## 开发环境设置

1. 确保已安装 Node.js (版本 >= 14.0.0)
2. 克隆或下载项目代码
3. 进入项目目录并安装依赖：

```bash
npm install
```

## 启动开发服务器

```bash
npm run dev
```

应用将在 http://localhost:3000 (或自动分配的可用端口如 http://localhost:3001) 上启动。

## 构建生产版本

```bash
npm run build
```

构建后的文件将位于 `dist/` 目录中，可以直接部署到任何静态文件服务器。

## 项目结构说明

```
medical-assistant-frontend/
├── public/                 # 静态资源
├── src/                    # 源代码
│   ├── assets/             # 图片、样式等静态资源
│   ├── components/         # 可复用的UI组件
│   ├── views/              # 页面组件
│   ├── router/             # 路由配置
│   ├── utils/              # 工具函数和服务
│   ├── App.vue             # 根组件
│   └── main.js             # 应用入口
├── package.json            # 项目配置和依赖
├── vite.config.js          # Vite 构建配置
└── README.md               # 项目说明
```

## API 配置

开发环境中，所有 `/api` 请求将被代理到 `http://localhost:8080`。如需修改，请编辑 `vite.config.js` 文件中的 proxy 设置。

## 部署说明

1. 构建生产版本：`npm run build`
2. 将 `dist/` 目录中的文件部署到静态文件服务器
3. 确保服务器配置正确处理 SPA 路由（将所有非静态资源请求重定向到 index.html）

## 环境变量

当前项目不使用环境变量，如需添加，请在项目根目录创建 `.env` 文件。

## 故障排除

- 如果遇到端口占用问题，Vite 会自动尝试其他端口
- 如果构建失败，请检查依赖是否完整：`npm install`
- 确保后端服务在指定地址运行以支持API请求