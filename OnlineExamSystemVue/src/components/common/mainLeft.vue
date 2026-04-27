<!--左边下拉导航栏-->
<template>
  <div id="left">
    <el-menu
      active-text-color="lightgrey"
      text-color="#000"
      :default-active="this.$route.path"
      class="el-menu-vertical-demo"
      @open="handleOpen"
      @close="handleClose"
      :collapse="flag"
      background-color="#2384d6"
      menu-trigger="click" router>
      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="toggleCollapse">
        <i :class="flag ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
      </div>
      
      <el-submenu v-for="(item,index) in menu" :index='item.index' :key="index" 
                  @mouseenter="handleMouseEnter" 
                  @mouseleave="handleMouseLeave">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="iconfont" :class="item.icon" style="font-size: 28px;"></i>
            <span slot="title" class="title"> {{item.title}}</span>
          </div>
        </template>
        <el-menu-item-group v-for="(list,index1) in item.content" :key="index1" >
          <el-menu-item @click="handleTitle(item.index)" :index="list.path" v-if="list.item2 != null" style="color:white;"><i :class="list.icon" style="font-size: 24px;color: white;"> </i> {{list.item2}}</el-menu-item>
          <el-menu-item @click="handleTitle(item.index)" :index="list.path" v-if="list.item3 != null" style="color:white;"><i :class="list.icon" style="font-size: 24px;color: white;"> </i> {{list.item3}}</el-menu-item>
        </el-menu-item-group>
      </el-submenu>
    </el-menu>
  </div>
</template>

<script>
import {mapState} from 'vuex'
export default {
  name: "mainLeft",
  data() {
    return {

    }
  },
  computed: mapState(["flag","menu"]),
  created() {
    this.addData()
  },
  mounted() {
    // 使用JS事件委托处理悬停效果 - 延迟执行确保覆盖Element UI的样式
    this.$nextTick(() => {
      const leftMenu = document.getElementById('left');
      if (leftMenu) {
        // 使用mouseenter/mouseleave代替mouseover/mouseout，避免冒泡问题
        leftMenu.addEventListener('mouseenter', (e) => {
          const target = e.target;
          if (target.classList.contains('el-menu-item') || target.closest('.el-menu-item')) {
            const menuItem = target.classList.contains('el-menu-item') ? target : target.closest('.el-menu-item');
            // 延迟10ms确保在Element UI设置样式之后执行
            setTimeout(() => {
              menuItem.style.cssText = menuItem.style.cssText.replace(/background-color:[^;]+;?/gi, '') + 'background-color: rgba(64, 158, 255, 0.4) !important;';
            }, 10);
          }
        }, true);
        
        leftMenu.addEventListener('mouseleave', (e) => {
          const target = e.target;
          if (target.classList.contains('el-menu-item') || target.closest('.el-menu-item')) {
            const menuItem = target.classList.contains('el-menu-item') ? target : target.closest('.el-menu-item');
            if (!menuItem.classList.contains('is-active')) {
              menuItem.style.cssText = menuItem.style.cssText.replace(/background-color:[^;]+;?/gi, '');
            }
          }
        }, true);
      }
    });
  },
  methods: {
    handleOpen(key, keyPath) {
      // console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      // console.log(key, keyPath);
    },
    //点击标题传递参数给navigator组件
    handleTitle(index) {
      this.bus.$emit('sendIndex',index)
    },
    // 鼠标进入事件 - 动态修改悬停样式
    handleMouseEnter(event) {
      const titleElement = event.target.querySelector('.el-submenu__title') || event.target.closest('.el-submenu__title');
      if (titleElement) {
        titleElement.style.backgroundColor = 'rgba(64, 158, 255, 0.3)';
        titleElement.style.color = '#ffffff';
        titleElement.style.transform = 'translateX(3px)';
        titleElement.style.transition = 'all 0.2s ease';
      }
    },
    // 鼠标离开事件 - 恢复原始样式
    handleMouseLeave(event) {
      const titleElement = event.target.querySelector('.el-submenu__title') || event.target.closest('.el-submenu__title');
      if (titleElement) {
        titleElement.style.backgroundColor = '';
        titleElement.style.color = '';
        titleElement.style.transform = '';
        titleElement.style.transition = '';
      }
    },
    addData() {
      // 根据用户角色设置菜单
      const role = this.$cookies.get("role") || sessionStorage.getItem('role');
      if (role) {
        this.$store.commit('setMenuByRole', parseInt(role));
      }
    },
    toggleCollapse() {
      this.$store.commit('setFlag', !this.flag)
    }
  },
}
</script>

<style>
.el-menu-vertical-demo .el-submenu__title {
  overflow: hidden;
}
.left-width .iconfont {
  font-size: 18px;
  color: #fff;
}
.left-width {
  width: 213px;
}
.el-menu-vertical-demo:not(.el-menu--collapse) {
  min-height: calc(100vh - 60px);
}
#left {
  height: calc(100vh - 60px);
  background-color: #2384d6;
  z-index: 0;
  transition: width 0.3s ease;
}

#left .el-menu-vertical-demo {
  transition: width 0.3s ease;
}
#left .el-menu-vertical-demo .title {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  margin-left: 14px;
}
.el-submenu {
  border-bottom: 1px solid #eeeeee0f !important;
}
/* 使用更强的选择器确保悬停样式生效 */
#left .el-submenu__title:hover,
.el-menu-vertical-demo .el-submenu__title:hover {
  background-color: rgba(64, 158, 255, 0.3) !important;
  color: #ffffff !important;
  transform: translateX(3px) !important;
  transition: all 0.2s ease !important;
}
.el-submenu__title i {
    color: #fbfbfc !important;
}

/* 菜单项悬停样式 - 使用更强的选择器 */
#left .el-menu-item:hover,
.el-menu-vertical-demo .el-menu-item:hover {
  background-color: rgba(64, 158, 255, 0.3) !important;
  color: #ffffff !important;
  transform: translateX(3px) !important;
  transition: all 0.2s ease !important;
  border-left: 3px solid #409eff !important;
}

/* 折叠按钮样式 */
#left .collapse-btn {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s;
}

#left .collapse-btn i {
  font-size: 20px;
  color: #fff !important;
}

#left .collapse-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 折叠后子菜单弹出样式 */
.el-menu--vertical .el-menu--popup {
  background-color: #2384d6 !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item {
  color: #fff !important;
  background-color: #2384d6 !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item i {
  color: #fff !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item:hover {
  background-color: rgba(64, 158, 255, 0.3) !important;
}
</style>

<style lang="less" scoped>
/* 使用深度选择器覆盖Element UI内置样式 */
#left {
  /deep/ .el-menu-item:hover,
  /deep/ .el-menu-item:focus {
    background-color: rgba(64, 158, 255, 0.35) !important;
    color: #ffffff !important;
  }
  /deep/ .el-submenu__title:hover,
  /deep/ .el-submenu__title:focus {
    background-color: rgba(64, 158, 255, 0.35) !important;
    color: #ffffff !important;
  }
  ::v-deep .el-menu-item:hover,
  ::v-deep .el-menu-item:focus {
    background-color: rgba(64, 158, 255, 0.35) !important;
    color: #ffffff !important;
  }
  ::v-deep .el-submenu__title:hover,
  ::v-deep .el-submenu__title:focus {
    background-color: rgba(64, 158, 255, 0.35) !important;
    color: #ffffff !important;
  }
}
</style>
