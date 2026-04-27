<template>
  <div id="nav">
    <el-breadcrumb separator-class="el-icon-arrow-right">
      <el-breadcrumb-item class="title">{{active && active.title ? active.title : ''}}</el-breadcrumb-item>
      <el-breadcrumb-item v-if="active && active.item1 != null">{{active.item1}}</el-breadcrumb-item>
      <el-breadcrumb-item v-if="active && active.item2 != null">{{active.item2}}</el-breadcrumb-item>
      <el-breadcrumb-item v-if="active && active.item3 != null">{{active.item3}}</el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script>
import {mapState} from 'vuex'
export default {
  data() {
    return {
      active: {
        title: '',
        item1: null,
        item2: null,
        item3: null
      },
      index1: null,
    }
  },
  computed: mapState(["menu"]),
  methods: {
    getIndex() {
      this.bus.$on('sendIndex',(data)=>{
        this.index1 = data
        if (this.menu && this.menu.length > 0 && data > 0 && data <= this.menu.length) {
          this.active = this.menu[data-1] || {
            title: '',
            item1: null,
            item2: null,
            item3: null
          }
        }
        // console.log(JSON.stringify(this.active)+'----')
      })
    }
  },
  created() {
    this.getIndex()
  },
  beforeDestroy() {
    // this.bus.$off('sendIndex') //销毁
  },
}
</script>

<style scoped>
#nav .el-breadcrumb {
  height: 60px;
  line-height: 60px;
  padding-left: 20px;
}
#nav .el-breadcrumb .title{
  font-weight: bold;
}

/* 面包屑悬停效果优化 */
#nav .el-breadcrumb .el-breadcrumb__inner:hover {
  color: #409eff !important;
  text-decoration: none;
  transition: color 0.3s ease;
}

/* 确保面包屑在浅色模式下有良好的对比度 */
#nav .el-breadcrumb .el-breadcrumb__inner {
  transition: color 0.3s ease;
}
</style>
