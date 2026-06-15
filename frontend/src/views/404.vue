<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="$router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.not-found-content {
  text-align: center;
  color: #fff;
}

.not-found-content h1 {
  font-size: 120px;
  margin: 0;
  font-weight: 700;
  text-shadow: 2px 2px 10px rgba(0, 0, 0, 0.2);
}

.not-found-content h2 {
  font-size: 32px;
  margin: 20px 0;
}

.not-found-content p {
  font-size: 16px;
  opacity: 0.8;
  margin-bottom: 30px;
}
</style>
