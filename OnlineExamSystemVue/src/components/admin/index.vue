<!--
 * @Description: 
 * @Author: 
 * @Date: 2024-03-08 20:38:49
-->
// 展示组件页面
<template>
  <div id="index">
    <header1 class="topbar"></header1>
    <section class="container">
      <div class="left_side">
        <mainLeft></mainLeft>
      </div>
      <div class="main_wrapper">
        <navigator class="nav"></navigator>
        <div class="content-wrapper">
          <topNavigation class="top-nav" @refresh-page="refreshCurrentPage"></topNavigation>
          <div class="page-content">
            <router-view></router-view>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import header from '@/components/common/header'
import mainLeft from '@/components/common/mainLeft'
import navigator from '@/components/common/navigator'
import topNavigation from '@/components/common/topNavigation'
export default {
  components:{
    header1: header,
    mainLeft: mainLeft,
    navigator: navigator,
    topNavigation: topNavigation
  },
  data() {
    return {
      username: '许如梦'
    }
  },
  methods: {
    // 刷新当前页面
    refreshCurrentPage() {
      // 可以在这里添加刷新逻辑，比如重新加载数据
      this.$nextTick(() => {
        // 触发当前组件的刷新方法
        if (this.$refs.routerView && this.$refs.routerView.refresh) {
          this.$refs.routerView.refresh();
        } else {
          // 如果没有刷新方法，就重新路由
          const currentRoute = this.$route;
          this.$router.replace({ path: '/temp' });
          this.$nextTick(() => {
            this.$router.replace(currentRoute);
          });
        }
      });
    }
  }
}
</script>

<style lang="less" scoped>
#index {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--bg-primary);
}

#index .topbar {
  height: 80px;
  background-color: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

#index .container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

#index .left_side {
  width: auto;
  background-color: transparent;
  overflow-y: auto;
  border-right: none;
}

#index .main_wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--bg-secondary);
}

#index .nav {
  background-color: var(--card-bg);
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid var(--border-color);
}

#index .content-wrapper {
  flex: 1;
  overflow: hidden;
  background-color: var(--bg-primary);
  display: flex;
  flex-direction: column;
}

#index .top-nav {
  flex-shrink: 0;
  margin: 16px 20px 0 20px;
  border-radius: 8px;
  background-color: var(--card-bg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
}

#index .page-content {
  flex: 1;
  overflow: auto;
  padding: 16px 20px 20px 20px;
}

/* 移除布局限制，让内容自然填充 */
</style>

